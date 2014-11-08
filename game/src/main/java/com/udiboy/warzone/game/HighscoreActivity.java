package com.udiboy.warzone.game;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class HighscoreActivity extends Activity {
    ArrayList<Integer> highscores = new ArrayList<Integer>(10);
    int score;
    public static final int ACTION_DISPLAY=0,
                            ACTION_SAVE=1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        Intent data = getIntent();
        int action=data.getIntExtra("action",ACTION_DISPLAY);

        switch(action) {
            case ACTION_DISPLAY:
                setContentView(R.layout.activity_highscore_display);

                LinearLayout list = (LinearLayout) findViewById(R.id.highscores);

                for (int i = 0; i < 10; i++) {
                    ((TextView) (list.getChildAt(i))).setText((i + 1) + ".) " + highscores.get(i));
                }
                break;
            case ACTION_SAVE:
                setContentView(R.layout.activity_highscore_save);
                score = data.getIntExtra("score",0);

                ((TextView)findViewById(R.id.final_score)).setText("Your Score: "+score);
        }
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

    public void mainMenu(View v){
        finish();
    }

    public void save(View v) {
        addToHighscores();
        finish();
    }

    public void addToHighscores(){
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
