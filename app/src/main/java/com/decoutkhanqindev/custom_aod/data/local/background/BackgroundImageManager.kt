package com.decoutkhanqindev.custom_aod.data.local.background

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import androidx.core.net.toUri
import com.decoutkhanqindev.custom_aod.utils.Tag
import com.decoutkhanqindev.custom_aod.utils.withContextCatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.IOException
import kotlin.math.roundToInt

class BackgroundImageManager(
    private val app: Application,
) : Tag {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val file = File(app.filesDir, FILE_NAME)

    // null = chưa kiểm xong file (kiểm trên IO, không đụng ổ đĩa ở main thread).
    private val _hasImage = MutableStateFlow<Boolean?>(null)
    val hasImage: StateFlow<Boolean?> = _hasImage.asStateFlow()

    init {
        scope.launch {
            withContextCatching(
                action = { _hasImage.value = file.exists() },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    // Lưu bản sao đã thu về cỡ màn hình: Photo Picker chỉ cho đọc tạm thời, và AOD giải mã ảnh nhỏ nhanh hơn ảnh gốc. Ghi file tạm rồi đổi tên để không bao giờ còn file ghi dở.
    suspend fun saveImage(uri: String): Boolean = withContextCatching(
        context = Dispatchers.IO,
        action = {
            val bitmap = decodeScaled(uri)
            val tempFile = File(app.filesDir, TEMP_FILE_NAME)
            try {
                tempFile.outputStream().use { output -> bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output) }
            } finally {
                bitmap.recycle()
            }
            if (!tempFile.renameTo(file)) throw IOException("Could not move the background image into place")
            _hasImage.value = true
            true
        },
        catch = { e ->
            Timber.tag(tag).e("Could not save the background image: ${e.stackTraceToString()}")
            false
        },
    )

    suspend fun loadImage(): Bitmap? = withContextCatching(
        context = Dispatchers.IO,
        action = { if (file.exists()) BitmapFactory.decodeFile(file.path) else null },
        catch = { e ->
            Timber.tag(tag).e("Could not load the background image: ${e.stackTraceToString()}")
            null
        },
    )

    fun removeImage() {
        scope.launch {
            withContextCatching(
                action = {
                    file.delete()
                    _hasImage.value = false
                },
                catch = { e -> Timber.tag(tag).e("Could not remove the background image: ${e.stackTraceToString()}") },
            )
        }
    }

    // ImageDecoder tự xoay theo EXIF và đọc được HEIC; cạnh dài nhất không vượt quá cạnh dài của màn hình.
    private fun decodeScaled(uri: String): Bitmap {
        val source = ImageDecoder.createSource(app.contentResolver, uri.toUri())
        val maxSide = app.resources.displayMetrics.run { maxOf(widthPixels, heightPixels) }
        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val longSide = maxOf(info.size.width, info.size.height)
            if (longSide > maxSide) {
                val scale = maxSide.toFloat() / longSide
                decoder.setTargetSize(
                    (info.size.width * scale).roundToInt(),
                    (info.size.height * scale).roundToInt(),
                )
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }

    companion object {
        private const val FILE_NAME = "aod_background.jpg"
        private const val TEMP_FILE_NAME = "aod_background.tmp"
        private const val JPEG_QUALITY = 90
    }
}
