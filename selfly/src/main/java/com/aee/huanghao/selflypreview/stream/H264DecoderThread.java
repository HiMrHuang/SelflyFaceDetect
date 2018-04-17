package com.aee.huanghao.selflypreview.stream;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.aee.huanghao.selflypreview.LocalFaceDetection.FaceDetectionHub;
import com.example.lib_curi_utility.CuriUtility;
import com.icatchtek.reliant.customer.exception.IchTryAgainException;
import com.icatchtek.reliant.customer.type.ICatchAudioFormat;
import com.icatchtek.reliant.customer.type.ICatchFrameBuffer;
import com.icatchtek.reliant.customer.type.ICatchVideoFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * this thread use get the stream data from the drone
 *
 */
public class H264DecoderThread {
    private static final String TAG = "H264DecoderThread";
    private  Rect drawFrameRect;
    private StreamProvider streamProvider;
    private Surface surface;
    private VideoThread videoThread;
    private AudioThread audioThread;
    private boolean audioPlayFlag = false;
    private int BUFFER_LENGTH = 1280 * 720 * 4;
    //    private int timeout = 60000;// us
    private int timeout = 20000;// us
    private MediaCodec decoder;
    private int previewLaunchMode;
    private ICatchVideoFormat videoFormat;
    private int frameWidth;
    private int frameHeight;
    private int viewWidth;
    private int viewHeight;
    private SurfaceHolder surfaceHolder;

    public H264DecoderThread(StreamProvider streamProvider, Surface surface, int previewLaunchMode, SurfaceHolder holder,int viewWidth, int viewHeight) {
        this.surface = surface;
        this.streamProvider = streamProvider;
        this.previewLaunchMode = previewLaunchMode;
        this.videoFormat = streamProvider.getVideoFormat();

        this.surfaceHolder = holder;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        holder.setFormat(PixelFormat.RGBA_8888);
        if (videoFormat != null) {
            frameWidth = videoFormat.getVideoW();
            frameHeight = videoFormat.getVideoH();
        }
    }

    public void start(boolean enableAudio, boolean enableVideo) {
        setFormat();
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
        audioPlayFlag = false;
    }

    long videoShowtime = 0;
    double curVideoPts = 0;


    /**
     * this thread is use to get the stream data
     *
     * the h264 data is stored in the frameBuffer
     *
     * you can by frameBuffer.getBuffer() to get the h264 data
     *
     */
    private class VideoThread extends Thread {

        private boolean done = false;
        private MediaCodec.BufferInfo info;
        long startTime = 0;
        int frameSize = 0;

        VideoThread() {
            super();
            done = false;
            drawFrameRect = new Rect(0, 0, frameWidth, frameHeight);
        }

        @Override
        public void run() {
            ByteBuffer[] inputBuffers = decoder.getInputBuffers();
            info = new MediaCodec.BufferInfo();
//            byte[] mPixel = new byte[BUFFER_LENGTH];
            byte[] mPixel = new byte[frameWidth * frameHeight * 4];
            ICatchFrameBuffer frameBuffer = new ICatchFrameBuffer(frameWidth * frameHeight * 4);
            frameBuffer.setBuffer(mPixel);
            int inIndex = -1;
            int sampleSize = 0;
            long pts = 0;
            boolean retvalue = true;
            boolean isFirst = true;
            long lastTime = System.currentTimeMillis();
            long endTime = System.currentTimeMillis();
            long currentTime;
            while (!done) {
                retvalue = false;
                curVideoPts = -1;
                try {
                    //TODO line added here
//                    FaceDetectionHub.getInstance().updateFrame(frameBuffer.getBuffer());
                    retvalue = streamProvider.getNextVideoFrame(frameBuffer);
                    if (!retvalue) {
                        continue;
                    }
                } catch (IchTryAgainException ex) {
                    ex.printStackTrace();
                    retvalue = false;
                    continue;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    retvalue = false;
                    break;
                }
                if (frameBuffer.getFrameSize() <= 0 || frameBuffer == null) {
                    retvalue = false;
                    continue;
                }
                if (!retvalue) {
                    continue;
                }
                inIndex = decoder.dequeueInputBuffer(timeout);
                curVideoPts = frameBuffer.getPresentationTime();
                frameSize++;
                if (isFirst) {
                    isFirst = false;
                    startTime = System.currentTimeMillis();
                }
                if (inIndex >= 0) {
                    sampleSize = frameBuffer.getFrameSize();
                    pts = (long) (frameBuffer.getPresentationTime() * 1000 * 1000); // (seconds
                    ByteBuffer buffer = inputBuffers[inIndex];
                    buffer.clear();
                    buffer.rewind();
                    buffer.put(frameBuffer.getBuffer(), 0, sampleSize);
                    decoder.queueInputBuffer(inIndex, 0, sampleSize, pts, 0);
                }
                int outBufId = decoder.dequeueOutputBuffer(info, timeout);
                if (outBufId >= 0) {

                    Image image = decoder.getOutputImage(outBufId);
                    if(null != image){
                            byte[] bytes = CuriUtility.getBytesFromImage(image,0);
                            Canvas canvas = surfaceHolder.lockCanvas();
                            int w = viewWidth;
                            int h = viewHeight;

                            //scale bitmap to full screen
                            drawFrameRect = ScaleTool.getScaledPosition(image.getWidth(),
                                    image.getHeight(), w, h);

                            //Get bitmap data
                            Bitmap videoBitmap = CuriUtility.createBitmapFromBgrBytes(
                                    bytes,
                                    image.getWidth(),
                                    image.getHeight());

                            //Detect
                            FaceDetectionHub.getInstance().updateFrame(videoBitmap, drawFrameRect);

                            if (null != canvas) {
                                canvas.drawBitmap(videoBitmap
                                        ,null, drawFrameRect, null);
                                surfaceHolder.unlockCanvasAndPost(canvas);

                            }

                            image.close();
                    }

                    decoder.releaseOutputBuffer(outBufId, true);
                    if (!audioPlayFlag) {
                        audioPlayFlag = true;
                        videoShowtime = System.currentTimeMillis();
                    }
                }
            }
            decoder.stop();
            decoder.release();
        }

        public boolean dequeueAndRenderOutputBuffer(int outtime) {
            int outIndex = decoder.dequeueOutputBuffer(info, outtime);

            switch (outIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    return false;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    return false;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    return false;
                case MediaCodec.BUFFER_FLAG_SYNC_FRAME:
                    return false;

                default:
                    decoder.releaseOutputBuffer(outIndex, true);
                    if (!audioPlayFlag) {
                        audioPlayFlag = true;
                        videoShowtime = System.currentTimeMillis();
                    }
                    return true;
            }
        }

        public void requestExitAndWait() {
            // 把这个线程标记为完成，并合并到主程序线程
            done = true;
//            if (this.isAlive()) {
//                try {
//                    join();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }

    private void setFormat() {
        ICatchVideoFormat videoFormat = this.videoFormat;
        int w = videoFormat.getVideoW();
        int h = videoFormat.getVideoH();
        String type = videoFormat.getMineType();
        MediaFormat format = MediaFormat.createVideoFormat(type, w, h);

        if (previewLaunchMode == PreviewLaunchMode.RT_PREVIEW_MODE) {
            format.setByteBuffer("csd-0", ByteBuffer.wrap(videoFormat.getCsd_0(), 0, videoFormat.getCsd_0_size()));
            format.setByteBuffer("csd-1", ByteBuffer.wrap(videoFormat.getCsd_1(), 0, videoFormat.getCsd_0_size()));
            format.setInteger("durationUs", videoFormat.getDurationUs());
            format.setInteger("max-input-size", videoFormat.getMaxInputSize());
        }

        String ret = videoFormat.getMineType();
        decoder = null;
        try {
            decoder = MediaCodec.createDecoderByType(ret);
        } catch (IOException e) {
            e.printStackTrace();
        }
        decoder.configure(format, null, null, 0);
        decoder.start();
    }

    private class AudioThread extends Thread {
        private boolean done = false;
        private AudioTrack audioTrack;

        @Override
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
}
