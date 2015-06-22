package com.example.user.mosquitosimulator;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MainActivity extends Activity {
    // 0 for Easy(default), 1 for Hard
    public static byte difficulty = 0;
    private static String mode = "Newbie Mode";
    Mosquito mosquito;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       requestWindowFeature(Window.FEATURE_NO_TITLE);
       super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if(intent != null && intent.getBooleanExtra("restart",false)){
            mosquito = new Mosquito(this,getWindowManager().getDefaultDisplay().getWidth(),getWindowManager().getDefaultDisplay().getHeight(),this);
            setContentView(mosquito,new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT));
            return;
        }
        //Menu Scene
        setContentView(R.layout.menu);
        TextView modeText = (TextView)findViewById(R.id.modeText);
        modeText.setText(mode);
       //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Log.v("customed","UIThread=:"+Thread.currentThread());
        if(mosquito != null) {
            mosquito = null;
            Log.v("customed", "Garbage Clean!!");
        }
        //mosquito = new Mosquito(this,getWindowManager().getDefaultDisplay().getWidth(),getWindowManager().getDefaultDisplay().getHeight(),this);

        //setContentView(mosquito,new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT));
        Log.v("customed","After setContentView");
       //setContentView(R.layout.activity_main);

    }
    public void startGame(View view){
        mosquito = new Mosquito(this,getWindowManager().getDefaultDisplay().getWidth(),getWindowManager().getDefaultDisplay().getHeight(),this);
        setContentView(mosquito,new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT));
    }
    public void setting(View view){
        setContentView(R.layout.difficulty);
    }
    //Choose the difficulty desired
    public void chooseEasy(View view){
        difficulty = 0;
        mode = "Newbie Mode";
        //mosquito = new Mosquito(this,getWindowManager().getDefaultDisplay().getWidth(),getWindowManager().getDefaultDisplay().getHeight(),this);
        setContentView(R.layout.menu);
        TextView modeText = (TextView)findViewById(R.id.modeText);
        modeText.setText(mode);
    }
    public void chooseHard(View view){
        difficulty = 1;
        mode = "Master Mode";
       // mosquito = new Mosquito(this,getWindowManager().getDefaultDisplay().getWidth(),getWindowManager().getDefaultDisplay().getHeight(),this);
        setContentView(R.layout.menu);
        TextView modeText = (TextView)findViewById(R.id.modeText);
        modeText.setText(mode);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
