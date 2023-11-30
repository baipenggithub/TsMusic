package com.ts.music.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.S)
public class PermissionActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 0x001;
    private static final String[] PERMISSIONS = {
             Manifest.permission.RECORD_AUDIO
            , Manifest.permission.ACCESS_FINE_LOCATION
            , Manifest.permission.ACCESS_COARSE_LOCATION
            , Manifest.permission.BLUETOOTH_ADMIN
            , Manifest.permission.BLUETOOTH};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("PermissionActivity", "onCreate");
        ArrayList<String> noPermissions = getNeedRequestPermission(PERMISSIONS);
        // 已有权限
        if (noPermissions == null || noPermissions.size() == 0) {
            Log.d("PermissionActivity", "已有权限。。。");
            startActivity(new Intent(this, MusicActivity.class));
            finish();
        } else {
            Log.d("PermissionActivity", "申请权限。。。");
            initPermission();
        }
    }

    private void initPermission() {
        Log.d("PermissionActivity", "申请权限。。。");
        List<String> listNonPermissions = new ArrayList<>();
        for (String type : PERMISSIONS) {
            if (checkSelfPermission(type) != PackageManager.PERMISSION_GRANTED) {
                listNonPermissions.add(type);
            }
        }
        if (listNonPermissions.size() > 0) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE);
        }
    }

    public ArrayList<String> getNeedRequestPermission(String[] permission) {
        ArrayList<String> needRequestPermission = new ArrayList<>();
        for (String s : permission) {
            int perm = this.checkCallingOrSelfPermission(s);
            if (perm != PackageManager.PERMISSION_GRANTED) {
                needRequestPermission.add(s);
            }
        }
        return needRequestPermission;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.e("PermissionActivity", "拒绝的权限名称：" + permissions[i] + "  ;拒绝的权限结果：" + grantResults[i]);
                } else {
                    Log.e("PermissionActivity", "授权的权限名称：" + permissions[i] + "  ;授权的权限结果：" + grantResults[i]);
                }
            }
        }
    }
}