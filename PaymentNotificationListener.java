package com.pocketledger.local;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.app.Notification;
import android.os.Bundle;
import org.json.JSONArray;
import java.util.HashSet;
import java.util.Set;

public class PaymentNotificationListener extends NotificationListenerService {
    private static final String PREFS = "pocketledger_native";
    private static final String QUEUE = "pending_notifications";
    private final Set<String> seen = new HashSet<>();

    @Override public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || sbn.getNotification() == null) return;
        Bundle extras = sbn.getNotification().extras;
        if (extras == null) return;
        CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        if (text == null) text = extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence sub = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
        String body = join(title, text, sub);
        if (body.length() < 8) return;
        String lower = body.toLowerCase();
        // Capture outgoing payment/debit alerts only. Avoid incoming credits, OTPs and refunds.
        if (lower.matches("(?s).*(credited|credit to|received|refund|reversed|otp|one.time.password).*")) return;
        if (!lower.matches("(?s).*(debited|debit|paid|spent|purchase|payment of|sent rs|sent inr|sent ₹).*")) return;
        if (!lower.matches("(?s).*(₹|inr|rs\\.?)[ ]*[0-9,]+([.][0-9]{1,2})?.*")) return;
        String key = sbn.getPackageName() + "|" + sbn.getPostTime() + "|" + body;
        synchronized (seen) {
            if (!seen.add(key)) return;
            if (seen.size() > 500) seen.clear();
        }
        enqueue(body);
    }

    private String join(CharSequence... values) {
        StringBuilder b = new StringBuilder();
        for (CharSequence v : values) if (v != null && v.length() > 0) {
            if (b.length() > 0) b.append(" — ");
            b.append(v);
        }
        return b.toString();
    }

    private void enqueue(String body) {
        synchronized (PaymentNotificationListener.class) {
            try {
                android.content.SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
                JSONArray queue = new JSONArray(prefs.getString(QUEUE, "[]"));
                if (queue.length() >= 100) return;
                queue.put(body);
                prefs.edit().putString(QUEUE, queue.toString()).apply();
            } catch (Exception ignored) { }
        }
    }
}
