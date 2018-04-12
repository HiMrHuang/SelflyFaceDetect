package com.aee.huanghao.selflypreview.LocalFaceDetection;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OnlineLogs {
    private static final String TAG = "OL_LOG";

    public static void addError(String error) {
        Log.e(TAG, error);
        Map<String, Object> map = getInitialMap();
        map.put("error", error);
        FirebaseFirestore.getInstance()
                .collection(getDateString())
                .add(map);
    }

    public static void addRepLog(String log) {
        Log.d(TAG, log);
    }

    public static void addNonRepLog(String log) {
        Log.d(TAG, log);
        Map<String, Object> map = getInitialMap();
        map.put("log", log);
        FirebaseFirestore.getInstance()
                .collection(getDateString())
                .add(map);
    }

    private static Map<String, Object> getInitialMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("time", new SimpleDateFormat("hh:mm a", Locale.US).format(new Date()));
        map.put("timestamp", new Date().getTime());
        return map;
    }

    private static String getDateString() {
        return new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(new Date());
    }
}
