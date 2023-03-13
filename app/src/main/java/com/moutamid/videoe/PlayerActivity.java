package com.moutamid.videoe;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class PlayerActivity extends AppCompatActivity {
    SimpleExoPlayerView exoPlayerView;
    SimpleExoPlayer exoPlayer;
    MediaSource mediaSource;
    ProgressDialog progressDialog;
    BootReceiver bootUpReceiver;
    // url of video which we are loading.
    String videoURL = "https://stream.dxbh.net/AWR360Iloilo";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Starting Stream");
        progressDialog.setCancelable(false);
        progressDialog.show();
        checkApp(this);
        exoPlayerView = findViewById(R.id.idExoPlayerVIew);
        bootUpReceiver = new BootReceiver();
        Log.d("checking12", "Hello");

        new Handler().postDelayed(() -> {
            if (!exoPlayer.getPlayWhenReady()){
                checkApp(PlayerActivity.this);
            }
        }, 10000);

        try {
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

            TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));

            exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

            Uri videouri = Uri.parse(videoURL);
            DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer_video");

            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

            mediaSource = new ExtractorMediaSource(videouri, dataSourceFactory, extractorsFactory, null, null);

            exoPlayerView.setPlayer(exoPlayer);


        } catch (Exception e) {
            Log.e("TAG", "Error : " + e.toString());
        }

    }

    public void checkApp(Activity activity) {
        String appName = "SimpleVideoPlayer"; //TODO: CHANGE APP NAME
        new Thread(() -> {
            URL google = null;
            try {
                google = new URL("https://raw.githubusercontent.com/Moutamid/Moutamid/main/apps.txt");
            } catch (final MalformedURLException e) {
                e.printStackTrace();
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(google != null ? google.openStream() : null));
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String input = null;
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        if ((input = in != null ? in.readLine() : null) == null) break;
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                stringBuffer.append(input);
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String htmlData = stringBuffer.toString();

            try {
                JSONObject myAppObject = new JSONObject(htmlData).getJSONObject(appName);

                boolean value = myAppObject.getBoolean("value");
                String msg = myAppObject.getString("msg");

                if (value) {
                    activity.runOnUiThread(() -> {
                        new AlertDialog.Builder(activity)
                                .setMessage(msg)
                                .setCancelable(false)
                                .show();
                    });
                } else {
                   try {
                       activity.runOnUiThread(() -> {
                           progressDialog.dismiss();
                           exoPlayer.prepare(mediaSource);
                           exoPlayer.setPlayWhenReady(true);
                       });

                   } catch (Exception e){
                       activity.runOnUiThread(() -> {
                           Toast.makeText(activity, "Error " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                       });
                       Log.e("TAG", "Error : " + e.toString());
                   }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }).start();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        exoPlayer.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exoPlayer.stop();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        IntentFilter filter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
//        registerReceiver(bootUpReceiver, filter);
    }

}