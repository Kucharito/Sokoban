package com.example.myapplication;

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

public class SokoView extends View {

    private static final String PREFS_NAME = "SokoPrefs";
    private static final String KEY_USE_NEW_IMAGES = "use_new_images";

    private Bitmap[] bmp;
    private int lW = 10;
    private int lH = 10;
    private int width;
    private int height;
    private int[] level;
    private int[] originalLevel;
    private int playerX;
    private int playerY;
    private MainActivity mainActivity;
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
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        boolean useNewImages = preferences.getBoolean(KEY_USE_NEW_IMAGES, false);

        bmp = new Bitmap[6];
        bmp[0] = BitmapFactory.decodeResource(getResources(), useNewImages ? R.drawable.emptynew : R.drawable.empty);
        bmp[1] = BitmapFactory.decodeResource(getResources(), useNewImages ? R.drawable.wallnew : R.drawable.wall);
        bmp[2] = BitmapFactory.decodeResource(getResources(), useNewImages ? R.drawable.boxnew : R.drawable.box);
        bmp[3] = BitmapFactory.decodeResource(getResources(), useNewImages ? R.drawable.goalnew : R.drawable.goal);
        bmp[4] = BitmapFactory.decodeResource(getResources(), useNewImages ? R.drawable.heronew : R.drawable.hero);
        bmp[5] = BitmapFactory.decodeResource(getResources(), useNewImages ? R.drawable.boxoknew : R.drawable.boxok);
    }

    public void loadLevel(String levelData) {
        //this.levelIndex=levelIndex;
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

        //level = new int[lW * lH];
        //originalLevel = new int[lW * lH];

        for (int y = 0; y < rows.length; y++) {
            String row = rows[y];
            for (int x = 0; x < row.length(); x++) {
                char tile = row.charAt(x);
                int index = y * lW + x;
                switch (tile) {
                    case '#': level[index] = 1; break;
                    case ' ': level[index] = 0; break;
                    case '$': level[index] = 2; break;
                    case '.': level[index] = 3; break;
                    case '@':
                        level[index] = 4;
                        playerX = x;
                        playerY = y;
                        break;
                    case '*': level[index] = 5; break;
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
            int savedMoves = mainActivity.getMovesForLevel(levelIndex);

            if (savedMoves == -1 || moveCount < savedMoves) {
                mainActivity.saveMovesForLevel(levelIndex, moveCount);
            }

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




