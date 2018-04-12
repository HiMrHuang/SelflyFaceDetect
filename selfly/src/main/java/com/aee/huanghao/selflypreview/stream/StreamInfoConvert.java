package com.aee.huanghao.selflypreview.stream;

/**
 * Created by huanghao
 * Created Time: 2018/3/13
 * Version code: 1.0
 * Description:
 */

public class StreamInfoConvert {

    public static StreamInfo convertToStreamInfoBean(String cmd){
        String[] temp;
        StreamInfo streamInfo = new StreamInfo();
        if(cmd.contains("FPS")){
            temp = cmd.split("\\?|&");
            streamInfo.mediaCodecType = temp[0];
            temp[1] = temp[1].replace("W=","");
            temp[2] = temp[2].replace("H=","");
            temp[3] = temp[3].replace("BR=","");
            temp[4] = temp[4].replace("FPS=","");
            streamInfo.width = Integer.parseInt(temp[1]);
            streamInfo.height = Integer.parseInt(temp[2]);
            streamInfo.bitrate = Integer.parseInt(temp[3]);
            streamInfo.fps = Integer.parseInt(temp[4]);
        }else {
            temp = cmd.split("\\?|&");
            streamInfo.mediaCodecType = temp[0];
            temp[1] = temp[1].replace("W=","");
            temp[2] = temp[2].replace("H=","");
            temp[3] = temp[3].replace("BR=","");
            streamInfo.width = Integer.parseInt(temp[1]);
            streamInfo.height = Integer.parseInt(temp[2]);
            streamInfo.bitrate = Integer.parseInt(temp[3]);
            streamInfo.fps = 30;
        }
        return  streamInfo;
    }

}
