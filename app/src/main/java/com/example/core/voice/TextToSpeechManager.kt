package com.example.core.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TextToSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var speechRate: Float = 1.05f
    private var pitch: Float = 0.95f // Slightly deeper, futuristic cadence

    var onSpeechCompleted: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("TTSManager", "Language not supported, falling back to default")
                tts?.setLanguage(Locale.getDefault())
            }
            tts?.setSpeechRate(speechRate)
            tts?.setPitch(pitch)
            isInitialized = true

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    onSpeechCompleted?.invoke()
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
        } else {
            Log.e("TTSManager", "Failed to initialize TTS")
        }
    }

    fun setSpeechParameters(rate: Float, voicePitch: Float) {
        this.speechRate = rate
        this.pitch = voicePitch
        tts?.setSpeechRate(rate)
        tts?.setPitch(voicePitch)
    }

    fun setVoiceLanguage(locale: Locale) {
        tts?.setLanguage(locale)
    }

    fun speak(text: String, flushQueue: Boolean = true) {
        if (!isInitialized || text.isBlank()) return
        val queueMode = if (flushQueue) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val utteranceId = "jarvis_speech_${System.currentTimeMillis()}"
        _isSpeaking.value = true
        tts?.speak(text, queueMode, null, utteranceId)
    }

    fun stop() {
        if (_isSpeaking.value) {
            tts?.stop()
            _isSpeaking.value = false
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
