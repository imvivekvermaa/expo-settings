import { registerWebModule, NativeModule } from 'expo';

import { ExpoSettingsModuleEvents } from './ExpoSettings.types';

class ExpoSettingsModule extends NativeModule<ExpoSettingsModuleEvents> {
  PI = Math.PI;
  async setValueAsync(value: string): Promise<void> {
    this.emit('onChange', { value });
  }
  hello() {
    return 'Hello world! 👋';
  }
}

export default registerWebModule(ExpoSettingsModule, 'ExpoSettingsModule');
