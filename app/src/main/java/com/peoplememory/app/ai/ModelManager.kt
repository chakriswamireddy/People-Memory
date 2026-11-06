package com.peoplememory.app.ai

import android.content.Context
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

sealed class ModelDownloadState {
    data object NotDownloaded : ModelDownloadState()
    data class Downloading(val progressPercent: Int, val downloadedBytes: Long, val totalBytes: Long) : ModelDownloadState()
    data class Ready(val modelFile: File, val sizeBytes: Long) : ModelDownloadState()
    data class Error(val message: String) : ModelDownloadState()
}

class ModelManager(private val context: Context) {
    companion object {
        const val MODEL_FILENAME = "gemma-1b-it.litertlm"
        const val APPROX_MODEL_SIZE_BYTES = 1_350_000_000L // ~1.35 GB
        const val REQUIRED_STORAGE_BYTES = 2_500_000_000L // Buffer required
        const val MODEL_DOWNLOAD_URL = "https://huggingface.co/litert-community/gemma-1b-it-litert-lm/resolve/main/gemma-1b-it.litertlm"
    }

    private val modelDir: File
        get() = File(context.filesDir, "litert_models").apply { if (!exists()) mkdirs() }

    val modelFile: File
        get() = File(modelDir, MODEL_FILENAME)

    private val _downloadState = MutableStateFlow<ModelDownloadState>(
        if (modelFile.exists() && modelFile.length() > 100_000_000L) {
            ModelDownloadState.Ready(modelFile, modelFile.length())
        } else {
            ModelDownloadState.NotDownloaded
        }
    )
    val downloadState: StateFlow<ModelDownloadState> = _downloadState.asStateFlow()

    fun isModelReady(): Boolean = modelFile.exists() && modelFile.length() > 100_000_000L

    fun getAvailableStorageBytes(): Long {
        val stat = StatFs(context.filesDir.path)
        return stat.availableBlocksLong * stat.blockSizeLong
    }

    suspend fun downloadModel(onProgress: ((Int) -> Unit)? = null): Result<File> = withContext(Dispatchers.IO) {
        val available = getAvailableStorageBytes()
        if (available < REQUIRED_STORAGE_BYTES) {
            val neededMb = (REQUIRED_STORAGE_BYTES - available) / (1024 * 1024)
            val err = "Insufficient storage: Free up at least $neededMb MB to download on-device AI model."
            _downloadState.value = ModelDownloadState.Error(err)
            return@withContext Result.failure(IllegalStateException(err))
        }

        val tempFile = File(modelDir, "$MODEL_FILENAME.download")
        var connection: HttpURLConnection? = null

        try {
            _downloadState.value = ModelDownloadState.Downloading(0, 0, APPROX_MODEL_SIZE_BYTES)
            val url = URL(MODEL_DOWNLOAD_URL)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
            }

            val contentLength = if (connection.contentLengthLong > 0) connection.contentLengthLong else APPROX_MODEL_SIZE_BYTES
            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(tempFile)
            val buffer = ByteArray(64 * 1024)
            var bytesRead: Int
            var totalRead: Long = 0
            var lastReportedPercent = 0

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalRead += bytesRead
                val percent = ((totalRead * 100) / contentLength).toInt().coerceIn(0, 100)
                if (percent != lastReportedPercent) {
                    lastReportedPercent = percent
                    _downloadState.value = ModelDownloadState.Downloading(percent, totalRead, contentLength)
                    onProgress?.invoke(percent)
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            if (modelFile.exists()) modelFile.delete()
            tempFile.renameTo(modelFile)

            _downloadState.value = ModelDownloadState.Ready(modelFile, modelFile.length())
            Result.success(modelFile)
        } catch (e: Exception) {
            if (tempFile.exists()) tempFile.delete()
            _downloadState.value = ModelDownloadState.Error("Download failed: ${e.message}")
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }

    fun deleteModel(): Boolean {
        return try {
            if (modelFile.exists()) modelFile.delete()
            _downloadState.value = ModelDownloadState.NotDownloaded
            true
        } catch (e: Exception) {
            false
        }
    }
}