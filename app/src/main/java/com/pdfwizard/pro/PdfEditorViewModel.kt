package com.pdfwizard.pro

import android.app.Application
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.pdfwizard.pro.R
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.min
import kotlin.math.max
import kotlin.math.roundToInt

class PdfEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = getApplication<Application>()

    init {
        PDFBoxResourceLoader.init(appContext)
    }

    private val _state = MutableStateFlow(PdfEditorState())
    val state: StateFlow<PdfEditorState> = _state

    private val textRecognizer: TextRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    fun clearNotifications() {
        _state.update { it.copy(message = null, error = null) }
    }

    fun setCurrentPage(index: Int) {
        _state.update { current ->
            if (!current.hasDocument) {
                current
            } else {
                current.copy(currentPageIndex = index.coerceIn(0, current.pageCount - 1))
            }
        }
    }

    fun loadPdf(uri: Uri) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isProcessing = true,
                    error = null,
                    message = null,
                    pages = emptyList(),
                    currentPageIndex = 0,
                    isRecognizingText = false,
                    contentEditor = null
                )
            }
            try {
                val pages = withContext(Dispatchers.IO) { renderPdf(appContext.contentResolver, uri) }
                if (pages.isEmpty()) {
                    _state.update { it.copy(isProcessing = false, error = appContext.getString(R.string.export_error)) }
                } else {
                    val displayName = resolveDisplayName(appContext.contentResolver, uri)
                    val baseName = displayName.substringBeforeLast('.')
                    _state.update {
                        it.copy(
                            documentName = displayName,
                            pages = pages,
                            currentPageIndex = 0,
                            isProcessing = false,
                            isRecognizingText = false,
                            metadata = PdfMetadata(title = baseName.ifBlank { displayName }),
                            lastSavedFilePath = null,
                            message = appContext.getString(R.string.pdf_loaded, pages.size),
                            contentEditor = null
                        )
                    }
                }
            } catch (ex: Exception) {
                if (ex is CancellationException) throw ex
                _state.update {
                    it.copy(
                        isProcessing = false,
                        isRecognizingText = false,
                        contentEditor = null,
                        error = ex.localizedMessage ?: appContext.getString(R.string.generic_error)
                    )
                }
            }
        }
    }

    fun mergeWithPdf(uri: Uri) {
        if (!_state.value.hasDocument) {
            loadPdf(uri)
            return
        }
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isProcessing = true,
                    isRecognizingText = false,
                    message = appContext.getString(R.string.merge_pdf_progress),
                    error = null
                )
            }
            val additionalPages = withContext(Dispatchers.IO) { renderPdf(appContext.contentResolver, uri) }
            if (additionalPages.isEmpty()) {
                _state.update { it.copy(isProcessing = false, isRecognizingText = false, error = appContext.getString(R.string.merge_pdf_error)) }
            } else {
                _state.update { current ->
                    val merged = (current.pages + additionalPages).mapIndexed { index, page -> page.copy(index = index) }
                    current.copy(
                        pages = merged,
                        currentPageIndex = current.currentPageIndex,
                        isProcessing = false,
                        isRecognizingText = false,
                        message = appContext.getString(R.string.merge_pdf_success)
                    )
                }
            }
        }
    }

    fun importImagesAsDocument(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, message = appContext.getString(R.string.images_to_pdf_progress), error = null) }
            val pages = withContext(Dispatchers.IO) { createPagesFromImages(uris) }
            if (pages.isEmpty()) {
                _state.update { it.copy(isProcessing = false, isRecognizingText = false, error = appContext.getString(R.string.images_to_pdf_error)) }
            } else {
                val normalized = pages.mapIndexed { index, page -> page.copy(index = index) }
                val title = appContext.getString(R.string.images_to_pdf_default_title, normalized.size)
                _state.update {
                    it.copy(
                        documentName = "$title.pdf",
                        pages = normalized,
                        currentPageIndex = 0,
                        isProcessing = false,
                        isRecognizingText = false,
                        metadata = PdfMetadata(title = title),
                        lastSavedFilePath = null,
                        message = appContext.getString(R.string.images_to_pdf_success),
                        contentEditor = null
                    )
                }
            }
        }
    }

    fun deleteCurrentPage() {
        _state.update { current ->
            if (!current.hasDocument) return@update current
            val updated = current.pages.toMutableList().also { list ->
                if (current.currentPageIndex in list.indices) {
                    list.removeAt(current.currentPageIndex)
                }
            }
            if (updated.isEmpty()) {
                current.copy(
                    pages = emptyList(),
                    currentPageIndex = 0,
                    message = appContext.getString(R.string.all_pages_removed),
                    contentEditor = null
                )
            } else {
                current.copy(
                    pages = updated.mapIndexed { index, page -> page.copy(index = index) },
                    currentPageIndex = current.currentPageIndex.coerceIn(0, updated.lastIndex),
                    message = appContext.getString(R.string.page_deleted),
                    contentEditor = null
                )
            }
        }
    }

    fun rotateCurrentPage() {
        _state.update { current ->
            if (!current.hasDocument) return@update current
            val index = current.currentPageIndex
            if (index !in current.pages.indices) return@update current
            val pages = current.pages.toMutableList()
            val page = pages[index]
            val rotatedBitmap = rotateBitmap(page.bitmap)
            pages[index] = page.copy(
                rotationDegrees = (page.rotationDegrees + 90) % 360,
                width = rotatedBitmap.width,
                height = rotatedBitmap.height,
                bitmap = rotatedBitmap,
                annotations = page.annotations.map { rotateAnnotation(it, page.height) },
                images = page.images.map { rotateImageStamp(it, page.height) },
                recognizedText = null
            )
            current.copy(
                pages = pages,
                message = appContext.getString(R.string.page_rotated),
                contentEditor = null
            )
        }
    }

    fun moveCurrentPageLeft() = movePageBy(-1)

    fun moveCurrentPageRight() = movePageBy(1)

    fun duplicateCurrentPage() {
        _state.update { current ->
            if (!current.hasDocument) return@update current
            val index = current.currentPageIndex
            if (index !in current.pages.indices) return@update current
            val page = current.pages[index]
            val duplicatedBitmap = page.bitmap.copy(page.bitmap.config ?: Bitmap.Config.ARGB_8888, true)
            val timestamp = System.currentTimeMillis()
            val duplicatedAnnotations = page.annotations.mapIndexed { idx, annotation ->
                annotation.copy(id = timestamp + idx)
            }
            val duplicatedImages = page.images.mapIndexed { idx, image ->
                image.copy(
                    id = timestamp + duplicatedAnnotations.size + idx,
                    bitmap = image.bitmap.copy(image.bitmap.config ?: Bitmap.Config.ARGB_8888, true)
                )
            }
            val pages = current.pages.toMutableList()
            val insertIndex = (index + 1).coerceAtMost(pages.size)
            pages.add(
                insertIndex,
                page.copy(
                    index = insertIndex,
                    bitmap = duplicatedBitmap,
                    annotations = duplicatedAnnotations,
                    images = duplicatedImages
                )
            )
            current.copy(
                pages = pages.mapIndexed { idx, item -> item.copy(index = idx) },
                currentPageIndex = insertIndex,
                message = appContext.getString(R.string.page_duplicated)
            )
        }
    }

    fun insertBlankPage() {
        _state.update { current ->
            val reference = current.pages.getOrNull(current.currentPageIndex)
            val width = reference?.width ?: 1240
            val height = reference?.height ?: 1754
            val blankBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                eraseColor(android.graphics.Color.WHITE)
            }
            val pages = current.pages.toMutableList()
            val insertIndex = if (current.hasDocument) {
                (current.currentPageIndex + 1).coerceAtMost(pages.size)
            } else {
                0
            }
            pages.add(
                insertIndex,
                PdfPageState(
                    index = insertIndex,
                    width = width,
                    height = height,
                    rotationDegrees = 0,
                    bitmap = blankBitmap
                )
            )
            val defaultName = appContext.getString(R.string.default_document_name)
            val defaultTitle = appContext.getString(R.string.default_document_title)
            val newDocumentName = if (current.documentName.isBlank()) defaultName else current.documentName
            val newMetadata = if (current.metadata.title.isBlank()) current.metadata.copy(title = defaultTitle) else current.metadata
            current.copy(
                pages = pages.mapIndexed { idx, page -> page.copy(index = idx) },
                currentPageIndex = insertIndex,
                documentName = newDocumentName,
                metadata = newMetadata,
                message = appContext.getString(R.string.blank_page_inserted)
            )
        }
    }

    fun toggleMetadataEditor(show: Boolean) {
        _state.update { it.copy(showMetadataEditor = show, message = null, error = null) }
    }

    fun updateMetadata(metadata: PdfMetadata) {
        _state.update {
            it.copy(
                metadata = metadata,
                showMetadataEditor = false,
                message = appContext.getString(R.string.metadata_updated)
            )
        }
    }

    fun addAnnotation(pageIndex: Int, annotation: PdfAnnotation) {
        _state.update { current ->
            if (!current.hasDocument || pageIndex !in current.pages.indices) return@update current
            val pages = current.pages.toMutableList()
            val page = pages[pageIndex]
            pages[pageIndex] = page.copy(annotations = page.annotations + annotation)
            current.copy(pages = pages)
        }
    }

    fun addImageStamp(pageIndex: Int, stamp: PdfImageStamp) {
        _state.update { current ->
            if (!current.hasDocument || pageIndex !in current.pages.indices) return@update current
            val pages = current.pages.toMutableList()
            val page = pages[pageIndex]
            pages[pageIndex] = page.copy(images = page.images + stamp)
            current.copy(pages = pages)
        }
    }

    fun addImageFromUri(uri: Uri) {
        viewModelScope.launch {
            val snapshot = _state.value
            val pageIndex = snapshot.currentPageIndex
            if (!snapshot.hasDocument || pageIndex !in snapshot.pages.indices) {
                return@launch
            }
            val bitmap = withContext(Dispatchers.IO) {
                appContext.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } ?: return@launch

            val page = snapshot.pages[pageIndex]
            val maxScale = min(
                page.width.toFloat() / bitmap.width.toFloat(),
                page.height.toFloat() / bitmap.height.toFloat()
            ).coerceAtMost(1f)
            val desiredScale = (maxScale * 0.6f).coerceAtLeast(0.2f)
            val scaledBitmap = Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * desiredScale).roundToInt().coerceAtLeast(1),
                (bitmap.height * desiredScale).roundToInt().coerceAtLeast(1),
                true
            )
            val position = Offset(
                (page.width - scaledBitmap.width) / 2f,
                (page.height - scaledBitmap.height) / 2f
            )
            addImageStamp(
                pageIndex,
                PdfImageStamp(
                    id = System.currentTimeMillis(),
                    pageIndex = pageIndex,
                    bitmap = scaledBitmap,
                    position = position,
                    scale = 1f
                )
            )
            _state.update {
                it.copy(message = appContext.getString(R.string.image_added))
            }
        }
    }

    fun exportPdf(onResult: (Uri?) -> Unit) {
        val snapshot = _state.value
        if (!snapshot.hasDocument) {
            onResult(null)
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, message = appContext.getString(R.string.saving_pdf), error = null) }
            val metadata = snapshot.metadata.touch()
            val outputFile = writePdfToFile(snapshot.pages, snapshot.documentName, metadata)
            if (outputFile != null) {
                val shareUri = FileProvider.getUriForFile(
                    appContext,
                    "${appContext.packageName}.fileprovider",
                    outputFile
                )
                _state.update {
                    it.copy(
                        isProcessing = false,
                        lastSavedFilePath = outputFile.absolutePath,
                        metadata = metadata,
                        message = appContext.getString(R.string.export_success)
                    )
                }
                onResult(shareUri)
            } else {
                _state.update { it.copy(isProcessing = false, error = appContext.getString(R.string.export_error)) }
                onResult(null)
            }
        }
    }

    private suspend fun writePdfToFile(
        pages: List<PdfPageState>,
        documentName: String,
        metadata: PdfMetadata,
        suffix: String = "",
        scaleFactor: Float = 1f
    ): File? = withContext(Dispatchers.IO) {
        try {
            val file = createExportFile(documentName, suffix = suffix)
            val document = PdfDocument()
            try {
                pages.forEachIndexed { pageIndex, page ->
                    val targetWidth = max(1, (page.width * scaleFactor).roundToInt())
                    val targetHeight = max(1, (page.height * scaleFactor).roundToInt())
                    val workingBitmap = if (scaleFactor != 1f) {
                        Bitmap.createScaledBitmap(page.bitmap, targetWidth, targetHeight, true)
                    } else {
                        page.bitmap
                    }
                    val pageInfo = PdfDocument.PageInfo.Builder(targetWidth, targetHeight, pageIndex + 1).create()
                    val pdfPage = document.startPage(pageInfo)
                    val canvas = pdfPage.canvas
                    canvas.drawBitmap(workingBitmap, 0f, 0f, null)
                    drawAnnotations(canvas, page, scaleFactor)
                    drawImages(canvas, page, scaleFactor)
                    document.finishPage(pdfPage)
                    if (workingBitmap !== page.bitmap) {
                        workingBitmap.recycle()
                    }
                }
                FileOutputStream(file).use { stream ->
                    document.writeTo(stream)
                }
            } finally {
                document.close()
            }
            applyMetadata(file, metadata)
            file
        } catch (io: IOException) {
            null
        }
    }

    fun exportPagesAsImages(onResult: (Uri?) -> Unit) {
        val snapshot = _state.value
        if (!snapshot.hasDocument) {
            _state.update { it.copy(error = appContext.getString(R.string.no_document_loaded_error)) }
            onResult(null)
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, message = appContext.getString(R.string.export_images_progress), error = null) }
            val archive = createImagesArchive(snapshot)
            if (archive != null) {
                val uri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", archive)
                _state.update { it.copy(isProcessing = false, message = appContext.getString(R.string.export_images_success)) }
                onResult(uri)
            } else {
                _state.update { it.copy(isProcessing = false, error = appContext.getString(R.string.export_images_error)) }
                onResult(null)
            }
        }
    }

    fun exportCompressedPdf(onResult: (Uri?) -> Unit, scaleFactor: Float = 0.75f) {
        val snapshot = _state.value
        if (!snapshot.hasDocument) {
            _state.update { it.copy(error = appContext.getString(R.string.no_document_loaded_error)) }
            onResult(null)
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, message = appContext.getString(R.string.compress_pdf_progress), error = null) }
            val metadata = snapshot.metadata.touch()
            val output = writePdfToFile(snapshot.pages, snapshot.documentName, metadata, suffix = "compressed", scaleFactor = scaleFactor)
            if (output != null) {
                val uri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", output)
                _state.update {
                    it.copy(
                        isProcessing = false,
                        lastSavedFilePath = output.absolutePath,
                        metadata = metadata,
                        message = appContext.getString(R.string.compress_pdf_success)
                    )
                }
                onResult(uri)
            } else {
                _state.update { it.copy(isProcessing = false, error = appContext.getString(R.string.compress_pdf_error)) }
                onResult(null)
            }
        }
    }

    fun recognizeCurrentPageText(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val snapshot = _state.value
            if (!snapshot.hasDocument) {
                _state.update { it.copy(error = appContext.getString(R.string.no_document_loaded_error)) }
                return@launch
            }
            val pageIndex = snapshot.currentPageIndex
            val page = snapshot.pages.getOrNull(pageIndex) ?: return@launch
            if (!forceRefresh && !page.recognizedText.isNullOrBlank()) {
                _state.update { it.copy(message = appContext.getString(R.string.text_recognition_cached_message)) }
                return@launch
            }
            _state.update { it.copy(isRecognizingText = true, message = appContext.getString(R.string.text_recognition_progress), error = null) }
            try {
                val recognized = ensureRecognizedTextForPage(pageIndex, forceRefresh)
                _state.update { current ->
                    val editor = current.contentEditor
                    val updatedEditor = if (editor?.pageIndex == pageIndex) {
                        editor.copy(
                            originalText = recognized,
                            editedText = recognized
                        )
                    } else {
                        editor
                    }
                    current.copy(
                        isRecognizingText = false,
                        contentEditor = updatedEditor,
                        message = appContext.getString(R.string.text_recognition_success)
                    )
                }
            } catch (ex: Exception) {
                if (ex is CancellationException) throw ex
                _state.update {
                    it.copy(
                        isRecognizingText = false,
                        error = ex.localizedMessage ?: appContext.getString(R.string.text_recognition_error)
                    )
                }
            }
        }
    }

    fun beginContentEditing() {
        viewModelScope.launch {
            val snapshot = _state.value
            if (!snapshot.hasDocument) {
                _state.update { it.copy(error = appContext.getString(R.string.no_document_loaded_error)) }
                return@launch
            }
            val pageIndex = snapshot.currentPageIndex
            val page = snapshot.pages.getOrNull(pageIndex) ?: return@launch
            if (page.recognizedText.isNullOrBlank()) {
                _state.update { it.copy(isRecognizingText = true, message = appContext.getString(R.string.text_recognition_progress), error = null) }
                try {
                    val recognized = ensureRecognizedTextForPage(pageIndex, forceRefresh = true)
                    _state.update {
                        it.copy(
                            isRecognizingText = false,
                            contentEditor = ContentEditorState(
                                pageIndex = pageIndex,
                                originalText = recognized,
                                editedText = recognized
                            )
                        )
                    }
                } catch (ex: Exception) {
                    if (ex is CancellationException) throw ex
                    _state.update {
                        it.copy(
                            isRecognizingText = false,
                            error = ex.localizedMessage ?: appContext.getString(R.string.text_recognition_error)
                        )
                    }
                }
            } else {
                _state.update {
                    it.copy(
                        contentEditor = ContentEditorState(
                            pageIndex = pageIndex,
                            originalText = page.recognizedText,
                            editedText = page.recognizedText
                        ),
                        message = null,
                        error = null
                    )
                }
            }
        }
    }

    fun updateContentEditor(text: String) {
        _state.update { current ->
            val editor = current.contentEditor ?: return@update current
            current.copy(contentEditor = editor.copy(editedText = text))
        }
    }

    fun applyContentEditorChanges() {
        _state.update { current ->
            val editor = current.contentEditor ?: return@update current
            val pages = current.pages.toMutableList()
            if (editor.pageIndex in pages.indices) {
                pages[editor.pageIndex] = pages[editor.pageIndex].copy(recognizedText = editor.editedText)
            }
            current.copy(
                pages = pages,
                contentEditor = null,
                message = appContext.getString(R.string.content_editor_saved_message)
            )
        }
    }

    fun dismissContentEditor() {
        _state.update { it.copy(contentEditor = null) }
    }

    fun exportRecognizedText(onResult: (Uri?) -> Unit) {
        viewModelScope.launch {
            val snapshot = _state.value
            if (!snapshot.hasDocument) {
                _state.update { it.copy(error = appContext.getString(R.string.no_document_loaded_error)) }
                onResult(null)
                return@launch
            }
            _state.update {
                it.copy(
                    isProcessing = true,
                    isRecognizingText = true,
                    message = appContext.getString(R.string.text_export_progress),
                    error = null
                )
            }
            try {
                snapshot.pages.indices.forEach { index ->
                    ensureRecognizedTextForPage(index, forceRefresh = false)
                }
                val updatedPages = _state.value.pages
                val exportContent = buildString {
                    updatedPages.forEach { page ->
                        appendLine(appContext.getString(R.string.text_export_page_title, page.index + 1))
                        val text = page.recognizedText
                        if (text.isNullOrBlank()) {
                            appendLine(appContext.getString(R.string.text_export_empty_placeholder))
                        } else {
                            appendLine(text)
                        }
                        appendLine()
                    }
                }
                val output = writeRecognizedTextToFile(_state.value.documentName, exportContent)
                if (output != null) {
                    val uri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", output)
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            isRecognizingText = false,
                            message = appContext.getString(R.string.text_export_success)
                        )
                    }
                    onResult(uri)
                } else {
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            isRecognizingText = false,
                            error = appContext.getString(R.string.text_export_error)
                        )
                    }
                    onResult(null)
                }
            } catch (ex: Exception) {
                if (ex is CancellationException) throw ex
                _state.update {
                    it.copy(
                        isProcessing = false,
                        isRecognizingText = false,
                        error = ex.localizedMessage ?: appContext.getString(R.string.text_export_error)
                    )
                }
                onResult(null)
            }
        }
    }

    private fun movePageBy(offset: Int) {
        _state.update { current ->
            if (!current.hasDocument) return@update current
            val fromIndex = current.currentPageIndex
            val toIndex = (fromIndex + offset).coerceIn(0, current.pageCount - 1)
            if (fromIndex == toIndex) return@update current
            val pages = current.pages.toMutableList()
            Collections.swap(pages, fromIndex, toIndex)
            current.copy(
                pages = pages.mapIndexed { idx, page -> page.copy(index = idx) },
                currentPageIndex = toIndex,
                message = appContext.getString(R.string.page_reordered),
                contentEditor = null
            )
        }
    }

    private suspend fun ensureRecognizedTextForPage(pageIndex: Int, forceRefresh: Boolean): String {
        val snapshot = _state.value
        val page = snapshot.pages.getOrNull(pageIndex) ?: return ""
        if (!forceRefresh && !page.recognizedText.isNullOrBlank()) {
            return page.recognizedText
        }
        val recognized = recognizePageText(page)
        _state.update { current ->
            val pages = current.pages.toMutableList()
            if (pageIndex in pages.indices) {
                pages[pageIndex] = pages[pageIndex].copy(recognizedText = recognized)
            }
            current.copy(pages = pages)
        }
        return recognized
    }

    private suspend fun recognizePageText(page: PdfPageState): String = withContext(Dispatchers.Default) {
        val image = InputImage.fromBitmap(page.bitmap, 0)
        processText(image)
    }

    private suspend fun processText(image: InputImage): String = suspendCancellableCoroutine { continuation ->
        textRecognizer.process(image)
            .addOnSuccessListener { result -> continuation.resume(result.text) }
            .addOnFailureListener { ex -> continuation.resumeWithException(ex) }
            .addOnCanceledListener { continuation.cancel() }
    }

    private fun writeRecognizedTextToFile(documentName: String, content: String): File? {
        return runCatching {
            val baseName = documentName.substringBeforeLast('.').ifBlank { appContext.getString(R.string.default_document_title) }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val safeName = "${baseName}_ocr_$timestamp.txt"
            val file = File(appContext.cacheDir, safeName.replace(File.separatorChar, '_'))
            FileOutputStream(file).use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
            }
            file
        }.getOrNull()
    }

    private fun drawAnnotations(canvas: Canvas, page: PdfPageState, scaleFactor: Float = 1f) {
        if (page.annotations.isEmpty()) return
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        page.annotations.forEach { annotation ->
            paint.color = annotation.color.toArgb()
            paint.textSize = annotation.fontSize * scaleFactor * canvas.scaleFactor
            canvas.drawText(
                annotation.text,
                annotation.position.x * scaleFactor,
                annotation.position.y * scaleFactor,
                paint
            )
        }
    }

    private fun drawImages(canvas: Canvas, page: PdfPageState, scaleFactor: Float = 1f) {
        page.images.forEach { stamp ->
            val matrix = Matrix().apply {
                setScale(stamp.scale * scaleFactor, stamp.scale * scaleFactor)
                postTranslate(stamp.position.x * scaleFactor, stamp.position.y * scaleFactor)
            }
            canvas.drawBitmap(stamp.bitmap, matrix, null)
        }
    }

    private fun rotateAnnotation(annotation: PdfAnnotation, pageHeight: Int): PdfAnnotation {
        val newPosition = Offset(
            pageHeight - annotation.position.y,
            annotation.position.x
        )
        return annotation.copy(position = newPosition)
    }

    private fun rotateImageStamp(stamp: PdfImageStamp, pageHeight: Int): PdfImageStamp {
        val rotatedBitmap = rotateBitmap(stamp.bitmap)
        val newPosition = Offset(
            pageHeight - stamp.position.y - rotatedBitmap.height,
            stamp.position.x
        )
        return stamp.copy(bitmap = rotatedBitmap, position = newPosition)
    }

    private suspend fun renderPdf(contentResolver: ContentResolver, uri: Uri): List<PdfPageState> = withContext(Dispatchers.IO) {
        val descriptor = contentResolver.openFileDescriptor(uri, "r") ?: return@withContext emptyList()
        val renderer = PdfRenderer(descriptor)
        val pages = mutableListOf<PdfPageState>()
        try {
            for (index in 0 until renderer.pageCount) {
                renderer.openPage(index).use { page ->
                    val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    pages += PdfPageState(
                        index = index,
                        width = bitmap.width,
                        height = bitmap.height,
                        rotationDegrees = 0,
                        bitmap = bitmap
                    )
                }
            }
        } finally {
            renderer.close()
            descriptor.close()
        }
        pages
    }

    private fun resolveDisplayName(contentResolver: ContentResolver, uri: Uri): String {
        return runCatching {
            contentResolver.query(
                uri,
                arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                val column = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                if (cursor.moveToFirst() && column != -1) cursor.getString(column) else null
            }
        }.getOrNull() ?: uri.lastPathSegment ?: "EditedDocument.pdf"
    }

    private suspend fun createImagesArchive(snapshot: PdfEditorState): File? = withContext(Dispatchers.IO) {
        if (snapshot.pages.isEmpty()) return@withContext null
        try {
            val baseName = sanitizeFileNameSegment(snapshot.documentName.substringBeforeLast('.').ifBlank { "PDF_Wizard_Export" })
            val archive = createExportFile(snapshot.documentName, suffix = "images", extension = "zip", subDirectory = "exports/images")
            ZipOutputStream(FileOutputStream(archive)).use { zip ->
                snapshot.pages.forEachIndexed { index, page ->
                    val entryName = "${baseName}_page_${index + 1}.png"
                    val bytes = ByteArrayOutputStream().use { buffer ->
                        page.bitmap.compress(Bitmap.CompressFormat.PNG, 100, buffer)
                        buffer.toByteArray()
                    }
                    val entry = ZipEntry(entryName).apply { size = bytes.size.toLong() }
                    zip.putNextEntry(entry)
                    zip.write(bytes)
                    zip.closeEntry()
                }
            }
            archive
        } catch (io: IOException) {
            null
        }
    }

    private fun createPagesFromImages(uris: List<Uri>): List<PdfPageState> {
        val pages = mutableListOf<PdfPageState>()
        uris.forEachIndexed { index, uri ->
            val bitmap = decodeBitmapFromUri(uri) ?: return@forEachIndexed
            pages += PdfPageState(
                index = index,
                width = bitmap.width,
                height = bitmap.height,
                rotationDegrees = 0,
                bitmap = bitmap
            )
        }
        return pages
    }

    private fun decodeBitmapFromUri(uri: Uri, maxDimension: Int = 2480): Bitmap? {
        return runCatching {
            val resolver = appContext.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            var sampleSize = 1
            while (bounds.outWidth / sampleSize > maxDimension || bounds.outHeight / sampleSize > maxDimension) {
                sampleSize *= 2
            }
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, decodeOptions) }
        }.getOrNull()
    }

    private fun createExportFile(
        documentName: String,
        suffix: String = "",
        extension: String = "pdf",
        subDirectory: String = "exports"
    ): File {
        val baseName = sanitizeFileNameSegment(
            documentName.substringBeforeLast('.').ifBlank { "PDF_Wizard_Export" }
        )
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val extra = suffix.trim().takeIf { it.isNotBlank() }
            ?.let { "_${sanitizeFileNameSegment(it)}" }.orEmpty()
        val exportDir = File(appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), subDirectory).apply {
            mkdirs()
        }
        return File(exportDir, "${baseName}_${timestamp}${extra}.$extension")
    }

    private fun sanitizeFileNameSegment(value: String): String {
        return value
            .replace("[^A-Za-z0-9-_]".toRegex(), "_")
            .replace("_+".toRegex(), "_")
            .trim('_')
            .ifBlank { "document" }
    }

    private fun applyMetadata(file: File, metadata: PdfMetadata) {
        runCatching {
            PDDocument.load(file).use { document ->
                val info = document.documentInformation ?: PDDocumentInformation().also { document.documentInformation = it }
                info.title = metadata.title
                info.author = metadata.author
                info.subject = metadata.subject
                info.keywords = metadata.keywords
                info.creator = metadata.creator
                info.producer = metadata.producer
                val calendar = Calendar.getInstance().apply { timeInMillis = metadata.modifiedAt }
                info.modificationDate = calendar
                if (info.creationDate == null) {
                    info.creationDate = calendar
                }
                document.save(file)
            }
        }
    }

    private fun Color.toArgb(): Int = android.graphics.Color.argb(
        (alpha * 255).toInt().coerceIn(0, 255),
        (red * 255).toInt().coerceIn(0, 255),
        (green * 255).toInt().coerceIn(0, 255),
        (blue * 255).toInt().coerceIn(0, 255)
    )

    private val Canvas.scaleFactor: Float
        get() = runCatching {
            val field = Canvas::class.java.getDeclaredField("mDensity")
            field.isAccessible = true
            val density = field.getInt(this)
            if (density <= 0) 1f else density / 72f
        }.getOrDefault(1f)

    private fun rotateBitmap(source: Bitmap): Bitmap {
        val matrix = Matrix().apply { postRotate(90f) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    override fun onCleared() {
        super.onCleared()
        textRecognizer.close()
    }
}
