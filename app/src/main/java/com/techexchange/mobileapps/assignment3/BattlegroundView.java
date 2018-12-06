package com.techexchange.mobileapps.assignment3;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import static com.techexchange.mobileapps.assignment3.MainActivity.GREEN_TANK_DOWN;
import static com.techexchange.mobileapps.assignment3.MainActivity.GREEN_TANK_LEFT;
import static com.techexchange.mobileapps.assignment3.MainActivity.GREEN_TANK_RIGHT;
import static com.techexchange.mobileapps.assignment3.MainActivity.GREEN_TANK_SHOOT;
import static com.techexchange.mobileapps.assignment3.MainActivity.GREEN_TANK_UP;
import static com.techexchange.mobileapps.assignment3.MainActivity.RED_TANK_DOWN;
import static com.techexchange.mobileapps.assignment3.MainActivity.RED_TANK_LEFT;
import static com.techexchange.mobileapps.assignment3.MainActivity.RED_TANK_RIGHT;
import static com.techexchange.mobileapps.assignment3.MainActivity.RED_TANK_SHOOT;
import static com.techexchange.mobileapps.assignment3.MainActivity.RED_TANK_UP;

public class BattlegroundView extends View implements GestureDetector.OnGestureListener {

    static final String TAG = "Game";

    private static final long DELAY_MS = 30;

    private final Context context;
    private final MainActivity activity;
    private final GestureDetectorCompat detector;

    Maze maze;
    Tank greenTank;     // TODO: Delete these
    Tank redTank;

    public BattlegroundView(Context context) {
        super(context);

        this.context = context;
        this.activity = (MainActivity) context;
        this.detector = new GestureDetectorCompat(context, this);

        this.maze = null;
        this.greenTank = null;
        this.redTank = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (this.maze != null) {
            maze.draw(canvas);
            greenTank.draw(canvas, maze.getBricks(), redTank);
            redTank.draw(canvas, maze.getBricks(), greenTank);
        }

        try {
            Thread.sleep(DELAY_MS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Sleep interrupted!", e);
        }

        invalidate();       // Forces a redraw.
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        maze = new Maze(this.context, w, h);
        greenTank = new Tank(this.context, Tank.Color.GREEN, w, h);
        redTank = new Tank(this.context, Tank.Color.RED, w, h);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.detector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (activity.host == MainActivity.Host.SERVER) {
            activity.sendReceiveThread.write(new byte[]{GREEN_TANK_SHOOT});
            greenTank.getShell().getExplosionRect(greenTank.getRect(), greenTank.getDirection());
        } else if (activity.host == MainActivity.Host.CLIENT){
            activity.sendReceiveThread.write(new byte[]{RED_TANK_SHOOT});
            redTank.getShell().getExplosionRect(redTank.getRect(), redTank.getDirection());
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d(TAG, "Green tank " + greenTank);
        Log.d(TAG, "Red tank " + redTank);
//        if (greenTank != null && redTank != null && activity.sendReceiveThread != null) {
            if (Math.abs(velocityX) >= Math.abs(velocityY)) {
                if (velocityX < 0) {
                    Log.d(TAG, "Left swipe");
                    if (activity.host == MainActivity.Host.SERVER) {
                        activity.sendReceiveThread.write(new byte[]{GREEN_TANK_LEFT});
                        greenTank.handleLeft(maze.getBricks(), redTank);
                    } else {
                        activity.sendReceiveThread.write(new byte[]{RED_TANK_LEFT});
                        redTank.handleLeft(maze.getBricks(), greenTank);
                    }
                } else {
                    Log.d(TAG, "Right swipe");
                    if (activity.host == MainActivity.Host.SERVER) {
                        activity.sendReceiveThread.write(new byte[]{GREEN_TANK_RIGHT});
                        greenTank.handleRight(maze.getBricks(), redTank);
                    } else {
                        activity.sendReceiveThread.write(new byte[]{RED_TANK_RIGHT});
                        redTank.handleRight(maze.getBricks(), greenTank);
                    }

                }
            } else {
                if (velocityY < 0) {
                    Log.d(TAG, "Up swipe");
                    if (activity.host == MainActivity.Host.SERVER) {
                        activity.sendReceiveThread.write(new byte[]{GREEN_TANK_UP});
                        greenTank.handleUp(maze.getBricks(), redTank);
                    } else {
                        activity.sendReceiveThread.write(new byte[]{RED_TANK_UP});
                        redTank.handleUp(maze.getBricks(), greenTank);
                    }
                } else {
                    Log.d(TAG, "Down swipe");
                    if (activity.host == MainActivity.Host.SERVER) {
                        activity.sendReceiveThread.write(new byte[]{GREEN_TANK_DOWN});
                        greenTank.handleDown(maze.getBricks(), redTank);
                    } else {
                        activity.sendReceiveThread.write(new byte[]{RED_TANK_DOWN});
                        redTank.handleDown(maze.getBricks(), greenTank);
                    }
                }
            }
//        }
        return true;
    }

    public void handleAction(byte action) {
//        if (greenTank != null && redTank != null && maze != null) {
            switch (action) {
                case GREEN_TANK_UP:
                    greenTank.handleUp(maze.getBricks(), redTank);
                    break;
                case GREEN_TANK_DOWN:
                    greenTank.handleDown(maze.getBricks(), redTank);
                    break;
                case GREEN_TANK_LEFT:
                    greenTank.handleLeft(maze.getBricks(), redTank);
                    break;
                case GREEN_TANK_RIGHT:
                    greenTank.handleRight(maze.getBricks(), redTank);
                    break;
                case GREEN_TANK_SHOOT:
                    greenTank.getShell().getExplosionRect(greenTank.getRect(), greenTank.getDirection());
                    break;
                case RED_TANK_UP:
                    redTank.handleUp(maze.getBricks(), greenTank);
                    break;
                case RED_TANK_DOWN:
                    redTank.handleDown(maze.getBricks(), greenTank);
                    break;
                case RED_TANK_LEFT:
                    redTank.handleLeft(maze.getBricks(), greenTank);
                    break;
                case RED_TANK_RIGHT:
                    redTank.handleRight(maze.getBricks(), greenTank);
                    break;
                case RED_TANK_SHOOT:
                    redTank.getShell().getExplosionRect(redTank.getRect(), redTank.getDirection());
                    break;
            }
//        }
    }
}
