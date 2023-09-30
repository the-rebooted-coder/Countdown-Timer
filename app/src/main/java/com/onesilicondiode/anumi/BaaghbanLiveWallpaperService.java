package com.onesilicondiode.anumi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import java.util.Calendar;

public class BaaghbanLiveWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new WallpaperEngine();
    }

    private class WallpaperEngine extends Engine {

        private final Handler handler = new Handler();
        private boolean visible = true;

        private int getCurrentTimeOfDay() {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);

            if (hour >= 6 && hour < 12) {
                return R.drawable.morning;
            } else if (hour >= 12 && hour < 17) {
                return R.drawable.day;
            } else if (hour >= 17 && hour < 19) {
                return R.drawable.dusk; // Replace with your afternoon image resource
            } else if (hour >= 19 && hour < 22) {
                return R.drawable.evening;
            } else {
                return R.drawable.night; // Replace with your night image resource
            }
        }        private final Runnable drawRunner = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;

            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    int timeOfDayImageResource = getCurrentTimeOfDay();
                    Bitmap wallpaperBitmap = BitmapFactory.decodeResource(getResources(), timeOfDayImageResource);

                    // Calculate the scaling factors
                    float scaleX = (float) canvas.getWidth() / wallpaperBitmap.getWidth();
                    float scaleY = (float) canvas.getHeight() / wallpaperBitmap.getHeight();

                    // Set the matrix for scaling the image
                    Matrix matrix = new Matrix();
                    matrix.postScale(scaleX, scaleY);

                    // Apply the matrix and draw the scaled image
                    canvas.drawBitmap(wallpaperBitmap, matrix, null);
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }

            handler.removeCallbacks(drawRunner);

            if (visible) {
                handler.postDelayed(drawRunner, 7200000);
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                handler.post(drawRunner);
            } else {
                handler.removeCallbacks(drawRunner);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            draw();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            handler.removeCallbacks(drawRunner);
        }


    }
}