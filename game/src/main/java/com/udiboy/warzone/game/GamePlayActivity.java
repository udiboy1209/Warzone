package com.udiboy.warzone.game;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager.LayoutParams;

public class GamePlayActivity extends Activity{
    DisplayPanel display;
    MediaPlayer bg_music;
    MediaPlayer end_music;

    @Override
    public void onCreate(Bundle icicle){
        super.onCreate(icicle);
        setContentView(R.layout.activity_game_play);
        display = (DisplayPanel) findViewById(R.id.display);

        display.setViews(findViewById(R.id.final_score), findViewById(R.id.current_score), findViewById(R.id.game_paused_layout));

        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

        bg_music=MediaPlayer.create(this, R.raw.music_game);
        bg_music.setLooping(true);
        bg_music.setVolume(1.0f, 1.0f);
        end_music=MediaPlayer.create(this, R.raw.music_end);
        end_music.setLooping(true);
        end_music.setVolume(1.0f, 1.0f);

        FontHelper.applyFont(this, findViewById(R.id.activity_root), "fonts/defused.ttf");
    }

    @Override
    public void onPause(){
        display.exit_action=DisplayPanel.ACTION_PAUSE;
        if(bg_music.isPlaying()) bg_music.pause();
        if(end_music.isPlaying()) end_music.pause();
        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
        if(display.state==DisplayPanel.STATE_GAME_OVER)
            end_music.start();
        else
            bg_music.start();
    }

    public void mainMenu(View v){
        synchronized (display.thread){
            display.thread.setPaused(false);
            display.thread.notify();
        }
        Intent i = new Intent();
        i.putExtra("score",display.health_and_score.getScore());
        i.putExtra("replay",false);
        setResult(RESULT_OK, i);
        finish();
    }

    public void replay(View v){
        Intent i = new Intent();
        i.putExtra("score",display.health_and_score.getScore());
        i.putExtra("replay",true);
        setResult(RESULT_OK, i);
        finish();
    }

    public void resume(View v){
        findViewById(R.id.game_paused_layout).setVisibility(View.GONE);
        synchronized (display.thread){
            display.thread.setPaused(false);
            display.thread.notify();
            display.state=DisplayPanel.STATE_GAME_RUNNING;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent i){

    }
}