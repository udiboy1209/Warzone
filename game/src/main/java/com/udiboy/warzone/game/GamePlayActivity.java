package com.udiboy.warzone.game;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;

public class GamePlayActivity extends Activity{

    @Override
    public void onCreate(Bundle icicle){
        super.onCreate(icicle);
        setContentView(R.layout.activity_game_play);
        DisplayPanel display = (DisplayPanel) findViewById(R.id.display);

        display.setViews(findViewById(R.id.final_score), findViewById(R.id.game_over_layout));

        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void mainMenu(View v){
        finish();
    }

    public void replay(View v){
        Intent i = getIntent();
        finish();
        startActivity(i);
    }

    /*@Override
    public void onPause(){
        super.onPause();
    }*/
}