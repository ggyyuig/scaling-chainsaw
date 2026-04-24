package com.example.tpshooter;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import java.util.*;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(new GameView(this));
    }

    static class GameView extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Random r = new Random();

        float playerX = 0;
        float playerZ = 0;
        float angle = 0;

        float joyX = 0;
        float joyY = 0;
        boolean moving = false;

        int score = 0;
        int hp = 100;

        ArrayList<Enemy> enemies = new ArrayList<>();
        ArrayList<Bullet> bullets = new ArrayList<>();

        long lastTime = System.currentTimeMillis();

        GameView(Activity a) {
            super(a);
            setFocusable(true);

            for (int i = 0; i < 8; i++) {
                enemies.add(new Enemy(r.nextFloat() * 1200 - 600, r.nextFloat() * 1200 - 600));
            }
        }
@Override
        protected void onDraw(Canvas c) {
            long now = System.currentTimeMillis();
            float dt = (now - lastTime) / 1000f;
            lastTime = now;

            update(dt);
            drawGame(c);

            invalidate();
        }

        void update(float dt) {
            float speed = 260f;

            if (moving) {
                float sin = (float) Math.sin(angle);
                float cos = (float) Math.cos(angle);

                playerX += (joyX * cos + joyY * sin) * speed * dt;
                playerZ += (joyY * cos - joyX * sin) * speed * dt;
            }

            for (Enemy e : enemies) {
                float dx = playerX - e.x;
                float dz = playerZ - e.z;
                float d = (float) Math.sqrt(dx * dx + dz * dz);

                if (d > 1) {
                    e.x += dx / d * 90f * dt;
                    e.z += dz / d * 90f * dt;
                }

                if (d < 45) {
                    hp -= 1;
                    if (hp < 0) hp = 0;
                }
            }
for (int i = bullets.size() - 1; i >= 0; i--) {
                Bullet b = bullets.get(i);
                b.x += Math.sin(b.a) * 600f * dt;
                b.z += Math.cos(b.a) * 600f * dt;
                b.life -= dt;

                if (b.life <= 0) {
                    bullets.remove(i);
                    continue;
                }

                for (Enemy e : enemies) {
                    float dx = b.x - e.x;
                    float dz = b.z - e.z;
                    float d = (float) Math.sqrt(dx * dx + dz * dz);

                    if (d < 35) {
                        e.x = r.nextFloat() * 1400 - 700;
                        e.z = r.nextFloat() * 1400 - 700;
                        bullets.remove(i);
                        score++;
                        break;
                    }
                }
            }
        }

        void drawGame(Canvas c) {
            int w = getWidth();
            int h = getHeight();

            c.drawColor(Color.rgb(80, 160, 230));

            p.setColor(Color.rgb(65, 170, 80));
            c.drawRect(0, h / 2f, w, h, p);

            p.setColor(Color.rgb(45, 120, 60));
for (int i = 0; i < 16; i++) {
                float y = h / 2f + i * 45;
                c.drawLine(0, y, w, y + 120, p);
            }

            drawWorldObject(c, playerX, playerZ, Color.BLUE, 55, true);

            for (Enemy e : enemies) {
                drawWorldObject(c, e.x, e.z, Color.RED, 45, false);
            }

            for (Bullet b : bullets) {
                drawWorldObject(c, b.x, b.z, Color.YELLOW, 18, false);
            }

            drawUI(c);
        }

        void drawWorldObject(Canvas c, float x, float z, int color, float size, boolean player) {
            int w = getWidth();
            int h = getHeight();

            float dx = x - playerX;
            float dz = z - playerZ;

            float sin = (float) Math.sin(-angle);
            float cos = (float) Math.cos(-angle);
float rx = dx * cos - dz * sin;
            float rz = dx * sin + dz * cos + 420;

            if (rz < 40) return;

            float scale = 600f / rz;
            float sx = w / 2f + rx * scale;
            float sy = h * 0.72f - scale * 120;

            float s = size * scale;

            p.setColor(color);

            if (player) {
                c.drawCircle(w / 2f, h * 0.68f, 42, p);

                p.setColor(Color.BLACK);
                p.setStrokeWidth(10);
                c.drawLine(w / 2f, h * 0.68f, w / 2f, h * 0.57f, p);
            } else {
                c.drawRect(sx - s, sy - s, sx + s, sy + s, p);
            }
        }

        void drawUI(Canvas c) {
            int w = getWidth();
            int h = getHeight();

            p.setTextSize(42);
            p.setColor(Color.WHITE);
            c.drawText("HP: " + hp, 40, 60, p);
            c.drawText("Score: " + score, 40, 115, p);
p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(5);
            p.setColor(Color.WHITE);
            c.drawCircle(135, h - 135, 95, p);

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(180, 255, 255, 255));
            c.drawCircle(135 + joyX * 55, h - 135 + joyY * 55, 35, p);

            p.setColor(Color.argb(180, 255, 80, 80));
            c.drawCircle(w - 140, h - 120, 75, p);

            p.setColor(Color.WHITE);
            p.setTextSize(34);
            c.drawText("FIRE", w - 183, h - 108, p);

            p.setColor(Color.WHITE);
            p.setStrokeWidth(4);
            c.drawLine(w / 2f - 20, h / 2f, w / 2f + 20, h / 2f, p);
            c.drawLine(w / 2f, h / 2f - 20, w / 2f, h / 2f + 20, p);

            p.setStyle(Paint.Style.FILL);

            if (hp <= 0) {
                p.setColor(Color.argb(200, 0, 0, 0));
                c.drawRect(0, 0, w, h, p);
p.setColor(Color.WHITE);
                p.setTextSize(70);
                c.drawText("GAME OVER", w / 2f - 210, h / 2f, p);

                p.setTextSize(34);
                c.drawText("Tap FIRE to restart", w / 2f - 150, h / 2f + 70, p);
            }
        }

        void shoot() {
            if (hp <= 0) {
                hp = 100;
                score = 0;
                bullets.clear();
                enemies.clear();

                for (int i = 0; i < 8; i++) {
                    enemies.add(new Enemy(r.nextFloat() * 1200 - 600, r.nextFloat() * 1200 - 600));
                }

                return;
            }

            bullets.add(new Bullet(playerX, playerZ, angle));
        }
@Override
        public boolean onTouchEvent(MotionEvent e) {
            int action = e.getActionMasked();
            int index = e.getActionIndex();

            float x = e.getX(index);
            float y = e.getY(index);

            int w = getWidth();
            int h = getHeight();

            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                if (x < w * 0.35f && y > h * 0.55f) {
                    moving = true;
                    updateJoystick(x, y);
                } else if (x > w - 250 && y > h - 250) {
                    shoot();
                }
            }

            if (action == MotionEvent.ACTION_MOVE) {
                for (int i = 0; i < e.getPointerCount(); i++) {
                    float mx = e.getX(i);
                    float my = e.getY(i);
if (mx < w * 0.35f && my > h * 0.55f) {
                        moving = true;
                        updateJoystick(mx, my);
                    } else if (mx > w * 0.35f) {
                        angle += e.getHistorySize() > 0 ? (mx - e.getHistoricalX(i, 0)) * 0.006f : 0;
                    }
                }
            }

            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL) {
                moving = false;
                joyX = 0;
                joyY = 0;
            }

            return true;
        }

        void updateJoystick(float x, float y) {
            int h = getHeight();

            float cx = 135;
            float cy = h - 135;

            joyX = (x - cx) / 95f;
            joyY = (y - cy) / 95f;
float len = (float) Math.sqrt(joyX * joyX + joyY * joyY);
            if (len > 1) {
                joyX /= len;
                joyY /= len;
            }
        }
    }

    static class Enemy {
        float x;
        float z;

        Enemy(float x, float z) {
            this.x = x;
            this.z = z;
        }
    }

    static class Bullet {
        float x;
        float z;
        float a;
        float life = 1.5f;

        Bullet(float x, float z, float a) {
            this.x = x;
            this.z = z;
            this.a = a;
        }
    }
}