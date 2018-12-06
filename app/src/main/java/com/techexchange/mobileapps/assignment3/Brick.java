package com.techexchange.mobileapps.assignment3;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Brick {

    static class Bitmaps {
        Bitmap good;
        Bitmap damaged;

        Bitmaps(Bitmap good, Bitmap damaged) {
            this.good = good;
            this.damaged = damaged;
        }
    }

    static class Start {
        int left;
        int top;

        Start(int left, int top) {
            this.left = left;
            this.top = top;
        }
    }

    static class Rectangle {
        private final Rect rect;

        Rectangle(Start start, int width, int height) {
            this.rect = new Rect(start.left, start.top,
                    start.left + width, start.top + height);
        }

        public Rect getRect() {
            return this.rect;
        }
    }

    enum Condition {
        GOOD, DAMAGED, DESTROYED
    }

    // Required Parameters
    private final Rect rect;
    private final Bitmaps bitmaps;

    private Condition condition;

    Brick(Rect rect, Bitmaps bitmaps) {
        this.rect = rect;
        this.bitmaps = bitmaps;
        setCondition(Condition.GOOD);
    }

    // ------------------------------- Public methods -----------------------------------

    public void draw(Canvas canvas) {
        if (condition != Condition.DESTROYED) {
            if (condition == Condition.GOOD) {
                canvas.drawBitmap(bitmaps.good, null, rect, null);
            }
            else if (condition == Condition.DAMAGED) {
                canvas.drawBitmap(bitmaps.damaged, null, rect, null);
            }
        }
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Condition getCondition() {
        return this.condition;
    }

    public Rect getRect() {
        return this.rect;
    }
}
