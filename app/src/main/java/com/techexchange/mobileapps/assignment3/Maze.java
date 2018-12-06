package com.techexchange.mobileapps.assignment3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

public class Maze {

    private final Context context;
    private final int screenWidth;
    private final int screenHeight;

    private final int brickWidth;
    private final int brickHeight;
    private final Brick.Bitmaps bitmaps;
    private final List<Brick> bricks;


    Maze(Context context, int screenWidth, int screenHeight) {
        this.context = context;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.brickWidth = screenWidth / 8;
        this.brickHeight = screenHeight / 12;
        this.bitmaps = getBitmaps();
        this.bricks = new ArrayList<>();
        createMaze();
    }

    // ------------------------------- Public methods -----------------------------------
    public void draw(Canvas canvas) {
        for (Brick brick : bricks) {
            brick.draw(canvas);
        }
    }

    public List<Brick> getBricks() {
        return this.bricks;
    }

    // ------------------------------- Private methods -----------------------------------

    private Brick.Bitmaps getBitmaps() {
        Bitmap good = BitmapFactory
                .decodeResource(this.context.getResources(), R.drawable.brick_good);
        Bitmap damaged = BitmapFactory
                .decodeResource(this.context.getResources(), R.drawable.brick_damaged);
        return new Brick.Bitmaps(good, damaged);
    }

    /**
     * Adds the column bricks to bricks.
     * Returns the start for next col or row.
     * */
    private Brick.Start createColumn(Brick.Start start, int numBricks) {
        for (int i = 0; i < numBricks; ++i) {
            Rect rect = new Brick.Rectangle(start, brickWidth, brickHeight).getRect();
            Brick brick = new Brick(rect, this.bitmaps);
            this.bricks.add(brick);
            start.top += brickHeight;
        }
        return start;
    }

    /**
     * Adds the row bricks to bricks.
     * Returns the start for the next col or row.
     * */
    private Brick.Start createRow(Brick.Start start, int numBricks) {
        for (int i = 0; i < numBricks; ++i) {
            Rect rect = new Brick.Rectangle(start, brickWidth, brickHeight).getRect();
            Brick brick = new Brick(rect, this.bitmaps);
            this.bricks.add(brick);
            start.left += brickWidth;
        }
        return start;
    }

    /**
     * Creates the maze of bricks.
     */
    private void createMaze() {
        Brick.Start start = createColumn(new Brick.Start(2 * brickWidth, 0), 3);
        createRow(start, 3);
        createRow(new Brick.Start(screenWidth - (3 * brickWidth), 0), 2);
        start = createRow(new Brick.Start(screenWidth - (6 * brickWidth),
                screenHeight - 3 * brickHeight), 2);
        start.top -= brickHeight * 2;
        start = createColumn(start, 3);
        start.left += brickWidth;
        start.top -= brickHeight;
        createColumn(start, 3);
    }
}
