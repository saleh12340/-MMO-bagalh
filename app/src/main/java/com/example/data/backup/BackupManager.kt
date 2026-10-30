package com.example.data.backup

import android.content.Context
import android.net.Uri
import com.example.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupManager(private val context: Context, private val db: AppDatabase) {

    companion object {
        private const val DB_NAME = "alezzi_pos.db"
        /** أسماء الملفات المسموح بها فقط داخل أرشيف النسخة الاحتياطية */
        private val ALLOWED_ENTRIES = setOf(DB_NAME, "$DB_NAME-wal", "$DB_NAME-shm")
    }

    suspend fun createBackup(targetUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val dbPath = context.getDatabasePath(DB_NAME)
            if (!dbPath.exists()) {
                return@withContext Result.failure(Exception("قاعدة البيانات غير موجودة"))
            }
            val walPath = File(dbPath.parent, "$DB_NAME-wal")
            val shmPath = File(dbPath.parent, "$DB_NAME-shm")

            // دمج بيانات WAL في الملف الرئيسي قبل النسخ لضمان نسخة كاملة
            db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()

            context.contentResolver.openOutputStream(targetUri)?.use { os ->
                ZipOutputStream(os.buffered()).use { zos ->
                    listOf(dbPath, walPath, shmPath).filter { it.exists() }.forEach { file ->
                        zos.putNextEntry(ZipEntry(file.name))
                        FileInputStream(file).buffered().use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            } ?: return@withContext Result.failure(Exception("تعذر فتح الملف المحدد للكتابة"))

            Result.success("تم حفظ النسخة الاحتياطية بنجاح")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(sourceUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        val dbPath = context.getDatabasePath(DB_NAME)
        val dbDir = dbPath.parentFile
            ?: return@withContext Result.failure(Exception("تعذر تحديد مجلد قاعدة البيانات"))
        val safetyCopies = mutableListOf<Pair<File, File>>() // (الملف الأصلي, نسخة الأمان)
        try {
            // 1) فك الضغط إلى مجلد مؤقت مع حماية من هجمات Zip Slip
            val tempDir = File(context.cacheDir, "restore_${System.currentTimeMillis()}").apply { mkdirs() }
            var foundMainDb = false
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                ZipInputStream(input.buffered()).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        // تجاهل أي مسار غير متوقع أو يحتوي على traversal (../)
                        val safeName = File(entry.name).name
                        if (safeName in ALLOWED_ENTRIES) {
                            val outFile = File(tempDir, safeName)
                            FileOutputStream(outFile).buffered().use { fos -> zis.copyTo(fos) }
                            if (safeName == DB_NAME) foundMainDb = true
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            } ?: run {
                tempDir.deleteRecursively()
                return@withContext Result.failure(Exception("تعذر قراءة ملف النسخة الاحتياطية"))
            }

            // 2) التحقق من أن الأرشيف يحتوي فعلاً على قاعدة البيانات
            if (!foundMainDb) {
                tempDir.deleteRecursively()
                return@withContext Result.failure(Exception("الملف المحدد ليس نسخة احتياطية صالحة (لا يحتوي على قاعدة البيانات)"))
            }

            // 3) أخذ نسخ أمان من الملفات الحالية قبل الاستبدال (للتراجع عند الفشل)
            ALLOWED_ENTRIES.forEach { name ->
                val current = File(dbDir, name)
                if (current.exists()) {
                    val safetyCopy = File(context.cacheDir, "$name.safetybak")
                    current.copyTo(safetyCopy, overwrite = true)
                    safetyCopies.add(current to safetyCopy)
                }
            }

            // 4) إغلاق القاعدة واستبدال الملفات
            db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
            db.close()
            ALLOWED_ENTRIES.forEach { name ->
                val from = File(tempDir, name)
                val to = File(dbDir, name)
                if (from.exists()) {
                    from.copyTo(to, overwrite = true)
                } else if (to.exists()) {
                    to.delete()
                }
            }
            tempDir.deleteRecursively()

            Result.success("تم استعادة البيانات بنجاح، يرجى إعادة تشغيل التطبيق")
        } catch (e: Exception) {
            // استرجاع النسخ الآمنة عند فشل الاستعادة لمنع فقدان البيانات
            safetyCopies.forEach { (target, safety) ->
                try { safety.copyTo(target, overwrite = true) } catch (_: Exception) {}
            }
            Result.failure(e)
        }
    }
}
