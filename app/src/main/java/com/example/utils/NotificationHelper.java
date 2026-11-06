package com.example.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.MainActivity;
import com.example.R;

public class NotificationHelper {
    public static final String CHANNEL_LIMITS = "channel_app_limits";
    public static final String CHANNEL_RULES = "channel_automation_rules";
    public static final String CHANNEL_BATTERY = "channel_battery_alerts";
    public static final String CHANNEL_SPEED = "channel_live_speed";

    public static void initChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;

            NotificationChannel limits = new NotificationChannel(
                CHANNEL_LIMITS,
                "App Usage Warnings",
                NotificationManager.IMPORTANCE_HIGH
            );
            limits.setDescription("Alerts for smart daily screen time limits");
            manager.createNotificationChannel(limits);

            NotificationChannel rules = new NotificationChannel(
                CHANNEL_RULES,
                "Automation Rules",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            rules.setDescription("Triggers for custom user rules");
            manager.createNotificationChannel(rules);

            NotificationChannel battery = new NotificationChannel(
                CHANNEL_BATTERY,
                "Battery Health Alerts",
                NotificationManager.IMPORTANCE_HIGH
            );
            battery.setDescription("Notifications for battery charging and temperature");
            manager.createNotificationChannel(battery);

            NotificationChannel speed = new NotificationChannel(
                CHANNEL_SPEED,
                "Live Speed Monitor",
                NotificationManager.IMPORTANCE_LOW
            );
            speed.setDescription("Persistent status bar real-time speed monitor");
            manager.createNotificationChannel(speed);
        }
    }

    public static void postAppLimitWarning(Context context, String appName, int percentage, int limitMinutes) {
        if (!PermissionUtils.hasPostNotificationsPermission(context)) return;

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            appName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = "Smart Usage Warning: " + appName;
        String content = percentage >= 100
            ? appName + " has reached its daily limit of " + limitMinutes + " minutes!"
            : appName + " has reached " + percentage + "% of its " + limitMinutes + "m daily limit.";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_LIMITS)
            .setSmallIcon(R.drawable.ic_nav_usage)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);

        try {
            NotificationManagerCompat.from(context).notify(appName.hashCode() + percentage, builder.build());
        } catch (SecurityException ignored) {}
    }

    public static void postAutomationRuleAlert(Context context, String ruleName, String message) {
        if (!PermissionUtils.hasPostNotificationsPermission(context)) return;

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            ruleName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_RULES)
            .setSmallIcon(R.drawable.ic_automation)
            .setContentTitle("Rule Alert: " + ruleName)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);

        try {
            NotificationManagerCompat.from(context).notify(ruleName.hashCode(), builder.build());
        } catch (SecurityException ignored) {}
    }

    public static void postBatteryTempWarning(Context context, float tempCelsius) {
        if (!PermissionUtils.hasPostNotificationsPermission(context)) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_BATTERY)
            .setSmallIcon(R.drawable.ic_battery)
            .setContentTitle("High Battery Temperature Alert")
            .setContentText(String.format("Battery temperature reached %.1f°C! Consider letting your device cool down.", tempCelsius))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(9901, builder.build());
        } catch (SecurityException ignored) {}
    }
}
