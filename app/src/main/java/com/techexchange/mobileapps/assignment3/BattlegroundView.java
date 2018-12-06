package com.techexchange.mobileapps.assignment3;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import static com.techexchange.mobileapps.assignment3.MainActivity.GREEN_SCORED;
import static com.techexchange.mobileapps.assignment3.MainActivity.GREEN_TANK_DOWN;
import static com.techexchange.mobileapps.assignment3.MainActivity.GREEN_TANK_LEFT;
import static com.techexchange.mobileapps.assignment3.MainActivity.GREEN_TANK_RIGHT;
import static com.techexchange.mobileapps.assignment3.MainActivity.GREEN_TANK_SHOOT;
import static com.techexchange.mobileapps.assignment3.MainActivity.GREEN_TANK_UP;
import static com.techexchange.mobileapps.assignment3.MainActivity.GREEN_TURN;
import static com.techexchange.mobileapps.assignment3.MainActivity.RED_SCORED;
import static com.techexchange.mobileapps.assignment3.MainActivity.RED_TANK_DOWN;
import static com.techexchange.mobileapps.assignment3.MainActivity.RED_TANK_LEFT;
import static com.techexchange.mobileapps.assignment3.MainActivity.RED_TANK_RIGHT;
import static com.techexchange.mobileapps.assignment3.MainActivity.RED_TANK_SHOOT;
import static com.techexchange.mobileapps.assignment3.MainActivity.RED_TANK_UP;
import static com.techexchange.mobileapps.assignment3.MainActivity.RED_TURN;

public class BattlegroundView extends View implements GestureDetector.OnGestureListener {

    static final String TAG = "Game";

    private static final long DELAY_MS = 30;

    private final Context context;
    private final MainActivity activity;
    private final GestureDetectorCompat detector;

    private boolean greenTurn;
    private boolean redTurn;

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

        this.greenTurn = true;
        this.redTurn = true;

        Toast.makeText(context, "GREEN's turn", Toast.LENGTH_SHORT).show();
    }

    public void enableGreenTurn() {
        this.greenTurn = true;
        this.redTurn = false;
    }

    public void enableRedTurn() {
        this.redTurn = true;
        this.greenTurn = false;
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
        if (activity.host == MainActivity.Host.SERVER && this.greenTurn) {
            this.enableRedTurn();
            activity.sendReceiveThread.write(new byte[]{GREEN_TANK_SHOOT, RED_TURN});
//            activity.sendReceiveThread.write(new byte[]{GREEN_TANK_SHOOT});
            greenTank.getShell().getExplosionRect(greenTank.getRect(), greenTank.getDirection());
        } else if (activity.host == MainActivity.Host.CLIENT && this.redTurn) {
            this.enableGreenTurn();
            activity.sendReceiveThread.write(new byte[]{RED_TANK_SHOOT, GREEN_TURN});
//            activity.sendReceiveThread.write(new byte[]{RED_TANK_SHOOT});
            redTank.getShell().getExplosionRect(redTank.getRect(), redTank.getDirection());
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d(TAG, "Green tank " + greenTank);
        Log.d(TAG, "Red tank " + redTank);
        if (Math.abs(velocityX) >= Math.abs(velocityY)) {
            if (velocityX < 0) {
                Log.d(TAG, "Left swipe");
                if (activity.host == MainActivity.Host.SERVER && this.greenTurn) {
                    this.enableRedTurn();
                    activity.sendReceiveThread.write(new byte[]{GREEN_TANK_LEFT, RED_TURN});
                    greenTank.handleLeft(maze.getBricks(), redTank);
                } else if (activity.host == MainActivity.Host.CLIENT && this.redTurn) {
                    this.enableGreenTurn();
                    activity.sendReceiveThread.write(new byte[]{RED_TANK_LEFT, GREEN_TURN});
                    redTank.handleLeft(maze.getBricks(), greenTank);
                }
            } else {
                Log.d(TAG, "Right swipe");
                if (activity.host == MainActivity.Host.SERVER && this.greenTurn) {
                    this.enableRedTurn();
                    activity.sendReceiveThread.write(new byte[]{GREEN_TANK_RIGHT, RED_TURN});
                    greenTank.handleRight(maze.getBricks(), redTank);
                } else if (activity.host == MainActivity.Host.CLIENT && this.redTurn) {
                    this.enableGreenTurn();
                    activity.sendReceiveThread.write(new byte[]{RED_TANK_RIGHT, GREEN_TURN});
                    redTank.handleRight(maze.getBricks(), greenTank);
                }

            }
        } else {
            if (velocityY < 0) {
                Log.d(TAG, "Up swipe");
                if (activity.host == MainActivity.Host.SERVER && this.greenTurn) {
                    this.enableRedTurn();
                    activity.sendReceiveThread.write(new byte[]{GREEN_TANK_UP, RED_TURN});
                    greenTank.handleUp(maze.getBricks(), redTank);
                } else if (activity.host == MainActivity.Host.CLIENT && this.redTurn) {
                    this.enableGreenTurn();
                    activity.sendReceiveThread.write(new byte[]{RED_TANK_UP, GREEN_TURN});
                    redTank.handleUp(maze.getBricks(), greenTank);
                }
            } else {
                Log.d(TAG, "Down swipe");
                if (activity.host == MainActivity.Host.SERVER && this.greenTurn) {
                    this.enableRedTurn();
                    activity.sendReceiveThread.write(new byte[]{GREEN_TANK_DOWN, RED_TURN});
                    greenTank.handleDown(maze.getBricks(), redTank);
                } else if (activity.host == MainActivity.Host.CLIENT && this.redTurn) {
                    this.enableGreenTurn();
                    activity.sendReceiveThread.write(new byte[]{RED_TANK_DOWN, GREEN_TURN});
                    redTank.handleDown(maze.getBricks(), greenTank);
                }
            }
        }
        return true;
    }

    public void handleAction(byte action) {
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
            case GREEN_SCORED:
                greenTank.getShell().incrementScore();
                break;
            case RED_SCORED:
                redTank.getShell().incrementScore();
                break;
            case GREEN_TURN:
                this.enableGreenTurn();
                Toast.makeText(getContext(), "GREEN's Turn", Toast.LENGTH_SHORT).show();
                break;
            case RED_TURN:
                this.enableRedTurn();
                Toast.makeText(getContext(), "RED's Turn", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
