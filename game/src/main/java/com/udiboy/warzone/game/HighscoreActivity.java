package com.udiboy.warzone.game;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class HighscoreActivity extends Activity {
    ArrayList<String> highscores = new ArrayList<String>(10);
    int score,
        position=10;
    public static final int ACTION_DISPLAY=0,
                            ACTION_SAVE=1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(openFileInput("highscores")));

            String line;
            while((line=in.readLine())!=null){
                highscores.add(line);

                if(line.split(",").length!=2) { //Should equal the no. of fields
                    highscores.clear();
                    for (int i = 0; i < 10; i++)
                        highscores.add("---,0");
                    break;
                }
            }

        } catch (FileNotFoundException fe){
            for(int i=0; i<10; i++)
                highscores.add("---,0");
        }
        catch (IOException ie){}

        Intent data = getIntent();
        int action=data.getIntExtra("action",ACTION_DISPLAY);

        switch(action) {
            case ACTION_DISPLAY:
                setContentView(R.layout.activity_highscore_display);

                LinearLayout list = (LinearLayout) findViewById(R.id.highscores);

                for (int i = 0; i < 10; i++) {
                    LinearLayout hsContainer = (LinearLayout)list.getChildAt(i);
                    ((TextView)hsContainer.findViewById(R.id.hs_score)).setText(""+getScore(i));
                    ((TextView)hsContainer.findViewById(R.id.hs_name)).setText(getName(i));
                    ((TextView)hsContainer.findViewById(R.id.hs_index)).setText((i+1)+".)");
                }
                break;
            case ACTION_SAVE:
                setContentView(R.layout.activity_highscore_save);
                score = data.getIntExtra("score",0);

                ((TextView)findViewById(R.id.final_score)).setText("Your Score: "+score);

                ((TextView)findViewById(R.id.top_score)).setText("Top Score: "+getScore(0));
                for(int i=0; i<10; i++){
                    if(score>getScore(i)){
                        position=i;
                        break;
                    }
                }

                if(position<10){
                    findViewById(R.id.highscore_name).setVisibility(View.VISIBLE);
                    findViewById(R.id.save_score).setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    public void mainMenu(View v){
        finish();
    }

    public void replay(View v){
        Intent i = new Intent();
        i.setClass(this,GamePlayActivity.class);
        startActivity(i);
        finish();
    }

    public void save(View v) {
        addToHighscores();

        try{
            FileOutputStream out = openFileOutput("highscores", Context.MODE_PRIVATE);

            for(String line : highscores){
                out.write((line+"\n").getBytes());
            }
            out.close();
        }catch(FileNotFoundException fe){}
        catch(IOException ie){}

        Intent i = new Intent();
        i.setClass(this,HighscoreActivity.class);
        i.putExtra("action",ACTION_DISPLAY);
        startActivity(i);
        finish();
    }

    public void addToHighscores(){
        if(position>9) return;
        highscores.remove(9);
        highscores.add(position,((EditText)findViewById(R.id.highscore_name)).getText().toString()+","+score);
    }

    public String getName(int index){
        return highscores.get(index).split(",")[0];
    }

    public int getScore(int index){
        return Integer.valueOf(highscores.get(index).split(",")[1]);
    }
}
