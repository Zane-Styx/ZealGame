package com.zeal.game.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.zeal.game.settings.Settings;

/**
 * Centralized settings UI. Two entry points:
 * - showForMainMenu(...) allows editing username (and will call onSaved when saved).
 * - showForInGame(...) shows only placeholder settings (username not editable here).
 */
public final class SettingsUI {
    private SettingsUI() {}

    /** Show settings when the player is on the main menu. Allows changing username. */
    public static void showForMainMenu(Stage stage, Settings settings, Runnable onSaved) {
        if (stage == null || settings == null) return;
        final Skin skin = new Skin(com.badlogic.gdx.Gdx.files.internal("ui/uiskin.json"));

        Dialog d = new Dialog("Settings", skin) {
            @Override
            public void hide() {
                super.hide();
                UIManager.dialogHidden(this);
            }
        };

        final TextField usernameField = new TextField(settings.getUsername(), skin);
        d.getContentTable().add(new Label("Username:", skin)).left();
        d.getContentTable().add(usernameField).width(300).row();

        TextButton save = new TextButton("Save", skin);
        save.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                settings.setUsername(usernameField.getText().trim());
                UIManager.closeCurrent();
                if (onSaved != null) onSaved.run();
            }
        });

        TextButton cancel = new TextButton("Cancel", skin);
        cancel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                UIManager.closeCurrent();
            }
        });

        d.getButtonTable().add(save).pad(6);
        d.getButtonTable().add(cancel).pad(6);

        UIManager.openDialog(d, stage);
    }

    /** Show settings when in-game. Username is not editable here; other settings can be added. */
    public static void showForInGame(Stage stage, Settings settings) {
        if (stage == null || settings == null) return;
        final Skin skin = new Skin(com.badlogic.gdx.Gdx.files.internal("ui/uiskin.json"));

        Dialog d = new Dialog("Settings", skin) {
            @Override
            public void hide() {
                super.hide();
                UIManager.dialogHidden(this);
            }
        };

        // For now show username as read-only and a placeholder for other settings
        d.getContentTable().add(new Label("Username:", skin)).left();
        d.getContentTable().add(new Label(settings.getUsername(), skin)).width(300).row();
        d.getContentTable().add(new Label("Other settings will be added here.", skin)).colspan(2).padTop(8).row();

        TextButton close = new TextButton("Close", skin);
        close.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                UIManager.closeCurrent();
            }
        });

        d.getButtonTable().add(close).pad(6);
        UIManager.openDialog(d, stage);
    }
}