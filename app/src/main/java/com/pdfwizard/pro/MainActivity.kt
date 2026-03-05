package com.pdfwizard.pro

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pdfwizard.pro.ui.theme.PdfWizardTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: PdfEditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            val context = LocalContext.current
            val snackbarHostState = remember { SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()
            val isDarkTheme = isSystemInDarkTheme()

            val pdfPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                if (uri != null) {
                    grantUriPermission(uri)
                    viewModel.loadPdf(uri)
                }
            }

            val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                if (uri != null) {
                    viewModel.addImageFromUri(uri)
                }
            }

            val mergePdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                if (uri != null) {
                    grantUriPermission(uri)
                    viewModel.mergeWithPdf(uri)
                }
            }

            val imagesToPdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
                if (!uris.isNullOrEmpty()) {
                    uris.forEach { grantUriPermission(it) }
                    viewModel.importImagesAsDocument(uris)
                }
            }

            PdfWizardTheme(darkTheme = isDarkTheme) {
                PdfEditorScreen(
                    state = state,
                    snackbarHostState = snackbarHostState,
                    onOpenPdf = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                    onDeletePage = viewModel::deleteCurrentPage,
                    onRotatePage = viewModel::rotateCurrentPage,
                    onMovePageLeft = viewModel::moveCurrentPageLeft,
                    onMovePageRight = viewModel::moveCurrentPageRight,
                    onDuplicatePage = viewModel::duplicateCurrentPage,
                    onInsertBlankPage = viewModel::insertBlankPage,
                    onAddAnnotation = viewModel::addAnnotation,
                    onInsertImage = { imagePickerLauncher.launch("image/*") },
                    onSave = { callback ->
                        viewModel.exportPdf { uri ->
                            callback(uri)
                            uri?.let { shareUri -> sharePdf(shareUri) }
                        }
                    },
                    onPageSelected = viewModel::setCurrentPage,
                    onEditMetadata = { viewModel.toggleMetadataEditor(true) },
                    onExportImages = {
                        viewModel.exportPagesAsImages { uri ->
                            uri?.let { shareFile(it, "application/zip", getString(R.string.share_images_title)) }
                        }
                    },
                    onCompressPdf = {
                        viewModel.exportCompressedPdf(onResult = { uri ->
                            uri?.let { shareFile(it, "application/pdf", getString(R.string.share_compressed_pdf_title)) }
                        })
                    },
                    onMergePdf = { mergePdfLauncher.launch(arrayOf("application/pdf")) },
                    onImagesToPdf = { imagesToPdfLauncher.launch(arrayOf("image/*")) },
                    onRecognizeText = viewModel::recognizeCurrentPageText,
                    onBeginContentEditing = viewModel::beginContentEditing,
                    onExportRecognizedText = {
                        viewModel.exportRecognizedText { uri ->
                            uri?.let { shareFile(it, "text/plain", getString(R.string.share_text_title)) }
                        }
                    },
                    onDismissContentEditor = viewModel::dismissContentEditor,
                    onContentEditorChanged = viewModel::updateContentEditor,
                    onContentEditorApply = viewModel::applyContentEditorChanges,
                    onDismissMetadata = { viewModel.toggleMetadataEditor(false) },
                    onMetadataUpdated = viewModel::updateMetadata
                )
            }

            LaunchedEffect(state.message, state.error) {
                state.message?.let { message ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
                        viewModel.clearNotifications()
                    }
                }
                state.error?.let { message ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
                        viewModel.clearNotifications()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val data = intent?.data ?: return
        grantUriPermission(data)
        viewModel.loadPdf(data)
    }

    private fun grantUriPermission(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) {
            // Uri may not be persistable
        }
    }

    private fun sharePdf(uri: Uri) {
        shareFile(uri, "application/pdf", getString(R.string.share_title))
    }

    private fun shareFile(uri: Uri, mimeType: String, chooserTitle: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, chooserTitle))
    }
}
