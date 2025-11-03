package com.ikun.slotmachine.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GameLogic {
    private static final int COST_PER_SPIN = 10;
    private static final int INITIAL_POINTS = 100;

    private int[] symbolIds = {
        android.R.drawable.ic_dialog_email,
        android.R.drawable.ic_dialog_info,
        android.R.drawable.ic_dialog_map,
        android.R.drawable.ic_menu_camera,
        android.R.drawable.ic_menu_day
    };

    private Random random = new Random();

    public int[] generateResults() {
        int[] results = new int[4];
        for (int i = 0; i < 4; i++) {
            results[i] = symbolIds[random.nextInt(symbolIds.length)];
        }
        return results;
    }

    public int calculateMultiplier(int[] results) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int result : results) {
            Integer count = counts.get(result);
            if (count == null) {
                counts.put(result, 1);
            } else {
                counts.put(result, count + 1);
            }
        }

        int maxCount = 0;
        for (int count : counts.values()) {
            if (count > maxCount) {
                maxCount = count;
            }
        }

        if (maxCount < 2) {
            return 0;
        }
        return maxCount;
    }

    public int calculateWinnings(int multiplier) {
        return COST_PER_SPIN * multiplier;
    }

    public boolean canAffordSpin(int currentPoints) {
        return currentPoints >= COST_PER_SPIN;
    }

    public int getCostPerSpin() {
        return COST_PER_SPIN;
    }

    public int getInitialPoints() {
        return INITIAL_POINTS;
    }

    public String getSymbolName(int symbolId, int index) {
        String[] names = {"Cherry", "Bell", "Map", "Camera", "Day"};
        for (int i = 0; i < symbolIds.length; i++) {
            if (symbolIds[i] == symbolId) {
                return names[i];
            }
        }
        return "Unknown";
    }
}
