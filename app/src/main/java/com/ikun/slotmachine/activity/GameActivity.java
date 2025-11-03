package com.ikun.slotmachine.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.TranslateAnimation;
import com.ikun.slotmachine.game.Symbol;
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
    private Symbol[] lastResults;

    private static final int ANIMATION_DURATION = 2500;
    private static final int[] REEL_STOP_TIMES = {600, 1200, 1800, 2400};
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
        Symbol[] allSymbols = gameLogic.getAllSymbols();
        lastResults = gameLogic.generateResults();

        long globalStartTime = System.currentTimeMillis();

        for (int i = 0; i < 4; i++) {
            animateReelWithScrolling(reels[i], allSymbols, i, globalStartTime);
        }

        handler.postDelayed(this::processResults, ANIMATION_DURATION + 1500);
    }

    private void animateReelWithScrolling(ImageView reel, Symbol[] allSymbols, int reelIndex, long globalStartTime) {
        final int[] frameIndex = {0};
        final int stopTime = REEL_STOP_TIMES[reelIndex];
        final Symbol targetSymbol = lastResults[reelIndex];

        final Runnable scrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (isSpinning) {
                    Symbol currentSymbol = allSymbols[frameIndex[0] % allSymbols.length];
                    reel.setImageResource(currentSymbol.drawableId);
                    
                    TranslateAnimation slideIn = new TranslateAnimation(0, 0, -100, 0);
                    slideIn.setDuration(100);
                    reel.startAnimation(slideIn);
                    
                    frameIndex[0]++;
                    
                    long elapsedTime = System.currentTimeMillis() - globalStartTime;
                    
                    if (elapsedTime < stopTime) {
                        float progress = (float) elapsedTime / stopTime;
                        float accelerationFactor = progress * progress;
                        int minDelay = 30;
                        int maxDelay = 500;
                        int delay = minDelay + (int) ((maxDelay - minDelay) * accelerationFactor);
                        handler.postDelayed(this, delay);
                    } else {
                        reel.setImageResource(targetSymbol.drawableId);
                        reel.clearAnimation();
                    }
                }
            }
        };

        handler.post(scrollRunnable);
    }

    private void processResults() {
        isSpinning = false;
        processWinnings();
    }

    private void processWinnings() {
        StringBuilder resultMessage = new StringBuilder();
        int totalPointChange = 0;

        for (Symbol symbol : lastResults) {
            int modifier = gameLogic.getPointModifier(symbol);
            if (modifier != 0) {
                totalPointChange += modifier;
                resultMessage.append(symbol.name).append(": ").append(modifier).append(" | ");
            }
        }

        int multiplier = gameLogic.calculateMultiplier(lastResults);
        int winnings = gameLogic.calculateWinnings(multiplier);

        if (multiplier > 0) {
            totalPointChange += winnings;
            resultMessage.append("Match Bonus: +").append(winnings);
        }

        if (totalPointChange != 0) {
            currentPoints += totalPointChange;
            String toastMsg = totalPointChange > 0 
                ? "WIN! +" + totalPointChange + " points!" 
                : "BOMB! " + totalPointChange + " points!";
            Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
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
