export type AudioFrameEvent = {
  sampleRate: number;
  channels: number;
  frames: number;
  pcm: string; // base64 encoded Int16 PCM
};

export type AudioErrorEvent = {
  error: string;
};

export type ExpoSettingsModuleEvents = {
  onAudioFrame: (event: AudioFrameEvent) => void;
  onAudioError: (event: AudioErrorEvent) => void;
};
