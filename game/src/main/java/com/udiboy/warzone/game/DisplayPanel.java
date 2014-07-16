package com.udiboy.warzone.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DisplayPanel extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener {
    DisplayThread thread;
    GameCharacter character;
    MissileRenderer missile_renderer;
    HealthAndScoreMeter health_and_score;
    SensorManager manager;
    Sensor accelerometer;
    Bitmap background;

    float character_movement = 0;

    int screen_height,
        screen_width,
        ground_level;

    int count_down_num = 3,
        count_down_interval = 20;
    float count_down_size = 54;

    float swipe_gesture_start=-1;

    public static final int STATE_GAME_COUNTDOWN = 3,
                            STATE_GAME_RUNNING = 0,
                            STATE_GAME_OVER = 1,
                            STATE_GAME_PAUSED = 2;
    int state;

    public DisplayPanel(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
        setWillNotDraw(false);

        manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        BitmapFactory.Options decodeOpts = new BitmapFactory.Options();
        decodeOpts.inDither = true;
        decodeOpts.inScaled = false;

        character = new GameCharacter(BitmapFactory.decodeResource(context.getResources(), R.drawable.stickman,decodeOpts),
                BitmapFactory.decodeResource(getResources(), R.drawable.stickman_standing,decodeOpts),
                BitmapFactory.decodeResource(getResources(), R.drawable.stickman_jumping,decodeOpts));
        missile_renderer = new MissileRenderer(BitmapFactory.decodeResource(getResources(), R.drawable.missile,decodeOpts),
                BitmapFactory.decodeResource(getResources(), R.drawable.missile_blink,decodeOpts),
                BitmapFactory.decodeResource(getResources(), R.drawable.missile_explode,decodeOpts));
        health_and_score = new HealthAndScoreMeter();

        thread = new DisplayThread(getHolder(),this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        screen_height = getHeight();
        screen_width = getWidth();
        ground_level =(screen_height *99)/100;
        int charHeight = (screen_height *2)/10;
        character.setDimensions(charHeight, ground_level);
        character.max_x = screen_width;

        character.setLocation(screen_width/2, ground_level);

        missile_renderer.setScreenDimensions(screen_width, screen_height, ground_level);
        missile_renderer.setDimensions(charHeight);

        health_and_score.setScreenDimensions(screen_width, screen_height);

        BitmapFactory.Options decodeOpts = new BitmapFactory.Options();
        decodeOpts.inDither = true;

        background = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.back, decodeOpts), getWidth(), getHeight(), true);

        thread.setRunning(true);
        state = STATE_GAME_COUNTDOWN;
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        Log.d("Panel","Destroying surface");
        try{
            thread.setRunning(false);
            thread.join();
        } catch(InterruptedException e){
            Log.d("Panel","Caught InterruptedException while destroying surface : "+e.getMessage());
        }
        manager.unregisterListener(this);
    }

    public void update(){
        if(health_and_score.getHealth() <= 0)
            state = STATE_GAME_OVER;

        switch(state){
            case STATE_GAME_COUNTDOWN :
                if(count_down_num > 0){
                    if(count_down_interval > 0)
                        count_down_interval--;
                    else {
                        count_down_interval = 20;
                        count_down_size = 54;
                        count_down_num--;
                    }
                } else
                    state=STATE_GAME_RUNNING;
                break;
            case STATE_GAME_RUNNING :
                character.moveDistance(character_movement * 1.8f + missile_renderer.getExplosionForce(character.x));

                missile_renderer.setMissileRenderField(character.x + character.getWidth() / 2);
                missile_renderer.update();

                health_and_score.update(missile_renderer.checkCollisionWithCharacter(character.getRect()) * -25 + 0.01f, 4 * Math.abs(character_movement) + 1);

                character_movement =0;
                break;
            case STATE_GAME_OVER : thread.setRunning(false);
                break;
        }
    }

    public void render(Canvas canvas){
        if(canvas==null) return;

        canvas.drawBitmap(background,0,0,null);
        character.draw(canvas);

        if(state != STATE_GAME_COUNTDOWN)
            missile_renderer.draw(canvas);

        health_and_score.draw(canvas);

        if(state == STATE_GAME_COUNTDOWN && count_down_num >0) {
            Paint p = new Paint();
            p.setColor(0xff77a0a0);
            p.setTextSize(count_down_size);
            p.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(count_down_num +"", screen_width /2, screen_height /2, p);
            count_down_size--;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) { }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() != Sensor.TYPE_ACCELEROMETER || state != STATE_GAME_RUNNING)
            return;
        character_movement += event.values[SensorManager.DATA_Y];
    }

    @Override
    public boolean onTouchEvent(MotionEvent me){
        switch(me.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                swipe_gesture_start=me.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                return swipe_gesture_start>=0;
            case MotionEvent.ACTION_UP:
                if(Math.abs(swipe_gesture_start-me.getY())>screen_height/5){
                    if(swipe_gesture_start>me.getY()){
                        character.jump();
                    }
                }
                break;
            default:
                return false;
        }
        return true;
    }
}
