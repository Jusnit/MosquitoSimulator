package com.example.user.mosquitosimulator;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Created by user on 2015/5/17.
 * 對玩家發出的聲音進行訊號處理
 */
public class VProcessor {
    public boolean win = false;
    private Activity mainActivity;
    private checkIntersect check;
    boolean isRecording = true;
    private Mosquito mosView;
   // private byte[] buffer;
    private short[] buffer;
    private int frame = 320;
    static final int frequency = 8000;
    static final int channelConfiguration =AudioFormat.CHANNEL_IN_MONO;
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    int recBufSize;
    AudioRecord audioRecord;
    public VProcessor(Mosquito mosView,checkIntersect check,Activity mainActivity) {
        this.check = check;
        recBufSize = AudioRecord.getMinBufferSize(frequency,
                channelConfiguration, audioEncoding);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
                channelConfiguration, audioEncoding, recBufSize);
        buffer = new short[recBufSize];
        this.mosView = mosView;
        this.mainActivity = mainActivity;
    }

    public void RecordStart() {
        (new Thread(new RecordRunnable())).start();

    }

    class RecordRunnable implements Runnable {
        private boolean recordBlock = false;
        Handler handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 4) {
                    Intent intent = new Intent(mainActivity, WinActivity.class);
                    intent.putExtra("time", SystemClock.uptimeMillis()-mosView.timeFlag);
                    mainActivity.startActivity(intent);
                    mainActivity.finish();

                }
                else if(msg.what == 0){
                    mosView.disBaseDrawable = mosView.angryBaseDrawable;
                }
                else if(msg.what == 1){
                    mosView.disBaseDrawable = mosView.sleepBaseDrawable;
                }
                else if(msg.what == 2){
                    mosView.disBaseDrawable = mosView.baseDrawable;
                }
                else if(msg.what == 3){
                    mosView.disBaseDrawable = mosView.angryBaseDrawable;
                }
                super.handleMessage(msg);

            }
        };

        public void run() {
            try {
                //byte[] innerBuffer = new byte[recBufSize];
                short[] innerBuffer = new short[recBufSize];
                audioRecord.startRecording();
                    while (isRecording && !check.die && !win) {
                        //保存數據到緩衝區
                        if(!recordBlock) {
                            audioRecord.startRecording();
                          //  Log.v("customed", "持續recording中...");
                            int bufferReadResult = audioRecord.read(innerBuffer, 0,
                                    recBufSize);

                           // byte[] tmpBuf = new byte[bufferReadResult];
                            short[] tmpBuf = new short[bufferReadResult];
                            System.arraycopy(innerBuffer, 0, tmpBuf, 0, bufferReadResult);
                            audioRecord.stop();
                            recordBlock = true;
                            pitchTracking(tmpBuf);
                           // Log.e("customed", "Audio Blocking....");

                        }
                    }
            } catch (Throwable t) {
            }
        }

        private void pitchTracking(short[] voice) {
            int totalVolume = 0;
            for(int i = 0;i < voice.length;i++){
                totalVolume = totalVolume + Math.abs(voice[i]);
            }
            if(totalVolume/voice.length < 450) {
                //Log.v("customed","mean Volume:"+String.valueOf(totalVolume/voice.length));
               // Log.e("customed","跳出pitchTracking...");
                recordBlock = false;
                return;
            }
            Log.v("customed","處理音訊");
            Log.v("customed","voice[]長度:"+String.valueOf(voice.length));
            int limit = frame / 2;
            // double[] voiceSlice = new double[frequency/frame];
            int index1 = 0,count = 0;
            for (int i = 0; i < voice.length / frame; i++) {
                //  Log.v("customed","count = "+String.valueOf(++count));
                int FP = 0;
                float FF = 0;
                //byte[] sum = new byte[limit];
                int[] sum = new int[limit];
                for (int s = 0; s < limit; s++) {
                    int counter = 0;
                    for (int t = s; t < limit - 1; t++) {
                        //Log.v("customed","voice["+String.valueOf(index1+t)+"]:"+String.valueOf(voice[index1+t]));
                       // sum[s] = (byte)(sum[s] + Math.abs(voice[index1 + t] * voice[index1 + (t - s)]));
                        sum[s] = (sum[s] + Math.abs(voice[index1 + t] * voice[index1 + (t - s)]));

                    }
                    if(check.die)
                        break;
                    Log.v("customed", "sum[ "+String.valueOf(s)+"] = " + String.valueOf(sum[s]));
                    if (s >= 2) {
                        if (sum[s - 1] > sum[s - 2] && sum[s - 1] > sum[s]) {
                            FP = s - 1;
                            FF = (float)frequency/FP;
                            Log.v("customed", "FF = " + String.valueOf(FF));
                            index1 = index1 +  frame;
                            break;
                        }
                    }
                }
                if(check.die)
                    break;
                if(mosView.progress > 0 ) {
                    int oldProgress = mosView.progress;
                    mosView.progress = (FF > 1100 && mosView.progress >= 0 && mosView.progress < 99) ? mosView.progress + 2 : mosView.progress - 1;//edit this
                    if(mosView.progress >= 25 && oldProgress < 25){
                        Message message = Message.obtain();
                        // 0->sleepbackground change to angry
                        message.what = 0;
                        handler.sendMessage(message);
                    }
                    else if(mosView.progress < 25 && oldProgress >= 25){
                        Message message = Message.obtain();
                        // 1 -> angrybackground change to sleep
                        message.what = 1;
                        handler.sendMessage(message);
                    }
                    else if(mosView.progress >= 75 && oldProgress < 75){
                        Message message = Message.obtain();
                        // 2 -> angrybackground change to bigeye
                        message.what = 2;
                        handler.sendMessage(message);
                    }
                    else if(mosView.progress < 75 && oldProgress >= 75){
                        Message message = Message.obtain();
                        // 3 -> bigEyeBackground change to angry
                        message.what = 3;
                        handler.sendMessage(message);
                    }

                }
                else if(mosView.progress == 0)
                    mosView.progress = (  FF > 1100 && mosView.progress >= 0 && mosView.progress < 99) ? mosView.progress + 2 : mosView.progress ;
                if(mosView.progress >= 99 && !check.die){
                    win = true;
                    Message message = Message.obtain();
                    // 4-> win
                    message.what = 4;
                    handler.sendMessage(message);
                }

            }
            Log.v("customed","結束pitchTracking...");
            recordBlock = false;
        }
    };


}







