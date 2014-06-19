package com.udiboy.warzone.game;

import android.graphics.Canvas;
import android.graphics.Paint;

public class HealthAndScoreMeter {
    int screenWidth, screenHeight;
    float score = 0, health = 100, displayedHealth = 100;

    public HealthAndScoreMeter(){
    }

    public void setScreenDimensions(int screenWidth, int screenHeight){
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public float getScore() {
        return score;
    }

    public float getHealth() {
        return health;
    }

    public float getDisplayedHealth() {
        return displayedHealth;
    }

    public void update(float healthIncr, float scoreIncr){
        health+=healthIncr;

        if(health < 0) health = 0;

        if(health > 100) health = 100;

        score+=scoreIncr;

        if(displayedHealth<Math.round(health))
            displayedHealth++;
        else if(displayedHealth > Math.round(health)){
            displayedHealth--;
        }
    }

    public void draw(Canvas canvas){
        //Draw inner health meter rectangle
        Paint paint = new Paint();
        paint.setARGB(0xff, 0x99, 0xbb, 0xbb);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(screenWidth*0.6f, screenHeight*0.1f, screenWidth*0.6f + getDisplayedHealth()*screenWidth*0.003f, screenHeight*0.15f, paint);

        //Draw outer border for meter
        paint.setARGB(0xff, 0x22, 0xbb, 0xbb);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((float) (0.005 * screenWidth));
        canvas.drawRect(screenWidth*0.6f, screenHeight*0.1f, screenWidth*0.9f, screenHeight*0.15f, paint);

        //Draw score text
        paint.setARGB(255, 70, 200, 200);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText(String.valueOf(Math.round(score)), screenWidth * 0.1f, screenHeight * 0.1f, paint);
    }
}
