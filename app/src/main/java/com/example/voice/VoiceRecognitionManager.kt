package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceRecognitionManager(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _rmsAmplitude = MutableStateFlow(0f)
    val rmsAmplitude: StateFlow<Float> = _rmsAmplitude.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private var onSpeechResultListener: ((String) -> Unit)? = null
    private var onSpeechErrorListener: ((String) -> Unit)? = null

    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        onSpeechResultListener = onResult
        onSpeechErrorListener = onError
        _partialText.value = ""

        mainHandler.post {
            try {
                stopListeningInternal()

                if (!isRecognitionAvailable()) {
                    _isListening.value = false
                    onError("Voice recognition service not available on this device. You can type queries directly into the console.")
                    return@post
                }

                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _isListening.value = true
                        }

                        override fun onBeginningOfSpeech() {
                            _isListening.value = true
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            // Normalize RMS dB (typical range -2 to 10) to 0f..1f
                            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1f)
                            _rmsAmplitude.value = normalized
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _isListening.value = false
                            _rmsAmplitude.value = 0f
                        }

                        override fun onError(error: Int) {
                            _isListening.value = false
                            _rmsAmplitude.value = 0f
                            val errorMessage = when (error) {
                                SpeechRecognizer.ERROR_AUDIO -> "Audio recording problem detected."
                                SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error."
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
                                SpeechRecognizer.ERROR_NETWORK -> "Network issue while processing speech."
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech network connection timed out."
                                SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please speak clearly into the microphone."
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is currently busy."
                                SpeechRecognizer.ERROR_SERVER -> "Voice processing server error."
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "I didn't hear anything, sir."
                                else -> "Voice recognition encountered an issue ($error)."
                            }
                            onSpeechErrorListener?.invoke(errorMessage)
                        }

                        override fun onResults(results: Bundle?) {
                            _isListening.value = false
                            _rmsAmplitude.value = 0f
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val spokenText = matches?.firstOrNull()?.trim().orEmpty()
                            if (spokenText.isNotEmpty()) {
                                _partialText.value = spokenText
                                onSpeechResultListener?.invoke(spokenText)
                            } else {
                                onSpeechErrorListener?.invoke("No clear voice detected.")
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val partials = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = partials?.firstOrNull().orEmpty()
                            if (text.isNotEmpty()) {
                                _partialText.value = text
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                _isListening.value = false
                onError("Unable to initialize speech recognizer: ${e.localizedMessage}")
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            stopListeningInternal()
        }
    }

    private fun stopListeningInternal() {
        try {
            _isListening.value = false
            _rmsAmplitude.value = 0f
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            // Ignore clean up errors
        }
    }
}
