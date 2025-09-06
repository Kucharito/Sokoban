package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "SokoPrefs";
    private static final String KEY_HIGHEST_LEVEL_UNLOCKED = "highest_level_unlocked";
    private static final String KEY_MOVES_FOR_LEVEL = "moves_for_level_";

    private SharedPreferences sharedPreferences;
    private int highestLevelUnlocked = 1;

    private List<String> levels;
    private SokoView mySoko;
    private int currentLevelIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        highestLevelUnlocked = sharedPreferences.getInt(KEY_HIGHEST_LEVEL_UNLOCKED, 1);

        mySoko = findViewById(R.id.sokoView);
        mySoko.setMainActivity(this);

        loadLevels();

        if (!levels.isEmpty()) {
            mySoko.loadLevel(levels.get(currentLevelIndex));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_levels) {
            showLevelSelectionDialog();
            return true;
        }
        else if(item.getItemId()==R.id.menu_restart){
            restartCurrentLevel();
            return true;
        }
        else if(item.getItemId()==R.id.clear_history){
            clearSharedPreferences();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void nextLevel() {
        if (currentLevelIndex < levels.size() - 1) {
            currentLevelIndex++;
            mySoko.loadLevel(levels.get(currentLevelIndex));
            Toast.makeText(this, "Level " + (currentLevelIndex + 1) + " loaded", Toast.LENGTH_SHORT).show();

            if (currentLevelIndex + 1 > highestLevelUnlocked) {
                highestLevelUnlocked = currentLevelIndex + 1;
                sharedPreferences.edit().putInt(KEY_HIGHEST_LEVEL_UNLOCKED, highestLevelUnlocked).apply();
            }
        } else {
            Toast.makeText(this, "Congratulations! All levels completed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearSharedPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        highestLevelUnlocked = 1;

        Toast.makeText(this, "History cleared!", Toast.LENGTH_SHORT).show();
    }

    private void restartCurrentLevel() {
        if (levels != null && !levels.isEmpty() && mySoko != null) {
            mySoko.loadLevel(levels.get(currentLevelIndex));
            Toast.makeText(this, "Level restarted", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveMovesForLevel(int levelIndex, int moves) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_MOVES_FOR_LEVEL + levelIndex, moves);
        editor.apply();
    }

    int getMovesForLevel(int levelIndex) {
        return sharedPreferences.getInt(KEY_MOVES_FOR_LEVEL + levelIndex, -1);
    }


    private void showLevelSelectionDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_level_selection);

        ListView levelListView = dialog.findViewById(R.id.levelListView);
        List<String> levelNames = new ArrayList<>();
        for (int i = 0; i < levels.size(); i++) {
            int moves = getMovesForLevel(i);
            String moveInfo = (moves >= 0) ? " | Moves: " + moves : "";
            String levelName = "Level " + (i + 1) + (i + 1 > highestLevelUnlocked ? " (Locked)" : "") + moveInfo;
            levelNames.add(levelName);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, levelNames);
        levelListView.setAdapter(adapter);

        levelListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position + 1 <= highestLevelUnlocked) {
                    currentLevelIndex = position;
                    mySoko.loadLevel(levels.get(currentLevelIndex));
                    dialog.dismiss();
                    Toast.makeText(MainActivity.this, "Level " + (currentLevelIndex + 1) + " loaded", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "This level is locked!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    private void loadLevels() {
        levels = new ArrayList<>();
        AssetManager assetManager = getAssets();

        try (InputStream input = assetManager.open("levels.txt")) {
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            String[] levelArray = new String(buffer).split("Level \\d+");

            for (String level : levelArray) {
                if (!level.trim().isEmpty()) {
                    levels.add(level.substring(level.indexOf('\n') + 1));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading levels", e);
        }
    }

}
