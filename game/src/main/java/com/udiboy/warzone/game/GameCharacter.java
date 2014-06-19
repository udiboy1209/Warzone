package com.udiboy.warzone.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

public class GameCharacter {
    float targetX, x, maxX;
    int y, width,height,
            sprite_offset = 0;

    public static int NUM_FRAMES = 14,
            FRAME_REPEAT_NO = 2;
    boolean direction = true,//true is to right, false is to left
            moving = false;
    Bitmap runningBitmap, standingBitmap;

    public GameCharacter(Bitmap runningBitmap, Bitmap standingBitmap){
        this.runningBitmap = runningBitmap;
        this.standingBitmap = standingBitmap;
    }

    public void setDimensions(int height){
        this.height = height;
        int w = (FRAME_REPEAT_NO*runningBitmap.getWidth()*height)/(NUM_FRAMES*runningBitmap.getHeight());
        this.width = (int)w;
        Log.d("Character",""+w+"\n"+runningBitmap.getWidth()+"\n"+runningBitmap.getHeight());
    }

    public void setLocation(int x, int y){
        this.x = x; this.targetX=x;
        this.y = y;
    }

    public void draw(Canvas canvas){
        int temp_sprite_offset = sprite_offset/FRAME_REPEAT_NO; // truncate the quotient to int
        //Log.i("GameCharacter","temp sprite offset: "+temp_sprite_offset);
        if(moving){
            Matrix m = new Matrix();
            m.setScale(direction ? 1.0f : -1.0f, 1.0f);

            Bitmap croppedBitmap = Bitmap.createBitmap(runningBitmap,temp_sprite_offset * 44, 0, FRAME_REPEAT_NO*runningBitmap.getWidth()/NUM_FRAMES, runningBitmap.getHeight(),m,true);

            canvas.drawBitmap(Bitmap.createScaledBitmap(croppedBitmap, width, height, true),Math.round(x) ,y ,null);
        } else {
            canvas.drawBitmap(Bitmap.createScaledBitmap(standingBitmap, width, height, true), Math.round(x), y, null);
        }
    }

    public void moveDistance(float d){
        targetX+=d;
        x += Math.min(20,(targetX-x)*0.5);
        if(targetX<0)targetX=0; if(targetX>maxX)targetX=maxX;
        if(x<0)x=0; if(x>maxX)x=maxX;
        sprite_offset = (++sprite_offset) % NUM_FRAMES;

        direction = d>0;
        moving = (int)d!=0;
    }

    public Rect getRect() {
        return new Rect(Math.round(x),y,Math.round(x)+width, y+height);
    }

    public int getWidth() {
        return width;
    }
}

