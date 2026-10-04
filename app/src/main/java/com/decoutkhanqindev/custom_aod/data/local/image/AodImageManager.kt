package com.decoutkhanqindev.custom_aod.data.local.image

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

// Ảnh nền và hình vẽ nhanh của AOD, mỗi thứ một file trong bộ nhớ riêng của app.
class AodImageManager(
    private val app: Application,
) : Tag {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val backgroundFile = File(app.filesDir, BACKGROUND_FILE_NAME)
    private val drawingFile = File(app.filesDir, DRAWING_FILE_NAME)

    // null = chưa kiểm xong file (kiểm trên IO, không đụng ổ đĩa ở main thread).
    private val _hasBackground = MutableStateFlow<Boolean?>(null)
    val hasBackground: StateFlow<Boolean?> = _hasBackground.asStateFlow()

    private val _hasDrawing = MutableStateFlow<Boolean?>(null)
    val hasDrawing: StateFlow<Boolean?> = _hasDrawing.asStateFlow()

    init {
        scope.launch {
            withContextCatching(
                action = {
                    _hasBackground.value = backgroundFile.exists()
                    _hasDrawing.value = drawingFile.exists()
                },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    // Lưu bản sao đã thu về cỡ màn hình: Photo Picker chỉ cho đọc tạm thời, và AOD giải mã ảnh nhỏ nhanh hơn ảnh gốc.
    suspend fun saveBackground(uri: String): Boolean = withContextCatching(
        context = Dispatchers.IO,
        action = {
            val bitmap = decodeScaled(uri)
            try {
                writeAtomically(file = backgroundFile, bitmap = bitmap, format = Bitmap.CompressFormat.JPEG)
            } finally {
                bitmap.recycle()
            }
            _hasBackground.value = true
            true
        },
        catch = { e ->
            Timber.tag(tag).e("Could not save the background image: ${e.stackTraceToString()}")
            false
        },
    )

    suspend fun loadBackground(): Bitmap? = load(backgroundFile)

    fun removeBackground() {
        remove(file = backgroundFile, hasFile = _hasBackground)
    }

    // PNG giữ nền trong suốt để AOD tô nét vẽ theo màu đồng hồ.
    suspend fun saveDrawing(bitmap: Bitmap): Boolean = withContextCatching(
        context = Dispatchers.IO,
        action = {
            writeAtomically(file = drawingFile, bitmap = bitmap, format = Bitmap.CompressFormat.PNG)
            _hasDrawing.value = true
            true
        },
        catch = { e ->
            Timber.tag(tag).e("Could not save the drawing: ${e.stackTraceToString()}")
            false
        },
    )

    suspend fun loadDrawing(): Bitmap? = load(drawingFile)

    fun removeDrawing() {
        remove(file = drawingFile, hasFile = _hasDrawing)
    }

    // Ghi file tạm rồi đổi tên: không bao giờ còn file ghi dở nếu app bị đóng giữa chừng.
    private fun writeAtomically(file: File, bitmap: Bitmap, format: Bitmap.CompressFormat) {
        val tempFile = File(file.parentFile, "${file.name}$TEMP_SUFFIX")
        tempFile.outputStream().use { output -> bitmap.compress(format, IMAGE_QUALITY, output) }
        if (!tempFile.renameTo(file)) throw IOException("Could not move ${file.name} into place")
    }

    private suspend fun load(file: File): Bitmap? = withContextCatching(
        context = Dispatchers.IO,
        action = { if (file.exists()) BitmapFactory.decodeFile(file.path) else null },
        catch = { e ->
            Timber.tag(tag).e("Could not load ${file.name}: ${e.stackTraceToString()}")
            null
        },
    )

    private fun remove(file: File, hasFile: MutableStateFlow<Boolean?>) {
        scope.launch {
            withContextCatching(
                action = {
                    file.delete()
                    hasFile.value = false
                },
                catch = { e -> Timber.tag(tag).e("Could not remove ${file.name}: ${e.stackTraceToString()}") },
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
        private const val BACKGROUND_FILE_NAME = "aod_background.jpg"
        private const val DRAWING_FILE_NAME = "aod_drawing.png"
        private const val TEMP_SUFFIX = ".tmp"
        private const val IMAGE_QUALITY = 90
    }
}
