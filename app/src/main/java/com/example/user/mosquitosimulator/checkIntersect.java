package com.example.user.mosquitosimulator;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;

import com.example.user.mosquitosimulator.Mosquito;

import java.util.ArrayList;
import java.util.logging.Handler;

/**
 * Created by Lab109 on 2015/5/18.
 */
public class checkIntersect implements Runnable{
    public boolean die = false;
    private Object synKey;
    private ArrayList<Mosquito.netRunnable> netArray;
    private ArrayList<Mosquito.netRunnable> netArrayCopy = new  ArrayList<Mosquito.netRunnable>();
    private Rect mosRect;
    private Mosquito mosView;
    private  Activity mainActivity;
    private Life life;
    private Vibrator vibrator;
    public android.os.Handler handler = new android.os.Handler(Looper.getMainLooper()){
        @Override
    public void handleMessage(Message msg) {
            Activity actTemp = (Activity)msg.obj;
            Vibrator vibrator = (Vibrator) actTemp.getApplication().getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(1800);
            actTemp.setContentView(new DieView(mainActivity,mosView,mainActivity));
            //((View)(msg.obj)).invalidate();
            super.handleMessage(msg);
        }
    };
    public checkIntersect(Object synKey,ArrayList<Mosquito.netRunnable> netArray,Rect mosRect,Mosquito mosView,Activity activity,Life life){
        this.synKey = synKey;
        this.netArray = netArray;
        this.mosRect = mosRect;
        this.mosView = mosView;
        this.mainActivity = activity;
        this.life = life;
        this.vibrator  = (Vibrator) this.mainActivity.getApplication().getSystemService(Service.VIBRATOR_SERVICE);
    }
    public void run(){
        //while(true){
            if (!netArray.isEmpty()) {

         //   synchronized(synKey) {
               //// if(!netArrayCopy.isEmpty())
                   // netArrayCopy.clear();
               // netArrayCopy.addAll(netArray);
                for (Mosquito.netRunnable netTemp : netArray) {
                 //   Log.i("customed","在checkintersect裡面");
                   /* if(mosRect == null||netTemp.netRect==null||netArrayCopy == null||netTemp==null)
                        Log.i("customed","Something is null!!");
                    else
                        Log.i("customed","No one is null!!");*/
                    if (mosRect.intersect(netTemp.netRect)&&netTemp.attackable) {
                        // test = true;
                        life.minusLife();
                        vibrator.vibrate(100);
                        if (life.getLifeNum() > 0) {
                            netTemp.attackable = false;
                            netTemp.visible = false;
                            break;
                        }
                            die = true;
                            mosView.test = true;
                            Message message2 = Message.obtain();
                            message2.obj = mainActivity;
                            handler.sendMessage(message2);
                           // mainActivity.setContentView(new DieView(mainActivity, mosView, mainActivity));
/*
                            int counter = 1;
                            while (counter <= 15) {
                                // if (ifDraw()) {
                                if (counter % 2 != 0) {
                                    //whichMos = 1;
                                    mosView.chooseMos(1);
                                    Log.v("customed", "變藍");
                                } else {
                                    //whichMos = 0;
                                    mosView.chooseMos(0);
                                    Log.v("customed", "變黃的");
                                }
                                counter++;
                                //}
                                Message msg3 = Message.obtain();
                                msg3.obj = mosView;
                                handler.sendMessage(msg3);
                                try {
                                    Thread.sleep(100);
                                } catch (Exception e) {
                                }

                            }
                            //whichMos = 2;
                            mosView.chooseMos(2);
                            Message msg3 = Message.obtain();
                            msg3.obj = mosView;
                            handler.sendMessage(msg3);
                            try {
                                Thread.sleep(1200);
                            } catch (Exception e) {
                            }
                            Intent intent = new Intent(mainActivity, LoseActivity.class);
                            Log.v("customed", "Intersect123");
                            mainActivity.startActivity(intent);
                            mainActivity.finish();
                            break;
*/

                    }

                }
           // }//
        }
       // } // While End
    }
}
