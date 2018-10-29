package com.alessandrocosma.picopiimx7dtemperature_nothreads;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.util.Log;

import com.alessandrocosma.picopiimx7dtemperature_nothreads.myviewmodel.MainActivityViewModel;
import com.google.android.things.contrib.driver.button.Button;


/**
 * A simple application for AndroidThings platform - PICO-PI-IMX7 with RainbowHat.
 * The RainbowHat BMP280 sensor reports the current temperature every 2 seconds
 * and displays it in the segment display.
 * If temperature >= MAX_TEMPERATURE the red led is turned on and the device plays an alarm.
 * If NORMAL_TEMPERATURE <= temperature < MAX_TEMPERATURE the green led is turned on.
 * Otherwise (temperature < 34) blue led is turned on.
 * N.B. Temperature readings are affected by heat radiated from your Pi’s CPU and the onboard LEDs;
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final float MAX_TEMPERATURE = 28.0f;
    private static final float NORMAL_TEMPERATURE = 24.0f;


    //Stringhe che mi rappresentano i led red, blue e green
    private final char R = 'R';
    private final char B = 'B';
    private final char G = 'G';




    private MainActivityViewModel mainActivityViewModel;


    private final Observer<Button> exitButtonLiveDataObserver = new Observer<Button>(){
        @Override
        public void onChanged(@Nullable Button button){

            if (button != null){
                MainActivity.this.finish();
            }
        }
    };

    private final Observer<Float> temperatureLiveDataObserver = new Observer<Float>() {
        @Override
        public void onChanged(@Nullable Float temperature) {

            mainActivityViewModel.display(temperature);

            if(temperature < NORMAL_TEMPERATURE){
                mainActivityViewModel.setLedLight(R,false);
                mainActivityViewModel.setLedLight(G,false);
                mainActivityViewModel.setLedLight(B,true);
            }

            else if(temperature >= NORMAL_TEMPERATURE && temperature < MAX_TEMPERATURE){
                mainActivityViewModel.setLedLight(R,false);
                mainActivityViewModel.setLedLight(G,true);
                mainActivityViewModel.setLedLight(B,false);
            }
            else {
                mainActivityViewModel.setLedLight(R,true);
                mainActivityViewModel.setLedLight(G,false);
                mainActivityViewModel.setLedLight(B,false);

                mainActivityViewModel.playSound();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        mainActivityViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        //spengo i led se accesi
        mainActivityViewModel.setLedLight(R,false);
        mainActivityViewModel.setLedLight(B,false);
        mainActivityViewModel.setLedLight(G,false);

        //apro la connessione con lo Speaker di allarme
        mainActivityViewModel.openSpeaker();

        //inizio ad osservare il ButtonLiveData
        mainActivityViewModel.getButtonLiveData().observe(MainActivity.this, exitButtonLiveDataObserver);

        //inizio ad osservare il TemperatureLiveData
        mainActivityViewModel.getTemperatureLiveData().observe(MainActivity.this, temperatureLiveDataObserver);


    }


    @Override
    protected void onDestroy() {

        //spengo i led se accesi
        mainActivityViewModel.setLedLight(R,false);
        mainActivityViewModel.setLedLight(B,false);
        mainActivityViewModel.setLedLight(G,false);

        //chiudo la connessione con lo Speaker di allarme
        mainActivityViewModel.closeSpeaker();

        //azzero le scritte sul display
        mainActivityViewModel.cleanDisplay();

        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
