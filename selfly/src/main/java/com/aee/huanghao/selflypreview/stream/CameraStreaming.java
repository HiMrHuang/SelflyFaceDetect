package com.aee.huanghao.selflypreview.stream;

import android.media.MediaCodec;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.aee.huanghao.selflypreviewdemo.cameracontrol.PanoramaPreviewPlayback;
import com.aee.huanghao.selflypreviewdemo.cameracontrol.Tristate;
import com.icatchtek.pancam.customer.stream.ICatchIStreamProvider;
import com.icatchtek.reliant.customer.type.ICatchCodec;
import com.icatchtek.reliant.customer.type.ICatchStreamParam;
import com.icatchtek.reliant.customer.type.ICatchVideoFormat;


public class CameraStreaming {
    private final String TAG = CameraStreaming.class.getSimpleName();
    private PanoramaPreviewPlayback previewPlayback;
    private ICatchIStreamProvider iCatchIStreamProvider;
    private StreamProvider streamProvider;
    private Surface surface;
    private SurfaceHolder holder;
    private MediaCodec decoder;
    private boolean isStreaming = false;
    private boolean freezeDecoder = false;
    private boolean disableAudio;
    private H264DecoderThread h264DecoderThread;
    private MjpgDecoderThread mjpgDecoderThread;
    private ICatchVideoFormat videoFormat;
    private int frmW = 0;
    private int frmH = 0;
    private int mWidth = 0;
    private int mHeigth = 0;
    private int previewCodec;

    public CameraStreaming(PanoramaPreviewPlayback previewPlayback) {
        this.previewPlayback = previewPlayback;
    }

    public void setSurface(SurfaceHolder holder) {
        this.surface = holder.getSurface();
        this.holder = holder;
    }

    public void setViewParam(int w, int h) {
        mWidth = w;
        mHeigth = h;
    }

    public void disnableRender() {
        this.iCatchIStreamProvider = previewPlayback.disnableRender();
        this.streamProvider = new StreamProvider(iCatchIStreamProvider);
    }

    /**
     *
     *   finally you can find startDecoderThread
     *
     */
    public Tristate start(ICatchStreamParam param, boolean enableAudio) {
        if (surface == null) {
            return Tristate.FALSE;
        }
        if (isStreaming) {
            return Tristate.NORMAL;
        }
        Tristate ret = previewPlayback.start(param, enableAudio);
        if (ret != Tristate.NORMAL) {
            return ret;
        }
        try {
            videoFormat = streamProvider.getVideoFormat();
            if (videoFormat != null) {
                frmW = videoFormat.getVideoW();
                frmH = videoFormat.getVideoH();
            }
            startDecoderThread(PreviewLaunchMode.RT_PREVIEW_MODE, videoFormat);
        } catch (Exception e) {
            return Tristate.FALSE;
        }
        return Tristate.NORMAL;
    }

    /**
     *
     *  The default stream from the drone is h264 data
     *
     *  So you can see H264DecoderThread, this thread is use to get the stream data
     *  from the drone and display in the surface
     *
     */
    private void startDecoderThread(int previewLaunchMode, ICatchVideoFormat videoFormat) {
        if (videoFormat == null) {
            return;
        }
        boolean enableAudio = streamProvider.containsAudioStream();
        previewCodec = videoFormat.getCodec();
        switch (previewCodec) {
            case ICatchCodec.ICH_CODEC_RGBA_8888:
                mjpgDecoderThread = new MjpgDecoderThread(streamProvider, holder, previewLaunchMode, mWidth, mHeigth);
                mjpgDecoderThread.start(enableAudio, true);
                setSurfaceViewArea();
                break;
            case ICatchCodec.ICH_CODEC_H264:
                h264DecoderThread = new H264DecoderThread(streamProvider, surface, previewLaunchMode,holder,mWidth, mHeigth);
                h264DecoderThread.start(enableAudio, true);
                setSurfaceViewArea();
                break;
            default:
                return;
        }
    }

    public boolean stop() {
        if (mjpgDecoderThread != null) {
            mjpgDecoderThread.stop();
        }
        if (h264DecoderThread != null) {
            h264DecoderThread.stop();
        }

        boolean ret = previewPlayback.stop();
        isStreaming = false;
        return ret;
    }

    public boolean isStreaming() {
        return isStreaming;
    }

    public void setSurfaceViewArea() {
        if (mWidth == 0 || mHeigth == 0) {
            return;
        }
        if (frmH <= 0 || frmW <= 0) {
            holder.setFixedSize(mWidth, mWidth * 9 / 16);
            return;
        }
        if (previewCodec == ICatchCodec.ICH_CODEC_RGBA_8888) {
            if (mjpgDecoderThread != null) {
                mjpgDecoderThread.redrawBitmap(holder, mWidth, mHeigth);
            }
        } else if (previewCodec == ICatchCodec.ICH_CODEC_H264) {
            if (mWidth * frmH / frmW <= mHeigth) {
                holder.setFixedSize(mWidth, mWidth * frmH / frmW);
            } else {
                holder.setFixedSize(mHeigth * frmW / frmH, mHeigth);
            }
        }
    }
}
