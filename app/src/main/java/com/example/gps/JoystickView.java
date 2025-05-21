package com.example.gps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {
    private float centerX, centerY, baseRadius, hatRadius;
    private float touchX = 0, touchY = 0;
    private Paint basePaint, hatPaint;
    private JoystickListener joystickCallback;

    public interface JoystickListener {
        void onJoystickMoved(float xPercent, float yPercent);
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        basePaint = new Paint();
        basePaint.setColor(Color.GRAY);
        basePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        hatPaint = new Paint();
        hatPaint.setColor(Color.RED);
        hatPaint.setStyle(Paint.Style.FILL);
    }

    public void setJoystickListener(JoystickListener listener) {
        this.joystickCallback = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        centerX = w / 2;
        centerY = h / 2;
        baseRadius = Math.min(w, h) / 3;
        hatRadius = Math.min(w, h) / 5;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(centerX, centerY, baseRadius, basePaint);
        canvas.drawCircle(touchX, touchY, hatRadius, hatPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float dx = event.getX() - centerX;
        float dy = event.getY() - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance < baseRadius) {
            touchX = event.getX();
            touchY = event.getY();
        } else {
            touchX = centerX + dx * baseRadius / distance;
            touchY = centerY + dy * baseRadius / distance;
        }

        invalidate();

        float xPercent = (touchX - centerX) / baseRadius;
        float yPercent = (touchY - centerY) / baseRadius;

        if (event.getAction() != MotionEvent.ACTION_UP) {
            if (joystickCallback != null)
                joystickCallback.onJoystickMoved(xPercent, yPercent);
        } else {
            touchX = centerX;
            touchY = centerY;
            invalidate();
            joystickCallback.onJoystickMoved(0, 0);
        }
        return true;
    }
}
