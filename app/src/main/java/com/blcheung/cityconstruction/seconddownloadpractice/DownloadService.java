package com.blcheung.cityconstruction.seconddownloadpractice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;

public class DownloadService extends Service {
    private final int REQUEST_CODE = 1;
    private final int ID_NOTIFICATION_DOWNLOAD = 0;
    private DownloadTask downloadTask;
    private String downloadUrl;
    private DownloadBinder mBinder = new DownloadBinder();

    public DownloadService() {

    }

    private DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManger().notify(ID_NOTIFICATION_DOWNLOAD,
                    getNotification("下载中...", progress));
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            // 下载成功时取消前台服务并显示下载成功提示
            stopForeground(true);
            getNotificationManger().notify(ID_NOTIFICATION_DOWNLOAD,
                    getNotification("下载成功", -1));
            Toast.makeText(DownloadService.this, "下载成功", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            // 下载失败时取消前台服务并显示下载失败提示
            stopForeground(true);
            getNotificationManger().notify(ID_NOTIFICATION_DOWNLOAD,
                    getNotification("下载失败", -1));
            Toast.makeText(DownloadService.this, "下载失败,请重试!",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPause() {
            downloadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this, "已暂停", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancle() {
            downloadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this, "已取消", Toast.LENGTH_SHORT).show();
        }
    };

    private NotificationManager getNotificationManger() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, int progress) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, REQUEST_CODE, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pi);
        // 显示进度
        if (progress > 0) {
            builder.setContentText(progress + "%")
                    .setProgress(100, progress, false);
        }
        return builder.build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * 具体下载逻辑事务
     */
    class DownloadBinder extends Binder{

        public void startDownload(String url) {
            if (downloadTask == null) {
                downloadUrl = url;
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(downloadUrl);
                // 把前台服务显示在通知栏上
                startForeground(ID_NOTIFICATION_DOWNLOAD,
                        getNotification("下载中...", 0));
                Toast.makeText(DownloadService.this, "下载中...",
                        Toast.LENGTH_SHORT).show();
            }
        }

        public void pauseDownload() {
            if (downloadTask != null) {
                downloadTask.pauseDownload();
            }
        }

        public void cancleDownload() {
            if (downloadTask != null) {
                downloadTask.cancleDownload();
            } else {
                // 如果线程不存在则删除文件并取消通知 & 关闭前台服务
                if (downloadUrl != null) {
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory + fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    getNotificationManger().cancel(ID_NOTIFICATION_DOWNLOAD);
                    stopForeground(true);
                    Toast.makeText(DownloadService.this, "源文件已删除",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
