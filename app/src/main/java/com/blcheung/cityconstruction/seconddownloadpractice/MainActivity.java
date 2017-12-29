package com.blcheung.cityconstruction.seconddownloadpractice;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnStart, btnPause, btnCancle;
    private final int REQUEST_DOWNLOAD = 1;
    private DownloadService.DownloadBinder downloadBinder;
    private String downloadUrl = "http://mirrors.ustc.edu.cn/eclipse/oomph/epp/oxygen/R2/" +
            "eclipse-inst-win64.exe";
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        // 运行时权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_DOWNLOAD);
        }

        // 开启并绑定服务
        Intent downloadIntent = new Intent(this, DownloadService.class);
        startService(downloadIntent);
        bindService(downloadIntent, conn, BIND_AUTO_CREATE);

        btnStart.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnCancle.setOnClickListener(this);
    }

    private void initViews() {
        btnStart = findViewById(R.id.btn_start);
        btnPause = findViewById(R.id.btn_pause);
        btnCancle = findViewById(R.id.btn_cancle);
    }

    @Override
    public void onClick(View v) {
        if (downloadBinder == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.btn_start:
                downloadBinder.startDownload(downloadUrl);
                break;
            case R.id.btn_pause:
                downloadBinder.pauseDownload();
                break;
            case R.id.btn_cancle:
                downloadBinder.cancleDownload();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_DOWNLOAD:
                if (grantResults.length > 0 && grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "拒绝权限将无法使用本程序",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解绑服务
        unbindService(conn);
    }
}
