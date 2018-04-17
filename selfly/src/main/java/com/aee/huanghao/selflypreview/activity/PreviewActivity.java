package com.aee.huanghao.selflypreview.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.aee.huanghao.selflypreview.LocalFaceDetection.FaceDetectionHub;
import com.aee.huanghao.selflypreview.LocalFaceDetection.FaceRep;
import com.aee.huanghao.selflypreview.LocalFaceDetection.ErrorLogs;
import com.aee.huanghao.selflypreview.R;
import com.aee.huanghao.selflypreview.stream.CameraControl;
import com.aee.huanghao.selflypreview.widget.RockerView;
import com.aee.huanghao.selflypreviewdemo.flycontrol.FlyControlData;


/**
 * Created by huanghao
 * Created Time: 2018/3/13
 * Version code: 1.0
 * Description:Camera Preview,You can see the preview data form the Drone Camera
 */

public class PreviewActivity extends BaseActivity implements RockerView.NewSingleRudderListener {

    private static final String TAG = "PreviewActivity";
    private SurfaceView mSurfaceView;
    private Button takeoffOrLand;
    private boolean isTakeOff = false;
    private FlyControlData flyControlData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preview);
        FaceRep faceRep = findViewById(R.id.m_face_rep);
        if(faceRep!=null){
            FaceDetectionHub.getInstance().init(this, faceRep);
        }else{
            ErrorLogs.addError("Face Rep Null in preview");
        }
        initView();
        initData();
    }

    /**
     * get camera stream from the drone and showed in the surface
     * <p>
     * you can get the stream data by startStreamAndPreview() this method
     */
    private void initData() {
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                CameraControl.getInstance().initRender(mSurfaceView.getHolder(), mSurfaceView);
                CameraControl.getInstance().startStreamAndPreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mSurfaceView.getHolder() == holder) {
                    CameraControl.getInstance().setDrawingArea(width, height);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                CameraControl.getInstance().stopMediaStreamAndPreview();
            }
        });
        flyControlData = FlyControlData.getInstance();
    }

    private void initView() {
        mSurfaceView = findViewById(R.id.m_preview);

        takeoffOrLand = findViewById(R.id.bt_takeoff_or_land);
        takeoffOrLand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isTakeOff) {
                    flyControlData.setTakeOff();
                    takeoffOrLand.setText(R.string.land);
                } else {
                    flyControlData.setLand();
                    takeoffOrLand.setText(R.string.take_off);
                }
                isTakeOff = !isTakeOff;
            }
        });

        RockerView leftRocker = findViewById(R.id.left_rocker);
        RockerView rightRocker = findViewById(R.id.right_rocker);
        leftRocker.setRockeType(0);
        rightRocker.setRockeType(1);
        rightRocker.setReverse(true);
        rightRocker.setFourIcon();
        leftRocker.setSingleRudderListener(this);
        rightRocker.setSingleRudderListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CameraControl.getInstance().startCameraPreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraControl.getInstance().stopMediaStreamAndPreview();
    }

    /**
     * that control drone accelerator,default is value is 128
     * the Interval value is 0-128(drop) 128 128-255(up)
     */
    private int throttle = 128;

    /**
     * that control drone forward or back,default is value is 128
     * the Interval value is 0-128(back) 128 128-255(forward)
     */
    private int pitch = 128;

    /**
     * that control drone left or right,default is value is 128
     * the Interval value is 0-128(left) 128 128-255(right)
     */
    private int roll = 128;

    /**
     * that control drone turn left or turn right,default is value is 128
     * the Interval value is 0-128(turn left) 128 128-255(turn right)
     */
    private int yaw = 128;

    /**
     * by operate rocker you can control drone
     *
     * @param rockerType left or right
     * @param action     no use now
     * @param x          x axis
     * @param y          y axis
     */
    @Override
    public void onNewSteeringWheelChanged(int rockerType, int action, int x, int y) {
        if (x > 255) {
            x = 255;
        } else if (x < 0) {
            x = 0;
        }

        if (y > 255) {
            y = 255;
        } else if (y < 0) {
            y = 0;
        }

        switch (rockerType) {
            case 0:
                //左边摇杆
                yaw = x;
                throttle = y;
                break;

            case 1:
                //右边摇杆
                roll = x;
                pitch = y;
                break;
        }

//        Log.e(TAG, "onNewSteeringWheelChanged:---> "+"roll==="+roll
//        +"---"+"pitch==="+pitch+"---"+"throttle==="+throttle+"---"+"yaw==="+yaw);
        flyControlData.setFlightData(roll, pitch,
                throttle, yaw);

    }


}
