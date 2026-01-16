import ExpoModulesCore
import AVFoundation

public class ExpoSettingsModule: Module {

  private let audioEngine = AVAudioEngine()
  private var isRunning = false

  public func definition() -> ModuleDefinition {
    Name("ExpoSettings")

    Events("onAudioFrame", "onAudioError")

    Function("start") {
      self.startAudio()
    }

    Function("stop") {
      self.stopAudio()
    }
  }

  private func startAudio() {
    if isRunning {
      return
    }

    let session = AVAudioSession.sharedInstance()

    do {
      try session.setCategory(
        .playAndRecord,
        mode: .voiceChat,
        options: [.defaultToSpeaker, .allowBluetooth]
      )

      try session.setPreferredSampleRate(16000)
      try session.setActive(true)

      session.requestRecordPermission { granted in
        if !granted {
          self.sendEvent("onAudioError", [
            "error": "Microphone permission denied"
          ])
          return
        }

        self.startEngine()
      }
    } catch {
      sendEvent("onAudioError", [
        "error": error.localizedDescription
      ])
    }
  }

  private func startEngine() {
    let inputNode = audioEngine.inputNode
    let inputFormat = inputNode.inputFormat(forBus: 0)

    let desiredFormat = AVAudioFormat(
      commonFormat: .pcmFormatInt16,
      sampleRate: 16000,
      channels: 1,
      interleaved: true
    )

    guard let format = desiredFormat else {
      sendEvent("onAudioError", [
        "error": "Failed to create audio format"
      ])
      return
    }

    inputNode.removeTap(onBus: 0)

    inputNode.installTap(
      onBus: 0,
      bufferSize: 320,
      format: inputFormat
    ) { buffer, _ in
      self.processBuffer(buffer: buffer, targetFormat: format)
    }

    do {
      try audioEngine.start()
      isRunning = true
    } catch {
      sendEvent("onAudioError", [
        "error": error.localizedDescription
      ])
    }
  }

  private func processBuffer(buffer: AVAudioPCMBuffer, targetFormat: AVAudioFormat) {
    guard let converter = AVAudioConverter(from: buffer.format, to: targetFormat) else {
      return
    }

    let frameCapacity = AVAudioFrameCount(targetFormat.sampleRate / 50)
    guard let pcmBuffer = AVAudioPCMBuffer(
      pcmFormat: targetFormat,
      frameCapacity: frameCapacity
    ) else {
      return
    }

    var error: NSError?
    let inputBlock: AVAudioConverterInputBlock = { _, outStatus in
      outStatus.pointee = .haveData
      return buffer
    }

    converter.convert(to: pcmBuffer, error: &error, withInputFrom: inputBlock)

    if error != nil {
      return
    }

    guard let channelData = pcmBuffer.int16ChannelData else {
      return
    }

    let channel = channelData.pointee
    let frameLength = Int(pcmBuffer.frameLength)
    let byteLength = frameLength * MemoryLayout<Int16>.size

    let data = Data(bytes: channel, count: byteLength)

    sendEvent("onAudioFrame", [
      "sampleRate": 16000,
      "channels": 1,
      "pcm": data.base64EncodedString(),
      "frames": frameLength
    ])
  }

  private func stopAudio() {
    if !isRunning {
      return
    }

    audioEngine.inputNode.removeTap(onBus: 0)
    audioEngine.stop()
    isRunning = false
  }
}
