package com.phuonghai.inspection.core.storage

import android.content.Context
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val offlineDir = File(context.filesDir, "offline_media")

    init {
        if (!offlineDir.exists()) {
            offlineDir.mkdirs()
        }
    }

    suspend fun saveImageLocally(imageUri: Uri, reportId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fileName = "${reportId}_image_${UUID.randomUUID()}.jpg"
            val destinationFile = File(offlineDir, fileName)

            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            Log.d(TAG, "Image saved locally: ${destinationFile.absolutePath}")
            Result.success(destinationFile.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving image locally", e)
            Result.failure(e)
        }
    }

    suspend fun saveVideoLocally(videoUri: Uri, reportId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fileName = "${reportId}_video_${UUID.randomUUID()}.mp4"
            val destinationFile = File(offlineDir, fileName)

            context.contentResolver.openInputStream(videoUri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            Log.d(TAG, "Video saved locally: ${destinationFile.absolutePath}")
            Result.success(destinationFile.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving video locally", e)
            Result.failure(e)
        }
    }

    suspend fun saveMultipleImagesLocally(imageUris: List<Uri>, reportId: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val localPaths = mutableListOf<String>()

            imageUris.forEachIndexed { index, uri ->
                val fileName = "${reportId}_image_${index}_${UUID.randomUUID()}.jpg"
                val destinationFile = File(offlineDir, fileName)

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(destinationFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                localPaths.add(destinationFile.absolutePath)
            }

            Log.d(TAG, "Multiple images saved locally: ${localPaths.size} files")
            Result.success(localPaths)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving multiple images locally", e)
            Result.failure(e)
        }
    }

    fun getLocalFile(filePath: String): File? {
        val file = File(filePath)
        return if (file.exists()) file else null
    }

    suspend fun deleteLocalFile(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            val deleted = file.delete()
            if (deleted) {
                Log.d(TAG, "Local file deleted: $filePath")
            } else {
                Log.w(TAG, "Failed to delete local file: $filePath")
            }
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting local file: $filePath", e)
            false
        }
    }

    suspend fun cleanupOldFiles(olderThanDays: Int = 7): Int = withContext(Dispatchers.IO) {
        try {
            val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
            var deletedCount = 0

            offlineDir.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        deletedCount++
                    }
                }
            }

            Log.d(TAG, "Cleaned up $deletedCount old files")
            deletedCount
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up old files", e)
            0
        }
    }

    fun getOfflineDirectorySize(): Long {
        return try {
            offlineDir.walkTopDown()
                .filter { it.isFile }
                .map { it.length() }
                .sum()
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating directory size", e)
            0L
        }
    }

    companion object {
        private const val TAG = "OfflineFileManager"
    }
}