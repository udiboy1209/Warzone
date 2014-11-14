package com.udiboy.warzone.game;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class HowToPlayDialog extends Activity {

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_how_to_play);
    }

    public void mainMenu(View v){
        finish();
    }

    public void startGame(View v){
        Intent i = new Intent();
        i.setClass(this, GamePlayActivity.class);
        startActivity(i);
        finish();
    }
}
