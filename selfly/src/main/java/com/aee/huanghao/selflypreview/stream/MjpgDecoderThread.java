package com.aee.huanghao.selflypreview.stream;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.view.SurfaceHolder;

import com.icatchtek.reliant.customer.exception.IchTryAgainException;
import com.icatchtek.reliant.customer.type.ICatchAudioFormat;
import com.icatchtek.reliant.customer.type.ICatchFrameBuffer;
import com.icatchtek.reliant.customer.type.ICatchVideoFormat;

import java.nio.ByteBuffer;


public class MjpgDecoderThread {
    private static final String TAG = "MjpgDecoderThread";
    private StreamProvider streamProvider;
    private Bitmap videoFrameBitmap;
    private int frameWidth;
    private int frameHeight;
    private SurfaceHolder surfaceHolder;
    private AudioThread audioThread;
    private VideoThread videoThread;
    private int previewLaunchMode;
    private Rect drawFrameRect;
    private ICatchVideoFormat videoFormat;
    private int viewWidth;
    private int viewHeight;

    public MjpgDecoderThread(StreamProvider streamProvider, SurfaceHolder holder, int previewLaunchMode, int viewWidth, int viewHeight) {
        this.streamProvider = streamProvider;
        this.surfaceHolder = holder;
        this.previewLaunchMode = previewLaunchMode;
        this.videoFormat = streamProvider.getVideoFormat();
        if (videoFormat != null) {
            frameWidth = videoFormat.getVideoW();
            frameHeight = videoFormat.getVideoH();
        }
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        holder.setFormat(PixelFormat.RGBA_8888);
    }

    public void start(boolean enableAudio, boolean enableVideo) {
        if (enableAudio) {
            audioThread = new AudioThread();
            audioThread.start();
        }
        if (enableVideo) {
            videoThread = new VideoThread();
            videoThread.start();
        }
    }

    public boolean isAlive() {
        if (videoThread != null && videoThread.isAlive() == true) {
            return true;
        }
        if (audioThread != null && audioThread.isAlive() == true) {
            return true;
        }
        return false;
    }

    public void stop() {
        if (audioThread != null) {
            audioThread.requestExitAndWait();
        }
        if (videoThread != null) {
            videoThread.requestExitAndWait();
        }
    }

    private class VideoThread extends Thread {
        private boolean done;
        private ByteBuffer bmpBuf;
        private byte[] pixelBuf;

        VideoThread() {
            super();
            done = false;
            pixelBuf = new byte[frameWidth * frameHeight * 4];
            bmpBuf = ByteBuffer.wrap(pixelBuf);
            // Trigger onDraw with those initialize parameters
            videoFrameBitmap = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888);
            drawFrameRect = new Rect(0, 0, frameWidth, frameHeight);
        }

        @Override
        public void run() {
            ICatchFrameBuffer buffer = new ICatchFrameBuffer(frameWidth * frameHeight * 4);
            buffer.setBuffer(pixelBuf);
            boolean ret = false;
            boolean isSaveBitmapToDb = false;
            boolean isFirstFrame = true;
            boolean isStartGet = true;
            long lastTime = System.currentTimeMillis();
            long testTime;
            while (!done) {
                ret = false;
                try {
                    ret = streamProvider.getNextVideoFrame(buffer);
                } catch (IchTryAgainException e) {
                    e.printStackTrace();
                    continue;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }
                if (ret == false) {
//                    AppLog.e(TAG,"getNextVideoFrame failed\n");
                    continue;
                }
                if (buffer == null || buffer.getFrameSize() == 0) {
                    continue;
                }

                bmpBuf.rewind();
                if (videoFrameBitmap == null) {
                    continue;
                }

                if (isFirstFrame) {
                    isFirstFrame = false;
                }
                videoFrameBitmap.copyPixelsFromBuffer(bmpBuf);
//                Test.saveImage(videoFrameBitmap,System.currentTimeMillis());
                if (!isSaveBitmapToDb) {
                    if (videoFrameBitmap != null && previewLaunchMode == PreviewLaunchMode.RT_PREVIEW_MODE) {
                        isSaveBitmapToDb = true;
                    }
                }
                Canvas canvas = surfaceHolder.lockCanvas();
                if (canvas == null) {
                    continue;
                }
                drawFrameRect = ScaleTool.getScaledPosition(frameWidth, frameHeight, viewWidth, viewHeight);
                canvas.drawBitmap(videoFrameBitmap, null, drawFrameRect, null);
                surfaceHolder.unlockCanvasAndPost(canvas);
//                if (onDecodeTimeListener != null && buffer != null) {
//                    if (System.currentTimeMillis() - lastTime > 500) {
//                        lastTime = System.currentTimeMillis();
//                        long decodeTime = buffer.getDecodeTime();
//                        onDecodeTimeListener.decodeTime(decodeTime);
//                    }
//                }
//                if (previewLaunchMode == PreviewLaunchMode.VIDEO_PB_MODE && videoPbUpdateBarLitener != null) {
//                    videoPbUpdateBarLitener.onFramePtsChanged(buffer.getPresentationTime());
//                }
            }
        }

        public void requestExitAndWait() {
            done = true;
            try {
                join();
            } catch (InterruptedException ex) {
            }
        }
    }

    private class AudioThread extends Thread {
        private boolean done = false;
        private AudioTrack audioTrack;

        public void run() {
            ICatchAudioFormat audioFormat;
            audioFormat = streamProvider.getAudioFormat();
            if (audioFormat == null) {
                return;
            }
            int bufferSize = AudioTrack.getMinBufferSize(audioFormat.getFrequency(), audioFormat.getNChannels() == 2 ? AudioFormat.CHANNEL_IN_STEREO
                    : AudioFormat.CHANNEL_IN_LEFT, audioFormat.getSampleBits() == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT);

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, audioFormat.getFrequency(), audioFormat.getNChannels() == 2 ? AudioFormat.CHANNEL_IN_STEREO
                    : AudioFormat.CHANNEL_IN_LEFT, audioFormat.getSampleBits() == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT,
                    bufferSize, AudioTrack.MODE_STREAM);

            audioTrack.play();
            byte[] audioBuffer = new byte[1024 * 50];
            ICatchFrameBuffer icatchBuffer = new ICatchFrameBuffer(1024 * 50);
            icatchBuffer.setBuffer(audioBuffer);
            boolean ret = false;
            while (!done) {
                ret = false;
                try {
                    ret = streamProvider.getNextAudioFrame(icatchBuffer);
                } catch (IchTryAgainException e) {
                    e.printStackTrace();
                    continue;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }
                if (false == ret) {
                    continue;
                }
                audioTrack.write(icatchBuffer.getBuffer(), 0, icatchBuffer.getFrameSize());
            }
            audioTrack.stop();
            audioTrack.release();
        }

        public void requestExitAndWait() {
            done = true;
            try {
                join();
            } catch (InterruptedException ex) {
            }
        }
    }

    public void redrawBitmap(SurfaceHolder holder, int w, int h) {
        SurfaceHolder surfaceHolder = holder;
        viewWidth = w;
        viewHeight = h;
        if (videoFrameBitmap != null) {
            Canvas canvas = surfaceHolder.lockCanvas();
            if (canvas != null) {
                drawFrameRect = ScaleTool.getScaledPosition(frameWidth, frameHeight, w, h);
                canvas.drawBitmap(videoFrameBitmap, null, drawFrameRect, null);
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

}

