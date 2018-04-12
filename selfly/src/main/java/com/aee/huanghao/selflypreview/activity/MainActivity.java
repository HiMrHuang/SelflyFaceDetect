package com.aee.huanghao.selflypreview.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.aee.huanghao.selflypreview.LocalFaceDetection.OnlineLogs;
import com.aee.huanghao.selflypreview.R;
import com.aee.huanghao.selflypreview.stream.CameraControl;
import com.aee.huanghao.selflypreviewdemo.cameracontrol.AppMessage;
import com.aee.huanghao.selflypreviewdemo.flycontrol.FlyControlService;
import com.aee.huanghao.selflypreviewdemo.flycontrol.Utils.WifiManagerUtils;

import java.lang.ref.WeakReference;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private ConnectHandler connectHandler;
    private Intent flyService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TODO remove after deployment
        OnlineLogs.addNonRepLog("App started");
//        Crashlytics.log("Test log");
        findViewById(R.id.bt_connect_device).setOnClickListener(this);
        connectHandler = new ConnectHandler(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_connect_device:
                connectDevice();
                break;
        }
    }

    /**
     * Connect Selfly Device
     */
    private void connectDevice() {
        WifiManagerUtils wifiManagerUtils = new WifiManagerUtils(this);
        String ipAddress = wifiManagerUtils.getConnectIpAddress();
        CameraControl.getInstance().connectCamera(ipAddress, connectHandler);
    }

    static class ConnectHandler extends Handler {

        WeakReference<MainActivity> mMainActivity;

        private ConnectHandler(MainActivity activity) {
            mMainActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity mActivity = mMainActivity.get();
            switch (msg.what) {

                case AppMessage.MESSAGE_CAMERA_CONNECT_FAIL:
                    //Connect Device Failed
                    Toast.makeText(mActivity, mActivity.getResources()
                            .getString(R.string.connect_failed), Toast.LENGTH_SHORT).show();
                    break;

                case AppMessage.MESSAGE_CAMERA_CONNECT_SUCCESS:
                    //Connect Device Success
                    mActivity.bindFlyService();
                    mActivity.startToPreview();
                    break;

            }
        }

    }

    private void startToPreview() {
        startActivity(new Intent(this, PreviewActivity.class));
    }

    /**
     * Bind Fly Control Service,then you can control the Drone
     */
    private void bindFlyService() {
        flyService = new Intent(this, FlyControlService.class);
        startService(flyService);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (flyService != null) {
            stopService(flyService);
        }
        CameraControl.getInstance().disConnectCamera();
    }


}
