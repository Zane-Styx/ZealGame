package com.zeal.game.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.zeal.game.settings.Settings;

/**
 * Small helper to show a pause dialog with Resume / Settings / Main Menu.
 */
public final class PauseUI {
    private PauseUI() {}
    final static Skin skin = loadSkin();
    static Dialog dialog = new Dialog("Paused", skin)    ;

    // UIManager will manage which dialogs are visible; PauseUI delegates to it.

    private static Skin loadSkin() {
        try {
            return new Skin(com.badlogic.gdx.Gdx.files.internal("ui/uiskin.json"));
        } catch (Exception e) {
            try { return new Skin(); } catch (Exception ex) { return new Skin(); }
        }
    }

    /**
     * Show the pause dialog on the given stage.
     * @param stage stage to attach dialogs to
     * @param settings settings instance to edit username
     * @param onMainMenu runnable invoked when user selects Main Menu
     */
    public static void show(Stage stage, Settings settings, Runnable onMainMenu) {
        com.badlogic.gdx.Gdx.app.log("PauseUI", "show() called - creating pause dialog");

        dialog = new Dialog("Paused", skin) {
            @Override
            public void hide() {
                super.hide();
                UIManager.dialogHidden(this);
            }
        };
        dialog.getContentTable().pad(10);
        dialog.getContentTable().add(new Label("Game paused", skin)).row();

        TextButton resume = new TextButton("Resume", skin);
        TextButton settingsBtn = new TextButton("Settings", skin);
        TextButton mainMenu = new TextButton("Main Menu", skin);

        resume.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // close the pause dialog via UIManager
                UIManager.closeCurrent();
            }
        });

        settingsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Use centralized SettingsUI for in-game settings (placeholder for more options)
                SettingsUI.showForInGame(stage, settings);
            }
        });

        mainMenu.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                UIManager.closeCurrent();
                if (onMainMenu != null) onMainMenu.run();
            }
        });

        dialog.getButtonTable().add(resume).pad(6);
        dialog.getButtonTable().add(settingsBtn).pad(6);
        dialog.getButtonTable().add(mainMenu).pad(6);
        UIManager.openDialog(dialog, stage);
    }

    public static void hide() {
        // Delegate hide to UIManager so the dialog stack is maintained correctly.
        UIManager.closeCurrent();
    }

    public static boolean isShowing() {
        // Backwards-compat shim: delegate to UIManager
        return UIManager.isShowing();
    }
}