package com.pdfwizard.pro

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdfwizard.pro.BuildConfig
import com.pdfwizard.pro.PdfMetadata
import java.text.DateFormat
import java.util.Date
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfEditorScreen(
    state: PdfEditorState,
    snackbarHostState: SnackbarHostState,
    onOpenPdf: () -> Unit,
    onDeletePage: () -> Unit,
    onRotatePage: () -> Unit,
    onMovePageLeft: () -> Unit,
    onMovePageRight: () -> Unit,
    onDuplicatePage: () -> Unit,
    onInsertBlankPage: () -> Unit,
    onAddAnnotation: (Int, PdfAnnotation) -> Unit,
    onInsertImage: () -> Unit,
    onSave: (onShareReady: (Uri?) -> Unit) -> Unit,
    onPageSelected: (Int) -> Unit,
    onEditMetadata: () -> Unit,
    onExportImages: () -> Unit,
    onCompressPdf: () -> Unit,
    onMergePdf: () -> Unit,
    onImagesToPdf: () -> Unit,
    onRecognizeText: () -> Unit,
    onBeginContentEditing: () -> Unit,
    onExportRecognizedText: () -> Unit,
    onDismissContentEditor: () -> Unit,
    onContentEditorChanged: (String) -> Unit,
    onContentEditorApply: () -> Unit,
    onDismissMetadata: () -> Unit,
    onMetadataUpdated: (PdfMetadata) -> Unit
) {
    val annotationDialogVisible = remember { mutableStateOf(false) }
    val annotationText = remember { mutableStateOf("") }
    val pendingAnnotation = remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            !state.hasDocument -> stringResource(R.string.no_document_title)
                            state.metadata.title.isNotBlank() -> state.metadata.title
                            else -> state.documentName
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    IconButton(onClick = onOpenPdf) {
                        Icon(imageVector = Icons.Default.UploadFile, contentDescription = stringResource(R.string.open_pdf))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!state.hasDocument) {
                EmptyDocumentState(onOpenPdf = onOpenPdf)
            } else {
                val currentPage = state.pages[state.currentPageIndex]
                PdfPageViewer(
                    page = currentPage,
                    isProcessing = state.isProcessing,
                    pendingAnnotation = pendingAnnotation,
                    onAnnotationPlaced = { offset ->
                        pendingAnnotation.value?.let { text ->
                            onAddAnnotation(
                                currentPage.index,
                                PdfAnnotation(
                                    id = System.currentTimeMillis(),
                                    pageIndex = currentPage.index,
                                    text = text,
                                    position = offset,
                                    fontSize = 16f
                                )
                            )
                            pendingAnnotation.value = null
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                PageControls(state = state, onPageSelected = onPageSelected)

                Spacer(modifier = Modifier.height(16.dp))

                ActionPanel(
                    state = state,
                    onDeletePage = onDeletePage,
                    onRotatePage = onRotatePage,
                    onMovePageLeft = onMovePageLeft,
                    onMovePageRight = onMovePageRight,
                    onDuplicatePage = onDuplicatePage,
                    onInsertBlankPage = onInsertBlankPage,
                    onAddText = { annotationDialogVisible.value = true },
                    onInsertImage = onInsertImage,
                    onSave = onSave,
                    onEditMetadata = onEditMetadata,
                    onExportImages = onExportImages,
                    onCompressPdf = onCompressPdf,
                    onMergePdf = onMergePdf,
                    onImagesToPdf = onImagesToPdf
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.hasDocument) {
                    ContentToolsCard(
                        state = state,
                        onRecognizeText = onRecognizeText,
                        onBeginContentEditing = onBeginContentEditing,
                        onExportRecognizedText = onExportRecognizedText
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DocumentInfoCard(state = state, onEditMetadata = onEditMetadata)
                }
            }
        }
    }

    if (annotationDialogVisible.value) {
        AnnotationDialog(
            textState = annotationText,
            onDismiss = { annotationDialogVisible.value = false },
            onConfirm = {
                pendingAnnotation.value = annotationText.value
                annotationText.value = ""
                annotationDialogVisible.value = false
            }
        )
    }

    if (state.showMetadataEditor) {
        MetadataEditorDialog(
            initialMetadata = state.metadata,
            onDismiss = onDismissMetadata,
            onConfirm = onMetadataUpdated
        )
    }

    state.contentEditor?.let { editorState ->
        ContentEditorDialog(
            editorState = editorState,
            onDismiss = onDismissContentEditor,
            onValueChange = onContentEditorChanged,
            onConfirm = onContentEditorApply
        )
    }
}

@Composable
private fun PdfPageViewer(
    page: PdfPageState,
    isProcessing: Boolean,
    pendingAnnotation: MutableState<String?>,
    onAnnotationPlaced: (Offset) -> Unit
) {
    var viewSize by remember { mutableStateOf(IntSize.Zero) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        val ratio = page.width.toFloat() / page.height.toFloat()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ratio)
                .background(Color.White)
                .border(1.dp, MaterialTheme.colorScheme.outline)
                .onGloballyPositioned { coordinates ->
                    viewSize = coordinates.size
                }
                .pointerInput(page.index, pendingAnnotation.value, viewSize) {
                    detectTapGestures { tapOffset ->
                        pendingAnnotation.value?.let {
                            val scaleX = if (viewSize.width == 0) 1f else page.width.toFloat() / viewSize.width
                            val scaleY = if (viewSize.height == 0) 1f else page.height.toFloat() / viewSize.height
                            val adjusted = Offset(tapOffset.x * scaleX, tapOffset.y * scaleY)
                            onAnnotationPlaced(adjusted)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                bitmap = page.bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )

            AnnotationsOverlay(page = page, viewSize = viewSize)
            ImagesOverlay(page = page, viewSize = viewSize)

            AnimatedVisibility(visible = isProcessing, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun BoxScope.AnnotationsOverlay(page: PdfPageState, viewSize: IntSize) {
    if (viewSize.width == 0 || viewSize.height == 0) return
    val widthRatio = viewSize.width / page.width.toFloat()
    val heightRatio = viewSize.height / page.height.toFloat()
    page.annotations.forEach { annotation ->
        Text(
            text = annotation.text,
            color = Color(0xFF0058FB),
            fontSize = (annotation.fontSize * heightRatio).sp,
            modifier = Modifier.offset {
                IntOffset(
                    (annotation.position.x * widthRatio).roundToInt(),
                    (annotation.position.y * heightRatio).roundToInt()
                )
            }
        )
    }
}

@Composable
private fun BoxScope.ImagesOverlay(page: PdfPageState, viewSize: IntSize) {
    if (viewSize.width == 0 || viewSize.height == 0) return
    val widthRatio = viewSize.width / page.width.toFloat()
    val heightRatio = viewSize.height / page.height.toFloat()
    val density = LocalDensity.current
    page.images.forEach { stamp ->
        androidx.compose.foundation.Image(
            bitmap = stamp.bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .offset {
                    IntOffset(
                        (stamp.position.x * widthRatio).roundToInt(),
                        (stamp.position.y * heightRatio).roundToInt()
                    )
                }
                .size(
                    width = with(density) { (stamp.bitmap.width * widthRatio).toDp() },
                    height = with(density) { (stamp.bitmap.height * heightRatio).toDp() }
                )
        )
    }
}

@Composable
private fun PageControls(
    state: PdfEditorState,
    onPageSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.page_label, state.currentPageIndex + 1, state.pageCount),
            style = MaterialTheme.typography.bodyMedium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { onPageSelected((state.currentPageIndex - 1).coerceAtLeast(0)) },
                enabled = state.currentPageIndex > 0
            ) {
                Text(text = "Previous")
            }
            OutlinedButton(
                onClick = { onPageSelected((state.currentPageIndex + 1).coerceAtMost(state.pageCount - 1)) },
                enabled = state.currentPageIndex < state.pageCount - 1
            ) {
                Text(text = "Next")
            }
        }
    }
}

@Composable
private fun ActionPanel(
    state: PdfEditorState,
    onDeletePage: () -> Unit,
    onRotatePage: () -> Unit,
    onMovePageLeft: () -> Unit,
    onMovePageRight: () -> Unit,
    onDuplicatePage: () -> Unit,
    onInsertBlankPage: () -> Unit,
    onAddText: () -> Unit,
    onInsertImage: () -> Unit,
    onSave: (onShareReady: (Uri?) -> Unit) -> Unit,
    onEditMetadata: () -> Unit,
    onExportImages: () -> Unit,
    onCompressPdf: () -> Unit,
    onMergePdf: () -> Unit,
    onImagesToPdf: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RowActionButton(
                icon = Icons.Default.Delete,
                label = stringResource(R.string.delete_page),
                enabled = state.pageCount > 0,
                onClick = onDeletePage
            )
            RowActionButton(
                icon = Icons.Default.RotateRight,
                label = stringResource(R.string.rotate_page),
                enabled = state.pageCount > 0,
                onClick = onRotatePage
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RowActionButton(
                icon = Icons.Default.ChevronLeft,
                label = stringResource(R.string.move_page_left),
                enabled = state.currentPageIndex > 0,
                onClick = onMovePageLeft
            )
            RowActionButton(
                icon = Icons.Default.ChevronRight,
                label = stringResource(R.string.move_page_right),
                enabled = state.currentPageIndex < state.pageCount - 1,
                onClick = onMovePageRight
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RowActionButton(
                icon = Icons.Default.ContentCopy,
                label = stringResource(R.string.duplicate_page),
                enabled = state.pageCount > 0,
                onClick = onDuplicatePage
            )
            RowActionButton(
                icon = Icons.Default.NoteAdd,
                label = stringResource(R.string.insert_blank_page),
                enabled = true,
                onClick = onInsertBlankPage
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RowActionButton(
                icon = Icons.Default.Add,
                label = stringResource(R.string.add_text),
                enabled = state.pageCount > 0,
                onClick = onAddText
            )
            RowActionButton(
                icon = Icons.Default.Image,
                label = stringResource(R.string.insert_image),
                enabled = state.pageCount > 0,
                onClick = onInsertImage
            )
        }
        OutlinedButton(
            onClick = onEditMetadata,
            enabled = state.pageCount > 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(imageVector = Icons.Default.Description, contentDescription = stringResource(R.string.edit_metadata))
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = stringResource(R.string.edit_metadata))
        }
        Button(
            onClick = { onSave {} },
            enabled = state.pageCount > 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = stringResource(R.string.save_pdf))
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = stringResource(R.string.save_pdf))
        }

        AdvancedToolsCard(
            hasDocument = state.hasDocument,
            onMergePdf = onMergePdf,
            onImagesToPdf = onImagesToPdf,
            onExportImages = onExportImages,
            onCompressPdf = onCompressPdf
        )
    }
}

@Composable
private fun ContentToolsCard(
    state: PdfEditorState,
    onRecognizeText: () -> Unit,
    onBeginContentEditing: () -> Unit,
    onExportRecognizedText: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val recognizedText = state.pages.getOrNull(state.currentPageIndex)?.recognizedText
    val previewScroll = rememberScrollState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.content_tools_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.content_tools_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (state.isRecognizingText) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Text(
                text = stringResource(R.string.text_preview_title),
                style = MaterialTheme.typography.titleSmall
            )
            if (recognizedText.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.text_preview_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp, max = 180.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                        .padding(12.dp)
                        .verticalScroll(previewScroll)
                ) {
                    Text(
                        text = recognizedText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RowActionButton(
                    icon = Icons.Default.ManageSearch,
                    label = stringResource(R.string.recognize_text_action),
                    enabled = state.hasDocument && !state.isProcessing && !state.isRecognizingText,
                    onClick = onRecognizeText
                )
                RowActionButton(
                    icon = Icons.Default.Edit,
                    label = stringResource(R.string.edit_text_action),
                    enabled = state.hasDocument && !state.isProcessing,
                    onClick = onBeginContentEditing
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RowActionButton(
                    icon = Icons.Default.TextSnippet,
                    label = stringResource(R.string.export_text_action),
                    enabled = state.hasDocument && !state.isProcessing,
                    onClick = onExportRecognizedText
                )
                RowActionButton(
                    icon = Icons.Default.ContentCopy,
                    label = stringResource(R.string.copy_text_action),
                    enabled = !recognizedText.isNullOrBlank(),
                    onClick = {
                        recognizedText?.let {
                            clipboardManager.setText(AnnotatedString(it))
                            Toast.makeText(context, R.string.text_copied_message, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AdvancedToolsCard(
    hasDocument: Boolean,
    onMergePdf: () -> Unit,
    onImagesToPdf: () -> Unit,
    onExportImages: () -> Unit,
    onCompressPdf: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.advanced_actions_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.advanced_actions_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RowActionButton(
                    icon = Icons.Default.UploadFile,
                    label = stringResource(R.string.merge_pdf_action),
                    enabled = hasDocument,
                    onClick = onMergePdf
                )
                RowActionButton(
                    icon = Icons.Default.Image,
                    label = stringResource(R.string.images_to_pdf_action),
                    enabled = true,
                    onClick = onImagesToPdf
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RowActionButton(
                    icon = Icons.Default.Collections,
                    label = stringResource(R.string.export_images_action),
                    enabled = hasDocument,
                    onClick = onExportImages
                )
                RowActionButton(
                    icon = Icons.Default.Tune,
                    label = stringResource(R.string.compress_pdf_action),
                    enabled = hasDocument,
                    onClick = onCompressPdf
                )
            }
        }
    }
}

@Composable
private fun RowScope.RowActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label)
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = label, textAlign = TextAlign.Center)
    }
}

@Composable
private fun EmptyDocumentState(onOpenPdf: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.UploadFile,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(96.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.pick_pdf_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onOpenPdf) {
            Text(text = stringResource(R.string.open_pdf))
        }
    }
}

@Composable
private fun AnnotationDialog(
    textState: MutableState<String>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.add_text)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(R.string.enter_annotation))
                OutlinedTextField(
                    value = textState.value,
                    onValueChange = { textState.value = it },
                    placeholder = { Text(text = stringResource(R.string.annotation_hint)) }
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = textState.value.isNotBlank()) {
                Text(text = stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun ContentEditorDialog(
    editorState: ContentEditorState,
    onDismiss: () -> Unit,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.content_editor_title, editorState.pageIndex + 1)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.content_editor_hint),
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = editorState.editedText,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = 320.dp),
                    minLines = 6,
                    maxLines = 12
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(R.string.content_editor_save_button))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun MetadataEditorDialog(
    initialMetadata: PdfMetadata,
    onDismiss: () -> Unit,
    onConfirm: (PdfMetadata) -> Unit
) {
    val title = remember(initialMetadata) { mutableStateOf(initialMetadata.title) }
    val author = remember(initialMetadata) { mutableStateOf(initialMetadata.author) }
    val subject = remember(initialMetadata) { mutableStateOf(initialMetadata.subject) }
    val keywords = remember(initialMetadata) { mutableStateOf(initialMetadata.keywords) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.edit_metadata)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title.value,
                    onValueChange = { title.value = it },
                    label = { Text(stringResource(R.string.metadata_title_label)) }
                )
                OutlinedTextField(
                    value = author.value,
                    onValueChange = { author.value = it },
                    label = { Text(stringResource(R.string.metadata_author_label)) }
                )
                OutlinedTextField(
                    value = subject.value,
                    onValueChange = { subject.value = it },
                    label = { Text(stringResource(R.string.metadata_subject_label)) }
                )
                OutlinedTextField(
                    value = keywords.value,
                    onValueChange = { keywords.value = it },
                    label = { Text(stringResource(R.string.metadata_keywords_label)) }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    initialMetadata.copy(
                        title = title.value,
                        author = author.value,
                        subject = subject.value,
                        keywords = keywords.value
                    )
                )
            }) {
                Text(text = stringResource(R.string.save_changes))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun DocumentInfoCard(state: PdfEditorState, onEditMetadata: () -> Unit) {
    val formattedDate = remember(state.metadata.modifiedAt) {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(state.metadata.modifiedAt))
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.document_details_title), style = MaterialTheme.typography.titleMedium)
            Divider()
            InfoRow(label = stringResource(R.string.document_name_label), value = state.documentName)
            InfoRow(label = stringResource(R.string.page_count_label), value = state.pageCount.toString())
            InfoRow(label = stringResource(R.string.document_creator_label), value = stringResource(R.string.app_creator_display))
            InfoRow(label = stringResource(R.string.document_version_label), value = BuildConfig.VERSION_NAME)
            InfoRow(label = stringResource(R.string.metadata_title_label), value = state.metadata.title.ifBlank { stringResource(R.string.metadata_not_set) })
            InfoRow(label = stringResource(R.string.metadata_author_label), value = state.metadata.author.ifBlank { stringResource(R.string.metadata_not_set) })
            InfoRow(label = stringResource(R.string.metadata_subject_label), value = state.metadata.subject.ifBlank { stringResource(R.string.metadata_not_set) })
            InfoRow(label = stringResource(R.string.metadata_keywords_label), value = state.metadata.keywords.ifBlank { stringResource(R.string.metadata_not_set) })
            InfoRow(label = stringResource(R.string.metadata_modified_label), value = formattedDate)
            OutlinedButton(onClick = onEditMetadata, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.edit_metadata))
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
