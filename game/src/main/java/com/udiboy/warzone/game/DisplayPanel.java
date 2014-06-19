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
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DisplayPanel extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener {
    DisplayThread thread;
    GameCharacter character;
    MissileRenderer missileRenderer;
    HealthAndScoreMeter healthAndScore;
    SensorManager manager;
    Sensor accelerometer;
    Bitmap background;

    float characterMovement = 0;

    int screenHeight,
        screenWidth,
        groundLevel;

    int countDownNum = 3,
        countDownInterval = 20;
    float countDownSize = 54;

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

        character = new GameCharacter(BitmapFactory.decodeResource(context.getResources(), R.drawable.stickman,decodeOpts), BitmapFactory.decodeResource(getResources(), R.drawable.stickman_standing,decodeOpts));
        missileRenderer = new MissileRenderer(BitmapFactory.decodeResource(getResources(), R.drawable.missile,decodeOpts),
                BitmapFactory.decodeResource(getResources(), R.drawable.missile_blink,decodeOpts),
                BitmapFactory.decodeResource(getResources(), R.drawable.missile_explode,decodeOpts));
        healthAndScore = new HealthAndScoreMeter();

        thread = new DisplayThread(getHolder(),this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        screenHeight = getHeight();
        screenWidth = getWidth();
        groundLevel=(screenHeight*99)/100;
        int charHeight = (screenHeight*2)/10;
        character.setDimensions(charHeight);
        character.maxX=screenWidth-character.width;

        int charWidth = character.getWidth();
        character.setLocation(screenWidth/2-charWidth/2,groundLevel-charHeight);

        missileRenderer.setScreenDimensions(screenWidth, screenHeight, groundLevel);
        missileRenderer.setDimensions(charHeight);

        healthAndScore.setScreenDimensions(screenWidth, screenHeight);

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
        if(healthAndScore.getHealth() <= 0)
            state = STATE_GAME_OVER;

        switch(state){
            case STATE_GAME_COUNTDOWN :
                if(countDownNum > 0){
                    if(countDownInterval > 0)
                        countDownInterval--;
                    else {
                        countDownInterval = 20;
                        countDownSize = 54;
                        countDownNum --;
                    }
                } else
                    state=STATE_GAME_RUNNING;
                break;
            case STATE_GAME_RUNNING :
                character.moveDistance(characterMovement*1.8f+missileRenderer.getExplosionForce(character.x));

                missileRenderer.setMissileRenderField(character.x + character.width / 2);
                missileRenderer.update();

                healthAndScore.update(missileRenderer.checkCollisionWithCharacter(character.getRect())*-25 + 0.01f, 4*Math.abs(characterMovement) + 1);

                characterMovement=0;
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
            missileRenderer.draw(canvas);

        healthAndScore.draw(canvas);

        if(state == STATE_GAME_COUNTDOWN && countDownNum>0) {
            Paint p = new Paint();
            p.setColor(0xff77a0a0);
            p.setTextSize(countDownSize);
            p.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(countDownNum+"", screenWidth/2, screenHeight/2, p);
            countDownSize--;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) { }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() != Sensor.TYPE_ACCELEROMETER || state != STATE_GAME_RUNNING)
            return;
        characterMovement += event.values[SensorManager.DATA_Y];
    }
}
