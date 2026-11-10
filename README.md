# Token Saver for Android

Token Saver is an opt-in prompt editing helper for Android. It does **not** read or change text automatically: after you enable Android's accessibility service and choose monitored apps, a small on-screen button appears when a likely prompt field can be shortened. It checks only field hint, accessibility label, or view ID metadata to decide whether a field looks prompt-related. Tap the button to review the suggestion, then choose **Apply** or **Cancel**. Applying is limited to the same focused field and exact text shown in the review.

## Privacy and limits

- The master switch and app allowlist are both required. The allowlist starts empty and is limited to the built-in choices (ChatGPT, Claude, Gemini, Perplexity, Chrome, Firefox, Brave); unselected packages are ignored.
- Accessibility access is sensitive. Android asks you to enable the service in Settings. Token Saver processes the current field on-device and does not transmit text.
- The local duplicate cache stores only SHA-256 fingerprints of prompts that you chose to apply. It stores no prompt text or answers and is capped at 500 fingerprints. Savings are heuristic estimates, not provider token counts.
- Compression removes a small list of filler phrases and whitespace, and minifies whitespace outside JSON strings. Always review the preview: edits may affect meaning or formatting. Sites and apps can reject accessibility text edits or change their input behavior.
- The floating control uses Android's accessibility-overlay window type; no separate draw-over-other-apps permission is requested.

## Build

Open this directory in Android Studio with JDK 17 and Android SDK 35 installed, or run `gradle test` and `gradle assembleDebug` using Gradle 8.9. The project uses Android Gradle Plugin 8.7.3, Kotlin 2.0.21, and Jetpack Compose. The unit tests cover prompt cleanup and token estimation.

## Setup

1. Install and open the app.
2. Choose which supported apps to monitor on the **Apps** tab.
3. Turn on **Prompt helper**. Android's Accessibility settings open; enable **Token Saver prompt helper** there.
4. Return to the app. Turn the master switch off at any time to stop processing; disabling the service in Android settings also stops it.
