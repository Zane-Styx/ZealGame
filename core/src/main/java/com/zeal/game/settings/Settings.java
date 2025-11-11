package com.zeal.game.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/** Simple typed settings wrapper using libGDX Preferences. */
public class Settings {
    private static final String PREFS_NAME = "zealgame";
    private static final String KEY_USERNAME = "username";

    private final Preferences prefs;

    public Settings() {
        this.prefs = Gdx.app.getPreferences(PREFS_NAME);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "Player");
    }

    public void setUsername(String username) {
        prefs.putString(KEY_USERNAME, username == null || username.isEmpty() ? "Player" : username);
        prefs.flush();
    }
}
