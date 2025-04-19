package com.example.signuploginfirebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.Random;

public class NotificationHelper {

    private static final String CHANNEL_ID = "booking_notifications";
    private static final String CHANNEL_NAME = "Booking Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for new bookings";

    // ✅ Create a notification channel (Required for Android 8.0+)
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // ✅ Show a notification
    public static void showNotification(Context context, String title, String message) {
        createNotificationChannel(context); // Ensure the notification channel exists

        // ✅ Check notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {

            // Show a toast message (Optional) if permission is missing
            Toast.makeText(context, "Enable notifications in settings", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Generate a random notification ID to allow multiple notifications
        int notificationId = new Random().nextInt(1000);

        // ✅ Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Use app icon if available
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // ✅ Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }

    // ✅ Show a Snackbar
    public static void showSnackbar(View view, String message) {
        if (view == null) {
            view = findRootView(view); // Use fallback root view
        }
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    // ✅ Get the root view if the passed view is null
    private static View findRootView(View view) {
        if (view == null) {
            return view.getRootView().findViewById(android.R.id.content);
        }
        return view;
    }

    // ✅ Check if notification permission is granted (for Android 13+)
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Permission is not required for versions below Android 13
    }
}
