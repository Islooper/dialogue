package com.example.dialogue.http;

import android.os.Environment;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.dialogue.BroadcastSender;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by looper on 2020/10/13.
 */
public class HttpUtils {

    public static String url = "http://118.25.37.124:8088/";
    /**
     * 获取当前天气
     */
    public static void getWeather (){
        OkHttpUtils.get()
                .url("https://v0.yiketianqi.com/api?version=v62&appid=67128388&appsecret=uAwS3dvF&city=无锡")
                .addHeader("Content-type","application/x-www-form-urlencoded;charset:UTF-8")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                    }

                    @Override
                    public void onResponse(String response) {
                        String result = decodeUnicode(response);

                        //对接收到的数据进行处理
                        // 处理接收到的数据
                        JSONObject data = JSONObject.parseObject(response);
                        String cityName = data.getString("city");

                        // 获取时间
                        String date = data.getString("date");
                        // 获取星期
                        String week = data.getString("week");

                        // 获取天气
                        String wea = data.getString("wea");

                        // 获取温度
                        String tem = data.getString("tem2");

                        // 获取风向
                        String win = data.getString("win");

                        // 获取空气质量
                        String airLevel = data.getString("air_level");

                        // 获取tips
                        String tips = data.getString("air_tips");

                        // 下面是具体参数

                        // 获取zhishu
                        JSONObject tipsJson = JSONObject.parseObject(data.getString("zhishu"));

                        // 获取穿衣
                        String clothes = tipsJson.getString("chuanyi");
                        JSONObject clothesJson = JSONObject.parseObject(clothes);
                        String levelClo = clothesJson.getString("level");
                        String tipsClo  = clothesJson.getString("tips");


                        // 获取带伞情况
                        String un = tipsJson.getString("daisan");
                        JSONObject unJson = JSONObject.parseObject(un);
                        String levelUn = unJson.getString("level");
                        String tipsUn  = unJson.getString("tips");

                        // 获取感冒情况
                        String ill = tipsJson.getString("ganmao");
                        JSONObject illJson = JSONObject.parseObject(ill);
                        String levelIll = illJson.getString("level");
                        String tipsIll  = illJson.getString("tips");

                        // 获取紫外线情况
                        String z = tipsJson.getString("ziwaixian");
                        JSONObject zJson = JSONObject.parseObject(z);
                        String levelZ = zJson.getString("level");
                        String tipsZ  = zJson.getString("tips");

                        // 获取晾衣服情况
                        String hangClo = tipsJson.getString("liangshai");
                        JSONObject hangCloJson = JSONObject.parseObject(hangClo);
                        String levelHang = hangCloJson.getString("level");
                        String tipsHang  = hangCloJson.getString("tips");


                        // 合成语音
                        String voice = "下面为您播报"+cityName+"当前的天气情况，"+ date +"，"+ week + "，天气："+
                                wea+"，"+tem+"摄氏度，风向："+ win + "，空气质量："+airLevel + "。"+tips+
                                "下面是详细参数:穿衣方面:"+levelClo+"，"+tipsClo+"带伞情况："+levelUn+"，"+tipsUn+
                                "感冒走向："+levelIll+"，"+tipsIll+"紫外线情况:"+levelZ+"，建议"+tipsZ+"晾衣情况："+
                                levelHang +"，建议"+tipsHang+"最后祝您开心每一天。";

                        // 发送广播
                        BroadcastSender sender = new BroadcastSender();
                        sender.send("voice" , voice);



                    }
                });
    }

    /**
     * 读取文字
     */
    public static void readWords(String voice,final String destFileDir, final String destFileName){
        BroadcastSender sender = new BroadcastSender();
        sender.send("words" , voice);
        OkHttpUtils.get()
                .url("https://tsn.baidu.com/text2audio")
                .addParams("tex" , voice)
                .addParams("tok" , "24.7de40ee61408a92cddbc3b2918e923c0.2592000.1605175138.282335-22694547")
                .addParams("cuid","iasdbkans")
                .addParams("ctp" , "1")
                .addParams("lan" , "zh")
                .addParams("aue", "3")
                .build()
                .execute(new Callback() {
                    @Override
                    public Object parseNetworkResponse(Response response) throws IOException {
                        InputStream is = null;
                        byte[] buf = new byte[2048];
                        int len = 0;
                        FileOutputStream fos = null;

                        //储存下载文件的目录
                        File dir = new File(destFileDir);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        File file = new File(dir, destFileName);

                        try {

                            is = response.body().byteStream();
                            long total = response.body().contentLength();
                            fos = new FileOutputStream(file);
                            long sum = 0;
                            while ((len = is.read(buf)) != -1) {
                                fos.write(buf, 0, len);
                                sum += len;
                                int progress = (int) (sum * 1.0f / total * 100);
                                //下载中更新进度条
                            }
                            fos.flush();
                            //下载完成
                            String path = file.getAbsolutePath();
                            // 发送广播
                            BroadcastSender sender = new BroadcastSender();
                            sender.send("reader" , "ok");
                        } catch (Exception e) {
                        }finally {

                            try {
                                if (is != null) {
                                    is.close();
                                }
                                if (fos != null) {
                                    fos.close();
                                }
                            } catch (IOException e) {

                            }

                        }
                        return null;
                    }

                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(Object response) {

                    }
                });
    }





    /*
     * unicode编码转中文
     */
    public static String decodeUnicode(String theString) {
        char aChar;
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);
        for (int x = 0; x < len;) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed   \\uxxxx   encoding.");
                        }

                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f')
                        aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else
                outBuffer.append(aChar);
        }
        return outBuffer.toString();
    }


    /**
     * 查找湿度传感器
     */


    public static void getHumidity(){
        OkHttpUtils.get()
                .url(url + "placeAndTypeSelectSensorData.do")
                .addParams("place" , "001")
                .addParams("types" , "56")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                    }

                    @Override
                    public void onResponse(String response) {
                        JSONObject data = JSONObject.parseObject(response);
                        JSONArray result = data.getJSONArray("result");
                        String res = result.getString(0);
                        JSONObject humidity = JSONObject.parseObject(res);

                        String value = humidity.getString("value");

                        // 判断数据
                        int thValue = Integer.parseInt(value);
                        String voice = "";
                        if (thValue < 10){
                            voice = "检测到湿度较低，快给我浇水啦";
                        }else {
                            voice = "当前湿度合适，不用浇水哦";
                        }

                        HttpUtils.readWords(voice, Environment.getExternalStorageDirectory().getAbsolutePath() , "1.mp3");

                    }
                });
    }


    /**
     * 查询云端问题答案
     * @param question：需要查询的问题
     */

    public static void searchAnswer(String question){
        OkHttpUtils.get()
                .url(url + "searchAnswer.do")
                .addParams("name" , question)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        JSONObject data = JSONObject.parseObject(response);

                        Integer code = data.getInteger("resultCode");
                        if (code == 0){
                            JSONArray arr = data.getJSONArray("result");
                            String result = arr.getString(0);
                            JSONObject anJson = JSONObject.parseObject(result);
                            String answer = anJson.getString("answer");

                            HttpUtils.readWords(answer, Environment.getExternalStorageDirectory().getAbsolutePath() , "1.mp3");
                        }
                    }
                });
    }
}
