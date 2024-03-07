package io.github.muntashirakon.setedit.boot;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.ServiceCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.muntashirakon.setedit.BuildConfig;
import io.github.muntashirakon.setedit.R;

public class BootService extends Service {
    public static final String TAG = BootService.class.getSimpleName();

    public static final String NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".notification.BOOT_SERVICE";

    private NotificationManagerCompat mNotificationManager;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = NotificationManagerCompat.from(this);
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Boot Service")
                .setProgress(0, 0, true)
                .setVibrate(null)
                .build();
        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mExecutor.submit(() -> {
            try {
                Log.i(TAG, "Started boot service.");
                Log.i(TAG, "Performing boot actions...");
                boolean isSuccess = BootUtils.runBootActions(this);
                if (isSuccess) {
                    Log.i(TAG, "Finished successfully!");
                } else {
                    Log.i(TAG, "Finished with errors!");
                    // TODO: 7/3/24 Display a notification
                }
            } finally {
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
            }
        });
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mExecutor.shutdownNow();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        NotificationChannelCompat notificationChannel = new NotificationChannelCompat.Builder(NOTIFICATION_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_MIN)
                .setVibrationEnabled(false)
                .setName("Boot Service")
                .build();
        mNotificationManager.createNotificationChannel(notificationChannel);
    }
}
