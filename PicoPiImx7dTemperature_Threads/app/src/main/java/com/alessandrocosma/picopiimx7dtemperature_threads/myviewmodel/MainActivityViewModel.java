package com.alessandrocosma.picopiimx7dtemperature_threads.myviewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.LiveData;
import android.os.HandlerThread;
import android.util.Log;
import android.os.Handler;

import com.alessandrocosma.picopiimx7dtemperature_threads.myviewmodel.mylivedata.ButtonLiveData;
import com.alessandrocosma.picopiimx7dtemperature_threads.myviewmodel.mylivedata.TemperatureLiveData;
import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.contrib.driver.ht16k33.Ht16k33;
import com.google.android.things.contrib.driver.pwmspeaker.Speaker;
import com.google.android.things.pio.Gpio;

import java.io.IOException;


public class MainActivityViewModel extends ViewModel {

    private static final String TAG = MainActivityViewModel.class.getSimpleName();

    private ButtonLiveData mButtonLiveData;
    private TemperatureLiveData mTemperatureLiveData;
    private AlphanumericDisplay alphanumericDisplay;
    private Speaker mSpeaker;

    private volatile boolean running = false;

    /**
     * A Handler to play buzzer sound.
     *  It is an asynchronous task in a parallel (and background) thread called buzzerSoundThread
     */
    private Handler buzzerSoundHandler;
    private HandlerThread buzzerSoundThread;


    public LiveData<Button> getButtonLiveData() {
        if (mButtonLiveData == null)
            mButtonLiveData = new ButtonLiveData();

        return mButtonLiveData;
    }

    public LiveData<Float> getTemperatureLiveData() {
        if (mTemperatureLiveData == null)
            mTemperatureLiveData = new TemperatureLiveData();

        return mTemperatureLiveData;
    }


    public void openSpeaker() {
        if (mSpeaker == null) {
            try {
                mSpeaker = RainbowHat.openPiezo();

                // Init the buzzerSoundThread
                Log.d(TAG,"Thread:"+Thread.currentThread().getName()+". Sono il thread principale. CREO buzzerSoundThread");
                buzzerSoundThread = new HandlerThread("buzzerSoundThread");

                running = true;

                // Start the thread
                Log.d(TAG,"Thread:"+Thread.currentThread().getName()+". Sono il thread principale. AVVIO buzzerSoundThread");
                buzzerSoundThread.start();

                //Init the handler
                Log.d(TAG, "Thread:" + Thread.currentThread().getName() + ". Sono il thread principale. CREO l'handler per far suonare l'allarme");
                buzzerSoundHandler = new Handler(buzzerSoundThread.getLooper());
            } catch (IOException e) {
                Log.e(TAG, "Unable to open the Speaker");
            }
        }
    }


    public void closeSpeaker() {

        running = false;

        synchronized (mSpeaker){

            if (mSpeaker == null)
                return;

            Log.d(TAG,"Thread:"+Thread.currentThread().getName()+". Sono il thread principale. CHIUDO l'handler buzzerSoundHandler");
            buzzerSoundHandler.removeCallbacks(playSoundRunnable);
            Log.d(TAG,"Thread:"+Thread.currentThread().getName()+". Sono il thread principale. CHIUDO il thread buzzerSoundThread");
            buzzerSoundThread.quitSafely();

            try {
                Log.d(TAG, "Thread:" + Thread.currentThread().getName() + ". Sono il thread principale CHIUDO lo speaker");
                mSpeaker.close();
            } catch (IOException e) {
                Log.e(TAG, "Unable to close the Speaker");
            }
        }
    }

    public void playSound() {
        if (mSpeaker == null) {
            Log.e(TAG, "Speaker not open!");
            return;
        }

        // Start the handler to play buzzer
        Log.d(TAG, "Thread:" + Thread.currentThread().getName() + ". Sono il thread principale. AVVIO l'handler buzzerSoundHandler");
        buzzerSoundHandler.post(playSoundRunnable);
    }


    private final Runnable playSoundRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running)
                return;
            synchronized (mSpeaker){
                try {
                    Log.d(TAG, "Thread:" + Thread.currentThread().getName() + ". Sono il thread per il suono dell'allarme. ACCENDO l'allarme ");
                    mSpeaker.play(2000);
                    Log.d(TAG, "Thread:" + Thread.currentThread().getName() + ". Sono il thread per il suono dell'allarme. Vado in SLEEP ");
                    Thread.sleep(1000);
                    Log.d(TAG, "Thread:" + Thread.currentThread().getName() + ". Sono il thread per il suono dell'allarme. SPENGO l'allarme ");
                    mSpeaker.stop();
                } catch (IOException | InterruptedException | IllegalStateException e) {
                    Log.e(TAG, "Unable to manage buzzer sound: "+ e.toString());
                }
            }
        }
    };


    public void setLedLight(char led, boolean value) {

        Gpio ledR;
        Gpio ledG;
        Gpio ledB;

        switch (led) {
            case 'R':
                try {
                    ledR = RainbowHat.openLedRed();
                    ledR.setValue(value);
                    ledR.close();
                    break;
                } catch (IOException e) {
                    Log.e(TAG, "Unable to manage the led" + String.valueOf(led));
                }


            case 'B':
                try {
                    ledB = RainbowHat.openLedBlue();
                    ledB.setValue(value);
                    ledB.close();
                    break;
                } catch (IOException e) {
                    Log.e(TAG, "Unable to manage the led" + String.valueOf(led));
                }


            case 'G':
                try {
                    ledG = RainbowHat.openLedGreen();
                    ledG.setValue(value);
                    ledG.close();
                    break;
                } catch (IOException e) {
                    Log.e(TAG, "Unable to manage the led" + String.valueOf(led));
                }


            default:
                Log.e(TAG, "identificatore led non corrispondente. Valori ammessi: R,G,B");

        }
    }

    public void display(Float value) {
        if (alphanumericDisplay == null) {
            try {
                alphanumericDisplay = RainbowHat.openDisplay();
                alphanumericDisplay.setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX);
                alphanumericDisplay.clear();
                alphanumericDisplay.setEnabled(true);
            } catch (IOException e) {
                Log.d(TAG, "display: " + e);
                alphanumericDisplay = null;
                return;
            }
        }

        try {
            alphanumericDisplay.display(value);
        } catch (IOException e) {
            Log.d(TAG, "display: " + e);
        }
    }

    public void cleanDisplay() {
        if (alphanumericDisplay == null) {
            try {
                alphanumericDisplay = RainbowHat.openDisplay();
            } catch (IOException e) {
                alphanumericDisplay = null;
                return;
            }
        }

        try {
            alphanumericDisplay.clear();
            alphanumericDisplay.setEnabled(true);
            alphanumericDisplay.close();
        } catch (IOException e) {
        }

    }
}

