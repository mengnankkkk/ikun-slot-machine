package com.ikun.slotmachine.storage;

import android.content.Context;
import android.content.SharedPreferences;
import com.ikun.slotmachine.model.User;
import java.util.HashSet;
import java.util.Set;

public class UserPreferences {
    private static final String PREFS_NAME = "slot_machine_prefs";
    private static final String KEY_USERS = "users_";
    private static final String KEY_CURRENT_USER = "current_user";
    private static final String KEY_POINTS = "points_";

    private SharedPreferences sharedPreferences;

    public UserPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean registerUser(String username, String password) {
        if (userExists(username)) {
            return false;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERS + username, password);
        editor.putInt(KEY_POINTS + username, 100);
        editor.apply();
        return true;
    }

    public boolean loginUser(String username, String password) {
        String storedPassword = sharedPreferences.getString(KEY_USERS + username, null);
        if (storedPassword != null && storedPassword.equals(password)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_CURRENT_USER, username);
            editor.apply();
            return true;
        }
        return false;
    }

    public boolean userExists(String username) {
        return sharedPreferences.contains(KEY_USERS + username);
    }

    public String getCurrentUser() {
        return sharedPreferences.getString(KEY_CURRENT_USER, null);
    }

    public int getUserPoints(String username) {
        return sharedPreferences.getInt(KEY_POINTS + username, 0);
    }

    public void updateUserPoints(String username, int points) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_POINTS + username, points);
        editor.apply();
    }

    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_CURRENT_USER);
        editor.apply();
    }
}
