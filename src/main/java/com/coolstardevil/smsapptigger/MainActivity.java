package com.coolstardevil.smsapptigger;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private InterstitialAd interstitialAd;
    public static Boolean toggle;
    Switch switchToggle;
    Button ok;
    ImageButton ad;
    EditText number;
    Button exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        interstitialAd = new InterstitialAd(this);
        this.setFinishOnTouchOutside(false);
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequestB = new AdRequest.Builder().build();
        mAdView.loadAd(adRequestB);

        switchToggle = (Switch) findViewById(R.id.switchButton);
        ad = (ImageButton) findViewById(R.id.ad);
        ok = (Button) findViewById(R.id.ok);
        number = (EditText) findViewById(R.id.number);
        exit = (Button) findViewById(R.id.stopMain);
        SharedPreferences load = getSharedPreferences("AppData", 0);
        toggle = load.getBoolean("ToggleApp", false);
        String numberData = load.getString("numberData", null);
        number.setText(numberData);
        Intent intent = new Intent(this, LocationService.class);
        startService(intent);
        interstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
        final AdRequest adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest);

        requestStoragePermission();
        isLocationEnabled();
        if (toggle) {
            switchToggle.setChecked(true);
            switchToggle.setBackgroundColor(Color.parseColor("#00ffffff"));
        }

        ok.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {

                String numberData = number.getText().toString();
                SharedPreferences save = getSharedPreferences("AppData", 0);
                SharedPreferences.Editor editor = save.edit();
                editor.putString("numberData", numberData);
                editor.apply();

                String number = save.getString("numberData", null);
                Toast.makeText(getApplicationContext(), "Number updated to \n  " + number, Toast.LENGTH_SHORT).show();
            }
        });

        ad.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {

                interstitialAd.loadAd(adRequest);
                interstitialAd.show();
                Toast.makeText(getApplicationContext(), "Thanks For Your Support\n               \uD83D\uDE0A", Toast.LENGTH_SHORT).show();
            }
        });
        final SharedPreferences save = getSharedPreferences("AppData", MODE_PRIVATE);
        switchToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = save.edit();
                if (isChecked) {
                    toggle = true;
                    editor.putBoolean("ToggleApp", toggle);
                    editor.apply();
                    switchToggle.setBackgroundColor(Color.parseColor("#00ffffff"));
                    Toast.makeText(getApplicationContext(), "Now all services are Enabled!", Toast.LENGTH_SHORT).show();
                } else {
                    if (!isChecked) {
                        toggle = false;
                        editor.putBoolean("ToggleApp", toggle);
                        editor.apply();
                        switchToggle.setBackgroundColor(Color.parseColor("#abffffff"));
                        Toast.makeText(getApplicationContext(), "Now all services are Disabled!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                exit();
            }
        });

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_POWER) {
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void exit() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setCancelable(false)
                .setMessage("Exit the SmsAppTrigger?")
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                moveTaskToBack(true);
                                android.os.Process.killProcess(android.os.Process.myPid());
                                System.exit(1);
                            }
                        })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    private void requestStoragePermission() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_SMS,
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            //Toast.makeText(getApplicationContext(), "All permissions are granted!", Toast.LENGTH_SHORT).show();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    private void isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setCancelable(false);
            alertDialog.setTitle("Enable Location");
            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu and set to HighAccuracy.");
            alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = alertDialog.create();
            alert.show();
        }
    }
}
