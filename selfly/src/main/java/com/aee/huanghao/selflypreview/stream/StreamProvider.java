package com.aee.huanghao.selflypreview.stream;

import com.icatchtek.pancam.customer.stream.ICatchIStreamProvider;
import com.icatchtek.reliant.customer.exception.IchStreamNotRunningException;
import com.icatchtek.reliant.customer.exception.IchTryAgainException;
import com.icatchtek.reliant.customer.exception.IchVideoStreamClosedException;
import com.icatchtek.reliant.customer.type.ICatchAudioFormat;
import com.icatchtek.reliant.customer.type.ICatchFrameBuffer;
import com.icatchtek.reliant.customer.type.ICatchVideoFormat;


public class StreamProvider {
    private String TAG = StreamProvider.class.getSimpleName();
    private ICatchIStreamProvider iCatchIStreamProvider;

    public StreamProvider(ICatchIStreamProvider streamProvider) {
        this.iCatchIStreamProvider = streamProvider;
    }

    public boolean containsVideoStream() {
        if (iCatchIStreamProvider == null) {
            return false;
        }
        boolean ret = false;
        try {
            ret = iCatchIStreamProvider.containsVideoStream();
        } catch (Exception e) {
        }
        return ret;
    }

    public boolean containsAudioStream() {
        if (iCatchIStreamProvider == null) {
            return false;
        }
        boolean ret = false;
        try {
            ret = iCatchIStreamProvider.containsAudioStream();
        } catch (Exception e) {
        }
        return ret;
    }

    public ICatchVideoFormat getVideoFormat() {
        if (iCatchIStreamProvider == null) {
            return null;
        }
        ICatchVideoFormat iCatchVideoFormat = null;
        try {
            iCatchVideoFormat = iCatchIStreamProvider.getVideoFormat();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return iCatchVideoFormat;
    }

    public ICatchAudioFormat getAudioFormat() {
        if (iCatchIStreamProvider == null) {
            return null;
        }

        ICatchAudioFormat iCatchAudioFormat = null;
        try {
            iCatchAudioFormat = iCatchIStreamProvider.getAudioFormat();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return iCatchAudioFormat;
    }

    public boolean getNextVideoFrame(ICatchFrameBuffer buffer) throws IchVideoStreamClosedException,IchTryAgainException,IchStreamNotRunningException {
        if (iCatchIStreamProvider == null) {
            return false;
        }
        boolean ret = false;
        try {
            ret = iCatchIStreamProvider.getNextVideoFrame(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;

    }

    public boolean getNextAudioFrame(ICatchFrameBuffer buffer) throws IchVideoStreamClosedException,IchTryAgainException,IchStreamNotRunningException {
        if (iCatchIStreamProvider == null) {
            return false;
        }
        boolean ret = false;
        try {
            ret = iCatchIStreamProvider.getNextAudioFrame(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

}
