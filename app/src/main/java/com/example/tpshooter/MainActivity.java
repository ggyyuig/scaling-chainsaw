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
            p.setColor(Color.WHIT