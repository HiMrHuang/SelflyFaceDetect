package com.aee.huanghao.selflypreview;

import android.app.Application;

import com.aee.huanghao.selflypreview.stream.CameraControl;

/**
 * Created by huanghao
 * Created Time: 2018/3/14
 * Version code: 1.0
 * Description:
 */

public class SelflyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CameraControl.init(this);
        //TODO remove for release
//        Fabric.with(this, new Crashlytics());
    }
}
