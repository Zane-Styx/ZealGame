package com.zeal.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.zeal.game.network.NetworkConstants;

/**
 * Simple main menu with Play / Settings / Credits.
 */
public class MainMenuScreen implements Screen {
    private final Game game;
    private final Stage stage;
    private final Skin skin;

    public MainMenuScreen(Game game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label title = new Label("Zeal Game", skin, "default");
        title.setFontScale(2f);

        TextButton play = new TextButton("Play", skin);
        TextButton settings = new TextButton("Settings", skin);
        TextButton credits = new TextButton("Credits", skin);

        root.add(title).padBottom(30).row();
        root.add(play).width(200).pad(8).row();
        root.add(settings).width(200).pad(8).row();
        root.add(credits).width(200).pad(8).row();

        // Play button opens an input dialog for server IP
        play.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showPlayDialog();
            }
        });

        // Settings allow editing username stored in Preferences
        settings.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showSettingsDialog();
            }
        });

        credits.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showCreditsDialog();
            }
        });
    }

    private void showPlayDialog() {
        Dialog dialog = new Dialog("Connect to server", skin) {
            @Override
            protected void result(Object object) {
                // overridden but we handle in button listeners
            }
        };

        dialog.getContentTable().pad(10);
        final TextField hostField = new TextField(NetworkConstants.DEFAULT_HOST, skin);
        dialog.getContentTable().add(new Label("Server IP:", skin)).left();
        dialog.getContentTable().add(hostField).width(250).row();

        TextButton connect = new TextButton("Connect", skin);
        TextButton cancel = new TextButton("Cancel", skin);

        connect.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String host = hostField.getText().trim();
                if (host.isEmpty()) host = NetworkConstants.DEFAULT_HOST;
                // switch to loading screen which will attempt connection and then open FirstScreen
                game.setScreen(new LoadingScreen(game, host));
                dialog.hide();
            }
        });

        cancel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dialog.hide();
            }
        });

        dialog.getButtonTable().add(connect).pad(6);
        dialog.getButtonTable().add(cancel).pad(6);
        dialog.show(stage);
    }

    private void showSettingsDialog() {
        Dialog dialog = new Dialog("Settings", skin);
        dialog.getContentTable().pad(10);

        final TextField usernameField = new TextField("", skin);
        // load saved username from preferences
        com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("zealgame");
        String saved = prefs.getString("username", "Player");
        usernameField.setText(saved);

        dialog.getContentTable().add(new Label("Username:", skin)).left();
        dialog.getContentTable().add(usernameField).width(250).row();

        TextButton save = new TextButton("Save", skin);
        TextButton cancel = new TextButton("Cancel", skin);

        save.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String username = usernameField.getText().trim();
                if (username.isEmpty()) username = "Player";
                prefs.putString("username", username);
                prefs.flush();
                dialog.hide();
            }
        });

        cancel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dialog.hide();
            }
        });

        dialog.getButtonTable().add(save).pad(6);
        dialog.getButtonTable().add(cancel).pad(6);
        dialog.show(stage);
    }

    private void showCreditsDialog() {
        Dialog dialog = new Dialog("Credits", skin);
        dialog.getContentTable().pad(10);
        dialog.getContentTable().add(new Label("Zeal Game\nDeveloped by You", skin)).row();
        TextButton ok = new TextButton("OK", skin);
        ok.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dialog.hide();
            }
        });
        dialog.getButtonTable().add(ok).pad(6);
        dialog.show(stage);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        // nothing
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
