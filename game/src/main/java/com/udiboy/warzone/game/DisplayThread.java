package com.udiboy.warzone.game;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class DisplayThread extends Thread{
    boolean running;
    SurfaceHolder holder;
    DisplayPanel panel;

    //FPS variables
    public final static int MAX_FPS = 25;
    public final static int MAX_FRAME_SKIPS = 1;
    public final static int FRAME_PERIOD = 1000/MAX_FPS;

    public DisplayThread(SurfaceHolder holder, DisplayPanel panel) {
        this.holder = holder;
        this.panel = panel;
    }

    @Override
    public void run(){
        Canvas canvas;

        long beginTime;
        long timeDiff;
        int sleepTime;
        int framesSkipped;

        while(running){
            canvas = null;
            try{
                canvas = holder.lockCanvas();
                synchronized (holder){
                    beginTime = System.currentTimeMillis();
                    framesSkipped = 0;

                    panel.update();
                    panel.render(canvas);

                    timeDiff = System.currentTimeMillis() - beginTime;
                    sleepTime = (int) (FRAME_PERIOD - timeDiff);

                    if(sleepTime > 0){
                        try{
                            Thread.sleep(sleepTime);
                            Log.d("Thread","Sleeping for:"+sleepTime+" ms");
                        } catch(InterruptedException e){}
                    }

                    while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS){
                        Log.w("Thread","Skipped frame! sleepTime: "+sleepTime);
                        this.panel.update();
                        sleepTime += FRAME_PERIOD;
                        framesSkipped++;
                    }
                }
            } finally {
                if(canvas!=null)
                    holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public void setRunning(boolean running){
        this.running = running;
    }
}
