package com.example.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class TextToSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speechAmplitude = MutableStateFlow(0f)
    val speechAmplitude: StateFlow<Float> = _speechAmplitude.asStateFlow()

    private var animationJob: Job? = null
    private val mainScope = CoroutineScope(Dispatchers.Main)

    private var onSpeechDoneCallback: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.language = Locale.getDefault()
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    mainScope.launch {
                        _isSpeaking.value = true
                        startAmplitudeSimulation()
                    }
                }

                override fun onDone(utteranceId: String?) {
                    mainScope.launch {
                        stopAmplitudeSimulation()
                        _isSpeaking.value = false
                        onSpeechDoneCallback?.invoke()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    mainScope.launch {
                        stopAmplitudeSimulation()
                        _isSpeaking.value = false
                        onSpeechDoneCallback?.invoke()
                    }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    mainScope.launch {
                        stopAmplitudeSimulation()
                        _isSpeaking.value = false
                        onSpeechDoneCallback?.invoke()
                    }
                }
            })
        } else {
            isInitialized = false
        }
    }

    fun speak(
        text: String,
        pitch: Float = 0.95f,
        rate: Float = 1.05f,
        onDone: (() -> Unit)? = null
    ) {
        if (!isInitialized || tts == null) {
            onDone?.invoke()
            return
        }

        stop()
        onSpeechDoneCallback = onDone

        tts?.setPitch(pitch)
        tts?.setSpeechRate(rate)

        val utteranceId = UUID.randomUUID().toString()
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        stopAmplitudeSimulation()
        _isSpeaking.value = false
        tts?.stop()
    }

    private fun startAmplitudeSimulation() {
        animationJob?.cancel()
        animationJob = mainScope.launch {
            while (isActive && _isSpeaking.value) {
                // Generate realistic dynamic audio wave patterns
                val base = Random.nextFloat() * 0.7f + 0.3f
                _speechAmplitude.value = base
                delay(80)
            }
            _speechAmplitude.value = 0f
        }
    }

    private fun stopAmplitudeSimulation() {
        animationJob?.cancel()
        animationJob = null
        _speechAmplitude.value = 0f
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
