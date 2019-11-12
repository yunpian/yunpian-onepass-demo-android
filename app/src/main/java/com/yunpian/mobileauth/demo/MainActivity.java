package com.yunpian.mobileauth.demo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.qipeng.yp.onepass.QPOnePass;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor();
        setContentView(R.layout.activity_main);
        requestPermissions();
        initSDK();

        TextView versionTv = findViewById(R.id.version_tv);
        versionTv.setText(versionTv.getText() + " " + Utils.getVersionName(this, getPackageName()));
    }

    private void requestPermissions() {
        int readPhonePermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int accessFineLocationCheck = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (readPhonePermissionCheck != PackageManager.PERMISSION_GRANTED
                || accessFineLocationCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "当前功能需要 电话、位置权限 才能正常工作", Toast.LENGTH_SHORT).show();
                requestPermissions();
                return;
            }
        }
        initSDK();
    }

    private void initSDK() {
        QPOnePass.getInstance().setLogEnable(true);
        QPOnePass.getInstance().init(this, "474b4c6159e54ace9bb28ab08e8406f2", new com.qipeng.yp.onepass.callback.QPResultCallback() {
            @Override
            public void onSuccess(String s) {

            }

            @Override
            public void onFail(String message) {
                Toast.makeText(MainActivity.this, "发生错误，失败原因：" + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onePass(View view) {
        if (!isNetworkConnected(this)) {
            Toast.makeText(MainActivity.this, "请确保网络连接正常", Toast.LENGTH_SHORT).show();
            return;
        }

        int readPhonePermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int accessFineLocationCheck = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (readPhonePermissionCheck != PackageManager.PERMISSION_GRANTED
                || accessFineLocationCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        startActivity(new Intent(this, ActivityOnePassLogin.class));
    }

    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setStatusBarColor() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.bg_activity));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setNavigationBarColor(getResources().getColor(R.color.bg_activity));
        }
    }


}
