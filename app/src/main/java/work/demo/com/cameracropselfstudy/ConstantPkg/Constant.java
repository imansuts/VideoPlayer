package work.demo.com.cameracropselfstudy.ConstantPkg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import work.demo.com.cameracropselfstudy.R;

/**
 * Created by su on 3/8/18.
 */

public final class Constant {

    public static final String BROADCASTINTENT = "PassDataFromServiceToActivityUsingBroadcast";
    public static final String VIDEO_POS = "VideoPostionToSeek";
    public static final String INTENT_FOR_REOPEN_ACTIVITY = "work.demo.com.cameracropselfstudy.action.ResumeActivity";

    public interface ACTION {
        public static String MAIN_ACTION = "work.demo.com.cameracropselfstudy.action.main";
        public static String INIT_ACTION = "work.demo.com.cameracropselfstudy.action.init";
        public static String PREV_ACTION = "work.demo.com.cameracropselfstudy.action.prev";
        public static String PLAY_ACTION = "work.demo.com.cameracropselfstudy.action.play";
        public static String NEXT_ACTION = "work.demo.com.cameracropselfstudy.action.next";
        public static String STARTFOREGROUND_ACTION = "work.demo.com.cameracropselfstudy.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "work.demo.com.cameracropselfstudy.action.stopforeground";

    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }

    public static Bitmap getDefaultAlbumArt(Context context) {
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            bm = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.default_album_art, options);
        } catch (Error ee) {
        } catch (Exception e) {
        }
        return bm;
    }
}
