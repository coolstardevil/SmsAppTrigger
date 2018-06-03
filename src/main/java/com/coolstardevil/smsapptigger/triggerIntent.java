package com.coolstardevil.smsapptigger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.AUDIO_SERVICE;


public class triggerIntent extends BroadcastReceiver {

    private CameraManager mCameraManager;
    private String mCameraId;
    public static Boolean playing;
    @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onReceive(Context context, Intent intent) {

        SharedPreferences load = context.getSharedPreferences("AppData", 0);
        MainActivity.toggle = load.getBoolean("ToggleApp", false);

        if (!MainActivity.toggle) {
            Toast.makeText(context, "Enable the toggle Button!", Toast.LENGTH_LONG).show();
            return;
        }
        SmsMessage[] msgs = null;
        String str = "";
        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {
                //---retrieve the SMS message received---
                Object[] pdus = (Object[]) bundle.get("pdus");
                msgs = new SmsMessage[pdus.length];
                for (int i = 0; i < msgs.length; i++) {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    str += msgs[i].getMessageBody().toString();
                    str += "\n";
                }
                if (str.contains("wifi on") || str.contains("WiFi On") || str.contains("WiFi on") || str.contains("Wifi On")) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    wifiManager.setWifiEnabled(true);
                    Toast.makeText(context, "WiFi On!", Toast.LENGTH_SHORT).show();

                } else if (str.contains("wifi off") || str.contains("WiFi Off") || str.contains("WiFi off") || str.contains("Wifi Off")) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    wifiManager.setWifiEnabled(false);
                    Toast.makeText(context, "WiFi Off!", Toast.LENGTH_SHORT).show();

                } else if (str.contains("flash on") || str.contains("Flash On") || str.contains("FLASH on") || str.contains("FLASH On") ||
                           str.contains("flashlight on") || str.contains("Flashlight On") || str.contains("FLASHLIGHT on") || str.contains("FLASHLIGHT On")|| str.contains("FLASHLIGHT ON")) {

                    mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                    try {
                        mCameraId = mCameraManager.getCameraIdList()[0];
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            mCameraManager.setTorchMode(mCameraId, true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }else if (str.contains("flash off") || str.contains("Flash Off") || str.contains("FLASH Off") || str.contains("FLASH OFF") ||str.contains("FLASH off") ||
                        str.contains("flashlight off") || str.contains("Flashlight Off") || str.contains("FLASHLIGHT Off") || str.contains("FLASHLIGHT OFF")||str.contains("FLASHLIGHT off")) {

                    mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                    try {
                        mCameraId = mCameraManager.getCameraIdList()[0];
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            mCameraManager.setTorchMode(mCameraId, false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else if (str.contains("lost") || str.contains("Lost") || str.contains("LOST")) {

                    AudioManager mAudioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                    playing=true;
                    Intent intentOne = new Intent(context.getApplicationContext(), AudioPlayer.class);
                    intentOne.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intentOne);

                } else if (str.contains("found") || str.contains("Found") || str.contains("FOUND")) {
                    playing=false;
                    Intent intentOne = new Intent(context.getApplicationContext(), AudioPlayer.class);
                    intentOne.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intentOne);

                } else if (str.contains("location") || str.contains("Location") || str.contains("LOCATION")) {
                    SharedPreferences sharedpreferences = context.getSharedPreferences("AppData", Context.MODE_PRIVATE);
                    String number = load.getString("numberData", null);

                    Intent intentOne = new Intent(context.getApplicationContext(), LocationService.class);
                    context.startService(intentOne);

                    String locationData = "Your Phone Currently Now\n" + "https://maps.google.com?q="+sharedpreferences.getString("Lat","NUll")+","
                            +sharedpreferences.getString("Lon","NUll");

                    //Send SMS text
                    try {
                        SmsManager smsManager = SmsManager.getDefault();

                        smsManager.sendTextMessage(number, null, locationData, null, null);
                        Toast.makeText(context.getApplicationContext(), "Message Sent",
                                Toast.LENGTH_LONG).show();
                    } catch (Exception ex) {
                        Toast.makeText(context.getApplicationContext(), ex.getMessage().toString(),
                                Toast.LENGTH_LONG).show();
                        ex.printStackTrace();
                    }

                }
            }
        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e);

        }
    }
}