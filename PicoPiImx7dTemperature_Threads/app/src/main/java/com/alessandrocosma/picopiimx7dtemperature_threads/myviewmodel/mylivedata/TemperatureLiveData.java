package com.alessandrocosma.picopiimx7dtemperature_threads.myviewmodel.mylivedata;

import android.arch.lifecycle.LiveData;
import android.util.Log;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.google.android.things.contrib.driver.bmx280.Bmx280;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.math.BigDecimal;

public class TemperatureLiveData extends LiveData<Float> {

    private static final String TAG = TemperatureLiveData.class.getSimpleName();
    private Bmx280 tempSensor;
    private HandlerThread reportTemperatureThread;
    private Handler reportTemperatureHandler;

    private final Runnable reportTemperature = new Runnable() {
        float temperature;

        @Override
        public void run() {
            Log.d(TAG,"Thread:"+Thread.currentThread().getName()+". Sono il thread per la lettura della temperatura. LEGGO la temperatura ");
            try {
                temperature = tempSensor.readTemperature();
                BigDecimal tempBG = new BigDecimal(temperature);
                tempBG = tempBG.setScale(2, BigDecimal.ROUND_HALF_UP);
                temperature = (tempBG.floatValue());
                Log.d(TAG, "Thread:"+Thread.currentThread().getName()+": NOTIFICO la temperatura all'Observer");
                //notifico il valore all'observer
                postValue(temperature);
            }
            catch (IOException | IllegalStateException e){
                Log.e(TAG, "Unable to read temperature");
                temperature = Float.valueOf(null);
            }

            Log.d(TAG,"Thread:"+Thread.currentThread().getName()+". Sono il thread per la lettura della temperatura. Vado in SLEEP ");
            //reportTemperatureHandler.postDelayed(reportTemperature, TimeUnit.SECONDS.toMillis(2));
            reportTemperatureHandler.postDelayed(reportTemperature, 500);
        }
    };


    @Override
    protected void onActive() {
        super.onActive();
        Log.d(TAG, "onActive");

        try {
            tempSensor = RainbowHat.openSensor();
            tempSensor.setTemperatureOversampling(Bmx280.OVERSAMPLING_1X);
            tempSensor.setMode(Bmx280.MODE_NORMAL);
        }
        catch (IOException e){
            Log.e(TAG, "Unable to open temperature sensor");
        }



        /** A Handler to report temperature values.
         *  It is an asynchronous task in a parallel (and background) thread called reportTemperatureThread
         */
        // Init the reportTemperatureThread
        reportTemperatureThread = new HandlerThread("reportTemperatureThread");
        Log.d(TAG,"Thread:"+Thread.currentThread().getName()+". Sono il thread principale. CREO reportTemperatureThread");

        // Start the thread
        reportTemperatureThread.start();
        Log.d(TAG,"Thread:"+Thread.currentThread().getName()+". Sono il thread principale. AVVIO reportTemperatureThread");

        // Init the reportTemperatureHandler
        reportTemperatureHandler = new Handler(reportTemperatureThread.getLooper());
        Log.d(TAG,"Thread:"+Thread.currentThread().getName()+". Sono il thread principale. CREO reportTemperatureHandler");

        // Start the reportTemperatureHandler to report temperature values
        reportTemperatureHandler.post(reportTemperature);
        Log.d(TAG,"Thread:"+Thread.currentThread().getName()+". Sono il thread principale. AVVIO reportTemperatureHandler");
    }

    @Override
    protected void onInactive() {

        Log.d(TAG,"Thread:"+Thread.currentThread().getName()+". Sono il thread principale. CHIUDO l'handler reportTemperatureHandler");
        reportTemperatureHandler.removeCallbacks(reportTemperature);
        Log.d(TAG,"Thread:"+Thread.currentThread().getName()+". Sono il thread principale. CHIUDO il thread reportTemperatureThread");
        reportTemperatureThread.quitSafely();

        try {
            tempSensor.close();
        }
        catch (IOException e) {
            Log.d(TAG, "onInactive: " + e);
        }

        super.onInactive();
        Log.d(TAG, "onInactive");
    }
}