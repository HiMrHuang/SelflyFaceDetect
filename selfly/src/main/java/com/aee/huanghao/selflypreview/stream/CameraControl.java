package com.aee.huanghao.selflypreview.stream;

import android.content.Context;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.aee.huanghao.selflypreviewdemo.cameracontrol.AppMessage;
import com.aee.huanghao.selflypreviewdemo.cameracontrol.CameraAction;
import com.aee.huanghao.selflypreviewdemo.cameracontrol.CameraProperties;
import com.aee.huanghao.selflypreviewdemo.cameracontrol.CameraState;
import com.aee.huanghao.selflypreviewdemo.cameracontrol.DroneCamera;
import com.aee.huanghao.selflypreviewdemo.cameracontrol.FileOperation;
import com.aee.huanghao.selflypreviewdemo.cameracontrol.GlobalInfo;
import com.aee.huanghao.selflypreviewdemo.cameracontrol.PanoramaPreviewPlayback;
import com.aee.huanghao.selflypreviewdemo.cameracontrol.PanoramaSession;
import com.aee.huanghao.selflypreviewdemo.cameracontrol.PreviewMode;
import com.aee.huanghao.selflypreviewdemo.cameracontrol.PropertyId;
import com.aee.huanghao.selflypreviewdemo.cameracontrol.Tristate;
import com.icatchtek.control.customer.type.ICatchCamPreviewMode;
import com.icatchtek.pancam.customer.ICatchPancamConfig;
import com.icatchtek.pancam.customer.exception.IchGLSurfaceNotSetException;
import com.icatchtek.pancam.customer.gl.surface.ICatchSurfaceContext;
import com.icatchtek.reliant.customer.type.ICatchH264StreamParam;
import com.icatchtek.reliant.customer.type.ICatchJPEGStreamParam;
import com.icatchtek.reliant.customer.type.ICatchStreamParam;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by huanghao
 * Created Time: 2018/3/13
 * Version code: 1.0
 * Description: CameraOperations
 */

public class CameraControl {

    private static CameraControl cameraControl;
    private GlobalInfo mGlobalInfo;
    private Context mContext;
    private ExecutorService mSingleThreadPool;
    private DroneCamera mCurrentCamera;
    private PanoramaPreviewPlayback panoramaPreviewPlayback;
    private CameraProperties cameraProperties;
    private CameraAction cameraAction;
    private CameraState cameraState;
    private FileOperation fileOperation;
    private CameraStreaming cameraStreaming;

    public int curMode = PreviewMode.APP_STATE_NONE_MODE;
    private ICatchSurfaceContext iCatchSurfaceContext;
    private boolean hasInitSurface = false;
    private int curCacheTime = 0;

    private CameraControl() {

    }

    private CameraControl(Context context) {
        this.mContext = context;
        mGlobalInfo = GlobalInfo.getInstance();
    }

    public static void init(Context context) {

        if (cameraControl == null) {
            cameraControl = new CameraControl(context);
        }

    }

    public static CameraControl getInstance() {
        if (cameraControl == null) {
            throw new RuntimeException("Must be init CameraFunctionUtils in application first!");
        } else {
            return cameraControl;
        }
    }

    public void connectCamera(final String ipString, final Handler handler) {
        if (mSingleThreadPool == null) {
            mSingleThreadPool = Executors.newSingleThreadExecutor();
        }
        mSingleThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                connectCameraInOtherThread(ipString, handler);
            }
        });
    }

    private void connectCameraInOtherThread(String ipString, Handler handler) {
        mCurrentCamera = new DroneCamera();
        if (!mCurrentCamera.getSDKsession().prepareSession(ipString)) {
            handler.obtainMessage(AppMessage.MESSAGE_CAMERA_CONNECT_FAIL).sendToTarget();
            return;
        }

        if (mCurrentCamera.getSDKsession().checkWifiConnection() == true) {
            mGlobalInfo.setCurrentCamera(mCurrentCamera);
            mCurrentCamera.initCamera();
            if (CameraProperties.getInstance().hasFuction(PropertyId.CAMERA_DATE)) {
                CameraProperties.getInstance().setCameraDate();
            }
            mCurrentCamera.setMyMode(1);
            handler.obtainMessage(AppMessage.MESSAGE_CAMERA_CONNECT_SUCCESS).sendToTarget();
            return;
        }

    }

    public void disConnectCamera() {
        destroyCamera();
    }

    private boolean destroyCamera() {
        if (mCurrentCamera != null) {
            return mCurrentCamera.destroyCamera();
        }
        return false;
    }


    public void startCameraPreview() {
        initData();
        initPreview();
    }

    private int initPreview() {
        int curIcatchMode;
        if (curMode == PreviewMode.APP_STATE_NONE_MODE) {
            curMode = PreviewMode.APP_STATE_VIDEO_PREVIEW;
            curIcatchMode = ICatchCamPreviewMode.ICH_CAM_VIDEO_PREVIEW_MODE;

        } else if (curMode == PreviewMode.APP_STATE_VIDEO_PREVIEW) {
            curIcatchMode = ICatchCamPreviewMode.ICH_CAM_VIDEO_PREVIEW_MODE;
        } else if (curMode == PreviewMode.APP_STATE_STILL_PREVIEW) {
//            changeCameraMode(curMode, ICatchPreviewMode.ICH_STILL_PREVIEW_MODE, handler);
            curIcatchMode = ICatchCamPreviewMode.ICH_CAM_STILL_PREVIEW_MODE;
        } else {
            curMode = PreviewMode.APP_STATE_VIDEO_PREVIEW;
            curIcatchMode = ICatchCamPreviewMode.ICH_CAM_VIDEO_PREVIEW_MODE;
        }
        cameraAction.changePreviewMode(curIcatchMode);
        return curMode;
    }

    private void initData() {
        panoramaPreviewPlayback = PanoramaSession.getInstance().getPanoramaPreviewPlayback();
        cameraProperties = CameraProperties.getInstance();
        cameraAction = CameraAction.getInstance();
        cameraState = CameraState.getInstance();
        fileOperation = FileOperation.getInstance();
        mCurrentCamera = GlobalInfo.getInstance().getCurrentCamera();
        cameraStreaming = new CameraStreaming(panoramaPreviewPlayback);

        if (cameraProperties.hasFuction(0xD7F0)) {
            cameraProperties.setCaptureDelayMode(1);
        }
    }

    public void initRender(SurfaceHolder surfaceHolder, SurfaceView mSurfaceView) {
        if (panoramaPreviewPlayback == null) {
            return;
        }

        View parentView = (View) mSurfaceView.getParent();
        int width = parentView.getWidth();
        int heigth = parentView.getHeight();
        if (width <= 0 || heigth <= 0) {
            width = 1080;
            heigth = 1920;
        }
        cameraStreaming.disnableRender();
        cameraStreaming.setSurface(surfaceHolder);
        cameraStreaming.setViewParam(width, heigth);
        hasInitSurface = true;
    }

    /**
     * get the Stream data from the drone
     *
     * you can find the Specific implementation method by viewing
     *
     * cameraStreaming.start(iCatchStreamParam, !GlobalInfo.disableAudio);
     *
     *
     */
    public Tristate startStreamAndPreview() {
        if (hasInitSurface == false) {
            return Tristate.NORMAL;
        }
        Tristate ret = Tristate.FALSE;

        int cacheTime = cameraProperties.getPreviewCacheTime();
        ICatchPancamConfig.getInstance().setPreviewCacheParam(cacheTime, 200);
        ICatchPancamConfig.getInstance().setSoftwareDecoder(GlobalInfo.enableSoftwareDecoder);

        curCacheTime = cacheTime;
        String streamUrl = cameraProperties.getCurrentStreamInfo();
        ICatchStreamParam iCatchStreamParam = null;
        if (streamUrl == null) {
            iCatchStreamParam = new ICatchH264StreamParam(1280, 720, 30);
        } else {
            StreamInfo streamInfo = StreamInfoConvert.convertToStreamInfoBean(streamUrl);
            GlobalInfo.curFps = streamInfo.fps;
            if (streamInfo.mediaCodecType.equals("MJPG")) {
                iCatchStreamParam = new ICatchJPEGStreamParam(streamInfo.width, streamInfo.height, streamInfo.fps, streamInfo.bitrate);
            } else if (streamInfo.mediaCodecType.equals("H264")) {
                iCatchStreamParam = new ICatchH264StreamParam(streamInfo.width, streamInfo.height, streamInfo.fps, streamInfo.bitrate);
            } else {
                iCatchStreamParam = new ICatchH264StreamParam(1280, 720, 30);
            }
        }

        ret = cameraStreaming.start(iCatchStreamParam, !GlobalInfo.disableAudio);

        if (ret == Tristate.NORMAL) {
            mCurrentCamera.isStreamReady = true;
        } else {
            mCurrentCamera.isStreamReady = false;
        }
        return ret;
    }

    public void setDrawingArea(int width, int height) {
        if (panoramaPreviewPlayback != null && iCatchSurfaceContext != null) {
            boolean ret = false;
            try {
                ret = iCatchSurfaceContext.setViewPort(0, 0, width, height);
            } catch (IchGLSurfaceNotSetException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean stopMediaStreamAndPreview() {
        if (GlobalInfo.enableDumpVideo) {
            ICatchPancamConfig.getInstance().disableDumpTransportStream(true);
        }

        if (mCurrentCamera.isStreamReady) {
            mCurrentCamera.isStreamReady = false;
            return cameraStreaming.stop();
        }
        return true;
    }

}
