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
    float[] explode_scale ={0.6f,1.1f,1.5f,1.8f,2.1f,2.3f, 2.5f};
    int[] explode_alpha = {255, 255, 220, 180, 120, 80, 20};

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

    public void incrementExplodeCount(){
        explode_count++;
    }

    public float getExplodeScale(){
        return explode_scale[explode_count];
    }

    public int getExplodeAlpha(){
        return explode_alpha[explode_count];
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
