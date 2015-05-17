package com.udiboy.warzone.game;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

public class DisplayPanel extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener {
    DisplayThread thread;
    GameCharacter character;
    MissileRenderer missile_renderer;
    HealthAndScoreMeter health_and_score;
    SensorManager manager;
    Sensor accelerometer;
    Bitmap background, button_pause, button_music_on, button_music_off;
    View game_paused;
    TextView final_score, current_score;
    Rect pause_button_rect, music_button_rect;

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

    public static final int ACTION_QUIT = 0,
                            ACTION_PAUSE = 1;

    int state,
        exit_action=ACTION_QUIT;

    boolean play_music=true;

    public DisplayPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
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
                BitmapFactory.decodeResource(getResources(), R.drawable.stickman_jumping,decodeOpts),
                BitmapFactory.decodeResource(getResources(), R.drawable.stickman_sliding,decodeOpts));
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

        if(exit_action == ACTION_QUIT){
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
            button_pause = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.button_pause, decodeOpts), getHeight()/8, getHeight()/8, true);
            button_music_on = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.button_music_on, decodeOpts), getHeight()/8, getHeight()/8, true);
            button_music_off = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.button_music_off, decodeOpts), getHeight()/8, getHeight()/8, true);

            pause_button_rect = new Rect(0,0,button_pause.getWidth(),button_pause.getHeight());
            music_button_rect = new Rect(button_pause.getWidth(),0,button_pause.getWidth()+button_music_on.getWidth(),button_music_on.getHeight());

            thread.setPaused(false);
            thread.setRunning(true);
            state = STATE_GAME_COUNTDOWN;
            thread.start();
        } else {
            Canvas canvas = getHolder().lockCanvas();
            synchronized (getHolder()){
                render(canvas);
            }
            getHolder().unlockCanvasAndPost(canvas);
            state = STATE_GAME_PAUSED;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        Log.d("Panel","Destroying surface");
        Log.d("Panel","exit_action = "+(exit_action==ACTION_PAUSE?"pause":"quit"));
        try{
            if(exit_action == ACTION_QUIT){
                synchronized (thread){
                    thread.setPaused(false);
                    thread.notify();
                }
                thread.setRunning(false);
                thread.join();
            } else {
                synchronized (thread){
                    thread.setPaused(true);
                    game_paused.post(new Runnable() {
                        @Override
                        public void run() {
                            current_score.setText("Your Score: "+health_and_score.getScore());
                            game_paused.setVisibility(View.VISIBLE);
                        }
                    });
                    state=STATE_GAME_PAUSED;
                }
            }
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

                health_and_score.update(missile_renderer.checkCollisionWithCharacter(character) * -25 + 0.01f, 4 * Math.abs(character_movement) + 1, missile_renderer.numMissilesDodged(character));

                character_movement = 0;
                break;
            case STATE_GAME_OVER :
                game_paused.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent i=new Intent();
                        i.putExtra("score",health_and_score.getScore());
                        i.setClass(getContext(),HighscoreActivity.class);
                        i.putExtra("action",HighscoreActivity.ACTION_SAVE);
                        getContext().startActivity(i);
                        ((GamePlayActivity)getContext()).finish();
                    }
                });
                thread.setRunning(false);
                break;
        }
    }

    public void render(Canvas canvas){
        if(canvas==null) return;

        canvas.drawBitmap(background,0,0,null);
        canvas.drawBitmap(button_pause,0,0, null);
        canvas.drawBitmap(play_music?button_music_on:button_music_off,music_button_rect.left,music_button_rect.top, null);

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
                    } else {
                        character.slide();
                    }
                } else if(state==STATE_GAME_RUNNING ){
                    if(pause_button_rect.contains(Math.round(me.getX()), Math.round(me.getY()))) {
                        synchronized (thread) {
                            thread.setPaused(true);
                            game_paused.post(new Runnable() {
                                @Override
                                public void run() {
                                    current_score.setText("Your Score: " + health_and_score.getScore());
                                    game_paused.setVisibility(View.VISIBLE);
                                }
                            });
                            state = STATE_GAME_PAUSED;
                        }
                    } else if(music_button_rect.contains(Math.round(me.getX()), Math.round(me.getY()))){
                        play_music=!play_music;
                        Log.d("Panel","Music button pressed");
                        game_paused.post(new Runnable() {
                            @Override
                            public void run() {
                                ((GamePlayActivity)getContext()).bg_music.setVolume(play_music?1.0f:0.0f,play_music?1.0f:0.0f);
                                ((GamePlayActivity)getContext()).end_music.setVolume(play_music?1.0f:0.0f,play_music?1.0f:0.0f);
                            }
                        });
                    }
                }
                break;
            default:
                return false;
        }
        return true;
    }

    public void setViews(View fs, View cs, View gpl){
        final_score=(TextView)fs;
        current_score=(TextView)cs;
        game_paused=gpl;
    }
}
