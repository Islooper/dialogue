package com.example.dialogue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.example.dialogue.http.HttpUtils;
import com.zhy.http.okhttp.https.HttpsUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements EventListener {

    /**
     * 语音识别
     */
    //语音识别核心库
    private EventManager asr;

    SiriWaveView siri;

    ImageView mic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findId();
//
        widgetInit();

        initPermission();


        //初始化EventManager对象
        asr = EventManagerFactory.create(this, "asr");

        //注册自己的输出事件类
        asr.registerListener(this); //  EventListener 中 onEvent方法
    }

    // 权限申请：申请开启录音权限
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限
            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }
    }

    private void widgetInit() {
//        siri.startAnim();
//        siri.setVolume(20);

        mic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        mic.setImageResource(R.drawable.mic_un);
                        asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0);
                        break;
                    case MotionEvent.ACTION_DOWN:
                        mic.setImageResource(R.drawable.mic);
                        asr.send(SpeechConstant.ASR_START, null, null, 0, 0);
                        break;
                }
                return true;
            }
        });
    }

    private void findId() {
//        siri = findViewById(R.id.siri_wave_view);

        mic = findViewById(R.id.iv_mic);
    }

    /**
     * 识别结果
     */
    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
            // 识别相关的结果都在这里
            if (params == null || params.isEmpty()) {
                return;
            }
            if (params.contains("\"final_result\"")) {
                // 一句话的最终识别结果
                String regrex = "\\[(.*?),";  //使用正则表达式抽取我们需要的内容
                Pattern pattern = Pattern.compile(regrex);
                Matcher matcher = pattern.matcher(params);
                if (matcher.find()) {
                    int a = matcher.group(0).indexOf("[");
                    int b = matcher.group(0).indexOf(",");
                    String voiceString = matcher.group(0).substring(a + 2, b - 3);

                    Log.e("voice result:", voiceString);
                    checkVoice(voiceString);
                }
            }
        }
    }

    /**
     * 判断语音走向
     * @param voiceString：目标解析
     */
    public void checkVoice(String voiceString){
        if (    voiceString.contains("天气") ||
                voiceString.contains("温度") ||
                voiceString.contains("热吗") ||
                voiceString.contains("冷吗") ||
                voiceString.contains("有雨吗") ||
                voiceString.contains("风大吗") ||
                voiceString.contains("风向") ||
                voiceString.contains("适合外出吗")

        )
            HttpUtils.getWeather();
    }
}