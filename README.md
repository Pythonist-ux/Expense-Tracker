# PocketLedger Local — Android project

This project wraps the supplied `Local Expense & Notification Tracker.html` in an Android WebView and adds a native `NotificationListenerService` for outgoing payment notifications.

## Build the APK

1. Install Android Studio (current stable) and Android SDK Platform 35.
2. Open this `PocketLedgerAndroid` folder in Android Studio.
3. Allow Gradle sync to download Android Gradle Plugin 8.7.3 and Gradle 8.9.
4. Select **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
5. Android Studio writes the debug APK to `app/build/outputs/apk/debug/app-debug.apk`.
6. Copy the APK to your phone and install it. For direct installation, Android may ask you to allow installs from the file manager/browser used to open it.

## Enable automatic capture

1. Open PocketLedger Local.
2. Open **Android Auto-Listener Setup**.
3. Tap **Enable Notification Access**.
4. In Android Settings, enable PocketLedger Local under Notification access and accept the Android warning only if you are comfortable granting this access.
5. Return to PocketLedger. Payment notifications are queued locally and imported when the app is opened/resumed.

## Important behaviour and limitations

- Android requires explicit Notification Access approval. The app cannot silently grant it.
- The listener filters for outgoing debit/payment wording and an INR/Rs amount, and excludes common incoming-credit, refund and OTP wording. Bank/app notification formats vary, so some alerts may not parse correctly; review the ledger.
- Android may restrict notification access after force-stop, app updates, or vendor battery-management actions. Reopen the app and check the access toggle if capture stops.
- Notifications are processed on-device. The app does not transmit notification text to a server. The HTML still loads Tailwind, Chart.js and SheetJS from their existing CDNs, so those UI/export libraries need internet access; transaction storage remains in WebView local storage.
- The notification listener queues at most 100 pending alerts while the app is closed. Open the app periodically to import them.
- This is a starter implementation, not a bank-certified reconciliation product. Test it with your own low-value transactions and verify the parsed merchant, amount and reference before relying on it.
