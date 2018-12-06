package com.techexchange.mobileapps.assignment3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

import static com.techexchange.mobileapps.assignment3.MainActivity.GREEN_SCORED;
import static com.techexchange.mobileapps.assignment3.MainActivity.RED_SCORED;

public class Shell {

    enum State {
        IN_MOTION, COLLIDED
    }

    // Constants
    private static final long DELAY_MS = 30;
    private static final float TIME_STEP = DELAY_MS / 1000.f;
    private static final int START_INDEX = 2;

    private final Context context;
    private final MainActivity activity;
    private final List<Bitmap> bitmaps;
    private final int screenWidth;
    private final int screenHeight;
    private final int tankWidth;
    private final int tankHeight;
    private final int xSpeed;
    private final int ySpeed;


    private int bitmapIndex;
    private Tank.Direction direction;
    private Rect rect;
    private State state;
    private Brick collidedBrick;

    private int score;

    Shell(Context context, int screenWidth, int screenHeight, int tankWidth, int tankHeight, int tankXSpeed, int tankYSpeed) {
        this.context = context;
        this.activity = (MainActivity) context;
        this.bitmaps = getShellBitmaps();
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.tankWidth = tankWidth;
        this.tankHeight = tankHeight;
        this.xSpeed = tankXSpeed * 3;
        this.ySpeed = tankYSpeed * 3;

        this.bitmapIndex = START_INDEX;
        this.direction = null;
        this.rect = null;
        this.state = State.COLLIDED;
        this.collidedBrick = null;

        this.score = 0;
    }

    // ------------------------------- Public methods -----------------------------------

    public void draw(Canvas canvas, List<Brick> bricks, Tank otherTank) {
        drawScore(canvas, otherTank);
        if (rect != null) {
            canvas.drawBitmap(bitmaps.get(bitmapIndex), null, rect, null);
            updatePosition(bricks, otherTank);
        }
    }

    public void getExplosionRect(Rect tankRect, Tank.Direction direction) {
        if (this.state == State.COLLIDED && rect == null) {
            int left = tankRect.left;
            int top = tankRect.top;
            this.direction = direction;
            this.state = State.IN_MOTION;

            if (direction == Tank.Direction.UP) {
                top -= tankHeight / 2;
            } else if (direction == Tank.Direction.DOWN) {
                top += tankHeight / 2;
            } else if (direction == Tank.Direction.LEFT) {
                left -= tankHeight / 2;
            } else if (direction == Tank.Direction.RIGHT) {
                left += tankHeight / 2;
            }

            this.rect = new Rect(left, top, left + tankWidth, top + tankHeight);
        }
    }

    public void incrementScore() {
        this.score += 1;
    }

    // ------------------------------- Private methods -----------------------------------

    private void drawScore(Canvas canvas, Tank otherTank) {
        String score = "Score: " + String.valueOf(this.score);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setTextSize(50);
        if (otherTank.getColor() == Tank.Color.RED) { // This is the green tank
            paint.setColor(Color.GREEN);
            canvas.drawText(score, screenWidth - 220, screenHeight - 20, paint);
        } else if (otherTank.getColor() == Tank.Color.GREEN) {
            paint.setColor(Color.RED);
            canvas.drawText(score, 16,66, paint);
        }
    }

    private void updatePosition(List<Brick> bricks, Tank otherTank) {
        if (state == State.IN_MOTION) {
            moveShell(bricks, otherTank);
        } else if (state == State.COLLIDED) {
            if (bitmapIndex < bitmaps.size() - 1) {
                bitmapIndex += 1;
            } else {
                if (collidedBrick != null) {
                    Brick.Condition brickCondition = collidedBrick.getCondition();
                    if (brickCondition == Brick.Condition.GOOD) {
                        collidedBrick.setCondition(Brick.Condition.DAMAGED);
                    } else if (brickCondition == Brick.Condition.DAMAGED) {
                        bricks.remove(collidedBrick);
                    }
                    collidedBrick = null;
                }
                bitmapIndex = START_INDEX;
                rect = null;
                direction = null;
            }
        }
    }

    private void moveShell(List<Brick> bricks, Tank otherTank) {
        if (this.rect != null) {

            if (direction == Tank.Direction.UP) {
                moveUp(bricks, otherTank);
            } else if (direction == Tank.Direction.DOWN) {
                moveDown(bricks, otherTank);
            } else if (direction == Tank.Direction.LEFT) {
                moveLeft(bricks, otherTank);
            } else if (direction == Tank.Direction.RIGHT) {
                moveRight(bricks, otherTank);
            }
        }
    }

    private void moveUp(List<Brick> bricks, Tank otherTank) {
        rect.top -= ySpeed * TIME_STEP;
        rect.bottom -= ySpeed * TIME_STEP;
        if (collides(bricks, otherTank)) {
            this.state = State.COLLIDED;
        }
    }

    private void moveDown(List<Brick> bricks, Tank otherTank) {
        rect.top += ySpeed * TIME_STEP;
        rect.bottom += ySpeed * TIME_STEP;
        if (collides(bricks, otherTank)) {
            this.state = State.COLLIDED;
        }
    }

    private void moveLeft(List<Brick> bricks, Tank otherTank) {
        rect.left -= xSpeed * TIME_STEP;
        rect.right -= xSpeed * TIME_STEP;
        if (collides(bricks, otherTank)) {
            this.state = State.COLLIDED;
        }
    }

    private void moveRight(List<Brick> bricks, Tank otherTank) {
        rect.left += xSpeed * TIME_STEP;
        rect.right += xSpeed * TIME_STEP;
        if (collides(bricks, otherTank)) {
            this.state = State.COLLIDED;
        }
    }

    private boolean collides(List<Brick> bricks, Tank otherTank) {
        // Would go off screen
        if (rect.right >= screenWidth || rect.left <= 0) {
            return true;
        }
        if (rect.bottom >= screenHeight || rect.top <= 0) {
            return true;
        }

        // Collides with other tank.
        if (otherTank.getRect().contains(rect.centerX(), rect.centerY())) {
            if (otherTank.getColor() == Tank.Color.RED) { // This is the green tank
                activity.sendReceiveThread.write(new byte[]{GREEN_SCORED});
            } else if (otherTank.getColor() == Tank.Color.GREEN){
                activity.sendReceiveThread.write(new byte[]{RED_SCORED});
            }
            incrementScore();
            return true;
        }

        // Collides with bricks
        for (Brick brick : bricks) {
            if (brick.getRect().contains(rect.centerX(), rect.centerY())) {
                this.collidedBrick = brick;
                return true;
            }
        }

        // Does not collide with anything.
        return false;
    }

    private List<Bitmap> getShellBitmaps() {
        List<Bitmap> explosionBitmaps = new ArrayList<>();
        Bitmap spriteSheet = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.explosions);
        int tileWidth = spriteSheet.getWidth() / 8;
        int tileHeight = spriteSheet.getHeight() / 4;
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 8; ++j) {
                Bitmap explosion = Bitmap.createBitmap(spriteSheet,
                        j * tileWidth, i * tileHeight, tileWidth, tileHeight);
                explosionBitmaps.add(explosion);
            }
        }
        return explosionBitmaps;
    }
}
