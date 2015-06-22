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
    int recBufSize, playBufSize;
    AudioRecord audioRecord;
    //AudioTrack audioTrack;

    public VProcessor(Mosquito mosView,checkIntersect check,Activity mainActivity) {
        this.check = check;
        recBufSize = AudioRecord.getMinBufferSize(frequency,
                channelConfiguration, audioEncoding);

        /*playBufSize = AudioTrack.getMinBufferSize(frequency,
                channelConfiguration, audioEncoding);*/
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
                channelConfiguration, audioEncoding, recBufSize);
        buffer = new short[recBufSize];
        this.mosView = mosView;
        this.mainActivity = mainActivity;

        /*audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency,
                channelConfiguration, audioEncoding,
                playBufSize, AudioTrack.MODE_STREAM);*/
    }

    public void RecordStart() {
        (new Thread(new RecordRunnable())).start();

    }

    class RecordRunnable implements Runnable {
        private boolean recordBlock = false;
        Handler handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
               // mosView.progress = msg.arg1;
                //((Mosquito)(msg.obj)).invalidate();
                //Activity actTemp = (Activity)msg.obj;
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

                    //mainActivity.setContentView(R.layout.activity_win);
                    //((View)(msg.obj)).invalidate();
                super.handleMessage(msg);

            }
        };

        public void run() {
            try {
                //byte[] innerBuffer = new byte[recBufSize];
                short[] innerBuffer = new short[recBufSize];
                audioRecord.startRecording();
                    while (isRecording && !check.die && !win) {
                        //保存數據到緩衝區a
                        if(!recordBlock) {
                            audioRecord.startRecording();
                          //  Log.v("customed", "持續recording中...");
                            int bufferReadResult = audioRecord.read(innerBuffer, 0,
                                    recBufSize);

                           // byte[] tmpBuf = new byte[bufferReadResult];
                            short[] tmpBuf = new short[bufferReadResult];
                            System.arraycopy(innerBuffer, 0, tmpBuf, 0, bufferReadResult);
                            //寫入數據
                            //audioTrack.write(tmpBuf, 0, tmpBuf.length);
                            audioRecord.stop();
                            recordBlock = true;
                            pitchTracking(tmpBuf);


                           // Log.e("customed", "Audio Blocking....");
                            //audioRecord.startRecording();
                        }
                    }

                //audioTrack.stop();
               // audioRecord.stop();
            } catch (Throwable t) {
                //Toast.makeText(testRecord.this, t.getMessage(), 1000);
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
                   // Log.v("customed","count = "+String.valueOf(count)+",進入第二圈for");
                    int counter = 0;
                    for (int t = s; t < limit - 1; t++) {
                       // Log.v("customed","count = "+String.valueOf(count)+",進入第三圈for");
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

                    //counter++;
                }
                if(check.die)
                    break;
               /* Message message = Message.obtain();
                message.arg1 = ( FF > 300) ? mosView.progress + 1 : mosView.progress;
                message.obj = mosView;
                handler.sendMessage(message);*/
                if(mosView.progress > 0 ) {
                    int oldProgress = mosView.progress;
                    mosView.progress = (FF > 1100 && mosView.progress >= 0 && mosView.progress < 99) ? mosView.progress + 2 : mosView.progress - 1;//edit this
                    if(mosView.progress >= 25 && oldProgress < 25){
                        Message message = Message.obtain();
                        message.what = 0;// 0->sleepbackground change to angry
                        handler.sendMessage(message);
                    }
                    else if(mosView.progress < 25 && oldProgress >= 25){
                        Message message = Message.obtain();
                        message.what = 1;// 1 -> angrybackground change to sleep
                        handler.sendMessage(message);
                    }
                    else if(mosView.progress >= 75 && oldProgress < 75){
                        Message message = Message.obtain();
                        message.what = 2;// 2 -> angrybackground change to bigeye
                        handler.sendMessage(message);
                    }
                    else if(mosView.progress < 75 && oldProgress >= 75){
                        Message message = Message.obtain();
                        message.what = 3;// 3 -> bigEyeBackground change to angry
                        handler.sendMessage(message);
                    }

                }
                else if(mosView.progress == 0)
                    mosView.progress = (  FF > 1100 && mosView.progress >= 0 && mosView.progress < 99) ? mosView.progress + 2 : mosView.progress ;
                if(mosView.progress >= 99 && !check.die){
                    win = true;
                    Message message = Message.obtain();
                    message.what = 4; // 4-> win
                    handler.sendMessage(message);
                }

            }
            Log.v("customed","結束pitchTracking...");
            recordBlock = false;
        }
    };


}







