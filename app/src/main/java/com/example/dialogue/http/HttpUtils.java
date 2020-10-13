package com.example.dialogue.http;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

/**
 * Created by looper on 2020/10/13.
 */
public class HttpUtils {

    /**
     * 获取当前天气
     */
    public static void getWeather (){
        OkHttpUtils.get()
                .url("https://v0.yiketianqi.com/api?version=v62&appid=67128388&appsecret=uAwS3dvF&city=无锡")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                        Log.e("wrong" , e.getMessage());
                    }

                    @Override
                    public void onResponse(String response) {
                        Log.e("get weather"  , response);

                        // TODO：解析朗读文本并发送广播
                        //对接收到的数据进行处理
                        // 处理接收到的数据
//                        JsonParser parser = new JsonParser();
//                        JsonArray jsonArray = parser.parse(response).getAsJsonArray();
//                        Gson gson = new Gson();
//                        for (JsonElement sensorDatas : jsonArray
//                        ) {
//                            if (revSensorName != null) {
//                                if (revSensorName.equals(sensorName)) {
//                                    String SensorLastActive = sensorData.getLastActive();
//                                    String SensorValue = sensorData.getValue();
//                                    String SensorOnlineStatus = sensorData.getOnline().equals("1") ? "在线" : "不在线";
//
//                                    // 发送广播
//                                    BroadcastSender sender = new BroadcastSender();
//                                    sender.send("result_sensor" , sensorName +";"+ SensorLastActive +";"+SensorOnlineStatus+";"+SensorOnlineStatus +";"+ SensorValue);
//                                    break;
//                                }
//                            }
//                        }
                    }
                });
    }

    /**
     * 读取文字
     */
    public static void readWords(){

    }
}
