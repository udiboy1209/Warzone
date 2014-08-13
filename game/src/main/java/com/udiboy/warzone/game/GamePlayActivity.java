package com.udiboy.warzone.game;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;

public class GamePlayActivity extends Activity{
    DisplayPanel display;
    @Override
    public void onCreate(Bundle icicle){
        super.onCreate(icicle);
        setContentView(R.layout.activity_game_play);
        display = (DisplayPanel) findViewById(R.id.display);

        display.setViews(findViewById(R.id.final_score), findViewById(R.id.current_score), findViewById(R.id.game_over_layout), findViewById(R.id.game_paused_layout));

        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void mainMenu(View v){
        synchronized (display.thread){
            display.thread.setPaused(false);
            display.thread.notify();
        }
        finish();
    }

    public void replay(View v){
        Intent i = getIntent();
        finish();
        startActivity(i);
    }

    public void resume(View v){
        findViewById(R.id.game_paused_layout).setVisibility(View.GONE);
        synchronized (display.thread){
            display.thread.setPaused(false);
            display.thread.notify();
            display.state=DisplayPanel.STATE_GAME_RUNNING;
        }
    }

    /*@Override
    public void onPause(){
        super.onPause();
    }*/
}