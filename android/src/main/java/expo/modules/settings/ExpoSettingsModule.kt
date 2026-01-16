package expo.modules.settings

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Base64
import androidx.core.app.ActivityCompat
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import java.util.concurrent.atomic.AtomicBoolean

class ExpoSettingsModule : Module() {

  private var audioRecord: AudioRecord? = null
  private val isRecording = AtomicBoolean(false)
  private var recordingThread: Thread? = null

  override fun definition() = ModuleDefinition {
    Name("ExpoSettings")

    Events("onAudioFrame", "onAudioError")

    Function("start") {
      startRecording()
    }

    Function("stop") {
      stopRecording()
    }
  }

  private fun startRecording() {
    if (isRecording.get()) return

    val context = appContext.reactContext ?: return
    val activity = appContext.currentActivity

    // 🔑 RUNTIME PERMISSION REQUEST (Android)
    if (
      ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      if (activity != null) {
        ActivityCompat.requestPermissions(
          activity,
          arrayOf(Manifest.permission.RECORD_AUDIO),
          1001
        )
      }
      // ⛔ Do NOT emit error here — user may still grant permission
      return
    }



    val sampleRate = 16000
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    val minBufferSize =
      AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
      sendEvent(
        "onAudioError",
        mapOf("error" to "Invalid AudioRecord buffer size")
      )
      return
    }

    audioRecord =
      AudioRecord(
        MediaRecorder.AudioSource.VOICE_RECOGNITION,
        sampleRate,
        channelConfig,
        audioFormat,
        minBufferSize
      )

    if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
      sendEvent(
        "onAudioError",
        mapOf("error" to "AudioRecord initialization failed")
      )
      return
    }

    audioRecord?.startRecording()
    isRecording.set(true)

    recordingThread =
      Thread {
        readAudioLoop(sampleRate)
      }.apply { start() }
  }

  private fun readAudioLoop(sampleRate: Int) {
    val buffer = ShortArray(320) // 20ms @ 16kHz

    while (isRecording.get()) {
      val read =
        audioRecord?.read(buffer, 0, buffer.size) ?: 0

      if (read > 0) {
        val byteBuffer = ByteArray(read * 2)
        for (i in 0 until read) {
          byteBuffer[i * 2] = (buffer[i].toInt() and 0xFF).toByte()
          byteBuffer[i * 2 + 1] = ((buffer[i].toInt() shr 8) and 0xFF).toByte()
        }

        val base64 =
          Base64.encodeToString(byteBuffer, Base64.NO_WRAP)

        sendEvent(
          "onAudioFrame",
          mapOf(
            "sampleRate" to sampleRate,
            "channels" to 1,
            "frames" to read,
            "pcm" to base64
          )
        )
      }
    }
  }

  private fun stopRecording() {
    if (!isRecording.get()) return

    isRecording.set(false)

    try {
      audioRecord?.stop()
      audioRecord?.release()
    } catch (_: Exception) {
    }

    audioRecord = null
    recordingThread = null
  }
}
