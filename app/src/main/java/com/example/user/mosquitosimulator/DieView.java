package com.example.user.mosquitosimulator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

/**
 * Created by Lab109 on 2015/5/19.
 */
public class DieView extends View {
    private float mosX,mosY;
    private Drawable mosDrawable ;
    private Drawable mosShockedDrawable;
    private Drawable mosBurnedDrawable;
    private Drawable disMosDrawable;
    private boolean start = false;
    private Activity mainActivity;
    private Mosquito mosView;
    public DieView(Context context,Mosquito mosView,Activity mainActivity){
        super(context);
        this.mosX = mosView.x;
        this.mosY = mosView.y;
        this.mosDrawable = mosView.mosDrawable;
        this.mosShockedDrawable = mosView.mosShockedDrawable;
        this.mosBurnedDrawable = mosView.mosBurnedDrawable;
        this.disMosDrawable = mosDrawable;
        this.mainActivity = mainActivity;
        this.mosView = mosView;
    }
    public void onDraw(Canvas canva){

           mosView.baseDrawable.draw(canva);
           disMosDrawable.setBounds((int)(mosX-mosView.mosSize / 2),(int)(mosY-mosView.mosSize / 2),(int)(mosX+mosView.mosSize / 2),(int)(mosY+mosView.mosSize / 2));
           disMosDrawable.draw(canva);
        if(!start) {
            new Thread(new DieRunnable(this)).start();
        }

    }

    class DieRunnable implements Runnable{
        private View view;
        private Handler handler = new Handler(Looper.getMainLooper()){
            public void handleMessage(Message msg) {
                ((View)(msg.obj)).invalidate();
                super.handleMessage(msg);
            }
        };
        public DieRunnable(View view){
            this.view = view;
        }

        public void run(){
            start = true;
            int counter = 1;
            while (counter <= 15) {
                // if (ifDraw()) {
                if (counter % 2 != 0) {
                    //whichMos = 1;
                    disMosDrawable = mosShockedDrawable;
                    Log.v("customed", "變藍");
                } else {
                    //whichMos = 0;
                    disMosDrawable = mosDrawable;
                    Log.v("customed", "變黃的");
                }
                counter++;
                //}
                Message msg3 = Message.obtain();
                msg3.obj = view;
                handler.sendMessage(msg3);
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }

            }
            //whichMos = 2;
            disMosDrawable = mosBurnedDrawable;
            Message msg3 = Message.obtain();
            msg3.obj = view;
            handler.sendMessage(msg3);
            try {
                Thread.sleep(1200);
            } catch (Exception e) {
            }
            Intent intent = new Intent(mainActivity, LoseActivity.class);
            Log.v("customed", "Intersect123");
            mainActivity.startActivity(intent);
            mainActivity.finish();

        }
    }
}
