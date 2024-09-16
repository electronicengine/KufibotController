package com.example.kufibotcontroller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;


public class JoystickView extends View {

    private Paint basePaint;
    private Paint handlePaint;
    private float handleX, handleY;
    private float centerX, centerY;
    private float baseRadius, handleRadius;
    private OnMoveListener onMoveListener;
    WebSocketControllerClient controllerClient = WebSocketControllerClient.getInstance();

    public interface OnMoveListener {
        void onMove(int angle, int strength);
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Set the background of the entire view to be transparent
        // Base paint with transparency (50% alpha)
        basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        basePaint.setColor(Color.GRAY);
        basePaint.setAlpha(128);  // Set transparency for the base
        basePaint.setStyle(Paint.Style.FILL);

        // Handle paint without transparency (full opacity)
        handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        handlePaint.setColor(Color.RED);
        handlePaint.setAlpha(128);  // Set transparency for the base
        handlePaint.setStyle(Paint.Style.FILL);

        controllerClient.connect("ws://192.168.1.39:8766");
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // Calculate the center and radii of the joystick
        centerX = getWidth() / 2f;
        centerY = getHeight() / 2f;
        baseRadius = Math.min(w, h) / 3f;  // Base radius is 1/3 of the smallest dimension
        handleRadius = Math.min(w, h) / 6f;  // Handle radius is 1/6 of the smallest dimension
        handleX = centerX;
        handleY = centerY;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw the base and handle of the joystick
        canvas.drawCircle(centerX, centerY, baseRadius, basePaint);
        canvas.drawCircle(handleX, handleY, handleRadius, handlePaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - centerX;
                float dy = event.getY() - centerY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                // Limit the handle position to stay within the base radius
                if (distance > baseRadius) {
                    // Calculate constrained position along the circle's edge
                    float ratio = baseRadius / distance;
                    handleX = centerX + dx * ratio;
                    handleY = centerY + dy * ratio;
                } else {
                    // Normal case, move the handle freely
                    handleX = event.getX();
                    handleY = event.getY();
                }
                // Notify the listener if set

                JSONObject jsonObject = new JSONObject();
                try {
                    // Put key-value pairs into the JSON object
                    jsonObject.put("Id", getResources().getResourceEntryName(getId()));
                    jsonObject.put("Angle", (int) Math.toDegrees(Math.atan2(dy, dx)) * -1);
                    jsonObject.put("Strength", (int) Math.min((distance / baseRadius * 100), 100));

                    // Convert JSON object to string
                    String jsonData = jsonObject.toString();

                    // Now jsonData contains your JSON formatted message
                    Log.d("joysticks", jsonData);

                    // Send JSON data over WebSocket
                    controllerClient.send(jsonData);

                } catch (JSONException e) {
                    Log.e("JSON Error", "Failed to create JSON object: " + e.getMessage());
                }

                invalidate();  // Redraw the joystick
                break;

            case MotionEvent.ACTION_UP:
                // Return the handle to the center position when the user lifts the finger
                handleX = centerX;
                handleY = centerY;

                JSONObject jsonStopObject = new JSONObject();
                try {
                    // Put key-value pairs into the JSON object
                    jsonStopObject.put("Id", getResources().getResourceEntryName(getId()));
                    jsonStopObject.put("Angle", (int) 0);
                    jsonStopObject.put("Strength", (int) 0);

                    String jsonStopData = jsonStopObject.toString();
                    Log.d("joysticks", jsonStopData);

                    controllerClient.send(jsonStopData);

                } catch (JSONException e) {
                    Log.e("JSON Error", "Failed to create JSON object: " + e.getMessage());
                }

                invalidate();
                break;
        }

        return true;
    }

    public void setOnMoveListener(OnMoveListener listener) {
        this.onMoveListener = listener;
    }
}