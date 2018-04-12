package com.aee.huanghao.selflypreview.LocalFaceDetection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

public class FaceRep extends View {
    private SparseArray<Face> faces = null;
    private Paint greenPaint;
    private Paint redPaint;

    public FaceRep(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        greenPaint = new Paint();
        greenPaint.setColor(Color.GREEN);
        greenPaint.setStyle(Paint.Style.STROKE);
        greenPaint.setStrokeWidth(5);


        redPaint = new Paint();
        redPaint.setColor(Color.RED);
        redPaint.setStyle(Paint.Style.STROKE);
        redPaint.setStrokeWidth(5);
//        setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    public void updateFaces(SparseArray<Face> faces) {
        this.faces = faces;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (faces == null) {
            OnlineLogs.addError("Faces null in FaceRep");
            return;
        }
        for (int i = 0; i < faces.size(); ++i) {
            Face face = faces.valueAt(i);
            for (Landmark landmark : face.getLandmarks()) {
                int cx = (int) (landmark.getPosition().x);// * scale);
                int cy = (int) (landmark.getPosition().y);// * scale);
                if (face.getIsSmilingProbability() > 0.5) {
                    canvas.drawCircle(cx, cy, 10, redPaint);
                } else {
                    canvas.drawCircle(cx, cy, 10, greenPaint);
                }
            }
        }
    }
}
