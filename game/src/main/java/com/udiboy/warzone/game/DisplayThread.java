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

        long begin_time;
        long time_diff;
        int sleep_time;
        int frames_skipped;

        while(running){
            canvas = null;
            try{
                canvas = holder.lockCanvas();
                synchronized (holder){
                    begin_time = System.currentTimeMillis();
                    frames_skipped = 0;

                    panel.update();
                    panel.render(canvas);

                    time_diff = System.currentTimeMillis() - begin_time;
                    sleep_time = (int) (FRAME_PERIOD - time_diff);

                    if(sleep_time > 0){
                        try{
                            Thread.sleep(sleep_time);
                            Log.d("Thread","Sleeping for:"+sleep_time+" ms");
                        } catch(InterruptedException e){}
                    }

                    while (sleep_time < 0 && frames_skipped < MAX_FRAME_SKIPS){
                        Log.w("Thread","Skipped frame! sleepTime: "+sleep_time);
                        this.panel.update();
                        sleep_time += FRAME_PERIOD;
                        frames_skipped++;
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
