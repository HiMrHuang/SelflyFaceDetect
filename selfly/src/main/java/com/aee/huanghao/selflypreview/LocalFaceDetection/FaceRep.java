package com.aee.huanghao.selflypreview.LocalFaceDetection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import com.google.android.gms.vision.face.Face;

import java.util.Locale;

public class FaceRep extends View {
    private SparseArray<Face> faces = null;
    private Paint greenPaint;
    private Paint redPaint;
    private Paint blackPaint;

//    private float widthScale;
//    private float heightScale;
    private Rect drawFrameRect;
    private int viewWidth;
    private int viewHeight;

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

        blackPaint = new Paint();
        blackPaint.setColor(Color.BLACK);
        blackPaint.setStyle(Paint.Style.STROKE);
        blackPaint.setStrokeWidth(8);

//        setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private double scale ;


    public void updateFaces(SparseArray<Face> faces, Rect drawFrameRect, float widthScale, float heightScale, int w, int h) {
        this.faces = faces;
//        this.widthScale = widthScale;
//        this.heightScale = heightScale;
        this.drawFrameRect = drawFrameRect;
        this.viewWidth = w;
        this.viewHeight = h;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (faces == null) {
//            ErrorLogs.addError("Faces null in FaceRep");
            return;
        }
        int totalFaces = 0;
        float totalX = 0;
        float totalY = 0;
        for (int i = 0; i < faces.size(); ++i) {
            Face face = faces.valueAt(i);
            totalFaces++;

//            float centerX = translateX(face.getPosition().x + face.getWidth() / 2.0f);
//            float centerY = translateY(face.getPosition().y + face.getHeight() / 2.0f);
//            float offsetX = scaleX(face.getWidth() / 2.0f);
//            float offsetY = scaleY(face.getHeight() / 2.0f);
//
//            float left = centerX - offsetX;
//            float right = centerX + offsetX;
//            float top = centerY - offsetY;
//            float bottom = centerY + offsetY;
//
//            Paint p = null;
//            if (face.getIsSmilingProbability() > 0.4) {
//                p = redPaint;
//            } else {
//                p = greenPaint;
//            }
//            canvas.drawRect(left, top, right, bottom, p);
//            canvas.drawText(String.format("id: %d", face.getId()), centerX, centerY, p);

//            float x = translateScaleX(face.getPosition().x);
//            float y = translateScaleY(face.getPosition().y);
//            totalX += x;
//            totalY += y;
//
//            float offsetX = translateScaleY(face.getWidth() / 2);
//            float offsetY = translateScaleX(face.getHeight() / 2);
//
//            float left = x - offsetX;
//            float right = x + offsetX;
//            float top = y - offsetY;
//            float bottom = y + offsetY;

            float left = (float)translateScaleX(face.getPosition().x);
            float top = (float)translateScaleX(face.getPosition().y);
            float right = (float)translateScaleX(face.getPosition().x + face.getWidth());
            float bottom = (float)translateScaleX(face.getPosition().y + face.getHeight());


            Paint p = null;
            if (face.getIsSmilingProbability() > 0.4) {
                p = redPaint;
            } else {
                p = greenPaint;
            }
            canvas.drawText(String.format(Locale.US, "%.2f", face.getWidth() * face.getHeight()), left, top, blackPaint);
            canvas.drawRect(left, top, right, bottom, p);
        }

        float meanX = totalX / totalFaces;
        float meanY = totalY / totalFaces;

        canvas.drawCircle(meanX, meanY, 3.0f, greenPaint);
        canvas.drawCircle(drawFrameRect.centerX(), drawFrameRect.centerY(), 10.0f, blackPaint);
    }

    private float translateScaleY(float y) {
        float scaleX = viewWidth / 1280;
        return y * scaleX;

    }

    private float translateScaleX(float x) {
        float scaleY = viewHeight / 720;
        return x*scaleY;
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
