package com.example.dialogue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;
import com.example.dialogue.adapter.TextTagsAdapter;
import com.example.dialogue.http.HttpUtils;
import com.example.dialogue.util.TiaoZiUtil;
import com.moxun.tagcloudlib.view.TagCloudView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

public class MainActivity extends AppCompatActivity implements EventListener {

    /**
     * 语音识别
     */
    //语音识别核心库
    private EventManager asr;

    SiriWaveView siri;

    ImageView mic;
    TextView discriminate , question;

    private TiaoZiUtil tiaoziUtil;

    /**
     * 广播
     */
    public static final String BROADCAST_ACTION = "com.example.corn";
    private BroadcastReceiver mBroadcastReceiver;

    Context mContext;

    // 创建百度语音合成的构造器
    SpeechSynthesizer mSpeechSynthesizer;

    String apiKey = "gSXKK3GyTSxSpTPbp4pBtGHd";
    String secretKey = "apXsAuqiHrs3WhTWU9pUFWiVcZe5rLlq";
    String appId = "22676139";
    // 初始化百度tts
    public void speakText(Context context , String resultVoicetString) {
        // 创建百度语音合成的解析器
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(context);
        // 2. 设置listener
        mSpeechSynthesizer.setSpeechSynthesizerListener(null);
        // 3. 设置appId，appKey.secretKey
        mSpeechSynthesizer.setAppId(appId);
        mSpeechSynthesizer.setApiKey(apiKey, secretKey);

        // 5. 以下setParam 参数选填。不填写则默认值生效
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 精品语音+
//        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "5118");
        // 设置合成的音量，0-15 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "15");
        // 设置合成的语速，0-15 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "4");
        // 设置合成的语调，0-15 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // 6. 初始化
        int result = mSpeechSynthesizer.initTts(TtsMode.ONLINE);

        mSpeechSynthesizer.speak(resultVoicetString); //开始播放

//        mSpeechSynthesizer.pause(); //暂停
//        mSpeechSynthesizer.resume(); //恢复
//        mSpeechSynthesizer.stop(); //停止

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 去除标题栏

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        findId();

        widgetInit();

        initBallTips();

        initPermission();

        mContext = this;
        // 初始化EventManager对象
        asr = EventManagerFactory.create(this, "asr");

        // 注册自己的输出事件类
        asr.registerListener(this); //  EventListener 中 onEvent方法


        // 广播接受
        mBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION);
        registerReceiver(mBroadcastReceiver, intentFilter);


    }


    String[] tips = {"今天天气怎么样？", "今天热吗", "你叫什么名字",
            "你口渴吗", "你是什么植物", "今天有雨吗",
            "今天风向是什么",
            "湿度是多少","今天适合出行吗"
    };

    private void initBallTips() {
        TagCloudView tagCloudView = (TagCloudView) findViewById(R.id.tag_cloud);
        tagCloudView.setBackgroundColor(Color.LTGRAY);

        TextTagsAdapter tagsAdapter = new TextTagsAdapter(tips);
        tagCloudView.setAdapter(tagsAdapter);
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
        mic = findViewById(R.id.iv_mic);
        discriminate = findViewById(R.id.tv_discriminate);
        question = findViewById(R.id.tv_question);
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
        question.setText(voiceString);
        if (    voiceString.contains("天气") ||
                voiceString.contains("温度") ||
                voiceString.contains("热吗") ||
                voiceString.contains("冷吗") ||
                voiceString.contains("有雨吗") ||
                voiceString.contains("风大吗") ||
                voiceString.contains("风向") ||
                voiceString.contains("适合外出吗") ||
                voiceString.contains("适合出行吗")

        )
            HttpUtils.getWeather();

        // 你口渴吗？你是谁？
        else if ( voiceString.contains("你是谁") ||
                voiceString.contains("你的名字") ||
                voiceString.contains("名字")  ||
                voiceString.contains("姓名")  ||
                voiceString.contains("什么草") ||
                voiceString.contains("植物")

        ){
            // 组语音包
            String voiceContent = "我的名字是"+PlantConfig.name+"。对我说：查看详细资料，我会告诉你更多哦";
            // 发送语音包
            HttpUtils.readWords(voiceContent, Environment.getExternalStorageDirectory().getAbsolutePath() , "1.mp3");
        }

        else if ( voiceString.contains("查看详细资料"))
        {
            // 组语音包
            String voiceContent = PlantConfig.detail;
            // 发送语音包
            HttpUtils.readWords(voiceContent, Environment.getExternalStorageDirectory().getAbsolutePath() , "1.mp3");
        }

        else if (    voiceString.contains("口渴") ||
                voiceString.contains("喝水") ||
                voiceString.contains("湿度") ||
                voiceString.contains("渴不渴")
        )
        {
            // 查找湿度传感器数据
            HttpUtils.getHumidity();
        }
        else if (voiceString.contains("噪声")){
            HttpUtils.getNoise();
        }else if (voiceString.contains("大气压")){
            HttpUtils.getPa();
        }else if (voiceString.contains("紫外线")){
            HttpUtils.getRays();
        }
        else
        {
            // 查询云端
            HttpUtils.searchAnswer(voiceString);
        }


    }



    /**
     * 广播接受类
     */

    MediaPlayer mediaPlayer = new MediaPlayer();
    class MyBroadcastReceiver extends BroadcastReceiver {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            String value = intent.getStringExtra("voice");

            if (value != null && !value.equals("")){
                // 获取音频文件
               HttpUtils.readWords(value, Environment.getExternalStorageDirectory().getAbsolutePath() , "1.mp3");

            }

            String reader = intent.getStringExtra("reader");

            if (reader != null && !reader.equals("")){
             // 播放

                try {
                    if (mediaPlayer.isPlaying()){
                        return;
                    }
                    mediaPlayer.setDataSource("/storage/emulated/0/1.mp3"); // 设置播放的文件位置

                    mediaPlayer.prepare(); // 准备文件

                    // 播放完成监听
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            discriminate.setText("");
                        }
                    });

                    mediaPlayer.start();


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


            String words = intent.getStringExtra("words");
            if (words != null && !words.equals("")){
                tiaoziUtil = new TiaoZiUtil(discriminate, words, 250);//调用构造方法，直接开启
            }

        }

    }



}