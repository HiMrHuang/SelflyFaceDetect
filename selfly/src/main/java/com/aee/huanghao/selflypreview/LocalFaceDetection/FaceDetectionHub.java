package com.aee.huanghao.selflypreview.LocalFaceDetection;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.aee.huanghao.selflypreview.activity.PreviewActivity;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.util.Date;

/**
 * Created by Code on 3/20/2018.
 */

public class FaceDetectionHub {
    private static final FaceDetectionHub instance = new FaceDetectionHub();
    private long lastTime;

    public static FaceDetectionHub getInstance() {
        return instance;
    }

    private PreviewActivity activity = null;
    private FaceRep faceRep = null;

    private FaceDetectionHub() {
        lastTime = new Date().getTime();
    }

    public void init(PreviewActivity activity, FaceRep faceRep) {
        this.activity = activity;
        this.faceRep = faceRep;
    }

    @SuppressLint("StaticFieldLeak")
    public void updateFrame(final Bitmap bitmap, final Rect drawFrameRect) {
        long nowTime = new Date().getTime();

        if (nowTime > (lastTime + 100)) {
            lastTime = nowTime;
        } else {
            return;
        }

        if (activity == null) {
            ErrorLogs.addError("Activity null in hub");
            return;
        }

        if (faceRep == null) {
            ErrorLogs.addError("Face Rep null in hub");
            return;
        }

        if (bitmap == null) {
            ErrorLogs.addError("Bitmap null");
            return;
        }

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                ErrorLogs.addRepLog("update frame execute");
                FaceDetector detector = new FaceDetector.Builder(activity.getApplicationContext())
                        .setTrackingEnabled(false)
                        .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                        .build();

                Detector<Face> safeDetector = new SafeFaceDetector(detector);

                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                final SparseArray<Face> faces = safeDetector.detect(frame);

                if (!safeDetector.isOperational()) {
                    ErrorLogs.addError("Face detector dependencies are not yet available. in hub");
                    Log.e("Aee", "run: ---->error");
                    // Check for low storage.  If there is low storage, the native library will not be
                    // downloaded, so detection will not become operational.
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
                            boolean hasLowStorage = activity.registerReceiver(null, lowstorageFilter) != null;

                            if (hasLowStorage) {
                                Toast.makeText(activity, "Low storage error", Toast.LENGTH_LONG).show();
                                ErrorLogs.addError("Low storage error");
                            }
                        }
                    });
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("Aee", "run: ---->update");
                        float widthScale = 1.0f, heightScale = 1.0f;
                        if (drawFrameRect != null) {
                            widthScale = drawFrameRect.width() / bitmap.getWidth();
                            heightScale = drawFrameRect.height() / bitmap.getHeight();
                        }
                        faceRep.updateFaces(faces, widthScale, heightScale);

                    }
                });
                return null;
            }
        };
    }

}
