package com.udiboy.warzone.game;

public class Missile {
    public static final int STATE_FALLING = 0;
    public static final int STATE_EXPLODING = 1;
    public static final int STATE_BLINKING = 2;

    float x,y;
    float velocity = 2, // velocity -> pixels/update
            acceleration;

    int state;

    int explode_count=0;

    boolean blink_state =false;
    int max_blink_update_skips =6,
        blink_updates_skipped = max_blink_update_skips,
        blink_count =0;

    public int getX() {
        return Math.round(x);
    }

    public void incrementYPos() {
        this.y+=velocity;
        //Log.i("Missile","Missile "+this.hashCode()+" Y pos incremented by "+Math.round(velocity)+"px");
        velocity+=acceleration;
    }

    public int getY() {
        return Math.round(y);
    }

    public int getState() {
        return state;
    }

    public Missile(float x, float y, float acc){
        this.x = x;
        this.y = y;
        acceleration = acc;
        state=STATE_FALLING;
    }

    public void setState(int state){
        this.state = state;
    }
}
