package com.example.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.UUID

class ThermalPrinterService {
    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    @SuppressLint("MissingPermission")
    suspend fun printReceiptBitmap(deviceAddress: String, bitmap: Bitmap): Result<Unit> = withContext(Dispatchers.IO) {
        var socket: BluetoothSocket? = null
        try {
            val adapter = BluetoothAdapter.getDefaultAdapter() ?: return@withContext Result.failure(Exception("البلوتوث غير مدعوم"))
            val device: BluetoothDevice = adapter.getRemoteDevice(deviceAddress)
            socket = device.createRfcommSocketToServiceRecord(sppUuid)
            socket.connect()
            val outputStream: OutputStream = socket.outputStream
            outputStream.write(byteArrayOf(0x1B, 0x40)) // ESC @
            val escPosData = convertBitmapToEscPosRaster(bitmap)
            outputStream.write(escPosData)
            outputStream.write(byteArrayOf(0x1B, 0x64, 0x04)) // Feed 4 lines
            outputStream.flush()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try { socket?.close() } catch (_: Exception) {}
        }
    }

    private fun convertBitmapToEscPosRaster(bitmap: Bitmap): ByteArray {
        val width = 384
        val scaledHeight = (bitmap.height * (384f / bitmap.width)).toInt()
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, scaledHeight, true)
        val widthBytes = width / 8
        val commandHeader = byteArrayOf(
            0x1D.toByte(), 0x76.toByte(), 0x30.toByte(), 0x00.toByte(),
            (widthBytes and 0xFF).toByte(),
            ((widthBytes shr 8) and 0xFF).toByte(),
            (scaledHeight and 0xFF).toByte(),
            ((scaledHeight shr 8) and 0xFF).toByte()
        )
        val imageBytes = ByteArray(widthBytes * scaledHeight)
        var index = 0
        for (y in 0 until scaledHeight) {
            for (x in 0 until width step 8) {
                var b = 0
                for (bit in 0 until 8) {
                    val px = scaledBitmap.getPixel(x + bit, y)
                    val luminance = (0.299 * Color.red(px) + 0.587 * Color.green(px) + 0.114 * Color.blue(px)).toInt()
                    if (luminance < 128) {
                        b = b or (1 shl (7 - bit))
                    }
                }
                imageBytes[index++] = b.toByte()
            }
        }
        return commandHeader + imageBytes
    }
}
