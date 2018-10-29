package com.alessandrocosma.picopiimx7dtemperature_nothreads.myviewmodel.mylivedata;

import android.arch.lifecycle.LiveData;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;

import com.google.android.things.contrib.driver.bmx280.Bmx280;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.math.BigDecimal;

public class TemperatureLiveData extends LiveData<Float> {

    private static final String TAG = TemperatureLiveData.class.getSimpleName();
    private Bmx280 tempSensor;
    private Handler reportTemperatureHandler;

    private final Runnable reportTemperature = new Runnable() {
        float temperature;

        @Override
        public void run() {
            Log.d(TAG,"Thread:"+Thread.currentThread().getName()+". Sono il task per la lettura della temperatura. LEGGO la temperatura ");
            try {
                temperature = tempSensor.readTemperature();
                BigDecimal tempBG = new BigDecimal(temperature);
                tempBG = tempBG.setScale(2, BigDecimal.ROUND_HALF_UP);
                temperature = (tempBG.floatValue());
                //notifico il valore all'observer
                setValue(temperature);
            }
            catch (IOException | IllegalStateException e){
                Log.e(TAG, "Unable to read temperature");
                temperature = Float.valueOf(null);
            }

            Log.d(TAG,"Thread:"+Thread.currentThread().getName()+". Sono il task per la lettura della temperatura. Vado in SLEEP ");
            reportTemperatureHandler.postDelayed(reportTemperature, TimeUnit.SECONDS.toMillis(2));
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
         *  It is an asynchronous task but still on the main thread
         */
        // Init the reportTemperatureHandler
        reportTemperatureHandler = new Handler(Looper.getMainLooper());
        Log.d(TAG,"Thread:"+Thread.currentThread().getName()+". Sono il thread principale. CREO l'reportTemperatureHandler per la lettura di temperatura");

        // Start the reportTemperatureHandler to report temperature values
        reportTemperatureHandler.post(reportTemperature);
        Log.d(TAG,"Thread:"+Thread.currentThread().getName()+". Sono il thread principale. AVVIO l'reportTemperatureHandler per la lettura di temperatura");
    }

    @Override
    protected void onInactive() {

        reportTemperatureHandler.removeCallbacks(reportTemperature);

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