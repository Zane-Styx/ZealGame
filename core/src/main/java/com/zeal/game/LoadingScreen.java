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

import java.util.concurrent.CompletableFuture;

/**
 * LoadingScreen attempts to connect to the server and shows progress.
 */
public class LoadingScreen implements Screen {
    private final Game game;
    private final Stage stage;
    private final Skin skin;
    private final String host;
    // no longer storing settings here; FirstScreen will load settings

    public LoadingScreen(Game game, String host) {
        this.game = game;
        this.host = host;

        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label label = new Label("Connecting to " + host + "...", skin);
        root.add(label);

        connect();
    }

    private void connect() {
        // Do a small socket check in background to verify server is reachable before opening the game screen.
        CompletableFuture.runAsync(() -> {
            try (java.net.Socket s = new java.net.Socket()) {
                s.connect(new java.net.InetSocketAddress(host, com.zeal.game.network.NetworkConstants.DEFAULT_PORT), 3000);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenRun(() -> Gdx.app.postRunnable(() -> game.setScreen(new FirstScreen(game, host))))
          .exceptionally(throwable -> {
              Gdx.app.postRunnable(() -> showError(throwable.getCause() == null ? throwable.getMessage() : throwable.getCause().getMessage()));
              return null;
          });
    }

    private void showError(String message) {
        Dialog dialog = new Dialog("Connection failed", skin);
        dialog.getContentTable().add(new Label(message == null ? "Failed to connect" : message, skin)).row();
        TextButton retry = new TextButton("Retry", skin);
        TextButton back = new TextButton("Back", skin);
        retry.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                dialog.hide();
                connect();
            }
        });
        back.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                dialog.hide();
                game.setScreen(new MainMenuScreen(game));
            }
        });
        dialog.getButtonTable().add(retry).pad(6);
        dialog.getButtonTable().add(back).pad(6);
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
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
