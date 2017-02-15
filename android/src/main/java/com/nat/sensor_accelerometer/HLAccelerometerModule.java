package com.nat.sensor_accelerometer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xuqinchao on 17/2/6.
 *  Copyright (c) 2017 Nat. All rights reserved.
 */

public class HLAccelerometerModule {

    private boolean mClearWatch;
    private float x;
    private float y;
    private float z;
    private Timer timer;
    int interval = 100;
    private SensorManager mWatchManager;
    private HLModuleResultListener mListener;

    private Context mContext;
    private static volatile HLAccelerometerModule instance = null;

    private HLAccelerometerModule(Context context){
        mContext = context;
    }

    public static HLAccelerometerModule getInstance(Context context) {
        if (instance == null) {
            synchronized (HLAccelerometerModule.class) {
                if (instance == null) {
                    instance = new HLAccelerometerModule(context);
                }
            }
        }

        return instance;
    }


    public void get(final HLModuleResultListener listener){
        if (listener == null) return;
        if (mContext == null) {
            listener.onResult(HLConstant.HL_ERROR_NULL_CONTEXT);
            return;
        }

        final SensorManager sm = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        int sensorType = Sensor.TYPE_ACCELEROMETER;
        sm.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                    float x = sensorEvent.values[0];
                    float y = sensorEvent.values[1];
                    float z = sensorEvent.values[2];

                    HashMap<String, Float> result = new HashMap<String, Float>();
                    result.put("x", x);
                    result.put("y", y);
                    result.put("z", z);
                    listener.onResult(result);
                    sm.unregisterListener(this);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        }, sm.getDefaultSensor(sensorType), SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void watch(HashMap<String, Integer> option, final HLModuleResultListener listener){
        if (mWatchManager != null)return;
        mListener = listener;
        if (option != null && option.containsKey("interval")) {
            interval = option.get("interval");
        }
        interval = 1000;
        timer = new Timer();
        timer.schedule(new MyTimerTask(), 0, interval);
        mWatchManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        int sensorType = Sensor.TYPE_ACCELEROMETER;
        mWatchManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                    if (mClearWatch) {
                        mWatchManager.unregisterListener(this);
                        mClearWatch = false;
                        if (timer != null) timer.cancel();
                        mWatchManager = null;
                        return;
                    }
                    x = sensorEvent.values[0];
                    y = sensorEvent.values[1];
                    z = sensorEvent.values[2];

                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        }, mWatchManager.getDefaultSensor(sensorType), SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void clearWatch(HLModuleResultListener listener){
        if (listener == null || mWatchManager == null)return;
        mClearWatch = true;
        listener.onResult(null);
    }

    class MyTimerTask extends TimerTask{

        @Override
        public void run() {
            if (mListener != null) {
                HashMap<String, Float> result = new HashMap<String, Float>();
                result.put("x", x);
                result.put("y", y);
                result.put("z", z);
                mListener.onResult(result);
            }
        }
    }
}
