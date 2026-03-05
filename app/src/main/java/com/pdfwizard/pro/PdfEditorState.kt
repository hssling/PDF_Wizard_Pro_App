package com.pdfwizard.pro

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class PdfAnnotation(
    val id: Long,
    val pageIndex: Int,
    val text: String,
    val position: Offset,
    val color: Color = Color(0xFF0058FB),
    val fontSize: Float = 16f
)

data class PdfImageStamp(
    val id: Long,
    val pageIndex: Int,
    val bitmap: Bitmap,
    val position: Offset,
    val scale: Float = 1f
)

data class PdfPageState(
    val index: Int,
    val width: Int,
    val height: Int,
    val rotationDegrees: Int,
    val bitmap: Bitmap,
    val annotations: List<PdfAnnotation> = emptyList(),
    val images: List<PdfImageStamp> = emptyList(),
    val recognizedText: String? = null
)

data class PdfMetadata(
    val title: String = "",
    val author: String = "",
    val subject: String = "",
    val keywords: String = "",
    val creator: String = "Dr Siddalingaiah H S",
    val producer: String = "PDF Wizard Pro • Dr Siddalingaiah H S",
    val modifiedAt: Long = System.currentTimeMillis()
) {
    fun touch(): PdfMetadata = copy(modifiedAt = System.currentTimeMillis())
}

data class ContentEditorState(
    val pageIndex: Int,
    val originalText: String,
    val editedText: String
)

data class PdfEditorState(
    val documentName: String = "",
    val pages: List<PdfPageState> = emptyList(),
    val currentPageIndex: Int = 0,
    val isProcessing: Boolean = false,
    val lastSavedFilePath: String? = null,
    val message: String? = null,
    val error: String? = null,
    val metadata: PdfMetadata = PdfMetadata(),
    val showMetadataEditor: Boolean = false,
    val isRecognizingText: Boolean = false,
    val contentEditor: ContentEditorState? = null
) {
    val hasDocument: Boolean get() = pages.isNotEmpty()
    val pageCount: Int get() = pages.size
}
