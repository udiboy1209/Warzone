package com.udiboy.warzone.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

public class GameCharacter {
    float target_x, x, max_x;
    int y, width,height,
            sprite_offset = 0;

    public static int NUM_FRAMES = 14,
            FRAME_REPEAT_NO = 2;
    boolean direction = true,//true is to right, false is to left
            moving = false;
    Bitmap running_bitmap, standing_bitmap;

    public GameCharacter(Bitmap running_bitmap, Bitmap standing_bitmap){
        this.running_bitmap = running_bitmap;
        this.standing_bitmap = standing_bitmap;
    }

    public void setDimensions(int height){
        this.height = height;
        this.width = (FRAME_REPEAT_NO* running_bitmap.getWidth()*height)/(NUM_FRAMES* running_bitmap.getHeight());
        Log.d("Character",""+this.width+"\n"+ running_bitmap.getWidth()+"\n"+ running_bitmap.getHeight());
    }

    public void setLocation(int x, int y){
        this.x = x; this.target_x =x;
        this.y = y;
    }

    public void draw(Canvas canvas){
        int temp_sprite_offset = sprite_offset/FRAME_REPEAT_NO; // truncate the quotient to int
        //Log.i("GameCharacter","temp sprite offset: "+temp_sprite_offset);
        if(moving){
            Matrix m = new Matrix();
            m.setScale(direction ? 1.0f : -1.0f, 1.0f);

            Bitmap cropped_bitmap = Bitmap.createBitmap(running_bitmap,temp_sprite_offset * 44, 0, FRAME_REPEAT_NO* running_bitmap.getWidth()/NUM_FRAMES, running_bitmap.getHeight(),m,true);

            canvas.drawBitmap(Bitmap.createScaledBitmap(cropped_bitmap, width, height, true),Math.round(x) ,y ,null);
        } else {
            canvas.drawBitmap(Bitmap.createScaledBitmap(standing_bitmap, width, height, true), Math.round(x), y, null);
        }
    }

    public void moveDistance(float d){
        target_x +=d;
        x += Math.min(20,(target_x -x)*0.5);
        if(target_x <0) target_x =0; if(target_x > max_x) target_x = max_x;
        if(x<0)x=0; if(x> max_x)x= max_x;
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

