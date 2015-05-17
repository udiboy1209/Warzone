package com.udiboy.warzone.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;

public class MissileRenderer {
    Bitmap bitmap_fall, bitmap_blink, bitmap_explode;
    ArrayList<Missile> missiles;
    int width, height, width_explode, height_explode, screen_width, screen_height, char_height;
    public static int NUM_EXPLODE_FRAMES=18;
    float max_missile_per_update = 1f,
          missile_count_update_rate = 0.08f,
          missile_collision_tolerance =0.2f;

    int updates_skipped = 50,//30,
        max_update_skips = 50,//30,
        missile_render_field_start,
        missile_render_field_end,
        missile_render_field_width,
        ground_level;

    int num_missiles_dodging=0;

    private static final float GRAVITY = 0.09f;// pixel/update^2

    public MissileRenderer(Bitmap bitmap_fall, Bitmap bitmap_blink, Bitmap bitmap_explode){
        this.bitmap_fall = bitmap_fall;
        this.bitmap_blink = bitmap_blink;
        this.bitmap_explode = bitmap_explode;
        missiles = new ArrayList<Missile>();

        //Log.i("Missile",bitmap_fall.getWidth()+"x"+ bitmap_fall.getHeight());
        //Log.i("Missile","<------- MissileRenderer Created ------->");
    }

    public void update(){
        if(updates_skipped < max_update_skips){
            updates_skipped++ ;
            //Log.v("Missile","-> Skipped adding missile. Skips: "+updates_skipped);
        } else {
            //Log.w("Missile","<------ Adding missile start. ---->");
            int missilesRendered = 0, tries=0;

            while(missilesRendered < Math.round(max_missile_per_update)){
                tries++;
                Missile newMissile = new Missile(missile_render_field_start + (float)Math.random() * (missile_render_field_end - missile_render_field_start), - height - (float)(Math.random()* screen_height *0.2),GRAVITY/*+(0.08*Math.random())-0.04*/);
                if(collidesWithMissile(newMissile)){
                    Log.e("Missile", "-> Missile not added due to collision");
                    if(tries<110) continue;
                    else break;
                }
                missiles.add(newMissile);
                missilesRendered ++ ;
                Log.d("Missile","-> Missile added. Total added: "+missilesRendered);
            }

            max_missile_per_update += missile_count_update_rate;
            missile_render_field_width =(screen_width *Math.round(max_missile_per_update))/10;
            if(max_missile_per_update >10) {
                missile_count_update_rate =0;
                max_missile_per_update =10;
            }
            updates_skipped =0;
            //Log.i("Missile","max_missile_per_update: "+max_missile_per_update+"\nmissile_count_update_rate: "+missile_count_update_rate+"\nTotal missilesFalling: "+missilesFalling.size());
            //Log.w("Missile","<------ Adding missile end. ------>");
        }

        //Log.w("Missile","<------ Missile position update start. ---->\nTotal missilesFalling: "+missilesFalling.size());
        for(int i=0; i< missiles.size(); i++){
            Missile missileI = missiles.get(i);
            switch (missileI.getState()){
                case Missile.STATE_FALLING :
                    if(missileI.getY()+height < ground_level){
                        missileI.incrementYPos();
                    } else {
                        missileI.setState(Missile.STATE_BLINKING);
                    }
                    break;
                case Missile.STATE_BLINKING:
                    if(missileI.blink_updates_skipped < missileI.max_blink_update_skips){
                        missileI.blink_updates_skipped++;
                    } else {
                        missileI.blink_state = !missileI.blink_state;
                        if(!missileI.blink_state) missileI.blink_count++;

                        if (missileI.blink_count == 3){
                            missileI.setState(Missile.STATE_EXPLODING);
                        }

                        missileI.blink_updates_skipped =0;
                    }
                    break;
                case Missile.STATE_EXPLODING:
                    if(missileI.explode_count==NUM_EXPLODE_FRAMES-1){
                        missiles.remove(i);
                        i--;
                    } else {
                        missileI.explode_count++;
                    }
                    break;
            }
        }
        //Log.w("Missile","<------ Missile position update end. ------>");
    }

    public void draw(Canvas canvas){
        for(Missile missile : missiles){
            switch(missile.getState()){
            case Missile.STATE_FALLING:
                canvas.drawBitmap(bitmap_fall, missile.getX(), missile.getY(), null);
                break;
            case Missile.STATE_BLINKING:
                canvas.drawBitmap(missile.blink_state ? bitmap_blink : bitmap_fall, missile.getX(), missile.getY(), null);
                break;
            case Missile.STATE_EXPLODING:
                int x=missile.getX()+(width-width_explode)/2,
                    y=missile.getY()+2*height/3-height_explode/2;
                Bitmap cropped_bitmap = Bitmap.createBitmap(bitmap_explode,missile.explode_count * width_explode, 0, width_explode, height_explode,null,true);
                canvas.drawBitmap(cropped_bitmap, x, y, null);
                cropped_bitmap.recycle();
                break;
            }
        }
    }

    public void setDimensions(int char_height){
        this.width = (bitmap_fall.getWidth() * char_height)/bitmap_fall.getHeight()/2;
        this.height = char_height/2;
        this.char_height =char_height;
        this.height_explode = 2*char_height/3;
        this.width_explode = this.height_explode*bitmap_explode.getWidth()/NUM_EXPLODE_FRAMES/bitmap_explode.getHeight();
        bitmap_fall=Bitmap.createScaledBitmap(bitmap_fall, width, height, false);
        bitmap_blink=Bitmap.createScaledBitmap(bitmap_blink, width, height, false);
        bitmap_explode=Bitmap.createScaledBitmap(bitmap_explode,width_explode*NUM_EXPLODE_FRAMES, height_explode, true);
        //Log.d("Missile","dimen: "+width+"x"+height);
    }

    public void setScreenDimensions(int screen_width, int screen_height, int ground_level){
        this.screen_height = screen_height;
        this.screen_width = screen_width;
        this.ground_level =ground_level;

        missile_render_field_width =(screen_width*Math.round(max_missile_per_update))/10;
    }

    public void setMissileRenderField(float char_x){
        missile_render_field_start = (int) (char_x - missile_render_field_width /2);
        missile_render_field_end = (int) (char_x + missile_render_field_width /2 - width);

        if(missile_render_field_start < 0){
            missile_render_field_end += Math.abs(missile_render_field_start);
            missile_render_field_start = 0;
        }

        if(missile_render_field_end > screen_width){
            missile_render_field_start -= missile_render_field_end - screen_width;
            missile_render_field_end = screen_width - width;
        }
    }

    public boolean collidesWithMissile(Missile m){
        Rect m_rect = new Rect(m.getX() - (int)(width* missile_collision_tolerance), m.getY() - (int)(height* missile_collision_tolerance),(int) (m.getX()+(1+ missile_collision_tolerance)*width), (int) (m.getY()+(1+ missile_collision_tolerance)*height));
        for(Missile missile : missiles){
            if(missile.getState() == Missile.STATE_FALLING) {
                Rect missile_rect = new Rect(missile.getX() - (int)(width* missile_collision_tolerance), missile.getY() - (int)(height* missile_collision_tolerance),(int) (missile.getX()+(1+ missile_collision_tolerance)*width), (int) (missile.getY()+(1+ missile_collision_tolerance)*height));
                if(Rect.intersects(m_rect,  missile_rect))
                    return true;
            }
        }
        return false;
    }

    public int checkCollisionWithCharacter(GameCharacter character) {
        int num = 0;
        for(Missile missile : missiles){
            if(missile.state==Missile.STATE_EXPLODING) continue;

            Rect missile_rect = new Rect(missile.getX(), missile.getY(), missile.getX()+width, missile.getY()+height);
            if(Rect.intersects(character.getRect(),  missile_rect)){
                num++;
                missile.setState(Missile.STATE_EXPLODING);
            }
        }

        return num;
    }

    public int numMissilesDodged(GameCharacter character){
        int num=0;

        for(Missile  missile : missiles){
            if(missile.getState() == Missile.STATE_EXPLODING) continue;

            if(missile.y < screen_height*2/3) continue;

            if(missile.x>(character.x-character.getWidth()/2) && character.x < (character.x+character.getWidth()/2)){
                num++;
            }
        }

        int num_dodged = num_missiles_dodging-num;

        num_missiles_dodging = num;

        return (num_dodged>0?num_dodged:0);
    }

    public float getExplosionForce(float x) {
        float dist=0;
        for(Missile missile:missiles){
            if(missile.getState() == Missile.STATE_EXPLODING){
                if(missile.explode_count==0) {
                    float a=x-missile.getX()-width_explode/2;
                    dist+= screen_width /a/2;
                }
            }
        }
        if(Math.abs(dist) > screen_width /2) dist = Math.signum(dist)* screen_width /2;
        return dist;
    }
}