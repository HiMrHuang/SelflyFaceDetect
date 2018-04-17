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

public class FaceRep extends View {
    private SparseArray<Face> faces = null;
    private Paint greenPaint;
    private Paint redPaint;
    private float widthScale;
    private float heightScale;

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

    public void updateFaces(SparseArray<Face> faces, float widthScale, float heightScale) {
        this.faces = faces;
        this.widthScale = widthScale;
        this.heightScale = heightScale;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (faces == null) {
            ErrorLogs.addError("Faces null in FaceRep");
            return;
        }
        for (int i = 0; i < faces.size(); ++i) {
            Face face = faces.valueAt(i);

            float x = translateScaleX(face.getPosition().x);
            float y = translateScaleY(face.getPosition().y);

            float offsetX = translateScaleX(face.getWidth() / 2);
            float offsetY = translateScaleY(face.getHeight() / 2);

            float left = x - offsetX;
            float right = x + offsetX;
            float top = y - offsetY;
            float bottom = y + offsetY;

            if (face.getIsSmilingProbability() > 0.4) {
                canvas.drawRect(left, top, right, bottom, redPaint);
            } else {
                canvas.drawRect(left, top, right, bottom, greenPaint);
            }
        }
    }

    float translateScaleX(float x) {
        return widthScale * x;
    }

    float translateScaleY(float y) {
        return heightScale * y;
    }

//    for (Landmark landmark : face.getLandmarks()) {
//        int cx = (int) (landmark.getPosition().x);// * scale);
//        int cy = (int) (landmark.getPosition().y);// * scale);
//        if (face.getIsSmilingProbability() > 0.5) {
//            canvas.drawCircle(cx, cy, 10, redPaint);
//        } else {
//            canvas.drawCircle(cx, cy, 10, greenPaint);
//        }
//    }
}
