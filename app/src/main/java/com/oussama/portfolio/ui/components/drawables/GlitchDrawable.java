package com.oussama.portfolio.ui.components.drawables;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GlitchDrawable extends Drawable {

    private final Drawable drawable;

    private final Paint paint = new Paint();
    private boolean drawGlitch = false;
    int[] glitchColors = {Color.parseColor("#69E12B"),
            Color.parseColor("#335DF8"),
            Color.parseColor("#8BFEFB"),
            Color.parseColor("#A5110A"),
            Color.parseColor("#ED76F8"),
            Color.parseColor("#70FBAA"),
            Color.parseColor("#3F8C09")};

    private final int numGlitchLayers = 5;

    private final Bitmap[] glitchBitmaps;
    private final Rect[] glitchBounds;
    private int tintColor;
    private final Random random = new Random();

    public GlitchDrawable(Drawable drawable, int tintColor) {
        this.drawable = drawable;
        glitchBitmaps = new Bitmap[numGlitchLayers];
        glitchBounds = new Rect[numGlitchLayers];
        if(this.drawable == null)
            return;
        this.tintColor = tintColor;
        this.drawable.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN));
        splitDrawable();
        startGlitchAnimation();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
    }

    public void setTint(int tintColor){
        this.tintColor = tintColor;
        drawable.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN));
        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if(!drawGlitch){
            drawable.setBounds(getBounds());
            drawable.draw(canvas);
            return;
        }
        Rect glitchArea = new Rect(getBounds());
        int centerX = getBounds().width() / 2;
        int centerY = getBounds().height() / 2;
        int radius = Math.min(centerX, centerY); // Define the radius of the glitch area

        glitchArea.inset(centerX - radius, centerY - radius);
        int artifactHeight = glitchArea.height() / 20;

        for (int i = 0; i < 8; i++) {
            int startY = glitchArea.top + random.nextInt(glitchArea.height());
            int startX = glitchArea.left + random.nextInt(glitchArea.width());
            int endX = startX + random.nextInt(glitchArea.width() / 2); // Adjust the artifact length
            int randomColor = glitchColors[random.nextInt(glitchColors.length)];
            paint.setColor(randomColor);
            canvas.drawRect(startX, startY, endX, startY + artifactHeight, paint);
        }
        for (int i = 0; i < numGlitchLayers; i++) {
            if(i == 1){
                drawable.setBounds(getBounds());
                drawable.draw(canvas);
            }
            canvas.drawBitmap(glitchBitmaps[i], null, glitchBounds[i], null);
        }
    }

    private void splitDrawable() {
        for (int i = 0; i < numGlitchLayers; i++) {
            glitchBitmaps[i] = createBitmapFromDrawable(drawable);
            applyTint(glitchBitmaps[i], glitchColors[i]);
            glitchBounds[i] = new Rect(0, 0, getBounds().width(), getBounds().height());
        }
    }

    private Bitmap createBitmapFromDrawable(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void applyTint(Bitmap bitmap, int color) {
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        paint.setAlpha(220);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap, 0, 0, paint);
    }

    public void startGlitchAnimation() {
        ValueAnimator glitchAnimator = ValueAnimator.ofInt(0, 10);
        glitchAnimator.setDuration(500);
        glitchAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                drawGlitch = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                drawGlitch = false;
                invalidateSelf();
            }
        });
        glitchAnimator.addUpdateListener(animation -> applyGlitch());

        glitchAnimator.start();
    }

    private void applyGlitch() {
        for (int i = 0; i < numGlitchLayers; i++) {
            glitchBounds[i].set(getBounds());
            glitchBounds[i].offset((int) (Math.random() * 20 - 10), (int) (Math.random() * 20 - 10));
        }
        invalidateSelf();
    }


    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }
}
