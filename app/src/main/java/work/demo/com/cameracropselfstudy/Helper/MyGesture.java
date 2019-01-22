package work.demo.com.cameracropselfstudy.Helper;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.VideoView;

import work.demo.com.cameracropselfstudy.Interface.FensterEventsListener;

import static android.content.ContentValues.TAG;

/**
 * Created by su on 3/2/18.
 */

public class MyGesture extends GestureDetector.SimpleOnGestureListener {

    private VideoView videoView;
    private Context context;
    private FensterEventsListener listener;
    private final float SWIPE_THRESHOLD = 2f, minFlingVelocity = 5f;

    public MyGesture( Context context, VideoView videoView) {
        this.videoView = videoView;
        this.context = context;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // Fling event occurred.  Notification of this one happens after an "up" event.
        Log.i(TAG, "Fling");
        boolean result = false;
        try {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            Log.d("diffX: ", String.valueOf(diffX));
            Log.d("diffY: ", String.valueOf(diffY));
            /*if (diffX < 0) {

                listener.onSwipeLeft(diffX);

            } else {
                listener.onSwipeRight(diffX);

            }

            if (diffY > 0) {

                listener.onSwipeBottom();

            } else {

                listener.onSwipeTop();

            }*/

            if (Math.abs(diffX) > Math.abs(diffY)) {

                    if (diffX > 0) {

                        listener.onSwipeRight(diffX);

                    } else {

                        listener.onSwipeLeft(diffX);

                    }

                result = true;
            } else  {
                if (diffY > 0) {

                    listener.onSwipeBottom(e2, e1);

                } else {

                    listener.onSwipeTop(e2, e1);

                }
            }

            /*if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > minFlingVelocity) {
                    if (diffX > 0) {

                            listener.onSwipeRight(diffX);

                    } else {

                            listener.onSwipeLeft(diffX);

                    }
                }
                result = true;
            } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > minFlingVelocity) {
                if (diffY > 0) {

                        listener.onSwipeBottom(e2, e1);

                } else {

                        listener.onSwipeTop(e2, e1);

                }
            }*/
            result = true;

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return result;
    }

    public void SetListener(FensterEventsListener listener){
        this.listener = listener;
    }


    @Override
    public boolean onDown(MotionEvent event) {
        Log.d("TAG","onDown: ");

        // don't return false here or else none of the other
        // gestures will work
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        listener.onTap();
        Log.i("TAG", "onSingleTapConfirmed: ");
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.i("TAG", "onLongPress: ");
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.i("TAG", "onDoubleTap: ");
        listener.onDoubleTap();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2,
                            float distanceX, float distanceY) {
        Log.i("TAG", "onScroll: ");
        return true;
    }
}
