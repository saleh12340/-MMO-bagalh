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
    suspend fun createBackup(targetUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val dbPath = context.getDatabasePath("alezzi_pos.db")
            val walPath = File(dbPath.parent, "alezzi_pos.db-wal")
            val shmPath = File(dbPath.parent, "alezzi_pos.db-shm")
            
            db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)")
            
            context.contentResolver.openOutputStream(targetUri)?.use { os ->
                ZipOutputStream(os).use { zos ->
                    listOf(dbPath, walPath, shmPath).filter { it.exists() }.forEach { file ->
                        zos.putNextEntry(ZipEntry(file.name))
                        FileInputStream(file).copyTo(zos)
                        zos.closeEntry()
                    }
                }
            }
            Result.success("تم حفظ النسخة الاحتياطية بنجاح")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(sourceUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            db.close()
            val dbDir = context.getDatabasePath("alezzi_pos.db").parentFile
            context.contentResolver.openInputStream(sourceUri)?.use { `is` ->
                ZipInputStream(`is`).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val targetFile = File(dbDir, entry.name)
                        FileOutputStream(targetFile).use { fos ->
                            zis.copyTo(fos)
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }
            Result.success("تم استعادة البيانات بنجاح، يرجى إعادة تشغيل التطبيق")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
