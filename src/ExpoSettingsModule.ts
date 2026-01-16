import { NativeModule, requireNativeModule } from "expo";
import { ExpoSettingsModuleEvents } from "./ExpoSettings.types";

declare class ExpoSettingsModule extends NativeModule<ExpoSettingsModuleEvents> {
  start(): void;
  stop(): void;
}

export default requireNativeModule<ExpoSettingsModule>("ExpoSettings");
