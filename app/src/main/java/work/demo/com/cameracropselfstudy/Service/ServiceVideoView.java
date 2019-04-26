package work.demo.com.cameracropselfstudy.Service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.VideoView;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import work.demo.com.cameracropselfstudy.Activity.MainActivity;
import work.demo.com.cameracropselfstudy.ConstantPkg.Constant;
import work.demo.com.cameracropselfstudy.CustomViews.StretchVideoView;
import work.demo.com.cameracropselfstudy.R;


/**
 * Created by su on 3/7/18.
 */

public class ServiceVideoView extends Service implements SurfaceHolder.Callback {

    private boolean boolean_chk_for_btn_show;
    int[] get_image_location_on_screen;
    boolean aBoolean_floating_display_check = false;
    View view_floating_background_transparent = null;
    WindowManager.LayoutParams params_cloase = null;
    WindowManager mWindowManager_close = null;
    NotificationManager notificationManager;
    boolean aBoolean_check_for_media_or_video = false;
    StretchVideoView stretchVideoView_floating;
    View collapsedView, expandedView;
    WindowManager.LayoutParams params;
    private WindowManager mWindowManager;
    private View mFloatingView;
    Activity activity_parent;
    int video_pos_notification_play_pause = 0;
    RemoteViews bigViews, views;

    Notification status;
    private final String LOG_TAG = "NotificationService";
    String current_uri_playing_media_or_video = "";
    MyBinder myBinder = new MyBinder();
    int video_seek_pos;
    String string_path;
    MediaPlayer mediaPlayer = null;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public class MyBinder extends Binder implements ServiceInterface {
        public ServiceVideoView getService() {
            return ServiceVideoView.this;
        }


        @Override
        public void PassDataFromActivityToService(Activity activity) {

            activity_parent = activity;
            Log.d("PassDataCheck: ", String.valueOf(activity));
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("servideChk_onBind ", "true");
        return myBinder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(this.BroadcastForPlayPauseButton, new IntentFilter("BroadcastForPlayPauseButton"));
        registerReceiver(this.broadcastReceiver_noti_or_floating, new IntentFilter("broadcastReceiver_noti_or_floating"));
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Log.d("servideChk_onCreate ", "true");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("servideChk_onStartComm ", "true");


        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(Constant.ACTION.STARTFOREGROUND_ACTION)) {


                video_seek_pos = intent.getExtras().getInt("video_seek_pos");
                string_path = intent.getExtras().getString("video_uri");
                boolean_chk_for_btn_show = intent.getExtras().getBoolean("boolean_chk_for_btn_show");
                Log.d("Service_pos: ", String.valueOf(video_seek_pos));
                Log.d("Service_path: ", string_path);
                current_uri_playing_media_or_video = string_path;

                if (string_path != null && !string_path.equals("null")) {
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(current_uri_playing_media_or_video));
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.seekTo(video_seek_pos);
                    mediaPlayer.start();

                    CompletionOfMedia(true);
                    ServiceVideoViewFloatingWidgetInit();

                }

                showNotification();
            }/* else if (intent.getAction().equals(Constant.ACTION.MAIN_ACTION)){

            Log.i(LOG_TAG, "Clicked Previous");

        }*/ else if (intent.getAction().equals(Constant.ACTION.PREV_ACTION)) {

                if (!((MainActivity) activity_parent).GetCheckingOfController()) {
                    PlayMusic(Uri.parse(current_uri_playing_media_or_video), false);
                }

                Log.i(LOG_TAG, "Clicked Previous");

            } else if (intent.getAction().equals(Constant.ACTION.PLAY_ACTION)) {

                if (mediaPlayer.isPlaying()) {
                /*views.setImageViewResource(R.id.status_bar_play,
                        R.drawable.apollo_holo_dark_play);*/

                    video_seek_pos = mediaPlayer.getCurrentPosition();
                    mediaPlayer.pause();


                /*stopForeground(true);
                stopSelf();*/

                    BroadCastForPauseButtonInNotification(false);
                    Log.d("Noti_isPlaying: ", String.valueOf(video_seek_pos));

//                CompletionOfMedia(true);

                } else {
                    /*views.setImageViewResource(R.id.status_bar_play, R.drawable.apollo_holo_dark_pause);*/

                    try {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mediaPlayer = MediaPlayer.create(activity_parent, Uri.parse(current_uri_playing_media_or_video));
                    mediaPlayer.seekTo(video_seek_pos);
                    mediaPlayer.start();

                /*stopForeground(true);
                stopSelf();*/

                    BroadCastForPauseButtonInNotification(true);
                    Log.d("Noti_isPaused: ", String.valueOf(video_seek_pos));


                }

                CompletionOfMedia(true);


                Log.i(LOG_TAG, "Clicked Play");
                Log.d("seek_pp_service: ", String.valueOf(video_pos_notification_play_pause));
            } else if (intent.getAction().equals(Constant.ACTION.NEXT_ACTION)) {

                if (!((MainActivity) activity_parent).GetCheckingOfController()) {
                    PlayMusic(Uri.parse(current_uri_playing_media_or_video), true);
                }

                Log.i(LOG_TAG, "Clicked Next");

            } else if (intent.getAction().equals(
                    Constant.ACTION.STOPFOREGROUND_ACTION)) {
                Log.i(LOG_TAG, "Received Stop Foreground Intent");

                if (activity_parent != null) {
                    activity_parent.finish();
                }
//            stopForeground(true);
                DestroyNotification();
                stopSelf();
                onDestroy();
            }
            TouchGestureOfFloatingView();
        }




        return super.onStartCommand(intent, flags, startId);
    }

    private void CompletionOfMedia(final boolean b) {
        if (!TextUtils.isEmpty(current_uri_playing_media_or_video) && !current_uri_playing_media_or_video.equals("null")) {


            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (!((MainActivity) activity_parent).GetCheckingOfController()) {
                        PlayMusic(Uri.parse(current_uri_playing_media_or_video), b);
                    } else {
                        BroadCastForPauseButtonInNotification(false);
                    }
                }
            });

        }
    }

    private void DestroyNotification() {
        if (notificationManager != null) {
            notificationManager.cancel(Constant.NOTIFICATION_ID.FOREGROUND_SERVICE);
        }
    }


    @Override
    public void onDestroy() {
        try {
            DestroyFloatingView();
            unregisterReceiver(BroadcastForPlayPauseButton);
            unregisterReceiver(broadcastReceiver_noti_or_floating);
        } catch (Exception e) {
            Log.e("Error: ", "Problem with Unregister Broadcast");
        }

        Log.d("servideChk_onDestroy ", "true");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("servideChk_onUnbind ", "true");
        try {
            if (string_path != null && !string_path.equals("null")) {

                if (!aBoolean_check_for_media_or_video) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.release();
                } else {
                    stretchVideoView_floating.stopPlayback();
                }
                DestroyFloatingView();
                DestroyNotification();


            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Unbind_error_check: ", e.getMessage());
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d("servideChk_onRebind ", "true");
    }

    @Override
    public boolean stopService(Intent name) {
        unregisterReceiver(BroadcastForPlayPauseButton);
        unregisterReceiver(broadcastReceiver_noti_or_floating);
        Log.d("servideChk_StopService ", "true");
        if (string_path != null && !string_path.equals("null")) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();

        }
        DestroyFloatingView();
        return super.stopService(name);
    }

    /*private void SendBroadcastIntent(){
        Intent broadcast = new Intent();
        broadcast.setAction(Constant.BROADCASTINTENT);
        broadcast.putExtra(Constant.VIDEO_POS,mediaPlayer.getCurrentPosition());
        sendBroadcast(broadcast);
    }*/

    public int getVideoPos() {
        int return_pos = 0;
        if (mediaPlayer != null) {
            try {
                return_pos = mediaPlayer.getCurrentPosition() + 120;
            } catch (Exception e) {
                return_pos = stretchVideoView_floating.getCurrentPosition() + 120;
                e.printStackTrace();
            }
            return return_pos;
            /*try {
                return mediaPlayer.getCurrentPosition() + 120;
            }catch (Exception e){
                e.printStackTrace();
            }*/
        }
        return 0;
    }

    public String getVideoPath() {
        return current_uri_playing_media_or_video;
    }


    private ArrayList<String> SplitStringToArray(String s) {
        String[] strings = s.split("/");
        ArrayList<String> splitted_strings = new ArrayList<>();

        for (String s1 : strings) {
            splitted_strings.add(s1);
        }

        return splitted_strings;
    }

    private void PlayMusic(Uri uri, boolean b) {
        String string_video_path_reconstruction = "";
        int video_pos = 0;

        Log.d("video_check_path: ", SplitStringToArray(String.valueOf(uri)).toString());
        try {

            video_pos = Integer.parseInt(SplitStringToArray(String.valueOf(uri)).get(SplitStringToArray(String.valueOf(uri)).size() - 1));
        } catch (Exception e) {
            e.printStackTrace();
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

        current_uri_playing_media_or_video = "";
        if (b) {
            current_uri_playing_media_or_video = string_video_path_reconstruction + (++video_pos);
        } else {
            current_uri_playing_media_or_video = string_video_path_reconstruction + (--video_pos);
        }
        Log.d("video_check_next: ", current_uri_playing_media_or_video);
        if (CheckForFileExistedOrNot(current_uri_playing_media_or_video)) {

            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(current_uri_playing_media_or_video));
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.start();

        } else {

            do {
                current_uri_playing_media_or_video = "";
                if (b) {
                    current_uri_playing_media_or_video = string_video_path_reconstruction + (++video_pos);
                } else {
                    current_uri_playing_media_or_video = string_video_path_reconstruction + (--video_pos);
                }
            } while (CheckForFileExistedOrNot(current_uri_playing_media_or_video));

            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(current_uri_playing_media_or_video));
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.start();


        }
        CompletionOfMedia(true);
    }

    private void PlayVideo(Uri uri, final boolean b) {
        String string_video_path_reconstruction = "";
        int video_pos = 0;

        Log.d("video_check_path: ", SplitStringToArray(String.valueOf(uri)).toString());
        try {

            video_pos = Integer.parseInt(SplitStringToArray(String.valueOf(uri)).get(SplitStringToArray(String.valueOf(uri)).size() - 1));
        } catch (Exception e) {
            e.printStackTrace();
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

        current_uri_playing_media_or_video = "";
        if (b) {
            current_uri_playing_media_or_video = string_video_path_reconstruction + (++video_pos);
        } else {
            current_uri_playing_media_or_video = string_video_path_reconstruction + (--video_pos);
        }
        Log.d("video_check_next: ", current_uri_playing_media_or_video);
        if (CheckForFileExistedOrNot(current_uri_playing_media_or_video)) {

            stretchVideoView_floating.stopPlayback();
            stretchVideoView_floating.setVideoURI(Uri.parse(current_uri_playing_media_or_video));
            stretchVideoView_floating.start();

        } else {

            do {
                current_uri_playing_media_or_video = "";
                if (b) {
                    current_uri_playing_media_or_video = string_video_path_reconstruction + (++video_pos);
                } else {
                    current_uri_playing_media_or_video = string_video_path_reconstruction + (--video_pos);
                }
            } while (CheckForFileExistedOrNot(current_uri_playing_media_or_video));

            stretchVideoView_floating.stopPlayback();
            stretchVideoView_floating.setVideoURI(Uri.parse(current_uri_playing_media_or_video));
            stretchVideoView_floating.start();


        }
        CompletionOfVideo(true);
    }

    private void CompletionOfVideo(final boolean b) {
        if (!TextUtils.isEmpty(current_uri_playing_media_or_video) && !current_uri_playing_media_or_video.equals("null")) {


            stretchVideoView_floating.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (!((MainActivity) activity_parent).GetCheckingOfController()) {
                        PlayVideo(getVideoUri(), b);
                    }
                }
            });

        }
    }


    private boolean CheckForFileExistedOrNot(String s) {

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

        return false;
    }


    private void showNotification() {

        // Using RemoteViews to bind custom layouts into Notification
        views = new RemoteViews(getPackageName(),
                R.layout.status_bar);
        bigViews = new RemoteViews(getPackageName(),
                R.layout.status_bar_expanded);

        // showing default album image
        views.setViewVisibility(R.id.status_bar_icon, View.VISIBLE);
        views.setViewVisibility(R.id.status_bar_album_art, View.GONE);
        bigViews.setImageViewBitmap(R.id.status_bar_album_art,
                Constant.getDefaultAlbumArt(this));

        /*Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constant.ACTION.MAIN_ACTION);
        notificationIntent.putExtra("video_path", getVideoPath());
        notificationIntent.putExtra("video_pos", getVideoPos());
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        stackBuilder.addNextIntentWithParentStack(notificationIntent);
//        PendingIntent pendingIntent =
//                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);*/

        Intent previousIntent = new Intent(this, ServiceVideoView.class);
        previousIntent.setAction(Constant.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, ServiceVideoView.class);
        playIntent.setAction(Constant.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, ServiceVideoView.class);
        nextIntent.setAction(Constant.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Intent closeIntent = new Intent(this, ServiceVideoView.class);
        closeIntent.setAction(Constant.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);


        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);

        views.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);

        views.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);

        views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);

        if (mediaPlayer.isPlaying()) {
            views.setImageViewResource(R.id.status_bar_play, R.drawable.apollo_holo_dark_pause);
            bigViews.setImageViewResource(R.id.status_bar_play, R.drawable.apollo_holo_dark_pause);
        } else {
            views.setImageViewResource(R.id.status_bar_play, R.drawable.apollo_holo_dark_play);
            bigViews.setImageViewResource(R.id.status_bar_play, R.drawable.apollo_holo_dark_play);
        }

        /*views.setTextViewText(R.id.status_bar_track_name, "Song Title");
        bigViews.setTextViewText(R.id.status_bar_track_name, "Song Title");

        views.setTextViewText(R.id.status_bar_artist_name, "Artist Name");
        bigViews.setTextViewText(R.id.status_bar_artist_name, "Artist Name");

        bigViews.setTextViewText(R.id.status_bar_album_name, "Album Name");*/

        status = new Notification.Builder(this).build();
        status.contentView = views;
        status.bigContentView = bigViews;
        status.flags = Notification.FLAG_ONGOING_EVENT;
        status.icon = R.drawable.ic_launcher;
//        status.contentIntent = pendingIntent;
        notificationManager.notify(Constant.NOTIFICATION_ID.FOREGROUND_SERVICE, status);

        createNotificationChannel();
//        startForeground(Constant.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
    }

    String CHANNEL_ID = "1";

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "VideoPlayer";
            String description = "Background VideoPlayer";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
//            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    BroadcastReceiver BroadcastForPlayPauseButton = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().getBoolean("video_state")) {


                views.setImageViewResource(R.id.status_bar_play,
                        R.drawable.apollo_holo_dark_pause);
                bigViews.setImageViewResource(R.id.status_bar_play,
                        R.drawable.apollo_holo_dark_pause);

                notificationManager.notify(Constant.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
//                startForeground(Constant.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
            } else {

                views.setImageViewResource(R.id.status_bar_play,
                        R.drawable.apollo_holo_dark_play);
                bigViews.setImageViewResource(R.id.status_bar_play,
                        R.drawable.apollo_holo_dark_play);

                notificationManager.notify(Constant.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
//                startForeground(Constant.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
            }
        }
    };

    public interface ServiceInterface {
        void PassDataFromActivityToService(Activity activity);
    }


    private void ServiceVideoViewFloatingWidgetInit() {
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);

        //Add the view_floating_background_transparent to the window.
        /*params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);*/
        if (android.os.Build.VERSION.SDK_INT >= 23 && android.os.Build.VERSION.SDK_INT < 26) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else if (android.os.Build.VERSION.SDK_INT >= 26) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        //Specify the view_floating_background_transparent position
        params.gravity = Gravity.TOP | Gravity.LEFT;        //Initially view_floating_background_transparent will be added to top-left corner
        params.x = 0;
        params.y = 100;

        //Add the view_floating_background_transparent to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (mWindowManager != null) {
            mWindowManager.addView(mFloatingView, params);
        }


        ServiceVideoViewFloatingWidgetFunction();
    }

    private void DestroyFloatingView() {
        if (mFloatingView.isAttachedToWindow()) {
            if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
        }
    }


    private Uri getVideoUri() {
        Uri mUri = null;
        try {
            Field mUriField = VideoView.class.getDeclaredField("mUri");
            mUriField.setAccessible(true);
            mUri = (Uri) mUriField.get(stretchVideoView_floating);
        } catch (Exception e) {
        }
        return mUri;
    }


    private void ServiceVideoViewFloatingWidgetFunction() {

        //The root element of the collapsed view_floating_background_transparent layout
        collapsedView = mFloatingView.findViewById(R.id.collapse_view);
        //The root element of the expanded view_floating_background_transparent layout
        expandedView = mFloatingView.findViewById(R.id.expanded_container);


        stretchVideoView_floating = (StretchVideoView) mFloatingView.findViewById(R.id.floating_video_view);
        /*stretchVideoView_floating.setVideoURI(Uri.parse(current_uri_playing_media_or_video));
        stretchVideoView_floating.seekTo(video_seek_pos);
        stretchVideoView_floating.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });*/

        //Set the close button
        ImageView closeButtonCollapsed = (ImageView) mFloatingView.findViewById(R.id.close_btn);
        closeButtonCollapsed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //close the service and remove the from from the window
                if (activity_parent != null) {
                    activity_parent.finish();
                }
//                stopForeground(true);
                DestroyNotification();
                stopSelf();
                onDestroy();
            }
        });


        //Set the view_floating_background_transparent while floating view_floating_background_transparent is expanded.
        //Set the play button.
        ImageView playButton = (ImageView) mFloatingView.findViewById(R.id.play_btn);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stretchVideoView_floating.isPlaying()) {
                    video_seek_pos = stretchVideoView_floating.getCurrentPosition();
                    stretchVideoView_floating.pause();
                } else {
                    stretchVideoView_floating.seekTo(video_seek_pos);
                    stretchVideoView_floating.start();
                }
//                Toast.makeText(getApplicationContext(), "Playing the song.", Toast.LENGTH_LONG).show();
            }
        });


        //Set the next button.
        ImageView nextButton = (ImageView) mFloatingView.findViewById(R.id.next_btn);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "Playing next song.", Toast.LENGTH_LONG).show();
                if (!((MainActivity) activity_parent).GetCheckingOfController()) {
                    PlayVideo(getVideoUri(), true);
                }
            }
        });


        //Set the pause button.
        ImageView prevButton = (ImageView) mFloatingView.findViewById(R.id.prev_btn);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "Playing previous song.", Toast.LENGTH_LONG).show();
                if (!((MainActivity) activity_parent).GetCheckingOfController()) {
                    PlayVideo(getVideoUri(), false);
                }
            }
        });
        if (boolean_chk_for_btn_show) {
            nextButton.setVisibility(View.INVISIBLE);
            prevButton.setVisibility(View.INVISIBLE);
        } else {
            nextButton.setVisibility(View.VISIBLE);
            prevButton.setVisibility(View.VISIBLE);
        }


        //Set the close button
        ImageView closeButton = (ImageView) mFloatingView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aBoolean_check_for_media_or_video = false;

                Uri current_path = null;
                video_seek_pos = stretchVideoView_floating.getCurrentPosition() + 150;
                current_path = getVideoUri();
                if (stretchVideoView_floating.isPlaying()) {
                    stretchVideoView_floating.stopPlayback();
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), current_path);
                    mediaPlayer.seekTo(video_seek_pos);
                    mediaPlayer.start();

                    BroadCastForPauseButtonInNotification(true);
                    Log.d("vido_position_close_1: ", String.valueOf(video_seek_pos));
                } else {
                    stretchVideoView_floating.stopPlayback();
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), current_path);
                    mediaPlayer.seekTo(video_seek_pos);
                    mediaPlayer.pause();

                    BroadCastForPauseButtonInNotification(false);
                    Log.d("vido_position_close_2: ", String.valueOf(video_seek_pos));
                }
                stretchVideoView_floating.setVisibility(View.GONE);
                expandedView.setVisibility(View.GONE);
                collapsedView.setVisibility(View.VISIBLE);


                Intent intent_broadcast = new Intent();
                intent_broadcast.setAction("broadcastReceiver_noti_or_floating");
                intent_broadcast.putExtra("notification_floating", false);
                sendBroadcast(intent_broadcast);

            }
        });


        //Open the application on the button click
        /*ImageView openButton = (ImageView) mFloatingView.findViewById(R.id.open_button);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view_floating_background_transparent) {
                //Open the application  click.
                activity_parent.finish();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setAction(Constant.INTENT_FOR_REOPEN_ACTIVITY);
                intent.putExtra("Video_uri", String.valueOf(getVideoUri()));
                intent.putExtra("Video_pos", stretchVideoView_floating.getCurrentPosition());
                if (stretchVideoView_floating.isPlaying()){
                    intent.putExtra("Video_play_pause_check", true);
                }else {
                    intent.putExtra("Video_play_pause_check", false);
                }
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);


                //close the service and remove view_floating_background_transparent from the view_floating_background_transparent hierarchy
                unregisterReceiver(BroadcastForPlayPauseButton);
                unregisterReceiver(broadcastReceiver_noti_or_floating);
                DestroyFloatingView();
                stopSelf();
            }
        });*/
//        TouchGestureOfFloatingView();
    }

    private void TouchGestureOfFloatingView() {
        //Drag and move floating view_floating_background_transparent using user'current_uri_playing_media_or_video touch action.
        if (mFloatingView != null) {
            mFloatingView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;


                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:


                            //remember the initial position.
                            initialX = params.x;
                            initialY = params.y;

                            Log.d("remove_X: ", String.valueOf(params.x));
                            Log.d("remove_Y: ", String.valueOf(params.y));

                            //get the touch location
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();

                            Log.d("initialTouchX_down: ", String.valueOf(initialTouchX));
                            Log.d("initialTouchY_down: ", String.valueOf(initialTouchY));

                            return true;
                        case MotionEvent.ACTION_MOVE:
                            //Calculate the X and Y coordinates of the view_floating_background_transparent.

                            Log.d("initialTouchX_move: ", String.valueOf(event.getRawX()));
                            Log.d("initialTouchY_move: ", String.valueOf(event.getRawY()));

                            if (!aBoolean_floating_display_check) {
                                aBoolean_floating_display_check = true;

                                view_floating_background_transparent = LayoutInflater.from(activity_parent).inflate(R.layout.floating_view_delete, null);
                                /*params_cloase = new WindowManager.LayoutParams(
                                        WindowManager.LayoutParams.MATCH_PARENT,
                                        WindowManager.LayoutParams.MATCH_PARENT,
                                        WindowManager.LayoutParams.TYPE_PHONE,
                                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                        PixelFormat.TRANSPARENT);*/
                                if (android.os.Build.VERSION.SDK_INT >= 23 && android.os.Build.VERSION.SDK_INT < 26) {
                                    params_cloase = new WindowManager.LayoutParams(
                                            WindowManager.LayoutParams.MATCH_PARENT,
                                            WindowManager.LayoutParams.MATCH_PARENT,
                                            WindowManager.LayoutParams.TYPE_PHONE,
                                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                                            PixelFormat.TRANSLUCENT);
                                } else if (android.os.Build.VERSION.SDK_INT >= 26) {
                                    params_cloase = new WindowManager.LayoutParams(
                                            WindowManager.LayoutParams.MATCH_PARENT,
                                            WindowManager.LayoutParams.MATCH_PARENT,
                                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                                            PixelFormat.TRANSLUCENT);
                                }


                                //Add the view_floating_background_transparent to the window
                                mWindowManager_close = (WindowManager) getSystemService(WINDOW_SERVICE);
                                if (mWindowManager_close != null) {
                                    mWindowManager_close.addView(view_floating_background_transparent, params_cloase);
                                }

                            }
                            get_image_location_on_screen = new int[2];
                            view_floating_background_transparent.findViewById(R.id.image_close).getLocationOnScreen(get_image_location_on_screen);

                            Log.d("img_x: ", String.valueOf(get_image_location_on_screen[0]));
                            Log.d("img_y: ", String.valueOf(get_image_location_on_screen[1]));



                        /*int[] tableLayoutCorners = new int[2];
                        view_floating_background_transparent.findViewById(R.id.image_close).getLocationInWindow(tableLayoutCorners);
                        Log.d("img_x: ", String.valueOf(tableLayoutCorners[0]));
                        Log.d("img_y: ", String.valueOf(tableLayoutCorners[1]));*/


                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);

                            Log.d("remove_getRawX: ", String.valueOf(event.getRawX()));
                            Log.d("remove_getRawY: ", String.valueOf(event.getRawY()));
                            Log.d("remove_X_move: ", String.valueOf(params.x));
                            Log.d("remove_Y_move: ", String.valueOf(params.y));

                            //Update the layout with new X & Y coordinate
                            try {
                                mWindowManager.updateViewLayout(mFloatingView, params);

                            } catch (Exception e) {

                            }


                            return true;

                        case MotionEvent.ACTION_UP:

                            Log.d("initialTouchX_up: ", String.valueOf(event.getRawX()));
                            Log.d("initialTouchY_up: ", String.valueOf(event.getRawY()));

                            if (get_image_location_on_screen != null) {
                                Log.d("img_x_2: ", String.valueOf(get_image_location_on_screen[0]));
                                Log.d("img_y_2: ", String.valueOf(get_image_location_on_screen[1]));
                                Log.d("img_event_2_x: ", String.valueOf((int) event.getRawX()));
                                Log.d("img_event_2_y: ", String.valueOf((int) event.getRawY()));


//                        if (params.y>1450 && params.y<1600 && params.x>450 && params.x<520){
                                if (((int) event.getRawX()) < get_image_location_on_screen[0] + 150 &&
                                        ((int) event.getRawX()) > get_image_location_on_screen[0] &&
                                        ((int) event.getRawY()) < get_image_location_on_screen[1] + 150 &&
                                        ((int) event.getRawY()) > get_image_location_on_screen[1]) {

                                    if (activity_parent != null) {
                                        activity_parent.finish();
                                    }
                                    DestroyNotification();
                                    if (view_floating_background_transparent != null)
                                        mWindowManager_close.removeView(view_floating_background_transparent);
                                    stopSelf();
                                    onDestroy();
                                } else {
                                    try {

                                        if (view_floating_background_transparent != null)
                                            mWindowManager_close.removeView(view_floating_background_transparent);
                                    } catch (Exception e) {

                                    }
                                    aBoolean_floating_display_check = false;
                                }
                            }


                            int Xdiff = (int) (event.getRawX() - initialTouchX);
                            int Ydiff = (int) (event.getRawY() - initialTouchY);
                            Log.d("diff_x: ", String.valueOf(Xdiff));
                            Log.d("diff_y: ", String.valueOf(Ydiff));


                            //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                            //So that is click event.
                            if (Xdiff < 20 && Ydiff < 20 && Xdiff >= 0 && Ydiff >= 0) {
                                aBoolean_check_for_media_or_video = true;
                                if (isViewCollapsed()) {

                                    BroadCastForPauseButtonInNotification(false);

                                    Intent intent_broadcast = new Intent();
                                    intent_broadcast.setAction("broadcastReceiver_noti_or_floating");
                                    intent_broadcast.putExtra("notification_floating", true);
                                    sendBroadcast(intent_broadcast);
                                    //When user clicks on the image view_floating_background_transparent of the collapsed layout,
                                    //visibility of the collapsed layout will be changed to "View.GONE"
                                    //and expanded view_floating_background_transparent will become visible.
                                    video_seek_pos = mediaPlayer.getCurrentPosition() + 150;

                                    if (mediaPlayer.isPlaying()) {

                                        mediaPlayer.stop();
                                        mediaPlayer.reset();
                                        mediaPlayer.release();

                                        collapsedView.setVisibility(View.GONE);
                                        expandedView.setVisibility(View.VISIBLE);
                                        stretchVideoView_floating.setVisibility(View.VISIBLE);
                                        stretchVideoView_floating.setVideoURI(Uri.parse(current_uri_playing_media_or_video));
                                        stretchVideoView_floating.seekTo(video_seek_pos);
                                        stretchVideoView_floating.start();


                                    } else {

                                        mediaPlayer.stop();
                                        mediaPlayer.reset();
                                        mediaPlayer.release();

                                        collapsedView.setVisibility(View.GONE);
                                        expandedView.setVisibility(View.VISIBLE);
                                        stretchVideoView_floating.setVisibility(View.VISIBLE);
                                        stretchVideoView_floating.setVideoURI(Uri.parse(current_uri_playing_media_or_video));
                                        stretchVideoView_floating.seekTo(video_seek_pos);
                                        stretchVideoView_floating.pause();
                                    }

                                    CompletionOfVideo(true);

                                    Log.d("video_position_open: ", String.valueOf(video_seek_pos));
                                }
                            }
                            return true;
                    }
                    return false;
                }
            });
        }
    }


    private boolean isViewCollapsed() {
        return mFloatingView == null || mFloatingView.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }

    BroadcastReceiver broadcastReceiver_noti_or_floating = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().getBoolean("notification_floating")) {
                DestroyNotification();
            } else {

                notificationManager.notify(Constant.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
            }

        }
    };

    private void BroadCastForPauseButtonInNotification(boolean b) {
        Intent broadcast = new Intent();
        broadcast.setAction("BroadcastForPlayPauseButton");
        broadcast.putExtra("video_state", b);
        sendBroadcast(broadcast);
    }

}
