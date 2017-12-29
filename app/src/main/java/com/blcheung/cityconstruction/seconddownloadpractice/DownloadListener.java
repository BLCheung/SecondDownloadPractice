package com.blcheung.cityconstruction.seconddownloadpractice;

/**
 * Created by BLCheung.
 * Date:2017年12月24日,0024 18:21
 */

public interface DownloadListener {
    void onProgress(int progress);

    void onSuccess();

    void onFailed();

    void onPause();

    void onCancle();
}
