package com.onesilicondiode.anumi;

import static com.onesilicondiode.anumi.ImageUtils.getImageResourceId;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import java.util.Calendar;

public class CountdownLiveWallpaperService extends WallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new CountdownEngine();
    }

    private class CountdownEngine extends Engine {
        private static final long DELAY_MILLIS = 1000;

        private final Handler handler = new Handler();
        private int currentDay;        private final Runnable drawRunner = () -> draw();
        private boolean isHappyHomecoming;
        private SharedPreferences prefs;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            prefs = PreferenceManager.getDefaultSharedPreferences(CountdownLiveWallpaperService.this);
            updateValues();
        }

        @Override
        public void onDestroy() {
            handler.removeCallbacks(drawRunner);
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                updateValues();
                draw();
            } else {
                handler.removeCallbacks(drawRunner);
            }
        }

        private void updateValues() {
            Calendar today = Calendar.getInstance();
            currentDay = getCurrentDay(today);
            long millisUntilEnd = getMillisUntilEnd(today);

            if (millisUntilEnd <= 0) {
                isHappyHomecoming = true;
            } else {
                isHappyHomecoming = false;
            }
        }

        private int getCurrentDay(Calendar today) {
            // Calculate the current day of the countdown
            Calendar startDate = Calendar.getInstance();
            startDate.set(Calendar.MONTH, Calendar.SEPTEMBER);
            startDate.set(Calendar.DAY_OF_MONTH, 30);

            long millisSinceStart = today.getTimeInMillis() - startDate.getTimeInMillis();
            int currentDay = (int) (millisSinceStart / (24 * 60 * 60 * 1000)) + 1; // Adjust for 1-based index
            return currentDay;
        }

        private long getMillisUntilEnd(Calendar today) {
            // Calculate the milliseconds remaining until the end date
            Calendar endDate = Calendar.getInstance();
            endDate.set(Calendar.YEAR, 2023);
            endDate.set(Calendar.MONTH, Calendar.OCTOBER);
            endDate.set(Calendar.DAY_OF_MONTH, 14);
            endDate.set(Calendar.HOUR_OF_DAY, 0);
            endDate.set(Calendar.MINUTE, 0);
            endDate.set(Calendar.SECOND, 0);
            endDate.set(Calendar.MILLISECOND, 0);

            long millisUntilEnd = endDate.getTimeInMillis() - today.getTimeInMillis();
            return millisUntilEnd;
        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;

            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    drawBackground(canvas);
                    drawImage(canvas);
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }

            handler.removeCallbacks(drawRunner);
            if (!isHappyHomecoming) {
                handler.postDelayed(drawRunner, DELAY_MILLIS);
            }
        }

        private void drawBackground(Canvas canvas) {
            canvas.drawColor(Color.BLACK);
        }

        private void drawImage(Canvas canvas) {
            int imageResource;

            if (isHappyHomecoming) {
                imageResource = R.drawable.wall_30;
            } else {
                imageResource = getImageResourceId(currentDay);
            }

            Drawable drawable = getResources().getDrawable(imageResource);

            int canvasWidth = canvas.getWidth();
            int canvasHeight = canvas.getHeight();

            float canvasAspectRatio = (float) canvasWidth / canvasHeight;
            float drawableAspectRatio = (float) drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight();

            int left, top, right, bottom;

            if (drawableAspectRatio > canvasAspectRatio) {
                // If the drawable's aspect ratio is wider, fit it to the height and crop the sides
                int scaledDrawableWidth = Math.round(canvasHeight * drawableAspectRatio);

                left = (canvasWidth - scaledDrawableWidth) / 2;
                top = 0;
                right = left + scaledDrawableWidth;
                bottom = canvasHeight;
            } else {
                // If the drawable's aspect ratio is taller, fit it to the width and crop the top and bottom
                int scaledDrawableHeight = Math.round(canvasWidth / drawableAspectRatio);

                left = 0;
                top = (canvasHeight - scaledDrawableHeight) / 2;
                right = canvasWidth;
                bottom = top + scaledDrawableHeight;
            }

            drawable.setBounds(left, top, right, bottom);
            drawable.draw(canvas);
        }


    }
}