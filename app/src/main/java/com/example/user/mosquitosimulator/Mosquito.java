package com.example.user.mosquitosimulator;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by user on 2015/5/10.
 * 主要的遊戲介面View
 */
public class Mosquito extends View implements View.OnTouchListener{
    private Thread currentThread = Thread.currentThread();
    private int netSleepTime = (MainActivity.difficulty == 0)?8:5;
    public double timeFlag = SystemClock.uptimeMillis();
    private VProcessor vProcessor;
    public int progress = 0;
    public Thread checkThread;
    private boolean firstNet = true;
    private boolean signalProcessingStart = false;
    private Life life;
    private Object synKey = new Object();
    private View view = this;
    //v = speedConst*distant
    private float speedConst = (float)0.005;
    //Drawable Resources
    public Drawable mosDrawable,mosShockedDrawable,mosBurnedDrawable,disMosDrawable,baseDrawable,disBaseDrawable,angryBaseDrawable,sleepBaseDrawable;
    private int windowWidth, windowHeight;
    //Position of mosquito
    public boolean test = false;
    public float x;
    public float y;
    private float lastX, lastY;
    private boolean netStart = false;
    public float mosSize = 100,netSize= (MainActivity.difficulty == 0)?150:200;
    private Bitmap netBitmap;
    private long timePass = 0;
    private Rect mosRect;
    private float mosRectRate = (float)0.65;
    private ArrayList<netRunnable> netArray = new ArrayList<netRunnable>();
    Activity mainActivity;
    private checkIntersect intersectCheck;

    public Mosquito(Context context,int windowWidth,int windowHeight,Activity mainActivity) {
        super(context);
        this.mainActivity = mainActivity;

        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        x =  windowWidth/2;
        y =  windowHeight/2;
        life = new Life(this);
        mosDrawable = getResources().getDrawable(R.drawable.mosquito100);
        disMosDrawable = mosDrawable;
        mosShockedDrawable = getResources().getDrawable(R.drawable.shocked100);
        mosBurnedDrawable = getResources().getDrawable(R.drawable.burned100);
        netBitmap = zoomImage(BitmapFactory.decodeResource(getResources(),
                R.drawable.net),netSize,netSize);
        baseDrawable = getResources().getDrawable(R.drawable.cartoonman);
        baseDrawable.setBounds(0,0,windowWidth,windowHeight);
        angryBaseDrawable = getResources().getDrawable(R.drawable.cartoonman2);
        angryBaseDrawable.setBounds(0,0,windowWidth,windowHeight);
        sleepBaseDrawable = getResources().getDrawable(R.drawable.cartoonman3);
        sleepBaseDrawable.setBounds(0,0,windowWidth,windowHeight);
        disBaseDrawable = sleepBaseDrawable;
        mosRect = new Rect((int)(x*mosRectRate),(int)(y*mosRectRate),(int)(mosRectRate*(x+mosSize)),(int)(mosRectRate*(y+mosSize)));
        lastX = x;
        lastY = y;
        intersectCheck = new checkIntersect(synKey,netArray,mosRect,this,mainActivity,life);
        vProcessor = new VProcessor(this,intersectCheck,mainActivity);
        setOnTouchListener(this);
    }

    public void onDraw(Canvas canva){
        super.onDraw(canva);
      //  Log.v("customed","Mosquitothread=:"+Thread.currentThread());
       // Log.v("customed", "OnDrawing...");
       Paint paint = new Paint();
        //要改,x.y是指左上角,非中央
        disBaseDrawable.draw(canva);
        //draw the fury bar.
        //canvas.drawRect(float left, float top, float right , float bottom, Paint paint);
        life.drawHeart(canva ,paint);
        if(test) {
            Log.v("customed", "產生電擊動畫");
        }
        disMosDrawable.setBounds((int)(x-mosSize / 2),(int)(y-mosSize / 2),(int)(x+mosSize / 2),(int)(y+mosSize / 2));
        disMosDrawable.draw(canva);
        //draw the fury bar.
        Paint paint2 = new Paint();  paint2.setColor(Color.RED);
        canva.drawRect( 15, windowHeight/4 - 230, 45,windowHeight/4 + 5, paint2);
        paint2.setColor(Color.BLACK);
        canva.drawRect( 20, windowHeight/4 - 225, 40,windowHeight/4, paint2);
        paint2.setColor(Color.YELLOW);
        canva.drawRect( 20, windowHeight/4 - 225*((float)progress/100), 40,windowHeight/4, paint2);
     //   synchronized (synKey) {
        if (!test) {
            if (!netArray.isEmpty()) {
                for (netRunnable netTemp : netArray) {
                    if (intersectCheck.die || vProcessor.win)
                        break;
                    if(netTemp.visible)
                       // canva.drawBitmap(netBitmap, netTemp.netX, netTemp.netY, paint);
                        canva.drawBitmap(netBitmap, netTemp.matrix,  paint);
                }
                checkThread = new Thread(intersectCheck);
                checkThread.start();
                try {
                 //   Log.i("customed","if裡");
                    checkThread.join();
                }catch(Exception e){}
                synchronized (synKey) {
                    synKey.notifyAll();
                  //  Log.i("customed","if外");
                }
            }
            else{
                synchronized (synKey) {
                   // Log.i("customed","else裡");
                    firstNet = true;
                    synKey.notifyAll();
                   // Log.i("customed","else外");
                }
            }
        }
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!intersectCheck.die){
            if(!netStart)
                netCreated();
            if(!signalProcessingStart) {
                signalProcessingStart = true;
                (vProcessor).RecordStart();
            }
        switch(event.getAction()) {
            case MotionEvent.ACTION_MOVE: {
                mosRunnable mosrunnable = new mosRunnable(this, event.getX(), event.getY());
                Thread thread = new Thread(mosrunnable);
                thread.start();
            }
        }
    }
        return true;
    }
//Function to create mosquito nets
    private void netCreated() {
        netStart = true;
        new Thread((new netThreadStart(this))).start();
    }
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            ((View)(msg.obj)).invalidate();
            super.handleMessage(msg);
        }


    };
    class mosRunnable implements Runnable{
        private float newx,newy;
        private View view;
        public mosRunnable(View view,float newx, float newy){
            this.newx = newx;
            this.newy = newy;
            this.view = view;
        }
        public void run(){
            float scale = Math.abs(newy-y)/Math.abs(newx-x);
            while(newx != x && newy != y && checkTouch()){
                if(newx > x){
                    x = x+1;
                   // lastX = x;
                    if(newx < x) {
                        x = newx;
                    }
                }
                else if(newx < x){
                    x = x -1;
                   // lastX = x;
                    if(newx >x) {
                        x = newx;
                    }
                }
                if(newy >y){
                    y = y+scale;
                   // lastY = y;
                    if(newy<y) {
                        y = newy;
                    }

                }
                else if (newy <y){
                    y = y - scale;
                   // lastY = y;
                    if(newy > y) {
                        y = newy;
                    }
                }
                mosRect.offsetTo((int)(x-mosSize/2+mosSize*(1-mosRectRate)*4/5), (int)(y-mosSize/2+mosSize*(1-mosRectRate)/2));
                Message msg2 = Message.obtain();
                msg2.obj = view;
                handler.sendMessage(msg2);

            }
            lastX = x;
            lastY = y;
        }
        private boolean checkTouch(){
            if (newx<lastX-(float)1.1*mosSize)
                return false;
            if(newx>lastX+(float)1.1*mosSize)
                return false;
            if(newy<lastY-(float)1.1*mosSize)
                return false;
            if(newy > lastY+(float)1.1*mosSize)
                return false;
            return true;
        }
    }
    class netRunnable implements Runnable {
        boolean visible = true;
        boolean attackable = true;
        float netRectRateX = (float)0.45,netRectRateY = (float)0.80;
        float netX=0,netY=0;
        Random random = new Random(SystemClock.uptimeMillis());
        private float speed;
        int choose = random.nextInt();
        public Rect netRect;
        float scale ;
        boolean UpOrNot,leftOrNot;
        private View view;
        //private float netX = 0, netY = 200;
        boolean done = false;
        Matrix matrix = new Matrix();
        float degree = 0;

        public netRunnable(View view) {
            this.view = view;
            if(choose%2 != 0)
            {
                netX = windowWidth * random.nextFloat();
                netY = (random.nextInt()%2 != 0) ? -netSize :windowHeight;
                //scale = Math.abs(y-netY)/Math.abs(x-netX);
                UpOrNot = (y -0.66*mosSize< netY) ?true:false;//以蚊子為主
                leftOrNot = (x -0.66*mosSize< netX) ?true:false;
            }
            else{
                netX =  (random.nextInt()%2 != 0) ? -netSize :windowWidth;
                netY = windowHeight * random.nextFloat();
               // scale = Math.abs(y-netY)/Math.abs(x-netX);
                UpOrNot = (y -0.66*mosSize< netY) ?true:false;
                leftOrNot = (x -0.66*mosSize< netX) ?true:false;
            }
           // netRect = new Rect((int)(netRectRateX*netX),(int)(netRectRateY*netY),(int)(netRectRateX*(netX+netSize)),(int)(netRectRateY*(netY+netSize)));
            netRect = new Rect((int)(netX + netSize * (1 - netRectRateX)*1.2 / 3),(int)(netY+netSize * (1 - netRectRateY) / 2),(int)(netX + netSize*netRectRateX+netSize * (1 - netRectRateX) *1.2/ 3),(int)(netY+netSize*netRectRateY+netSize * (1 - netRectRateY) / 2));
            scale = (float)(Math.abs((y-0.66*mosSize)-(netY))/Math.abs(x-0.66*mosSize-(netX)));
            speed = modifiedSpeed((int)x,(int)netX);
           // matrix.setTranslate(netX+netSize/2, netY+netSize/2);
            //matrix.preRotate(degree,(float)netBitmap.getWidth(),(float)netBitmap.getHeight());
            matrix.reset();
            matrix.setTranslate(this.netX, this.netY);
            matrix.postRotate(degree, netX+netSize/2, netY+netSize/2);
            degree = 0;
        }

        public void run() {

            if(firstNet) {
              //  Log.i("customed","加入第一個NET");

                netArray.add(this);
                firstNet = false;
            }
            else{
                synchronized (synKey) {
                    try {
                      //  Log.i("customed","net被擋");

                        synKey.wait();
                      //  Log.i("customed","NET擋玩出來");

                        netArray.add(this);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
                while (!done) {
                    netX = (leftOrNot) ? netX - speed : netX + speed;
                    netY = (UpOrNot) ? netY - speed * scale : netY + speed * scale;
                    degree = degree + (float)1.6;
                    matrix.reset();
                    matrix.setTranslate(this.netX, this.netY);
                    matrix.postRotate(degree, netX+netSize/2, netY+netSize/2);
                    try {
                        Thread.sleep(netSleepTime);
                        netRect.offsetTo((int) (netX + netSize * (1 - netRectRateX)*1.2 / 3), (int) (netY+netSize * (1 - netRectRateY) / 2));
                        Message msg2 = Message.obtain();
                        msg2.obj = view;
                        handler.sendMessage(msg2);
                    } catch (Exception e) {
                        Log.v("customed", "NetException..." + e.getMessage());
                    }
                    if (netX >= 520 || netX <= -netSize || netY >= 960 || netY <= -netSize) {
                        done = true;
                        //Log.v("customed", "Remove Net from ArrayList!");
                        synchronized (synKey) {
                            try {
                              //  Log.i("customed","消NET被擋");

                                synKey.wait();
                            //    Log.i("customed","消NET出來");

                                netArray.remove(this);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

        }
    }

    class netThreadStart implements Runnable{
        View view ;
        public netThreadStart(View view){
            this.view = view;
        }
        public void run(){
            while (!intersectCheck.die && !vProcessor.win) {
                if (ifCreateNet()) {
                        new Thread((new netRunnable(this.view))).start();
                }
            }
        }
    }
    private boolean ifCreateNet(){
        //timethreshold用來決定蚊拍產生頻率
        double timethreshold = (MainActivity.difficulty == 0)?2000.0*Math.pow((1-0.74*(progress/100.0)),2.0):1000.0*Math.pow((1-0.74*(progress/100.0)),2.0);
        if (SystemClock.uptimeMillis()-timePass > timethreshold){
            timePass = SystemClock.uptimeMillis();
            return true;
        }
        return false;
    }

    //function used to zoom pictures
    public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
                                   double newHeight) {
        // get Height and Width of the picture
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // instaniate Matrix object used to deal with picture
        Matrix matrix = new Matrix();
        // 計算寬高縮放比率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 縮放圖片的動作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }
    private float modifiedSpeed(int x,int netX){
        return (float)(speedConst*(Math.abs(x -0.66*mosSize-( netX))));
    }





}
