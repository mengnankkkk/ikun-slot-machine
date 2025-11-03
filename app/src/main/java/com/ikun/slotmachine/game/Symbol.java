package com.ikun.slotmachine.game;

public enum Symbol {
    CHERRY(android.R.drawable.ic_menu_camera, "Cherry", 0),
    BELL(android.R.drawable.ic_dialog_email, "Bell", 0),
    MAP(android.R.drawable.ic_dialog_info, "Map", 0),
    CAMERA(android.R.drawable.ic_dialog_map, "Camera", 0),
    DAY(android.R.drawable.ic_menu_day, "Day", 0),
    BOMB(android.R.drawable.ic_dialog_alert, "Bomb", -20);

    public final int drawableId;
    public final String name;
    public final int pointModifier;

    Symbol(int drawableId, String name, int pointModifier) {
        this.drawableId = drawableId;
        this.name = name;
        this.pointModifier = pointModifier;
    }
}
