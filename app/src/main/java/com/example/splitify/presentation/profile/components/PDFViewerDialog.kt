package com.example.splitify.presentation.profile.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.splitify.presentation.theme.PrimaryColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PDFViewerDialog(
//    pdfFile: File,
//    onDismiss: () -> Unit
//) {
//    val context = LocalContext.current
//    var pdfPages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
//    var isLoading by remember { mutableStateOf(true) }
//    var scale by remember { mutableStateOf(1f) }
//    var offsetX by remember { mutableStateOf(0f) }
//    var offsetY by remember { mutableStateOf(0f) }
//
//    LaunchedEffect(pdfFile) {
//        withContext(Dispatchers.IO) {
//            try {
//                val fileDescriptor = ParcelFileDescriptor.open(
//                    pdfFile,
//                    ParcelFileDescriptor.MODE_READ_ONLY
//                )
//                val pdfRenderer = PdfRenderer(fileDescriptor)
//
//                val pages = mutableListOf<Bitmap>()
//                for (i in 0 until pdfRenderer.pageCount) {
//                    val page = pdfRenderer.openPage(i)
//
//                    // Create bitmap with higher resolution
//                    val bitmap = Bitmap.createBitmap(
//                        page.width * 2,
//                        page.height * 2,
//                        Bitmap.Config.ARGB_8888
//                    )
//
//                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
//                    pages.add(bitmap)
//                    page.close()
//                }
//
//                pdfRenderer.close()
//                fileDescriptor.close()
//
//                pdfPages = pages
//                isLoading = false
//            } catch (e: Exception) {
//                isLoading = false
//            }
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Resume", color = Color.White) },
//                navigationIcon = {
//                    IconButton(onClick = onDismiss) {
//                        Icon(
//                            imageVector = Icons.Default.Close,
//                            contentDescription = "Close",
//                            tint = Color.White
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = PrimaryColors.Primary600
//                )
//            )
//        }
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .background(Color(0xFF303030))
//        ) {
//            when {
//                isLoading -> {
//                    CircularProgressIndicator(
//                        modifier = Modifier.align(Alignment.Center),
//                        color = PrimaryColors.Primary600
//                    )
//                }
//                pdfPages.isEmpty() -> {
//                    Text(
//                        "Failed to load PDF",
//                        modifier = Modifier.align(Alignment.Center),
//                        color = Color.White
//                    )
//                }
//                else -> {
//                    LazyColumn(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .pointerInput(Unit) {
//                                detectTransformGestures { _, pan, zoom, _ ->
//                                    scale = (scale * zoom).coerceIn(1f, 3f)
//                                    if (scale > 1f) {
//                                        offsetX += pan.x
//                                        offsetY += pan.y
//                                    } else {
//                                        offsetX = 0f
//                                        offsetY = 0f
//                                    }
//                                }
//                            },
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        contentPadding = PaddingValues(16.dp),
//                        verticalArrangement = Arrangement.spacedBy(16.dp)
//                    ) {
//                        items(pdfPages) { page ->
//                            Image(
//                                bitmap = page.asImageBitmap(),
//                                contentDescription = "PDF Page",
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .graphicsLayer(
//                                        scaleX = scale,
//                                        scaleY = scale,
//                                        translationX = offsetX,
//                                        translationY = offsetY
//                                    )
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PDFViewerDialog(
    pdfFile: File,
    onDismiss: () -> Unit
) {
    var pdfPages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(pdfFile) {
        withContext(Dispatchers.IO) {
            try {
                val fileDescriptor = ParcelFileDescriptor.open(
                    pdfFile,
                    ParcelFileDescriptor.MODE_READ_ONLY
                )
                val pdfRenderer = PdfRenderer(fileDescriptor)

                val pages = mutableListOf<Bitmap>()

                for (i in 0 until pdfRenderer.pageCount) {
                    val page = pdfRenderer.openPage(i)

                    // Render at EXACT page resolution
                    val bitmap = Bitmap.createBitmap(
                        page.width,
                        page.height,
                        Bitmap.Config.ARGB_8888
                    )

                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    pages.add(bitmap)
                    page.close()
                }

                pdfRenderer.close()
                fileDescriptor.close()

                pdfPages = pages
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resume") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {

            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                pdfPages.isEmpty() -> {
                    Text(
                        "Failed to load PDF",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pdfPages) { page ->
                            Image(
                                bitmap = page.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            )
                        }
                    }
                }
            }
        }
    }
}
