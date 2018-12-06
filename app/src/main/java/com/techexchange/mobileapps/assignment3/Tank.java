package com.techexchange.mobileapps.assignment3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;

import java.util.List;

public class Tank {

    static class Bitmaps {
        Bitmap up;
        Bitmap down;
        Bitmap left;
        Bitmap right;

        Bitmaps(Bitmap up, Bitmap down, Bitmap left, Bitmap right) {
            this.up = up;
            this.down = down;
            this.left = left;
            this.right = right;
        }
    }

    enum State {
        IN_MOTION, STATIONARY
    }

    enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    enum Color {
        GREEN, RED
    }

    // Constants
    private static final long DELAY_MS = 30;
    private static final float TIME_STEP = DELAY_MS / 1000.f;

    // Required Parameters
    private final Context context;
    private final Color color;
    private final int screenWidth;
    private final int screenHeight;

    private final Bitmaps bitmaps;
    private final int brickWidth;
    private final int brickHeight;
    private final int tankWidth;
    private final int tankHeight;
    private final int leftOffset;       // Offset of the tank's left from bricks
    private final int topOffset;        // Offset of the tank's top from bricks
    private final int xSpeed;
    private final int ySpeed;

    private State state;
    private Direction direction;
    private Bitmap bitmap;
    private Rect rect;
    private int destinationLeft;
    private int destinationTop;
    private Shell shell;


    Tank(Context context, Color color, int screenWidth, int screenHeight) {
        this.context = context;
        this.color = color;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        this.bitmaps = getBitmaps();
        this.brickWidth = screenWidth / 8;
        this.brickHeight = screenHeight / 12;
        this.tankWidth = Math.min(brickWidth, brickHeight) - 8;        // NOTE: Tank is square.
        this.tankHeight = Math.min(brickWidth, brickHeight) - 8;
        this.leftOffset = (brickWidth - tankWidth) / 2;
        this.topOffset = (brickHeight - tankHeight) / 2;
        this.ySpeed = tankHeight * 2;
        this.xSpeed = tankWidth * 2;

        this.state = State.STATIONARY;
        this.direction = (this.color == Color.GREEN) ? Direction.UP : Direction.DOWN;
        this.bitmap = (this.color == Color.GREEN) ? bitmaps.up : bitmaps.down;
        this.rect = getStartRect();
        this.destinationLeft = rect.left;
        this.destinationTop = rect.top;
        this.shell = new Shell(context, screenWidth, screenHeight, tankWidth, tankHeight, xSpeed, ySpeed);
    }

    // ------------------------------- Public methods -----------------------------------

    public void draw(Canvas canvas, List<Brick> bricks, Tank otherTank) {
        canvas.drawBitmap(bitmap, null, rect, null);
        this.shell.draw(canvas, bricks, otherTank);
        updatePosition();
    }

    public Rect getRect() {
        return this.rect;
    }

    public Shell getShell() {
        return this.shell;
    }

    public Direction getDirection() {
        return  this.direction;
    }

    public Color getColor() { return this.color; }

    public void handleUp(List<Brick> bricks, Tank otherTank) {
        if (state == State.STATIONARY
                || (state == State.IN_MOTION && direction == Direction.DOWN)) {
            bitmap = bitmaps.up;
            direction = Direction.UP;
            if (!collides(bricks, otherTank)) {
                destinationTop -= brickHeight;
                moveUp();
            }
        }
    }

    public void handleDown(List<Brick> bricks, Tank otherTank) {
        if (state == State.STATIONARY
                || (state == State.IN_MOTION && direction == Direction.UP)) {
            bitmap = bitmaps.down;
            direction = Direction.DOWN;
            if (!collides(bricks, otherTank)) {
                destinationTop += brickHeight;
                moveDown();
            }
        }
    }

    public void handleLeft(List<Brick> bricks, Tank otherTank) {
        if (state == State.STATIONARY
                || (state == State.IN_MOTION && direction == Direction.RIGHT)) {
            bitmap = bitmaps.left;
            direction = Direction.LEFT;
            if (!collides(bricks, otherTank)) {
                destinationLeft -= brickWidth;
                moveLeft();
            }
        }
    }

    public void handleRight(List<Brick> bricks, Tank otherTank) {
        if (state == State.STATIONARY
                || (state == State.IN_MOTION && direction == Direction.LEFT)) {
            bitmap = bitmaps.right;
            direction = Direction.RIGHT;
            if (!collides(bricks, otherTank)) {
                destinationLeft += brickWidth;
                moveRight();
            }
        }
    }

    // ------------------------------- Private methods -----------------------------------

    private void updatePosition() {
        if (state == State.IN_MOTION) {
            if (direction == Direction.UP) {
                moveUp();
            } else if (direction == Direction.DOWN) {
                moveDown();
            } else if (direction == Direction.LEFT) {
                moveLeft();
            } else if (direction == Direction.RIGHT) {
                moveRight();
            }
        }
    }

    private void moveUp() {
        state = State.IN_MOTION;
        rect.top -= ySpeed * TIME_STEP;
        rect.bottom -= ySpeed * TIME_STEP;
        if (rect.top <= destinationTop) {
            state = State.STATIONARY;
            int depth = destinationTop - rect.top;
            rect.top += depth;
            rect.bottom += depth;
        }
    }

    private void moveDown() {
        state = State.IN_MOTION;
        rect.top += ySpeed * TIME_STEP;
        rect.bottom += ySpeed * TIME_STEP;
        if (rect.top >= destinationTop) {
            state = State.STATIONARY;
            int depth = rect.top - destinationTop;
            rect.top -= depth;
            rect.bottom -= depth;
        }
    }

    private void moveLeft() {
        state = State.IN_MOTION;
        rect.left -= xSpeed * TIME_STEP;
        rect.right -= xSpeed * TIME_STEP;
        if (rect.left <= destinationLeft) {
            state = State.STATIONARY;
            int depth = destinationLeft - rect.left;
            rect.left += depth;
            rect.right += depth;
        }
    }

    private void moveRight() {
        state = State.IN_MOTION;
        rect.left += xSpeed * TIME_STEP;
        rect.right += xSpeed * TIME_STEP;
        if (rect.left > destinationLeft) {
            state = State.STATIONARY;
            int depth = rect.left - destinationLeft;
            rect.left -= depth;
            rect.right -= depth;
        }
    }

    private boolean collides(List<Brick> bricks, Tank otherTank) {
        int destLeft = destinationLeft;
        int destTop = destinationTop;
        int destRight = destLeft + tankWidth;
        int destBottom = destTop + tankHeight;

        if (direction == Direction.UP) {
            destTop -= brickHeight;
            destBottom -= brickHeight;
        } else if (direction == Direction.DOWN) {
            destTop += brickHeight;
            destBottom += brickHeight;
        } else if (direction == Direction.LEFT) {
            destLeft -= brickWidth;
            destRight -= brickWidth;
        } else if (direction == Direction.RIGHT) {
            destLeft += brickWidth;
            destRight += brickWidth;
        }

        // Would go off screen
        if (destRight >= screenWidth || destLeft <= 0) {
            return true;
        }
        if (destBottom >= screenHeight || destTop <= 0) {
            return true;
        }

        int destCenterX = destLeft + (brickWidth / 2);
        int destCenterY = destTop + (brickHeight / 2);

        // Collides with other tank.
        if (otherTank.getRect().contains(destCenterX, destCenterY)) {
            return true;
        }


        // Would collide with bricks
        for (Brick brick : bricks) {
            if (brick.getRect().contains(destCenterX, destCenterY)) {
                return true;
            }
        }

        // Does not collide with anything.
        return false;
    }

    private Bitmap getRightBitmap() {
        Bitmap spriteSheet = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.multicolortanks);
        int tileWidth = spriteSheet.getWidth() / 8;
        int tileHeight = spriteSheet.getHeight() / 8;
        if (color == Color.GREEN) {
            return Bitmap.createBitmap(spriteSheet, 0, 0, tileWidth, tileHeight);
        } else if (color == Color.RED) {
            return Bitmap.createBitmap(spriteSheet, 0, tileHeight, tileWidth, tileHeight);
        }
        return null;
    }

    private Bitmap rotateClockwise90Deg(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
    }

    private Bitmaps getBitmaps() {
        Bitmap right = getRightBitmap();
        Bitmap down  = rotateClockwise90Deg(right);
        Bitmap left  = rotateClockwise90Deg(down);
        Bitmap up  = rotateClockwise90Deg(left);
        return new Bitmaps(up, down, left, right);
    }

    private Rect getStartRect() {
        int left = leftOffset + screenWidth - (4 * brickWidth);
        int right = left + tankWidth;
        int top = 0;
        if (color == Color.GREEN) {
            top = topOffset + (screenHeight - brickHeight);
        } else if (color == Color.RED) {
            top = topOffset;
        }
        int bottom = top + tankHeight;
        return new Rect(left, top, right, bottom);
    }
}
