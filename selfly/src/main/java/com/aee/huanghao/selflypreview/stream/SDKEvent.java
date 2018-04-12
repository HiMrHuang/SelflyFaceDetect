package com.aee.huanghao.selflypreview.stream;

import android.os.Handler;

import com.aee.huanghao.selflypreviewdemo.cameracontrol.CameraAction;

/**
 * Created by huanghao
 * Created Time: 2015/11/23 18:00
 * Version code: 1.0
 * Description:
 */
public class SDKEvent {
    private static final String TAG = "SDKEvent";
    public static final int EVENT_BATTERY_ELETRIC_CHANGED = 0;
    public static final int EVENT_CAPTURE_COMPLETED = 1;
    public static final int EVENT_CAPTURE_START = 3;
    public static final int EVENT_SD_CARD_FULL = 4;
    public static final int EVENT_VIDEO_OFF = 5;
    public static final int EVENT_VIDEO_ON = 6;
    public static final int EVENT_FILE_ADDED = 7;
    public static final int EVENT_CONNECTION_FAILURE = 8;
    public static final int EVENT_TIME_LAPSE_STOP = 9;
    public static final int EVENT_SERVER_STREAM_ERROR = 10;
    public static final int EVENT_FILE_DOWNLOAD = 11;
    public static final int EVENT_VIDEO_RECORDING_TIME = 12;
    public static final int EVENT_FW_UPDATE_COMPLETED = 13;
    public static final int EVENT_FW_UPDATE_POWEROFF = 14;
    public static final int EVENT_SEARCHED_NEW_CAMERA = 15;
    public static final int EVENT_SDCARD_REMOVED = 16;
    public static final int EVENT_SDCARD_INSERT = 17;
    public static final int EVENT_VIDEO_PLAY_PTS = 23;
    public static final int EVENT_VIDEO_PLAY_CLOSED = 24;
    public static final int EVENT_FRAME_INTERVAL_INFO = 25;

    private CameraAction cameraAction = CameraAction.getInstance();
    private Handler handler;


    public SDKEvent(Handler handler) {
        this.handler = handler;
    }





}
