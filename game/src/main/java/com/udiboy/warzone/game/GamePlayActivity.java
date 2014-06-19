package com.udiboy.warzone.game;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager.LayoutParams;

public class GamePlayActivity extends Activity{

    @Override
    public void onCreate(Bundle icicle){
        super.onCreate(icicle);
        setContentView(new DisplayPanel(this));

        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /*@Override
    public void onPause(){
        super.onPause();
    }*/
}