package com.apexai.crossfit.feature.vision.presentation.camera

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.CameraState
import com.apexai.crossfit.core.domain.model.Movement
import com.apexai.crossfit.core.domain.model.PoseOverlayData
import com.apexai.crossfit.feature.vision.data.MediaPipePoseLandmarkerHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Inject

// Add CameraState to core models
enum class CameraState { INITIALIZING, READY, RECORDING, ERROR }

data class VisionUiState(
    val cameraState: CameraState = CameraState.INITIALIZING,
    val isRecording: Boolean = false,
    val recordingDurationMs: Long = 0L,
    val currentPoseResult: PoseOverlayData? = null,
    val selectedMovement: Movement? = null,
    val fps: Int = 0,
    val error: String? = null,
    val isFrontCamera: Boolean = false
)

sealed interface VisionEvent {
    data object StartRecording   : VisionEvent
    data object StopRecording    : VisionEvent
    data object PauseRecording   : VisionEvent
    data object DiscardRecording : VisionEvent
    data object FlipCamera       : VisionEvent
    data class MovementSelected(val movement: Movement) : VisionEvent
}

sealed interface VisionEffect {
    data class NavigateToReview(val videoUri: String) : VisionEffect
    data class ShowError(val message: String)         : VisionEffect
}

@HiltViewModel
class VisionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val poseLandmarkerHelper: MediaPipePoseLandmarkerHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisionUiState())
    val uiState: StateFlow<VisionUiState> = _uiState.asStateFlow()

    private val _effects = Channel<VisionEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val analysisExecutor = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var currentRecording: Recording? = null

    private var lastFrameTimestamp = 0L
    private var frameCount = 0

    init {
        setupMediaPipe()
    }

    private fun setupMediaPipe() {
        viewModelScope.launch {
            runCatching {
                poseLandmarkerHelper.resultListener = { poseData ->
                    // FPS calculation
                    frameCount++
                    val now = System.currentTimeMillis()
                    if (now - lastFrameTimestamp >= 1000L) {
                        _uiState.update { it.copy(fps = frameCount) }
                        frameCount = 0
                        lastFrameTimestamp = now
                    }
                    _uiState.update { it.copy(currentPoseResult = poseData) }
                }
                poseLandmarkerHelper.errorListener = { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                poseLandmarkerHelper.setup()
            }.onFailure { e ->
                _uiState.update { it.copy(cameraState = CameraState.ERROR, error = e.message) }
            }
        }
    }

    /**
     * Binds CameraX use cases to the provided lifecycle owner and PreviewView.
     * PreviewView MUST be in PERFORMANCE mode (set before calling this).
     */
    fun startCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        ProcessCameraProvider.getInstance(context).also { future ->
            future.addListener({
                cameraProvider = future.get()
                bindCameraUseCases(lifecycleOwner, previewView)
            }, ContextCompat.getMainExecutor(context))
        }
    }

    private fun bindCameraUseCases(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val provider = cameraProvider ?: return
        val isFront = _uiState.value.isFrontCamera

        val cameraSelector = if (isFront) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(analysisExecutor) { imageProxy ->
                    poseLandmarkerHelper.detectAsync(imageProxy, isFront)
                }
            }

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HD))
            .build()
        val videoCapture = VideoCapture.withOutput(recorder)
        this.videoCapture = videoCapture

        runCatching {
            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis, videoCapture)
            _uiState.update { it.copy(cameraState = CameraState.READY, error = null) }
        }.onFailure { e ->
            _uiState.update { it.copy(cameraState = CameraState.ERROR, error = e.message) }
        }
    }

    fun onEvent(event: VisionEvent) {
        when (event) {
            is VisionEvent.StartRecording    -> startRecording()
            is VisionEvent.StopRecording     -> stopRecording()
            is VisionEvent.PauseRecording    -> pauseRecording()
            is VisionEvent.DiscardRecording  -> discardRecording()
            is VisionEvent.FlipCamera        -> flipCamera()
            is VisionEvent.MovementSelected  -> _uiState.update { it.copy(selectedMovement = event.movement) }
        }
    }

    private fun startRecording() {
        val capture = videoCapture ?: run {
            _uiState.update { it.copy(cameraState = CameraState.ERROR, error = "Camera not ready for recording") }
            return
        }
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "apex_${System.currentTimeMillis()}.mp4")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/ApexAI")
            }
        }
        val outputOptions = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()

        currentRecording = capture.output
            .prepareRecording(context, outputOptions)
            .start(ContextCompat.getMainExecutor(context)) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        _uiState.update { it.copy(isRecording = true, cameraState = CameraState.RECORDING) }
                    }
                    is VideoRecordEvent.Status -> {
                        _uiState.update {
                            it.copy(recordingDurationMs = event.recordingStats.recordedDurationNanos / 1_000_000L)
                        }
                    }
                    is VideoRecordEvent.Finalize -> {
                        _uiState.update { it.copy(isRecording = false, cameraState = CameraState.READY) }
                        viewModelScope.launch {
                            if (!event.hasError()) {
                                _effects.send(VisionEffect.NavigateToReview(event.outputResults.outputUri.toString()))
                            } else {
                                _effects.send(VisionEffect.ShowError("Recording failed (code ${event.error})"))
                            }
                        }
                    }
                    else -> {}
                }
            }
    }

    private fun stopRecording() {
        currentRecording?.stop()
        currentRecording = null
        // NavigateToReview is emitted by the VideoRecordEvent.Finalize callback above
    }

    private fun pauseRecording() {
        currentRecording?.pause()
    }

    private fun discardRecording() {
        currentRecording?.stop()
        currentRecording = null
        _uiState.update { it.copy(isRecording = false, cameraState = CameraState.READY) }
    }

    private fun flipCamera() {
        val current = _uiState.value
        _uiState.update { it.copy(isFrontCamera = !current.isFrontCamera) }
        // LiveCameraScreen's LaunchedEffect(lifecycleOwner, uiState.isFrontCamera) re-triggers startCamera
    }

    override fun onCleared() {
        poseLandmarkerHelper.close()
        analysisExecutor.shutdown()
        cameraProvider?.unbindAll()
        super.onCleared()
    }
}
