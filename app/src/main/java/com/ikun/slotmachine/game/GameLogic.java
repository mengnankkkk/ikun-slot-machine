package com.ikun.slotmachine.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GameLogic {
    private static final int COST_PER_SPIN = 10;
    private static final int INITIAL_POINTS = 100;

    private Symbol[] symbols = Symbol.values();
    private Random random = new Random();

    public Symbol[] generateResults() {
        Symbol[] results = new Symbol[4];
        for (int i = 0; i < 4; i++) {
            results[i] = symbols[random.nextInt(symbols.length)];
        }
        return results;
    }

    public int getPointModifier(Symbol symbol) {
        return symbol.pointModifier;
    }

    public int calculateMultiplier(Symbol[] results) {
        Map<Symbol, Integer> counts = new HashMap<>();
        for (Symbol result : results) {
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

    public Symbol[] getAllSymbols() {
        return symbols;
    }
}
