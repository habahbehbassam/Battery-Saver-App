package com.example.batteryoptimizer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    BroadcastReceiver batteryBroadcast;
    IntentFilter intentFilter;
    TextView level, volt, health, status, type, source, temp, alarmLimit, tvpath;
    SeekBar seekBar;
    Button setAlarmbtn, cancelbtn;
    ImageButton changeRingtone;
    boolean alarmFlag;
    int batteryLevel, batteryLevelLimit;
    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    String buttonOnColor = "FFFFB477";
    String buttonOffColor = "FFD5D2D0";

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(batteryBroadcast, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //unregisterReceiver(batteryBroadcast);
    }


    /*-------------------------------Main method------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getAllId();
        intentFilterAndBroadcast();
        batteryLevelLimit = 80;

        setAlarmbtn.setTextColor(Color.parseColor("#" + buttonOnColor));
        cancelbtn.setTextColor(Color.parseColor("#" + buttonOffColor));

        getSeekbarProgress();
        buttonPerform();
        playRingtone();

    }

    private void getSeekbarProgress() {
        seekBar.setProgress(batteryLevelLimit);
        alarmLimit.setText(batteryLevelLimit + "%");
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                batteryLevelLimit = progress;
                alarmLimit.setText("" + progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void playRingtone() {
        final Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (alarmFlag == true) {
                    if (batteryLevel >= batteryLevelLimit) {
                        if (status.getText().equals("Charging") || status.getText().equals("Full charged")) {
                            ringtone.play();
                        } else {
                            ringtone.stop();
                        }
                    } else {
                        ringtone.stop();
                    }
                } else {
                    ringtone.stop();
                }
            }
        }, 0, 1000);
    }

    private void getAllId() {
        changeRingtone = findViewById(R.id.changeRingtonebtn);
        level = findViewById(R.id.level);
        volt = findViewById(R.id.voltage);
        health = findViewById(R.id.health);
        status = findViewById(R.id.status);
        type = findViewById(R.id.type);
        source = findViewById(R.id.source);
        temp = findViewById(R.id.temp);
        alarmLimit = findViewById(R.id.alarmLimit);
        seekBar = findViewById(R.id.seekBar);
        setAlarmbtn = findViewById(R.id.alarmbtn);
        cancelbtn = findViewById(R.id.cancelbtn);
    }

    private void buttonPerform() {
        changeRingtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_SOUND_SETTINGS), 0);
            }
        });
        setAlarmbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alarmFlag = true;
                setAlarmbtn.setTextColor(Color.parseColor("#" + buttonOffColor));
                cancelbtn.setTextColor(Color.parseColor("#" + buttonOnColor));
                setAlarmbtn.setEnabled(false);
                cancelbtn.setEnabled(true);
            }
        });
        cancelbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alarmFlag = false;
                setAlarmbtn.setTextColor(Color.parseColor("#" + buttonOnColor));
                cancelbtn.setTextColor(Color.parseColor("#" + buttonOffColor));
                setAlarmbtn.setEnabled(true);
                cancelbtn.setEnabled(false);
            }
        });
    }


    private void setHealth(Intent intent) {
        int val = intent.getIntExtra("health", 0);
        switch (val) {
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                health.setText("Unknown");
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                health.setText("Good");
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                health.setText("Overheat");
                break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                health.setText("Dead");
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                health.setText("Over Voltage");
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                health.setText("Unspecified failure");
                break;
            case BatteryManager.BATTERY_HEALTH_COLD:
                health.setText("Cold");
                break;
        }
    }

    private void intentFilterAndBroadcast() {

        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        batteryBroadcast = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                    /*-------level--------*/
                    batteryLevel = intent.getIntExtra("level", 0);
                    level.setText(String.valueOf(batteryLevel + "%"));
                    /*-------voltage--------*/
                    float vlotTemp = (float) (intent.getIntExtra("voltage", 0) * 0.001);
                    volt.setText(vlotTemp + "v");
                    /*-------Health--------*/
                    setHealth(intent);
                    /*-------Source type--------*/
                    type.setText(intent.getStringExtra("technology"));
                    getChargingSource(intent);

                    /*-------Temperature--------*/
                    float tempTemp = (float) intent.getIntExtra("temperature", -1) / 10;
                    temp.setText(tempTemp + " °C");
                    /*-------ChargingStatus--------*/
                    setChargingStatus(intent);
                }
            }
        };
    }

    private void setChargingStatus(Intent intent) {
        int chargingStat = intent.getIntExtra("status", -1);
        switch (chargingStat) {
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
                status.setText("Unknown");
                break;
            case BatteryManager.BATTERY_STATUS_CHARGING:
                status.setText("Charging");
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                status.setText("Discharging");
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                status.setText("Not Charging");
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                status.setText("Full charged");
                break;
            default:
                status.setText("NULL");

        }
    }

    private void getChargingSource(Intent intent) {
        int sourceType = intent.getIntExtra("plugged", -1);
        switch (sourceType) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                source.setText("AC");
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                source.setText("USB");
                break;
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                source.setText("WIRELESS");
                break;
            default:
                source.setText("NULL");
                break;
        }
    }
}