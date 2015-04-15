package northern.captain.vendingman.gui;

import android.graphics.Rect;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

/**
 * A class, that can be used as a TouchListener on any view (e.g. a Button).
 * It cyclically runs a clickListener, emulating keyboard-like behaviour. First
 * click is fired immediately, next after initialInterval, and subsequent after
 * normalInterval.
 * <p/>
 * <p>Interval is scheduled after the onClick completes, so it has to run fast.
 * If it runs slow, it does not generate skipped onClicks.
 */
public abstract class RepeatListenerAbstract implements OnTouchListener
{

    private Handler handler = new Handler();

    private int initialInterval;
    private final int normalInterval;

    private Runnable handlerRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            handler.postDelayed(this, normalInterval);
            onClick(downView);
        }
    };

    private View downView;

    public RepeatListenerAbstract()
    {
        this(400, 100);
    }

    /**
     * @param initialInterval The interval after first click event
     * @param normalInterval  The interval after second and subsequent click
     *                        events
     */
    public RepeatListenerAbstract(int initialInterval, int normalInterval)
    {
        if (initialInterval < 0 || normalInterval < 0)
            throw new IllegalArgumentException("negative interval");

        this.initialInterval = initialInterval;
        this.normalInterval = normalInterval;
    }

    private Rect rect; // Variable rect to hold the bounds of the view

    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        switch (motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                rect = new Rect(view.getLeft(), view.getTop(), view.getRight(),
                        view.getBottom());
                handler.removeCallbacks(handlerRunnable);
                handler.postDelayed(handlerRunnable, initialInterval);
                downView = view;
                onClick(view);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handler.removeCallbacks(handlerRunnable);
                downView = null;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!rect.contains(view.getLeft() + (int) motionEvent.getX(),
                        view.getTop() + (int) motionEvent.getY()))
                {
                    // User moved outside bounds
                    handler.removeCallbacks(handlerRunnable);
                    downView = null;
                }
                break;
        }

        return false;
    }

    public abstract void onClick(View view);
}
