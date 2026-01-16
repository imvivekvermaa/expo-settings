// import { EventSubscription } from "expo-modules-core";
import * as ExpoSettings from "expo-settings";

import { useEffect, useRef } from "react";
import { Button, Text, View } from "react-native";
// import * as ExpoSettings from "./index";

export default function App() {
  const frameCountRef = useRef(0);

  useEffect(() => {
    const frameSub = ExpoSettings.addAudioFrameListener((event) => {
      frameCountRef.current += 1;

      if (frameCountRef.current % 50 === 0) {
        const pcmBytes =
          (event.pcm.length * 3) / 4; // base64 → bytes approximation

        console.log(
          `[AUDIO] frames=${event.frames} sampleRate=${event.sampleRate} bytes≈${pcmBytes}`
        );
      }
    });

    const errorSub = ExpoSettings.addAudioErrorListener((event) => {
      console.error("[AUDIO ERROR]", event.error);
    });

    return () => {
      frameSub.remove();
      errorSub.remove();
    };
  }, []);

  return (
    <View
      style={{
        flex: 1,
        alignItems: "center",
        justifyContent: "center",
        gap: 12,
      }}
    >
      <Text>Live PCM Microphone Test</Text>

      <Button title="Start Recording" onPress={ExpoSettings.start} />
      <Button title="Stop Recording" onPress={ExpoSettings.stop} />
    </View>
  );
}
