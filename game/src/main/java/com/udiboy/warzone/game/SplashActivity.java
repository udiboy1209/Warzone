package com.udiboy.warzone.game;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SplashActivity extends Activity {
    ArrayList<Integer> highscores = new ArrayList<Integer>(10);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(openFileInput("highscores")));

            String line;
            while((line=in.readLine())!=null){
                highscores.add(Integer.valueOf(line));
            }
        } catch (FileNotFoundException fe){
            for(int i=0; i<10; i++)
                highscores.add(0);
        }
        catch (IOException ie){}
    }

    @Override
    public void onStop(){
        try{
            FileOutputStream out = openFileOutput("highscores", Context.MODE_PRIVATE);

            for(int i : highscores){
                out.write((i+"\n").getBytes());
            }
        }catch(FileNotFoundException fe){}
        catch(IOException ie){}
        super.onStop();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        if(hasFocus){
            ImageView splash_logo = (ImageView) findViewById(R.id.splash_logo);
            splash_logo.setBackgroundResource(R.drawable.splash_logo);

            ((AnimationDrawable) splash_logo.getBackground()).start();
        }

        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    public void startGame(View v){
        Intent i = new Intent();
        i.setClass(this, GamePlayActivity.class);
        startActivityForResult(i, 1);
    }

    public void highscores(View v){
        Intent i = new Intent();
        i.setClass(this, HighscoreActivity.class);
        i.putExtra("highscores",highscores);
        startActivity(i);
    }

    public void onActivityResult(int reqCode, int resCode, Intent data){
        if(resCode == RESULT_OK){
            addToHighscores(data.getIntExtra("score", -1));
            if(data.getBooleanExtra("replay",false)){
                Intent i = new Intent();
                i.setClass(this, GamePlayActivity.class);
                startActivityForResult(i, 1);
            }
        }
    }

    public void addToHighscores(int score){
        for(int i=0; i<10; i++){
            if(score>highscores.get(i)){
                highscores.remove(9);
                highscores.add(i, score);
                Toast toast=Toast.makeText(this,"Highscore added at position "+(i+1), Toast.LENGTH_SHORT);
                toast.show();
                break;
            }
        }
    }
}
