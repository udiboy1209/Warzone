package com.udiboy.warzone.game;

import java.util.ArrayList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

public class MissileRenderer {
    Bitmap bitmap_fall, bitmap_blink, bitmap_explode;
    ArrayList<Missile> missilesFalling,missilesForCollisionCheck,missilesExploding;
    int width, height, width_explode, height_explode, screenWidth, screenHeight, charHeight;
    float maxMissilePerUpdate = 3f,
          missileCountUpdateRate = 0.05f,
          missileCollisionTolerance=0.2f;

    int updatesSkipped = 30,
        maxUpdateSkips = 30,
        missileRenderFieldStart,
        missileRenderFieldEnd,
        missileRenderFieldWidth,
        groundLevel;

    private static final float GRAVITY = 0.06f;// pixel/update^2

    public MissileRenderer(Bitmap bitmap_fall, Bitmap bitmap_blink, Bitmap bitmap_explode){
        this.bitmap_fall = bitmap_fall;
        this.bitmap_blink = bitmap_blink;
        this.bitmap_explode = bitmap_explode;
        missilesFalling = new ArrayList<Missile>();
        missilesForCollisionCheck = new ArrayList<Missile>();
        missilesExploding = new ArrayList<Missile>();

        //Log.i("Missile",bitmap_fall.getWidth()+"x"+ bitmap_fall.getHeight());
        //Log.i("Missile","<------- MissileRenderer Created ------->");
    }

    public void update(){
        if(updatesSkipped < maxUpdateSkips){
            updatesSkipped ++ ;
            //Log.v("Missile","-> Skipped adding missile. Skips: "+updatesSkipped);
        } else {
            //Log.w("Missile","<------ Adding missile start. ---->");
            int missilesRendered = 0, tries=0;

            while(missilesRendered < Math.round(maxMissilePerUpdate)){
                tries++;
                Missile newMissile = new Missile(missileRenderFieldStart + (float)Math.random() * (missileRenderFieldEnd - missileRenderFieldStart), - height - (float)(Math.random()*screenHeight*0.2),GRAVITY/*+(0.08*Math.random())-0.04*/);
                if(collidesWithMissile(newMissile)){
                    Log.e("Missile", "-> Missile not added due to collision");
                    if(tries<110) continue;
                    else break;
                }
                missilesFalling.add(newMissile);
                missilesRendered ++ ;
                Log.d("Missile","-> Missile added. Total added: "+missilesRendered);
            }

            maxMissilePerUpdate += missileCountUpdateRate;
            missileRenderFieldWidth=(screenWidth*Math.round(maxMissilePerUpdate))/10;
            if(maxMissilePerUpdate>10) {
                missileCountUpdateRate=0;
                maxMissilePerUpdate=10;
            }
            updatesSkipped=0;
            //Log.i("Missile","maxMissilePerUpdate: "+maxMissilePerUpdate+"\nmissileCountUpdateRate: "+missileCountUpdateRate+"\nTotal missilesFalling: "+missilesFalling.size());
            //Log.w("Missile","<------ Adding missile end. ------>");
        }

        //Log.w("Missile","<------ Missile position update start. ---->\nTotal missilesFalling: "+missilesFalling.size());
        for(int i=0; i< missilesFalling.size(); i++){
            Missile missileI = missilesFalling.get(i);

            if(missileI.getY()+height > groundLevel){
                missilesFalling.remove(i);
                i--;
                Log.d("Missile","Missile "+missileI.hashCode()+" removed");
            } else {
                missileI.incrementYPos();
                if(!missilesForCollisionCheck.contains(missileI) && Rect.intersects(new Rect(0,missileI.getY(),screenWidth,missileI.getY()+height), new Rect(0,groundLevel - charHeight,screenWidth, groundLevel))){
                    missilesForCollisionCheck.add(missileI);
                    //Log.w("Missile","Missile "+missileI.hashCode() +" added for collision check. Array size: "+missilesForCollisionCheck.size());
                }
            }
        }

        for(int i=0; i<missilesExploding.size(); i++){
            Missile missileI=missilesExploding.get(i);

            if(missileI.explode_count==6){
                missilesExploding.remove(i);
                i--;
            } else {
                missileI.incrementExplodeCount();
            }
        }
        //Log.w("Missile","<------ Missile position update end. ------>");
    }

    public void draw(Canvas canvas){
        for(Missile missile : missilesFalling){
            canvas.drawBitmap(bitmap_fall, missile.getX(), missile.getY(), null);
            //canvas.drawText(missile.hashCode()+"", (float) missile.getX(), (float) missile.getY(), new Paint(Paint.LINEAR_TEXT_FLAG));
        }
        Paint paint=new Paint();
        for(Missile missile: missilesExploding){
            paint.setAlpha(missile.getExplodeAlpha());
            int x=missile.getX()+width/2-(int)(width_explode*missile.getExplodeScale())/2,
                y=missile.getY()+2*height/3-(int)(height_explode*missile.getExplodeScale())/2;
            canvas.drawBitmap(Bitmap.createScaledBitmap(bitmap_explode,(int)(width_explode*missile.getExplodeScale()),(int)(height_explode*missile.getExplodeScale()),false), x, y, paint);
        }
    }

    public void setDimensions(int charHeight){
        this.width = (bitmap_fall.getWidth() * charHeight)/bitmap_fall.getHeight()/2;
        this.height = charHeight/2;
        this.charHeight=charHeight;
        this.width_explode=2*(bitmap_explode.getWidth() * charHeight)/bitmap_explode.getHeight()/3;
        this.height_explode = 2*charHeight/3;

        bitmap_fall=Bitmap.createScaledBitmap(bitmap_fall, width, height, false);
        //Log.d("Missile","dimen: "+width+"x"+height);
    }

    public void setScreenDimensions(int screenWidth, int screenHeight, int groundLevel){
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
        this.groundLevel=groundLevel;

        missileRenderFieldWidth=(screenWidth*Math.round(maxMissilePerUpdate))/10;
    }

    public void setMissileRenderField(float charX){
        missileRenderFieldStart = (int) (charX - missileRenderFieldWidth/2);
        missileRenderFieldEnd = (int) (charX + missileRenderFieldWidth/2 - width);

        if(missileRenderFieldStart < 0){
            missileRenderFieldEnd += Math.abs(missileRenderFieldStart);
            missileRenderFieldStart = 0;
        }

        if(missileRenderFieldEnd > screenWidth){
            missileRenderFieldStart -= missileRenderFieldEnd -screenWidth;
            missileRenderFieldEnd = screenWidth - width;
        }
    }

    public boolean collidesWithMissile(Missile m){
        Rect mRect = new Rect(m.getX() - (int)(width*missileCollisionTolerance), m.getY() - (int)(height*missileCollisionTolerance),(int) (m.getX()+(1+missileCollisionTolerance)*width), (int) (m.getY()+(1+missileCollisionTolerance)*height));
        for(Missile missile : missilesFalling){
            Rect missileRect = new Rect(missile.getX() - (int)(width*missileCollisionTolerance), missile.getY() - (int)(height*missileCollisionTolerance),(int) (missile.getX()+(1+missileCollisionTolerance)*width), (int) (missile.getY()+(1+missileCollisionTolerance)*height));
            if(Rect.intersects(mRect,  missileRect))
                return true;
        }
        return false;
    }

    public int checkCollisionWithCharacter(Rect rect) {
        int num = 0;
        for(int i = 0; i < missilesForCollisionCheck.size(); i++){
            Missile missile = missilesForCollisionCheck.get(i);
            Rect missileRect = new Rect(missile.getX(), missile.getY(), missile.getX()+width, missile.getY()+height);
            if(Rect.intersects(rect,  missileRect)){
                num++;
                missilesFalling.remove(missile);
                missile.setState(Missile.STATE_EXPLODING);
                missilesExploding.add(missile);
                missilesForCollisionCheck.remove(i);
                i--;
            }else if(missile.getY()+height > groundLevel){
                missilesForCollisionCheck.remove(i);
                missilesExploding.add(missile);
            }
        }


        return num;
    }

    public float getExplosionForce(float x) {
        float dist=0;
        for(Missile missile:missilesExploding){
            if(missile.explode_count>0) continue;
            float a=x-missile.getX()-width_explode*missile.getExplodeScale()/2;
            dist+=screenWidth/a/2;
        }
        if(Math.abs(dist) > screenWidth/2) dist = Math.signum(dist)*screenWidth/2;
        //Log.i("Missile","Dist : "+dist);
        return dist;
    }
}

