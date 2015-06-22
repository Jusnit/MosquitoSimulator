package com.example.user.mosquitosimulator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

/**
 * Created by user on 2015/5/17.
 */
public class Life {
    private Bitmap heart;
    private View view;
    public byte lifeNum = 3;

    //private Canvas canva;
    public Life(View view){
        this.view = view;
        heart = BitmapFactory.decodeResource(view.getResources(),R.drawable.heart35);
    }

    public void drawHeart(Canvas canva,Paint paint){
        for (int i = 0;i < lifeNum;i++){
            canva.drawBitmap(heart,480-60*i,20,paint);
            //Log.v("customed", "drawHeart");
        }
    }
    public void drawHeartThread(Canvas canva,Paint paint){
        (new Thread(new heartRunnable(canva,paint))).start();
    }

    class heartRunnable implements Runnable{
        private Canvas innerCanva;
        private Paint innerPaint;
        public heartRunnable(Canvas canva,Paint paint){
            this.innerCanva = canva;
            this.innerPaint = paint;
        }
        public void run(){
            drawHeart(innerCanva,innerPaint);
        }
    }

   public void minusLife(){
       lifeNum--;
   }
   public int getLifeNum(){return lifeNum;}
}
