package com.aee.huanghao.selflypreview.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.icatchtek.control.customer.ICatchCameraConfig;

/**
 * Created by huanghao
 * Created Time: 2018/3/13
 * Version code: 1.0
 * Description:
 */

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initConifg();
    }

    private void initConifg() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ICatchCameraConfig.getInstance().enablePTPReconnection(true);
        ICatchCameraConfig.getInstance().setPreviewCacheParam(1000, 1000);
    }


}
