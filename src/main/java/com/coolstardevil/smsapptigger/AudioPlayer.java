package com.coolstardevil.smsapptigger;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import static com.coolstardevil.smsapptigger.triggerIntent.playing;

public class AudioPlayer extends AppCompatActivity {
    Button stop;
    MediaPlayer mediaPlayer, mediaPlayer2;
    private InterstitialAd interstitialAd;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_player);
        interstitialAd = new InterstitialAd(this);

        interstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
        final AdRequest adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest);

        stop = (Button) findViewById(R.id.stop);
        mediaPlayer = MediaPlayer.create(this, R.raw.high);
        mediaPlayer2 = MediaPlayer.create(this, R.raw.lost);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopMusic();
                finish();
            }
        });
        mediaPlayer2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mediaPlayer2) {
                stopMusic();
                finish();
            }
        });

        if (playing) {
            mediaPlayer.start();
            mediaPlayer2.start();
        } else {
            stopMusic();
        }
        stop.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                stopMusic();
            }
        });
    }

    private void stopMusic() {
        mediaPlayer.release();
        mediaPlayer2.release();
        Toast.makeText(getApplicationContext(), "Music Stop!", Toast.LENGTH_SHORT).show();
        interstitialAd.show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
    }
}