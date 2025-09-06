package com.tamz.soko2024;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

public class SokoView extends View {

    private Bitmap[] bmp;
    private int lW = 10;
    private int lH = 10;
    private int width;
    private int height;
    private int[] level;
    private int[] originalLevel;
    private int playerX;
    private int playerY;
    private MainActivity mainActivity; // Reference to MainActivity
    private int moveCount=0;
    private int levelIndex;

    public SokoView(Context context) {
        super(context);
        init(context);
    }

    public SokoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SokoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setMainActivity(MainActivity activity) {
        this.mainActivity = activity;
    }

    private void init(Context context) {
        bmp = new Bitmap[6];
        bmp[0] = BitmapFactory.decodeResource(getResources(), R.drawable.empty);
        bmp[1] = BitmapFactory.decodeResource(getResources(), R.drawable.wall);
        bmp[2] = BitmapFactory.decodeResource(getResources(), R.drawable.box);
        bmp[3] = BitmapFactory.decodeResource(getResources(), R.drawable.goal);
        bmp[4] = BitmapFactory.decodeResource(getResources(), R.drawable.hero);
        bmp[5] = BitmapFactory.decodeResource(getResources(), R.drawable.boxok);
    }

    public void loadLevel(String levelData, int levelIndex) {
        this.levelIndex=levelIndex;
        moveCount=0;
        calculateGridSize(levelData);

        level = new int[lW * lH];
        originalLevel = new int[lW * lH];

        parseLevelData(levelData);

        resetPlayerPosition();
        invalidate();
    }

    private void calculateGridSize(String levelData) {
        String[] rows = levelData.split("\n");
        lH = rows.length;

        lW = 0;
        for (String row : rows) {
            if (row.length() > lW) {
                lW = row.length();
            }
        }
    }



    private void parseLevelData(String data) {
        String[] rows = data.split("\n");

        level = new int[lW * lH];
        originalLevel = new int[lW * lH];

        for (int y = 0; y < rows.length; y++) {
            String row = rows[y];
            for (int x = 0; x < row.length(); x++) {
                char tile = row.charAt(x);
                int index = y * lW + x;
                switch (tile) {
                    case '#': level[index] = 1; break; // Wall
                    case ' ': level[index] = 0; break; // Empty
                    case '$': level[index] = 2; break; // Box
                    case '.': level[index] = 3; break; // Goal
                    case '@':
                        level[index] = 4; // Player start
                        playerX = x;
                        playerY = y;
                        break;
                    case '*': level[index] = 5; break; // Box on goal
                    default: level[index] = 0; break;
                }
                originalLevel[index] = level[index];
            }
        }
    }

    private void resetPlayerPosition() {
        level[playerY * lW + playerX] = 4;
    }

    private void checkLevelComplete() {
        boolean isComplete = true;
        for (int i = 0; i < level.length; i++) {
            if ((originalLevel[i] == 3 || originalLevel[i] == 5) && level[i] != 5) {
                isComplete = false;
                break;
            }
        }
        if (isComplete && mainActivity != null) {
            mainActivity.saveMovesForLevel(levelIndex, moveCount); // Save moves
            mainActivity.nextLevel();
        }
    }

    private void movePlayer(int x, int y) {
        int newX = playerX + x;
        int newY = playerY + y;
        moveCount++;
        if (newX < 0 || newX >= lW || newY < 0 || newY >= lH) {
            return;
        }

        int targetIndex = newY * lW + newX;
        int targetTile = level[targetIndex];

        if (targetTile == 2 || targetTile == 5) {
            int boxNewX = newX + x;
            int boxNewY = newY + y;

            if (boxNewX < 0 || boxNewX >= lW || boxNewY < 0 || boxNewY >= lH) {
                return;
            }

            int boxTargetIndex = boxNewY * lW + boxNewX;
            int boxTargetTile = level[boxTargetIndex];

            if (boxTargetTile == 0 || boxTargetTile == 3) {
                level[boxTargetIndex] = (boxTargetTile == 3) ? 5 : 2;
                level[playerY * lW + playerX] = (originalLevel[playerY * lW + playerX] == 3) ? 3 : 0;
                playerX = newX;
                playerY = newY;
                level[playerY * lW + playerX] = 4;
                invalidate();
                checkLevelComplete();
            }

        } else if (targetTile == 0 || targetTile == 3) {
            level[playerY * lW + playerX] = (originalLevel[playerY * lW + playerX] == 3 | originalLevel[playerY * lW + playerX] == 5) ? 3 : 0;
            playerX = newX;
            playerY = newY;
            level[playerY * lW + playerX] = 4;
            invalidate();
            checkLevelComplete();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w / lW;
        height = h / lH;

        super.onSizeChanged(w, h, oldw, oldh);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        for (int y = 0; y < lH; y++) {
            for (int x = 0; x < lW; x++) {
                canvas.drawBitmap(bmp[level[y * lW + x]], null,
                        new Rect(x * width, y * height, (x + 1) * width, (y + 1) * height), null);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            if (x < width * playerX) {
                movePlayer(-1, 0);
            } else if (x > width * (playerX + 1)) {
                movePlayer(1, 0);
            } else if (y < height * playerY) {
                movePlayer(0, -1);
            } else if (y > height * (playerY + 1)) {
                movePlayer(0, 1);
            }
        }
        return true;
    }
}




