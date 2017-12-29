package com.blcheung.cityconstruction.seconddownloadpractice;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by BLCheung.
 * Date:2017年12月24日,0024 23:59
 */

public class DownloadTask extends AsyncTask<String, Integer, Integer> {
    public static final int TYPE_SUCCESS = 1;
    public static final int TYPE_PAUSE = 2;
    public static final int TYPE_CANCLE = 3;
    public static final int TYPE_FAILE = 0;
    private boolean isPaused = false;
    private boolean isCancled = false;
    private int lastProgress;
    private DownloadListener listener;

    public DownloadTask(DownloadListener listener) {
        this.listener = listener;
    }

    @Override
    protected Integer doInBackground(String... params) {
        InputStream is = null;
        RandomAccessFile saveFile = null;
        File file = null;
        try {
            String downloadUrl = params[0];
            long downloadLength = 0;
            // 文件名
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            // 获取SD卡目录
            String directory = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).getPath();
            file = new File(directory + fileName);
            if (file.exists()) {
                // 读取已下载的字节
                downloadLength = file.length();
            }
            // 文件总时长
            long contentLength = getContentLength(downloadUrl);
            // 已下载的文件长度等于文件总长度,下载成功
            if (contentLength == downloadLength) {
                return TYPE_SUCCESS;
            // 下载失败
            } else if (contentLength == 0) {
                return TYPE_FAILE;
            }
            // 断点续传
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("RANGE", "bytes=" + downloadLength + "-")
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            // 请求成功
            if (response != null) {
                is = response.body().byteStream();
                saveFile = new RandomAccessFile(file, "rw");
                // 跳到需要下载的字节
                saveFile.seek(downloadLength);
                byte[] bytes = new byte[1024];
                int total = 0;
                int len;
                // 传输
                while ((len = is.read(bytes)) != -1) {
                    if (isPaused) {
                        return TYPE_PAUSE;
                    } else if (isCancled) {
                        return TYPE_CANCLE;
                    } else {
                        total += len;
                        saveFile.write(bytes, 0, len);
                        // 计算已下载进度百分比
                        int progress = (int) ((total + downloadLength) * 100 / contentLength);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (saveFile != null) {
                    saveFile.close();
                }
                if (file != null && isCancled) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return TYPE_FAILE;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress) {
            listener.onProgress(progress);
            lastProgress = progress;
        }
    }

    @Override
    protected void onPostExecute(Integer status) {
        switch (status) {
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;
            case TYPE_PAUSE:
                listener.onPause();
                break;
            case TYPE_CANCLE:
                listener.onCancle();
                break;
            case TYPE_FAILE:
                listener.onFailed();
                break;
        }
    }

    /**
     * 获取下载文件的总长度
     * @param downloadUrl
     * @return
     * @throws IOException
     */
    private long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            response.close();
            return contentLength;
        }
        return 0;
    }

    /**
     * 暂停下载
     */
    public void pauseDownload() {
        isPaused = true;
    }

    /**
     * 取消下载
     */
    public void cancleDownload() {
        isCancled = true;
    }
}
