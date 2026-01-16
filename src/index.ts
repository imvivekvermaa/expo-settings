import { EventSubscription } from "expo-modules-core";
import ExpoSettingsModule from "./ExpoSettingsModule";
import { AudioFrameEvent, AudioErrorEvent } from "./ExpoSettings.types";

export function start(): void {
  ExpoSettingsModule.start();
}

export function stop(): void {
  ExpoSettingsModule.stop();
}

export function addAudioFrameListener(
  listener: (event: AudioFrameEvent) => void
): EventSubscription {
  return ExpoSettingsModule.addListener("onAudioFrame", listener);
}

export function addAudioErrorListener(
  listener: (event: AudioErrorEvent) => void
): EventSubscription {
  return ExpoSettingsModule.addListener("onAudioError", listener);
}
