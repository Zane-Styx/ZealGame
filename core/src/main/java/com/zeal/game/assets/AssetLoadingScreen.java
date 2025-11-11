package com.zeal.game.assets;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.zeal.game.MainMenuScreen;

/**
 * Lightweight asset loading screen that drives AssetManager.update() and shows progress.
 */
public class AssetLoadingScreen implements Screen {
    private final Game game;
    private final Stage stage;
    private final Skin skin;
    private final ProgressBar progressBar;
    private final Label progressLabel;

    public AssetLoadingScreen(Game game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        progressBar = new ProgressBar(0f, 1f, 0.01f, false, skin);
        progressBar.setValue(0f);
        progressBar.setAnimateDuration(0.1f);

        progressLabel = new Label("Loading: 0%", skin);

        root.add(progressLabel).pad(8).row();
        root.add(progressBar).width(300).height(24).pad(8).row();

        // Queue assets for async loading
        Assets.queueAssets();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Advance manager
        boolean finished = Assets.update();
        float progress = Assets.getProgress();
        progressBar.setValue(progress);
        progressLabel.setText(String.format("Loading: %d%%", Math.round(progress * 100f)));

        stage.act(delta);
        stage.draw();

        if (finished) {
            // Ensure models are loaded synchronously (OBJ/G3D) and then continue
            Assets.finishAndLoadModelsSync();
            // Move to main menu
            Gdx.app.postRunnable(() -> game.setScreen(new MainMenuScreen((Game) game)));
        }
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
