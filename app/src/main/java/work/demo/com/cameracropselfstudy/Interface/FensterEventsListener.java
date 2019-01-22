package work.demo.com.cameracropselfstudy.Interface;

import android.view.MotionEvent;

/**
 * Created by su on 3/2/18.
 */

public interface FensterEventsListener {

    void onTap();

    void onDoubleTap();

    void onHorizontalScroll(MotionEvent event, float delta);

    void onVerticalScroll(MotionEvent event, float delta);

    void onSwipeRight(float v);

    void onSwipeLeft(float v);

    void onSwipeBottom(MotionEvent e2, MotionEvent e1);

    void onSwipeTop(MotionEvent e2, MotionEvent e1);
}
