import * as React from 'react';

import { ExpoSettingsViewProps } from './ExpoSettings.types';

export default function ExpoSettingsView(props: ExpoSettingsViewProps) {
  return (
    <div>
      <iframe
        style={{ flex: 1 }}
        src={props.url}
        onLoad={() => props.onLoad({ nativeEvent: { url: props.url } })}
      />
    </div>
  );
}
