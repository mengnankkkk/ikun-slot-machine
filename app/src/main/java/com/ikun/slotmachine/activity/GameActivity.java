package com.ikun.slotmachine.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.ikun.slotmachine.R;
import com.ikun.slotmachine.game.GameLogic;
import com.ikun.slotmachine.storage.UserPreferences;

public class GameActivity extends AppCompatActivity {
    private ImageView[] reels = new ImageView[4];
    private TextView pointsView;
    private Button spinButton;
    private Button logoutButton;
    private TextView usernameView;

    private GameLogic gameLogic;
    private UserPreferences userPreferences;
    private String currentUsername;
    private int currentPoints;
    private boolean isSpinning = false;
    private int[] lastResults;

    private static final int ANIMATION_DURATION = 1800;
    private static final int STOP_DELAY_INTERVAL = 300;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameLogic = new GameLogic();
        userPreferences = new UserPreferences(this);

        currentUsername = userPreferences.getCurrentUser();
        if (currentUsername == null) {
            goToLogin();
            return;
        }

        initializeViews();
        loadUserData();
    }

    private void initializeViews() {
        reels[0] = findViewById(R.id.reel_1);
        reels[1] = findViewById(R.id.reel_2);
        reels[2] = findViewById(R.id.reel_3);
        reels[3] = findViewById(R.id.reel_4);

        pointsView = findViewById(R.id.points_view);
        spinButton = findViewById(R.id.spin_button);
        logoutButton = findViewById(R.id.logout_button);
        usernameView = findViewById(R.id.username_view);

        spinButton.setOnClickListener(v -> handleSpin());
        logoutButton.setOnClickListener(v -> handleLogout());
    }

    private void loadUserData() {
        currentPoints = userPreferences.getUserPoints(currentUsername);
        updatePointsDisplay();
        usernameView.setText("User: " + currentUsername);
    }

    private void updatePointsDisplay() {
        pointsView.setText("Points: " + currentPoints);
    }

    private void handleSpin() {
        if (isSpinning) {
            return;
        }

        // Check if user can afford spin
        if (!gameLogic.canAffordSpin(currentPoints)) {
            Toast.makeText(this, "Insufficient points! Please recharge or wait.", Toast.LENGTH_SHORT).show();
            return;
        }

        isSpinning = true;
        spinButton.setEnabled(false);

        // Deduct cost
        currentPoints -= gameLogic.getCostPerSpin();
        updatePointsDisplay();

        // Start animation
        startReelAnimation();
    }

    private void startReelAnimation() {
        int[] animationFrames = {
            android.R.drawable.ic_menu_camera,
            android.R.drawable.ic_dialog_email,
            android.R.drawable.ic_dialog_info,
            android.R.drawable.ic_dialog_map,
            android.R.drawable.ic_menu_day
        };

        for (ImageView reel : reels) {
            animateReelWithRotation(reel, animationFrames);
        }

        for (int i = 0; i < 4; i++) {
            final int reelIndex = i;
            handler.postDelayed(() -> stopReelWithDelay(reelIndex), 
                ANIMATION_DURATION + (i * STOP_DELAY_INTERVAL));
        }

        handler.postDelayed(this::getResults, 
            ANIMATION_DURATION + (3 * STOP_DELAY_INTERVAL) + 500);
    }

    private void animateReelWithRotation(ImageView reel, int[] frames) {
        final int[] frameIndex = {0};
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isSpinning) {
                    reel.setImageResource(frames[frameIndex[0] % frames.length]);
                    frameIndex[0]++;
                    handler.postDelayed(this, 80);
                }
            }
        });
    }

    private void stopReelWithDelay(int reelIndex) {
        Animation stopAnimation = AnimationUtils.loadAnimation(this, R.anim.reel_stop);
        reels[reelIndex].startAnimation(stopAnimation);
    }

    private void getResults() {
        isSpinning = false;
        lastResults = gameLogic.generateResults();
        displayResultsWithDelay(lastResults);
    }

    private void displayResultsWithDelay(int[] results) {
        for (int i = 0; i < 4; i++) {
            final int index = i;
            handler.postDelayed(() -> {
                reels[index].setImageResource(results[index]);
                reels[index].clearAnimation();
            }, i * 150L);
        }

        handler.postDelayed(this::processWinnings, 600);
    }

    private void processWinnings() {
        int multiplier = gameLogic.calculateMultiplier(lastResults);
        int winnings = gameLogic.calculateWinnings(multiplier);

        if (multiplier > 0) {
            currentPoints += winnings;
            Toast.makeText(this, "WIN! " + multiplier + " symbols match! +$ " + winnings + " points!", 
                Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "No match, try again!", Toast.LENGTH_SHORT).show();
        }

        updatePointsDisplay();
        saveUserData();
        spinButton.setEnabled(true);
    }

    private void saveUserData() {
        userPreferences.updateUserPoints(currentUsername, currentPoints);
    }

    private void handleLogout() {
        userPreferences.logout();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(GameActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
