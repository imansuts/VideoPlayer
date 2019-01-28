package work.demo.com.cameracropselfstudy.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import work.demo.com.cameracropselfstudy.ConstantPkg.Constant;
import work.demo.com.cameracropselfstudy.CustomViews.StretchVideoView;
import work.demo.com.cameracropselfstudy.CustomViews.VerticalSeekbar;
import work.demo.com.cameracropselfstudy.DataType.DataTypeVideoSeek;
import work.demo.com.cameracropselfstudy.Helper.MyGesture;
import work.demo.com.cameracropselfstudy.Interface.FensterEventsListener;
import work.demo.com.cameracropselfstudy.R;
import work.demo.com.cameracropselfstudy.Service.ServiseVideoView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, LocationListener, SensorEventListener {

    boolean aBoolean_check_for_mediaController_working = false, service_opening_if_not_backPressed = false;
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    String last_played_video_path = "";
    boolean aBoolean_check_bind = false, mServiceBound = false, aBoolean_check_for_service_opening = false;
    ServiseVideoView mBoundService;
    boolean check_for_play = false;
    Intent intent;
    AudioManager audioManager;
    int video_seek_pos = 0;
    //variable for counting two successive up-down events
    int clickCount_right = 0, clickCount_left = 0;
    //variable for storing the time of first click
    long startTime_right, endTime_right, time_interval_of_touch_right, time_interval_of_touch_left,
            startTime_left, endTime_left;
    //variable for calculating the total time
    long duration_right, duration_left;
    //constant for defining the time duration_right between the click that can be considered as double-tap
    static final int MAX_DURATION = 500;


    private int i_pos_video = 0;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 102;
    private Uri uri_path;
    //    private View decorView;
    private boolean aBoolean_check_for_hide_bars = false;
    private static final int MY_PERMISSIONS_REQUEST_FINE = 100;
    String string_video_path_reconstruction = "";
    int video_pos;
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 100;
    private com.theartofdev.edmodo.cropper.CropImageView imageView;
    //    private VideoView videoView;
    private StretchVideoView videoView;
    private MediaController mediaController;
    //    private ImageView imageView;
    private Button camera, gallery;
    private boolean aBoolean_for_play = false;


    //    protected LocationListener locationListener;
    protected Context context;
    TextView txtLat;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        registerReceiver(this.AppendCount, new IntentFilter(Constant.BROADCASTINTENT));


        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        context = MainActivity.this;
        intent = new Intent(MainActivity.this, ServiseVideoView.class);

//        decorView = getWindow().getDecorView();

        imageView = findViewById(R.id.imageView);
        camera = (Button) findViewById(R.id.camera);
        gallery = (Button) findViewById(R.id.gallery);
        videoView = findViewById(R.id.video_view);
        txtLat = (TextView) findViewById(R.id.textview1);

        camera.setOnClickListener(this);
        gallery.setOnClickListener(this);


        //Check if the application has draw over other apps permission or not?
        //This permission is by default available for API<23. But for API > 23
        //you have to ask for the permission in runtime.
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {


            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        } else {
            initializeView();
        }*/


        //Creating MediaController
        mediaController = new MediaController(this);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                ) {
            aBoolean_for_play = true;
        }
        TriggerIntentData();

        videoView.setPlayPauseListener(new StretchVideoView.PlayPauseListener() {
            @Override
            public void onPlay() {
                aBoolean_check_for_service_opening = true;
                video_seek_pos = videoView.getCurrentPosition();
                Log.d("pos_1: ", String.valueOf(video_seek_pos));
            }

            @Override
            public void onPause() {
                aBoolean_check_for_service_opening = false;
                video_seek_pos = videoView.getCurrentPosition();
            }
        });


        mediaController.setAnchorView(videoView);


        findViewById(R.id.btn_loc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (canGetLocation()) {
                    if (location != null) {
                        Log.d("Latitude: ", String.valueOf(location.getLatitude()));
                        Log.d("Longitude: ", String.valueOf(location.getLongitude()));
                        Toast.makeText(context, "See Log for value", Toast.LENGTH_SHORT).show();

                        try {
                            GetLocationDetails();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    showSettingsAlert();
                }

               /* try {
                    GetLocationDetails();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
        });


        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                Uri mUri = null;
                if (TextUtils.isEmpty(String.valueOf(uri_path)) && String.valueOf(uri_path).equals("null")
                        || String.valueOf(uri_path) == null) {

                    try {
                        Field mUriField = VideoView.class.getDeclaredField("mUri");
                        mUriField.setAccessible(true);
                        mUri = (Uri) mUriField.get(videoView);
                        uri_path = mUri;
                    } catch (Exception e) {
                    }
                }

                string_video_path_reconstruction = "";

                if (what == 100) {
                    videoView.stopPlayback();
                    PlayVideo(uri_path);

                } else if (what == 1) {

                    videoView.stopPlayback();
                    PlayVideo(uri_path);

                } else if (what == 800) {
                    videoView.stopPlayback();
                    PlayVideo(uri_path);
                } else if (what == 701) {
                    videoView.stopPlayback();
                    PlayVideo(uri_path);
                } else if (what == 700) {
                    videoView.stopPlayback();
                    PlayVideo(uri_path);
                } else if (what == -38) {
                    videoView.stopPlayback();
                    PlayVideo(uri_path);
                }
                return false;
            }
        });

        MyGesture myGesture = new MyGesture(MainActivity.this, videoView);
        final GestureDetector gestureDetector = new GestureDetector(MainActivity.this, myGesture);


        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        myGesture.SetListener(new FensterEventsListener() {
            @Override
            public void onTap() {
                mediaController.show();
                CustomizationOfStatusBarAndNavigationBAr();
                Log.d("check_touch_tap ", "true");
            }

            @Override
            public void onDoubleTap() {

                /*if (videoView.isPlaying()){
                    videoView.pause();
                    video_seek_pos = videoView.getCurrentPosition();

                }else {

                        videoView.setVideoURI(getVideoUri());
                        videoView.seekTo(video_seek_pos);
                        videoView.start();

                }*/
            }

            @Override
            public void onHorizontalScroll(MotionEvent event, float delta) {
                Log.d("check_touch_hori ", "true");

            }

            @Override
            public void onVerticalScroll(MotionEvent event, float delta) {
                Log.d("check_touch_verti ", "true");

            }

            @Override
            public void onSwipeRight(float v) {
                Log.d("check_touch_swipe_R", "true");
                videoView.seekTo((int) v * 100 + videoView.getCurrentPosition());
                mediaController.hide();
                CustomizationOfStatusBarAndNavigationBAr();
            }

            @Override
            public void onSwipeLeft(float v) {
                Log.d("check_touch_swipe_L", "true");
                videoView.seekTo((int) v * 100 + videoView.getCurrentPosition());
                mediaController.hide();
//                HideBars();
                CustomizationOfStatusBarAndNavigationBAr();
            }

            @Override
            public void onSwipeBottom(MotionEvent e2, MotionEvent e1) {
                Log.d("check_touch_swipe_B", "true");
                if (e2.getX() > videoView.getWidth() / 2 && e1.getX() > videoView.getWidth() / 2) {
                    audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);

                } else {
//                    Toast.makeText(context, "Swipe Bottom", Toast.LENGTH_SHORT).show();
                    int oldBrightness = 0;
                    try {
                        oldBrightness = Settings.System.getInt(
                                getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
                    } catch (Settings.SettingNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (oldBrightness < 0) {
                        oldBrightness = 0;
                    }
//                    android.provider.Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
//                            oldBrightness-(int)((e2.getX()-e1.getX())));

                    ScreenBrightness(oldBrightness - 50, MainActivity.this);

//                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
//                            oldBrightness-50);
                    Log.d("brightness_down: ", String.valueOf(oldBrightness + (int) ((e2.getX() - e1.getX()))));
                }
                mediaController.hide();
            }

            @Override
            public void onSwipeTop(MotionEvent e2, MotionEvent e1) {
                Log.d("check_touch_swipe_T", "true");
                if (e2.getX() > videoView.getWidth() / 2 && e1.getX() > videoView.getWidth() / 2) {
                    audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
                    /*int current_vol = audioManager.getStreamMaxVolume(AudioManager.FLAG_PLAY_SOUND);
                    int max_vol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    if (current_vol+50<=max_vol) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, current_vol + 50, AudioManager.ADJUST_RAISE);
                    }else {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max_vol, AudioManager.ADJUST_RAISE);
                    }*/
                } else {

                    int oldBrightness = 0;
                    try {
                        oldBrightness = Settings.System.getInt(getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS);
                    } catch (Settings.SettingNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (oldBrightness < 0) {
                        oldBrightness = 0;
                    }
//                    android.provider.Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
//                            oldBrightness+(int)((e2.getX()-e1.getX())));

                    ScreenBrightness(oldBrightness + 50, MainActivity.this);

//                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
//                            oldBrightness+50);
                    Log.d("brightness_up: ", String.valueOf(oldBrightness + (int) ((e2.getX() - e1.getX()))));
                }
                mediaController.hide();
            }

        });


        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                videoView.setMediaController(null);

                if ((event.getRawX() > videoView.getWidth() / 2) && (event.getRawX() < videoView.getWidth())) {
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:

                            startTime_right = System.currentTimeMillis();
                            time_interval_of_touch_right = startTime_right - endTime_right;
                            Log.d("interval_touch_right: ", String.valueOf(time_interval_of_touch_right));
                            if (clickCount_right == 1 && time_interval_of_touch_right > 500) {
                                clickCount_right = 0;
                                duration_right = 0;

                                clickCount_left = 0;
                                duration_left = 0;
                                break;
                            } else {
                                if (time_interval_of_touch_right > 500) {
                                    clickCount_right = 0;
                                    duration_right = 0;

                                    clickCount_left = 0;
                                    duration_left = 0;
                                }

                                /*if (clickCount_right == 0) {
                                    mediaController.show();
                                    ShowBars();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            HideBars();
                                        }
                                    }, 1500);
                                }*/
                                clickCount_right++;
                                break;
                            }

                        case MotionEvent.ACTION_UP:
                            endTime_right = System.currentTimeMillis();
                            if (clickCount_right > 1 && time_interval_of_touch_right < 500) {
//                                mediaController.hide();
                                long time = endTime_right - startTime_right;
                                duration_right = duration_right + time;
                                Log.d("count_touch: ", String.valueOf(clickCount_right));
                                if (clickCount_right > 1) {
//                                    if (duration_right <= MAX_DURATION) {
                                    int i = videoView.getCurrentPosition();
                                    i = i + (10000 * clickCount_right);  // it is millisecond
                                    mediaController.setAnchorView(videoView);
                                    videoView.seekTo(i);
//                                    }
//                                    clickCount_right = 0;
//                                    duration_right = 0;
                                    break;
                                }
                            }
                    }
                } else if ((event.getRawX() < videoView.getWidth() / 2) && (event.getRawX() > 0f)) {
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:

                            startTime_left = System.currentTimeMillis();
                            time_interval_of_touch_left = startTime_left - endTime_left;
                            if (clickCount_left == 0) {
                                mediaController.show();
                            }
                            Log.d("interval_touch_left: ", String.valueOf(time_interval_of_touch_left));
                            if (clickCount_left == 1 && time_interval_of_touch_left > 500) {
                                clickCount_left = 0;
                                duration_left = 0;

                                clickCount_right = 0;
                                duration_right = 0;
                                break;
                            } else {
                                if (time_interval_of_touch_left > 500) {
                                    clickCount_left = 0;
                                    duration_left = 0;

                                    clickCount_right = 0;
                                    duration_right = 0;
                                }
                                /*if (clickCount_left == 0) {
                                    mediaController.show();
                                    ShowBars();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            HideBars();
                                        }
                                    }, 1500);
                                }*/
                                clickCount_left++;
                                break;
                            }
                        case MotionEvent.ACTION_UP:
                            endTime_left = System.currentTimeMillis();
                            if (clickCount_left > 1 && time_interval_of_touch_left < 500) {
//                                mediaController.hide();
                                long time = endTime_left - startTime_left;
                                duration_left = duration_left + time;
                                Log.d("count_touch: ", String.valueOf(clickCount_left));
                                if (clickCount_left > 1) {
//                                    if (duration_right <= MAX_DURATION) {
                                    int i = videoView.getCurrentPosition();
                                    i = i - (10000 * clickCount_left);  // it is millisecond
                                    mediaController.setAnchorView(videoView);
                                    videoView.seekTo(i);
//                                    }
//                                    clickCount_right = 0;
//                                    duration_right = 0;
                                    break;
                                }
                            }
                    }
                }
                return gestureDetector.onTouchEvent(event);
            }
        });


    }

    private void AdjustBrightness(int brightnessValue) {
        if (brightnessValue >= 0 && brightnessValue <= 255) {
            Settings.System.putInt(
                    getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightnessValue
            );
        }
    }

    private void TriggerIntentData() {


        if (aBoolean_for_play) {
            if (getIntent() != null) {
                System.out.println("check_action_intent: " + getIntent().getAction());
                /*if (getIntent().getAction().equals(Constant.ACTION.MAIN_ACTION)){

                            try {
                                unbindService(mServiceConnection);
                            }catch (Exception e){
                            }
                        stopService(intent);


                    videoView.stopPlayback();
                    videoView.setVisibility(View.VISIBLE);

                    aBoolean_check_for_hide_bars = true;
//                        FeelLikeVideoPlayer();


                    PlayVideo(Uri.parse(getIntent().getExtras().getString("video_path")));
                    SetStatusBarTransparent();
                    videoView.seekTo(getIntent().getExtras().getInt("video_pos"));
                    videoView.resume();

                }else */

                if (getIntent().getAction().equals(Intent.ACTION_SEND) ||
                        getIntent().getAction().equals(Intent.ACTION_GET_CONTENT)) {
                    videoView.setVisibility(View.VISIBLE);
                    aBoolean_check_for_mediaController_working = false;

                    Log.d("action_check: ", String.valueOf(getIntent().getType()));

                    if (getIntent().getType().startsWith("video/")) {
                        Toast.makeText(this, "Hello", Toast.LENGTH_SHORT).show();

                        Log.d("path_action_send: ", String.valueOf(getIntent().getParcelableExtra(Intent.EXTRA_STREAM)));
                        aBoolean_check_for_hide_bars = true;
//                        FeelLikeVideoPlayer();

                        PlayVideo((Uri) getIntent().getParcelableExtra(Intent.EXTRA_STREAM));

                    }

                } else if (getIntent().getAction().equals(Intent.ACTION_MAIN)) {
                   /* videoView.setVisibility(View.VISIBLE);
                    videoView.setMediaController(mediaController);
                    mediaController.setMediaPlayer(videoView);
//                    videoView.setVideoURI((Uri.parse("https://s3.amazonaws.com/qbprod/1b3449ae003e4cbea88db997d014f77300")));
                    videoView.setVideoURI((Uri.parse("http://www.w3schools.com/html/mov_bbb.mp4")));
                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
//                            try {
//                                mp.prepare();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
                            mp.start();
                        }
                    });*/


//                    PlayVideo(Uri.parse("https://s3.amazonaws.com/qbprod/1b3449ae003e4cbea88db997d014f77300"));
                    aBoolean_check_for_mediaController_working = false;
                    //app has been launched directly, not from share list


//                    startActivity(new Intent(MainActivity.this, VideoListActivity.class));

                    startActivity(new Intent(MainActivity.this, VideoListActivity.class));
                    finish();
                } else if (getIntent().getAction().equals(Intent.ACTION_VIEW)) {
                    videoView.setVisibility(View.VISIBLE);
                    aBoolean_check_for_mediaController_working = false;

                    // App will be launched when video from gallery is clicked and video will be played in app
                    Log.d("action_check: ", String.valueOf(getIntent().getType()));

//                    if (getIntent().getType().startsWith("video/")) {
                    // App will be launched when video from gallery is clicked and video will be played in app


                    Log.d("path_action_opened: ", String.valueOf(getIntent().getData()));
                    aBoolean_check_for_hide_bars = true;


                    PlayVideo(getIntent().getData());
                    SetStatusBarTransparent();
//                    }
                } else if (getIntent().getAction().equals(Constant.ACTION.INIT_ACTION)) {
                    aBoolean_check_for_mediaController_working = true;
                    videoView.setVisibility(View.VISIBLE);
                    String s = getIntent().getExtras().getString("video_path");

                    Log.d("path_action_opened: ", s);
                    aBoolean_check_for_hide_bars = true;

                    PlayVideo(Uri.parse(s));
                    SetStatusBarTransparent();

                } else if (getIntent().getAction().equals(Constant.INTENT_FOR_REOPEN_ACTIVITY)) {
                    videoView.setVisibility(View.VISIBLE);
                    aBoolean_check_for_mediaController_working = false;

                    aBoolean_check_for_hide_bars = true;

                    int i_pos = getIntent().getExtras().getInt("Video_pos");


                    Log.d("chck_path_uri: ", getIntent().getExtras().getString("Video_uri"));
//                    PlayVideo(Uri.parse(getIntent().getExtras().getString("Video_uri")));
                    videoView.setMediaController(mediaController);
                    mediaController.setMediaPlayer(videoView);
                    FeelLikeVideoPlayer();


                    Log.d("posssss: ", String.valueOf(i_pos));
                    Log.d("check_for_open: ", String.valueOf(getIntent().getExtras().getBoolean("Video_play_pause_check")));
                    if (getIntent().getExtras().getBoolean("Video_play_pause_check")) {
                        videoView.setVideoPath(getIntent().getExtras().getString("Video_uri"));
                        videoView.seekTo(i_pos);
                        videoView.start();
                    } else {
                        videoView.setVideoPath(getIntent().getExtras().getString("Video_uri"));
                        videoView.seekTo(i_pos);
                        videoView.pause();
                    }
                    SetStatusBarTransparent();

                    mediaController.setPrevNextListeners(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (!aBoolean_check_for_mediaController_working) {
                                PlayPrevVideo();
                                if (aBoolean_check_for_hide_bars) {
                                    FeelLikeVideoPlayer();
                                }
                                CustomizationOfStatusBarAndNavigationBAr();

                            } else {
                                Toast.makeText(MainActivity.this, "Select Video from the list", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!aBoolean_check_for_mediaController_working) {
                                PlayNextVideo();

                                if (aBoolean_check_for_hide_bars) {
                                    FeelLikeVideoPlayer();
                                }
                                CustomizationOfStatusBarAndNavigationBAr();

                            } else {
                                Toast.makeText(MainActivity.this, "Select Video from the list", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            if (!aBoolean_check_for_mediaController_working) {
                                PlayNextVideo();
                            }
                        }
                    });
                }
            }
        }
    }

    private void SetStatusBarTransparent() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    private void FeelLikeVideoPlayer() {
        findViewById(R.id.btn_loc).setVisibility(View.GONE);
        camera.setVisibility(View.GONE);
        gallery.setVisibility(View.GONE);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        videoView.setLayoutParams(layoutParams);

        HideBars();
    }

    private void HideBars() {
        View decorView = getWindow().getDecorView();
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void ShowBars() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }


    @Override
    public void onResume() {
        super.onResume();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {


            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        } else {
//            initializeView();
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED
                    ) {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_SETTINGS
                        },
                        MY_PERMISSIONS_REQUEST_FINE);
            }
        }

        Log.d("aBoolean_service ", String.valueOf(aBoolean_check_for_service_opening));
        if (aBoolean_check_for_service_opening) {
            if (aBoolean_check_bind) {
                if (mBoundService.getVideoPos() != 0) {
                    videoView.stopPlayback();
                    videoView.setVideoURI(Uri.parse(mBoundService.getVideoPath()));
                    videoView.seekTo(mBoundService.getVideoPos() + 50);
                    videoView.start();
                    aBoolean_check_bind = false;

                    Log.d("pos_service_back: ", String.valueOf(mBoundService.getVideoPos()));

                }
            }
        } else {

            videoView.seekTo(video_seek_pos);
            videoView.pause();
            /*if (aBoolean_check_bind) {
                if (mBoundService.getVideoPos() != 0) {
                    videoView.stopPlayback();
                    videoView.setVideoURI(Uri.parse(mBoundService.getVideoPath()));
                    videoView.seekTo(mBoundService.getVideoPos());
                    videoView.pause();
                    aBoolean_check_bind = false;

                }
            }*/
        }

        if (check_for_play) {
            if (mServiceBound) {
                try {
                    unbindService(mServiceConnection);

                } catch (Exception e) {

                }
            }
            stopService(intent);
        }


//        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (check_for_play) {
            if (mServiceBound) {
                try {
                    unbindService(mServiceConnection);

                } catch (Exception e) {

                }
            }
            stopService(intent);
        }
//        unregisterReceiver(AppendCount);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        service_opening_if_not_backPressed = true;
        if (check_for_play) {
            if (mServiceBound) {
                try {
                    unbindService(mServiceConnection);

                } catch (Exception e) {

                }
            }
            stopService(intent);
        }

    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @SuppressLint("MissingPermission")
    public Location getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {

                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }

                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {

                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("GPS Enabled", "GPS Enabled");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }

                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

//        GetLocationDetails();

        return location;
    }

    private boolean CheckForFileExistedOrNot(String s) {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.MANAGE_DOCUMENTS)
                != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.MANAGE_DOCUMENTS
                    },
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
            // app-defined int constant that should be quite unique


        } else {
            ContentResolver cr = getContentResolver();
            String[] projection = {MediaStore.MediaColumns.DATA};
            Cursor cur = cr.query(Uri.parse(s), projection, null, null, null);
            if (cur != null) {
                if (cur.moveToFirst()) {
                    String filePath = cur.getString(0);

                    if (new File(filePath).exists()) {
                        // do something if it exists
                        return true;
                    } else {
                        // File was not found
                    }
                } else {
                    // Uri was ok but no entry found.
                }
                cur.close();
            } else {
                // content Uri was invalid or some other error occurred
            }
        }
        return false;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.camera) {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(MainActivity.this);
//            CropImage.startPickImageActivity(MainActivity.this);
        } else if (v.getId() == R.id.gallery) {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
        }

    }


    private void initializeView() {
        onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            //Check if the permission is granted or not.
            if (android.os.Build.VERSION.SDK_INT >= 23) {   //Android M Or Over
                if (Settings.canDrawOverlays(this)) {
                    // ADD UI FOR USER TO KNOW THAT UI for SYSTEM_ALERT_WINDOW permission was not granted earlier...
                    initializeView();
                }
            }
            /*if (resultCode == RESULT_OK) {
                initializeView();
            } else {

            }*/
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                imageView.setImageUriAsync(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        } else if (requestCode == REQUEST_TAKE_GALLERY_VIDEO && resultCode == RESULT_OK) {

            videoView.setVisibility(View.VISIBLE);

            string_video_path_reconstruction = "";
            Uri selectedImageUri = data.getData();
            uri_path = selectedImageUri;
            Log.d("video_check_string: ", String.valueOf(selectedImageUri));

            /*video_pos = Integer.parseInt(splitString(String.valueOf(selectedImageUri))[(splitString(String.valueOf(selectedImageUri)).length)-1]);
            Log.d("video_check: ", String.valueOf(video_pos));

            for (int i = 0; i<(splitString(String.valueOf(selectedImageUri)).length)-1; i++){
                Log.d("video_check-"+i+" ",splitString(String.valueOf(selectedImageUri))[i]);
                string_video_path_reconstruction = string_video_path_reconstruction+ splitString(String.valueOf(selectedImageUri))[i];
            }
            Log.d("video_check_path: ", string_video_path_reconstruction);*/

            // OI FILE Manager
            /*String filemanagerstring = selectedImageUri.getPath();

            // MEDIA GALLERY
            String selectedImagePath = getPath(selectedImageUri);
            if (selectedImagePath != null) {

                Intent intent = new Intent(MainActivity.this,
                        VideoplayAvtivity.class);
                intent.putExtra("path", selectedImagePath);
                startActivity(intent);
            }*/


            //Setting MediaController and URI, then starting the videoView
            PlayVideo(selectedImageUri);
        }
    }

    // UPDATED!
    /*public String getPath(Uri uri) {
        String[] projection = { MediaStore.Video.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }*/

    /*public String[] splitString(String s) {
        List<String> results = new ArrayList<>();
        int start = 0;

        while (true) {
            int next = s.indexOf('/', start);
            Log.d("next_check: " , String.valueOf(next));
            if (next < 0) {
                break;
            }
            results.add(s.substring(start, next));
            results.add(s.substring(next + 1));
            start = next + 1;
        }

        return results.toArray(new String[results.size()]);
    }*/

    private ArrayList<String> SplitStringToArray(String s) {
        String[] strings = s.split("/");
        ArrayList<String> splitted_strings = new ArrayList<>();

        for (String s1 : strings) {
            splitted_strings.add(s1);
        }

        return splitted_strings;
    }

    private void PlayVideo(Uri uri) {

        if (aBoolean_check_for_hide_bars) {
            FeelLikeVideoPlayer();
        }

        Log.d("video_check_path: ", SplitStringToArray(String.valueOf(uri)).toString());
        if (!aBoolean_check_for_mediaController_working) {
            if (!SplitStringToArray(String.valueOf(uri)).get(SplitStringToArray(String.valueOf(uri)).size() - 1).equals("null")) {
                try {

                    video_pos = Integer.parseInt(SplitStringToArray(String.valueOf(uri)).get(SplitStringToArray(String.valueOf(uri)).size() - 1));
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("error_check: ", e.getMessage());
                }
            }
        }

        for (int i = 0; i < SplitStringToArray(String.valueOf(uri)).size(); i++) {
            String s = "";
            if (TextUtils.isEmpty(SplitStringToArray(String.valueOf(uri)).get(i))) {
                s = "/";
                string_video_path_reconstruction = string_video_path_reconstruction + s;
            } else {
                if (i != SplitStringToArray(String.valueOf(uri)).size() - 1) {
                    string_video_path_reconstruction = string_video_path_reconstruction + SplitStringToArray(String.valueOf(uri)).get(i) + "/";
                }
            }
        }
        Log.d("path_check: ", string_video_path_reconstruction);
        Log.d("path_check_uri: ", String.valueOf(uri));

//        mediaController = new MediaController(MainActivity.this);
        videoView.setMediaController(mediaController);
        mediaController.setMediaPlayer(videoView);
        videoView.setVideoURI(uri);
//        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
            }
        });
//        videoView.start();


        mediaController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!aBoolean_check_for_mediaController_working) {
                    PlayPrevVideo();
                    if (aBoolean_check_for_hide_bars) {
                        FeelLikeVideoPlayer();
                    }
                    CustomizationOfStatusBarAndNavigationBAr();
                   /* if (new File(URI.create(s).getPath()).exists()) {
                        videoView.setVideoURI(Uri.parse(s));
                        videoView.requestFocus();
                        videoView.start();
                    }else {
                        Toast.makeText(MainActivity.this, "No more videos", Toast.LENGTH_SHORT).show();
                    }*/
                } else {
                    Toast.makeText(MainActivity.this, "Select Video from the list", Toast.LENGTH_SHORT).show();
                }
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!aBoolean_check_for_mediaController_working) {
                    PlayNextVideo();

                    if (aBoolean_check_for_hide_bars) {
                        FeelLikeVideoPlayer();
                    }
                    CustomizationOfStatusBarAndNavigationBAr();
                    /*if (new File(URI.create(s).getPath()).exists()) {
                        videoView.setVideoURI(Uri.parse(s));
                        videoView.requestFocus();
                        videoView.start();
                    }else {
                        Toast.makeText(MainActivity.this, "No more videos", Toast.LENGTH_SHORT).show();
                    }*/
                } else {
                    Toast.makeText(MainActivity.this, "Select Video from the list", Toast.LENGTH_SHORT).show();
                }
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (!aBoolean_check_for_mediaController_working) {
                    PlayNextVideo();
                }
            }
        });

        CustomizationOfStatusBarAndNavigationBAr();


    }

    private void PlayPrevVideo() {
        String s = "";
        s = string_video_path_reconstruction + (--video_pos);
        Log.d("video_check_next: ", s);
        if (CheckForFileExistedOrNot(s)) {
            videoView.setVideoURI(Uri.parse(s));
//                    videoView.requestFocus();
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    videoView.start();
                }
            });
        } else {

            do {
                s = "";
                s = string_video_path_reconstruction + (--video_pos);
            } while (CheckForFileExistedOrNot(s));
            videoView.setVideoURI(Uri.parse(s));
//                    videoView.requestFocus();
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    videoView.start();
                }
            });

        }
    }

    private void PlayNextVideo() {
        String s = "";
        s = string_video_path_reconstruction + (++video_pos);
        Log.d("video_check_next: ", s);
        if (CheckForFileExistedOrNot(s)) {
            videoView.setVideoURI(Uri.parse(s));
//                    videoView.requestFocus();
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    videoView.start();
                }
            });
        } else {

            do {
                s = "";
                s = string_video_path_reconstruction + (++video_pos);
            } while (CheckForFileExistedOrNot(s));
            if (!TextUtils.isEmpty(s)) {
                videoView.setVideoURI(Uri.parse(s));
//                    videoView.requestFocus();
                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        videoView.start();
                    }
                });
            }

        }
    }

    private void CustomizationOfStatusBarAndNavigationBAr() {

        ShowBars();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SetStatusBarTransparent();
                HideBars();
            }
        }, 1500);
    }

    @Override
    public void onLocationChanged(Location location) {

        this.location = location;

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    private void GetLocationDetails() throws IOException {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> addresses = null;
        if (location != null) {
            if (geocoder.isPresent()) {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } else {
                Toast.makeText(context, "GeoCoder is gone", Toast.LENGTH_SHORT).show();
            }
            /*try {

            } catch (IOException e) {
                e.printStackTrace();
            }*/
            String address1 = addresses.get(0).getAddressLine(0);
            String address2 = addresses.get(0).getAddressLine(1);
            String address3 = addresses.get(0).getAddressLine(2);
            String address4 = addresses.get(0).getAddressLine(3);
            String flat_building_no = addresses.get(0).getFeatureName();
            String state = addresses.get(0).getAdminArea();
            String district = addresses.get(0).getSubAdminArea();
            String locality = addresses.get(0).getLocality();
            String thoroughfare = addresses.get(0).getThoroughfare();
            String postalcode = addresses.get(0).getPostalCode();
            String countrycode = addresses.get(0).getCountryCode();
            String countryname = addresses.get(0).getCountryName();


//        Toast.makeText(this, flat_building_no, Toast.LENGTH_SHORT).show();
            System.out.println("address1: " + address1);
            System.out.println("address2: " + address2);
            System.out.println("address3: " + address3);
            System.out.println("address4: " + address4);
            System.out.println("flat_building_no: " + flat_building_no);
            System.out.println("state: " + state);
            System.out.println("district: " + district);
            System.out.println("locality: " + locality);
            System.out.println("thoroughfare: " + thoroughfare);
            System.out.println("postalcode: " + postalcode);
            System.out.println("countrycode: " + countrycode);
            System.out.println("countryname: " + countryname);

            Toast.makeText(context, state, Toast.LENGTH_SHORT).show();


//            Log.d("address1: ",address1);
//            Log.d("address2: ",address2);
//            Log.d("address3: ",address3);
//            Log.d("address4: ",address4);
//            Log.d("flat_building_no: ", flat_building_no);
//            Log.d("state: ",state);
//            Log.d("district: ",district);
//            Log.d("locality: ",locality);
//            Log.d("thoroughfare: ",thoroughfare);
//            Log.d("postalcode: ",postalcode);
//            Log.d("countrycode: ",countrycode);
//            Log.d("countryname: ",countryname);


        } else {
//            Toast.makeText(this, "Location is null", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


//                    getLocation();
                    aBoolean_for_play = true;
                    TriggerIntentData();
//                    Log.d("Latitude: ", String.valueOf(getLocation().getLatitude()));
//                    Log.d("Longitude: ", String.valueOf(getLocation().getLongitude()));

                } else {
//                    Toast.makeText(this, "Permission Failed", Toast.LENGTH_SHORT).show();

                }
            }
            default:
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        turnGPSOff();
        senSensorManager.unregisterListener(this);

        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }

        video_seek_pos = videoView.getCurrentPosition();
        last_played_video_path = String.valueOf(getVideoUri());
    }


    private Uri getVideoUri() {

        Uri mUri = null;
        try {
            Field mUriField = VideoView.class.getDeclaredField("mUri");
            mUriField.setAccessible(true);
            mUri = (Uri) mUriField.get(videoView);
        } catch (Exception e) {
        }

        return mUri;
    }

    @Override
    protected void onStop() {


        if (videoView.getCurrentPosition() != 0) {
            video_seek_pos = videoView.getCurrentPosition();
        }
        last_played_video_path = String.valueOf(getVideoUri());
        videoView.stopPlayback();

        Log.d("seek_check_pos: ", String.valueOf(video_seek_pos));
        Log.d("seek_check_path: ", String.valueOf(getVideoUri()));

//        if (videoView.isPlaying() && mUri!=null) {
        if (!service_opening_if_not_backPressed) {
            if (aBoolean_check_for_service_opening) {
                intent.putExtra("video_seek_pos", video_seek_pos + 150);
                intent.putExtra("video_uri", last_played_video_path);
                if (GetCheckingOfController()) {
                    intent.putExtra("boolean_chk_for_btn_show", true);
                } else {
                    intent.putExtra("boolean_chk_for_btn_show", false);
                }
                intent.setAction(Constant.ACTION.STARTFOREGROUND_ACTION);
                startService(intent);
                aBoolean_check_bind = true;
                bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
                check_for_play = true;
            }
        }

        super.onStop();

    }

    private void turnGPSOn() {
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (!provider.contains("gps")) { //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }
    }

    private void turnGPSOff() {
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (provider.contains("gps")) { //if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }
    }


    private boolean canToggleGPS() {
        PackageManager pacman = getPackageManager();
        PackageInfo pacInfo = null;

        try {
            pacInfo = pacman.getPackageInfo("com.android.settings", PackageManager.GET_RECEIVERS);
        } catch (PackageManager.NameNotFoundException e) {
            return false; //package not found
        }

        if (pacInfo != null) {
            for (ActivityInfo actInfo : pacInfo.receivers) {
                //test if recevier is exported. if so, we can toggle GPS.
                if (actInfo.name.equals("com.android.settings.widget.SettingsAppWidgetProvider") && actInfo.exported) {
                    return true;
                }
            }
        }
        return false; //default
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        //To change body of implemented methods use File | Settings | File Templates.
        Sensor mySensor = event.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float z = event.values[2];

            Log.d("value_z: ", String.valueOf(z));

            if (event.values[2] > -4) {
                if (!videoView.isPlaying()) {
                    videoView.seekTo(i_pos_video);
                    videoView.start();
                }
            } else {
                if (videoView.isPlaying()) {
                    i_pos_video = videoView.getCurrentPosition();
                    videoView.pause();
                }
            }

            Log.d("video_pos: ", String.valueOf(i_pos_video));


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("sensor_check: ", String.valueOf(sensor));
        Log.d("accuracy_check: ", String.valueOf(accuracy));


    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        video_seek_pos = videoView.getCurrentPosition();
        outState.putInt("SeekPos", video_seek_pos);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);

        video_seek_pos = savedInstanceState.getInt("SeekPos");
        videoView.seekTo(video_seek_pos);
        videoView.start();
    }

    @Subscribe
    public void onEvent(DataTypeVideoSeek dataTypeVideoSeek) {
        if (dataTypeVideoSeek.getVideo_seek() != 0) {
            videoView.seekTo(dataTypeVideoSeek.getVideo_seek());
            videoView.start();
            Log.d("test_event: ", String.valueOf(dataTypeVideoSeek.getVideo_seek()));
        }
    }

    BroadcastReceiver AppendCount = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            video_seek_pos = b.getInt(Constant.VIDEO_POS);
            if (video_seek_pos != 0) {
                videoView.seekTo(video_seek_pos);
                Log.d("test_pos_broadcast: ", String.valueOf(video_seek_pos));
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;

            Log.d("onServiceDisconnected: ", "true");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ServiseVideoView.MyBinder myBinder = (ServiseVideoView.MyBinder) service;
            mBoundService = myBinder.getService();
            mServiceBound = true;

            ((ServiseVideoView.MyBinder) service).PassDataFromActivityToService(MainActivity.this);

            Log.d("onServiceConnected: ", "true");
        }
    };


    boolean ScreenBrightness(int level, Context context) {

        try {
            android.provider.Settings.System.putInt(
                    context.getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS, level);


            android.provider.Settings.System.putInt(context.getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
                    android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

            android.provider.Settings.System.putInt(
                    context.getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS,
                    level);


            return true;
        } catch (Exception e) {
            Log.e("Screen Brightness", "error changing screen brightness");
            return false;
        }
    }




    /*public void fn_video() {

        int int_position = 0;
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name,column_id,thum;

        String absolutePathOfImage = null;
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME,MediaStore.Video.Media._ID,MediaStore.Video.Thumbnails.DATA};

        final String orderBy = MediaStore.Images.Media.DISPLAY_NAME;
        cursor = getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
        column_id = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
        thum = cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA);

        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            Log.e("Column", absolutePathOfImage);
            Log.e("Folder", cursor.getString(column_index_folder_name));
            Log.e("column_id", cursor.getString(column_id));
            Log.e("thum", cursor.getString(thum));

            Model_Video obj_model = new Model_Video();
            obj_model.setBoolean_selected(false);
            obj_model.setStr_path(absolutePathOfImage);
            obj_model.setStr_thumb(cursor.getString(thum));

            al_video.add(obj_model);

        }
        Log.d("chck_lis: ", String.valueOf(al_video.size()));
        Log.d("chk_path: ", al_video.get(10).getStr_path());
        Log.d("chk_thumb: ", al_video.get(10).getStr_thumb());

    }*/

    public boolean GetCheckingOfController() {
        return aBoolean_check_for_mediaController_working;
    }


    private void initControls() {
        ViewGroup.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        VerticalSeekbar volumeSeekbar = new VerticalSeekbar(context);
        volumeSeekbar.setLayoutParams(layoutParams);

        ShapeDrawable thumb = new ShapeDrawable(new OvalShape());
        thumb.setIntrinsicHeight(80);
        thumb.setIntrinsicWidth(30);
        volumeSeekbar.setThumb(thumb);
        volumeSeekbar.setProgress(1);
        volumeSeekbar.setVisibility(View.VISIBLE);
        volumeSeekbar.setBackgroundColor(Color.BLUE);

        try {
//            volumeSeekbar = (SeekBar)findViewById(R.id.seekBar1);
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volumeSeekbar.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumeSeekbar.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));


            volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            progress, 0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}