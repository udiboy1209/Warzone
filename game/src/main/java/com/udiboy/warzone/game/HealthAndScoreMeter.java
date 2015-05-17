package com.udiboy.warzone.game;

import android.graphics.Canvas;
import android.graphics.Paint;

public class HealthAndScoreMeter {
    int screen_width, screen_height;
    float score = 0, health = 100, displayed_health = 100;
    int dodged=0;

    public HealthAndScoreMeter(){
    }

    public void setScreenDimensions(int screenWidth, int screenHeight){
        this.screen_width = screenWidth;
        this.screen_height = screenHeight;
    }

    public int getScore() {
        return Math.round(score);
    }

    public float getHealth() {
        return health;
    }

    public float getDisplayedHealth() {
        return displayed_health;
    }

    public void update(float health_incr, float score_incr, int dodged){
        health+=health_incr;

        if(health < 0) health = 0;

        if(health > 100) health = 100;

        score+=score_incr;
        score+=dodged*100;

        this.dodged+=dodged;

        if(displayed_health <Math.round(health))
            displayed_health++;
        else if(displayed_health > Math.round(health)){
            displayed_health--;
        }
    }

    public void draw(Canvas canvas){
        //Draw inner health meter rectangle
        Paint paint = new Paint();
        paint.setARGB(0xff, 0x99, 0xbb, 0xbb);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(screen_width *0.6f, screen_height *0.12f, screen_width *0.6f + getDisplayedHealth()* screen_width *0.003f, screen_height *0.17f, paint);

        //Draw outer border for meter
        paint.setARGB(0xff, 0x22, 0xbb, 0xbb);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((float) (0.005 * screen_width));
        canvas.drawRect(screen_width * 0.6f, screen_height * 0.12f, screen_width * 0.9f, screen_height * 0.17f, paint);

        //Draw score text
        paint.setARGB(255, 70, 200, 200);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(screen_height * 0.05f);
        canvas.drawText("Score: "+Math.round(score)+" Dodged: "+dodged, screen_width * 0.6f, screen_height * 0.1f, paint);


    }
}
