package com.udiboy.warzone.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

public class GameCharacter {
    public static final int STATE_JUMPING = 0;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_STANDING = 2;
    public static final int STATE_SLIDING = 3;

    float target_x,
        x,
        max_x,
        y,
        velocity_y,

        GRAVITY = 1.5f,
        INITIAL_VELOCITY;

    int ground_level,
        sprite_offset = 0,
        state = STATE_STANDING,

        max_sliding_update_skips=15,
        sliding_updates_skipped=0;

    int[] width = new int[4],
        height = new int[4];//size should match no. of states

    public static int NUM_FRAMES = 9;
            //FRAME_REPEAT_NO = 1;
    int frame_width=0;
    boolean direction = true;//true is to right, false is to left
    Bitmap running_bitmap, standing_bitmap, jumping_bitmap, sliding_bitmap;

    public GameCharacter(Bitmap running_bitmap, Bitmap standing_bitmap, Bitmap jumping_bitmap, Bitmap sliding_bitmap){
        this.running_bitmap = running_bitmap;
        this.standing_bitmap = standing_bitmap;
        this.jumping_bitmap = jumping_bitmap;
        this.sliding_bitmap = sliding_bitmap;
    }

    public void setDimensions(int height_standing, int ground_level){
        height[STATE_STANDING] = height_standing;
        float height_scale_factor = (float)height_standing/standing_bitmap.getHeight();
        height[STATE_RUNNING] = (int)(running_bitmap.getHeight()*height_scale_factor);
        height[STATE_JUMPING] = (int)(jumping_bitmap.getHeight()*height_scale_factor);
        height[STATE_SLIDING] = (int)(sliding_bitmap.getHeight()*height_scale_factor);

        width[STATE_RUNNING] = (running_bitmap.getWidth()*height[STATE_RUNNING])/(NUM_FRAMES* running_bitmap.getHeight());
        width[STATE_STANDING] = (standing_bitmap.getWidth()*height[STATE_STANDING])/standing_bitmap.getHeight();
        width[STATE_JUMPING] = (jumping_bitmap.getWidth()*height[STATE_JUMPING])/jumping_bitmap.getHeight();
        width[STATE_SLIDING] = (sliding_bitmap.getWidth()*height[STATE_SLIDING])/sliding_bitmap.getHeight();

        frame_width = running_bitmap.getWidth()/NUM_FRAMES;
        this.ground_level=ground_level;
        INITIAL_VELOCITY = -(float)Math.sqrt((double)2*GRAVITY*height[STATE_STANDING]);
    }

    public void setLocation(int x, int y){
        this.x = x; this.target_x =x;
        this.y = y;
    }

    public void draw(Canvas canvas){
        //Log.i("GameCharacter","temp sprite offset: "+temp_sprite_offset);
        Matrix m = new Matrix();
        Bitmap flipped_bitmap;
        m.setScale(direction ? 1.0f : -1.0f, 1.0f);

        switch(state){
            case STATE_RUNNING:
                Bitmap cropped_bitmap = Bitmap.createBitmap(running_bitmap,sprite_offset * frame_width, 0, frame_width, running_bitmap.getHeight(),m,true);

                canvas.drawBitmap(Bitmap.createScaledBitmap(cropped_bitmap, getWidth(), getHeight(), true),Math.round(x)-getWidth()/2 ,Math.round(y)-getHeight() ,null);
                break;
            case STATE_STANDING:
                canvas.drawBitmap(Bitmap.createScaledBitmap(standing_bitmap, getWidth(), getHeight(), true), Math.round(x)-getWidth()/2, Math.round(y)-getHeight(), null);
                break;
            case STATE_JUMPING:
                flipped_bitmap = Bitmap.createBitmap(jumping_bitmap,0,0,jumping_bitmap.getWidth(),jumping_bitmap.getHeight(),m,true);

                canvas.drawBitmap(Bitmap.createScaledBitmap(flipped_bitmap, getWidth(), getHeight(), true), Math.round(x) - getWidth() / 2, Math.round(y)-getHeight(), null);
                break;
            case STATE_SLIDING:
                flipped_bitmap = Bitmap.createBitmap(sliding_bitmap,0,0,sliding_bitmap.getWidth(),sliding_bitmap.getHeight(),m,true);

                canvas.drawBitmap(Bitmap.createScaledBitmap(flipped_bitmap, getWidth(), getHeight(), true), Math.round(x) - getWidth() / 2, Math.round(y)-getHeight(), null);
                break;
        }
    }

    public void moveDistance(float d){
        target_x +=d;
        x += Math.min(20,(target_x -x)*0.5);
        if(target_x <0) target_x =0; if(target_x > max_x) target_x = max_x;
        if(x<0)x=0; if(x> max_x)x= max_x;
        sprite_offset = (++sprite_offset) % NUM_FRAMES;

        if(state==STATE_JUMPING){
            y+=velocity_y;
            velocity_y+=GRAVITY;

            if(Math.round(y) >= ground_level){
                state=STATE_RUNNING;
                y=ground_level;
            }
        } else if(state==STATE_SLIDING){
            if(sliding_updates_skipped < max_sliding_update_skips)
                sliding_updates_skipped++;
            else {
                state=STATE_RUNNING;
                sliding_updates_skipped=0;
            }
        }

        direction = d>0;
        if(state!=STATE_JUMPING && state!=STATE_SLIDING) state = (int)d!=0?STATE_RUNNING:STATE_STANDING;
    }

    public void jump(){
        if(state != STATE_JUMPING){
            state = STATE_JUMPING;
            velocity_y = INITIAL_VELOCITY;
        }
    }

    public void slide(){
        if(state != STATE_JUMPING){
            state=STATE_SLIDING;
        }
    }

    public Rect getRect() {
        return new Rect(Math.round(x)-getWidth()/2,Math.round(y)-getHeight(),Math.round(x)+getWidth()/2, Math.round(y));
    }

    public int getWidth(){
        return width[state];
    }

    public int getHeight(){
        return height[state];
    }
}

