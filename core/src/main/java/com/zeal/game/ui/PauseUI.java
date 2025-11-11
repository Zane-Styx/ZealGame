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

    // Track the currently shown pause dialog so repeated calls to show()
    // don't create multiple stacked dialogs.
    private static Dialog currentDialog = null;

    /**
     * Returns true if a pause dialog is currently visible.
     */
    public static boolean isShowing() {
        return currentDialog != null && currentDialog.isVisible();
    }

    /**
     * Hide the currently shown pause dialog, if any.
     */
    public static void hide() {
        if (currentDialog != null) {
            currentDialog.hide();
            // hide() override will clear currentDialog, but ensure null just in case
            currentDialog = null;
        }
    }

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
        final Skin skin = loadSkin();

        // If a pause dialog is already visible, bring it to front and do nothing.
        if (currentDialog != null && currentDialog.isVisible()) {
            currentDialog.toFront();
            return;
        }

        // Create a dialog and override hide() so we can clear the currentDialog reference
        final Dialog dialog = new Dialog("Paused", skin) {
            @Override
            public void hide() {
                super.hide();
                currentDialog = null;
            }
        };
        currentDialog = dialog;
        dialog.getContentTable().pad(10);
        dialog.getContentTable().add(new Label("Game paused", skin)).row();

        TextButton resume = new TextButton("Resume", skin);
        TextButton settingsBtn = new TextButton("Settings", skin);
        TextButton mainMenu = new TextButton("Main Menu", skin);

        resume.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dialog.hide();
            }
        });

        settingsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // show settings dialog
                Dialog s = new Dialog("Settings", skin);
                final TextField usernameField = new TextField(settings.getUsername(), skin);
                s.getContentTable().add(new Label("Username:", skin)).left();
                s.getContentTable().add(usernameField).width(250).row();
                TextButton save = new TextButton("Save", skin);
                save.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        settings.setUsername(usernameField.getText().trim());
                        s.hide();
                    }
                });
                s.getButtonTable().add(save);
                s.show(stage);
            }
        });

        mainMenu.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dialog.hide();
                if (onMainMenu != null) onMainMenu.run();
            }
        });

        dialog.getButtonTable().add(resume).pad(6);
        dialog.getButtonTable().add(settingsBtn).pad(6);
        dialog.getButtonTable().add(mainMenu).pad(6);
        dialog.show(stage);
    }
}
