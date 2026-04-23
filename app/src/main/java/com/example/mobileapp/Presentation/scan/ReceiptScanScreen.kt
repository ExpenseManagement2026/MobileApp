package com.example.mobileapp.presentation.scan

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobileapp.domain.model.ReceiptScanResult
import java.util.concurrent.Executor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScanScreen(
    onNavigateBack: () -> Unit,
    onScanComplete: (ReceiptScanResult) -> Unit,
    viewModel: ReceiptScanViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var hasCameraPermission by remember { mutableStateOf(false) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { hasCameraPermission = it }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Hóa Đơn") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") }
                },
                actions = {
                    IconButton(onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT)
                            CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT
                    }) {
                        Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Đổi Camera", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2DC98E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                !hasCameraPermission -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Cần quyền Camera để scan") }
                capturedBitmap != null -> CapturedImageContent(
                    bitmap = capturedBitmap!!,
                    uiState = uiState,
                    onRetake = { capturedBitmap = null; viewModel.resetState() },
                    onConfirm = { onScanComplete(it) }
                )
                else -> CameraPreviewContent(
                    lensFacing = lensFacing,
                    onImageCaptured = { bitmap ->
                        capturedBitmap = bitmap
                        viewModel.scanReceipt(bitmap)
                    }
                )
            }
        }
    }
}

@Composable
private fun CameraPreviewContent(
    lensFacing: Int,
    onImageCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraExecutor = remember { ContextCompat.getMainExecutor(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        key(lensFacing) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                        
                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()
                        imageCapture = capture

                        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, capture)
                        } catch (e: Exception) { Log.e("Camera", "Failed", e) }
                    }, cameraExecutor)
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))) {
                Text("Đặt hóa đơn vào khung hình", modifier = Modifier.padding(16.dp), color = Color.White)
            }
            Spacer(modifier = Modifier.weight(1f))
            FloatingActionButton(
                onClick = { imageCapture?.let { captureImage(it, lensFacing, cameraExecutor, onImageCaptured) } },
                modifier = Modifier.size(72.dp).padding(bottom = 32.dp),
                containerColor = Color(0xFF2DC98E)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Chụp", modifier = Modifier.size(32.dp), tint = Color.White)
            }
        }
    }
}

private fun captureImage(imageCapture: ImageCapture, lensFacing: Int, executor: Executor, onImageCaptured: (Bitmap) -> Unit) {
    imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val bitmap = image.toBitmap()
            val rotation = image.imageInfo.rotationDegrees.toFloat()
            val finalBitmap = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                flipBitmap(rotateBitmap(bitmap, rotation))
            } else {
                rotateBitmap(bitmap, rotation)
            }
            onImageCaptured(finalBitmap)
            image.close()
        }
        override fun onError(e: ImageCaptureException) { Log.e("Camera", "Error", e) }
    })
}

@Composable
private fun CapturedImageContent(bitmap: Bitmap, uiState: ScanUiState, onRetake: () -> Unit, onConfirm: (ReceiptScanResult) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize())
        }
        when (uiState) {
            is ScanUiState.Loading -> Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) { CircularProgressIndicator() }
            is ScanUiState.Success -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Kết quả AI Scan", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        HorizontalDivider()
                        Text("Cửa hàng: ${uiState.result.merchantName ?: "Không rõ"}")
                        Text("Tổng tiền: %,.0f đ".format(uiState.result.totalAmount ?: 0.0), color = Color(0xFF2DC98E), fontWeight = FontWeight.Bold)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = onRetake, Modifier.weight(1f)) { Text("Chụp lại") }
                    Button(onClick = { onConfirm(uiState.result) }, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DC98E))) { Text("Xác nhận") }
                }
            }
            is ScanUiState.Error -> {
                Text("Lỗi: ${uiState.message}", color = Color.Red)
                Button(onClick = onRetake, Modifier.fillMaxWidth()) { Text("Thử lại") }
            }
            else -> {}
        }
    }
}

private fun ImageProxy.toBitmap(): Bitmap {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun flipBitmap(bitmap: Bitmap): Bitmap {
    val matrix = Matrix().apply { preScale(-1f, 1f) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
