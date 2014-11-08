package com.udiboy.warzone.game;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class HighscoreActivity extends Activity {
    ArrayList<Integer> highscores;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore);

        Intent data = getIntent();
        highscores=data.getIntegerArrayListExtra("highscores");
        LinearLayout list = (LinearLayout)findViewById(R.id.highscores);

        for(int i=0; i<10; i++){
            ((TextView)(list.getChildAt(i))).setText((i+1)+".) "+highscores.get(i));
        }
    }

    public void mainMenu(View v){
        finish();
    }
}
