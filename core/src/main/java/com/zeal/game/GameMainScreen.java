package com.zeal.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
// imports trimmed: removed debug-only imports
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.zeal.game.network.client.GameClient;
import com.zeal.game.ui.ChatUI;
import com.zeal.game.ui.PauseUI;
import com.zeal.game.settings.Settings;

/** First screen of the application. Displayed after the application is created. */
public class GameMainScreen implements Screen {
    private final Game game;
    private final Stage stage;
    private final ChatUI chatUI;
    private final GameClient gameClient;
    private final Settings settings;
    private InputMultiplexer inputMultiplexer;
    // PauseUI is a static helper now; we call PauseUI.show(...) when needed.

    public GameMainScreen(Game game, String host) {
        this.game = game;
        // Create stage with a viewport
        stage = new Stage(new ScreenViewport());

        // Load the UI skin
        Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Load username from typed settings
        this.settings = new Settings();
        String username = settings.getUsername() + "-" + System.currentTimeMillis();

        // Initialize the game client with chosen host
        gameClient = new GameClient(host, com.zeal.game.network.NetworkConstants.DEFAULT_PORT, username);
        gameClient.connect().thenRun(() -> {
            Gdx.app.log("FirstScreen", "Connected to server");
        }).exceptionally(throwable -> {
            Gdx.app.error("FirstScreen", "Failed to connect: " + throwable.getMessage());
            return null;
        });

        // Create and add chat UI
        chatUI = new ChatUI(stage, skin, gameClient);

        // Pause UI is shown via the static helper when needed. We'll call PauseUI.show(stage, settings, onMainMenu).

        // Note: removed debug overlay and fallback pause square/button per user request.

        // (key logger will be added after the InputMultiplexer is created)

        // Use an InputMultiplexer so we can listen for global keys (like '/') while still
        // allowing the stage to receive input events for UI widgets.
        inputMultiplexer = new com.badlogic.gdx.InputMultiplexer();
        // Add a permanent key logger to the InputMultiplexer so we can see keyDown events
        inputMultiplexer.addProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override public boolean keyDown(int keycode) { Gdx.app.log("KeyLogger", "keyDown: " + keycode); return false; }
        });
        // Add our global input adapter first so it gets events before the stage consumes them.
        inputMultiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                Gdx.app.log("FirstScreen", "keyDown: " + keycode);
                // Toggle chat on SLASH key
                if (keycode == com.badlogic.gdx.Input.Keys.SLASH) {
                    chatUI.toggleVisibility();
                    return true;
                }
                // ESC: close pause if open, otherwise open pause (when chat isn't visible)
                if (keycode == com.badlogic.gdx.Input.Keys.ESCAPE) {
                    if (PauseUI.isShowing()) {
                        PauseUI.hide();
                        return true;
                    }
                    if (!chatUI.isVisible()) {
                        showPauseMenu();
                        return true;
                    }
                }
                return false;
            }
        });
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);

        // Pause button removed; use ESC or F10 to open pause menu.

        // Debug message to confirm initialization
        Gdx.app.log("FirstScreen", "Screen initialized, press '/' to open chat");
    }
    
    @Override
    public void show() {
        // ensure the multiplexer (stage + our global input adapter) is active when this screen is shown
        if (inputMultiplexer != null) {
            Gdx.input.setInputProcessor(inputMultiplexer);
        } else {
            Gdx.input.setInputProcessor(stage);
        }
    }

    private void showPauseMenu() {
        // Show the static PauseUI dialog helper
        PauseUI.show(stage, settings, () -> game.setScreen(new MainMenuScreen(game)));
    }

    @Override
    public void render(float delta) {
        
        // Clear the screen
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // Poll ESC here (polling is independent of input processors) so pause works even
        // if another processor temporarily replaced the multiplexer.
        // Ensure our InputMultiplexer stays installed so global keys are captured.
        if (Gdx.input.getInputProcessor() != inputMultiplexer) {
            Gdx.app.log("FirstScreen", "Input processor was replaced (" +
                    (Gdx.input.getInputProcessor() == null ? "null" : Gdx.input.getInputProcessor().getClass().getSimpleName()) +
                    "), re-installing our InputMultiplexer");
            Gdx.input.setInputProcessor(inputMultiplexer);
        }
        // (debug indicator removed)

        // NOTE: ESC handling moved to the InputMultiplexer keyDown handler to avoid
        // double-handling (keyDown + polling) which caused immediate open/close.

        // Debug shortcuts: F11 = dump stage actors; F10 = force-show pause UI
        if (Gdx.input.isKeyJustPressed(Keys.F11)) {
            Gdx.app.log("FirstScreen", "--- Stage Actors ---");
            for (int i = 0; i < stage.getActors().size; i++) {
                com.badlogic.gdx.scenes.scene2d.Actor a = stage.getActors().get(i);
                Gdx.app.log("FirstScreen", String.format("%d: %s visible=%s x=%.1f y=%.1f w=%.1f h=%.1f", i,
                        a.getClass().getSimpleName(), a.isVisible(), a.getX(), a.getY(), a.getWidth(), a.getHeight()));
            }
            Gdx.app.log("FirstScreen", "--- end actors ---");
        }

        if (Gdx.input.isKeyJustPressed(Keys.F10)) {
            Gdx.app.log("FirstScreen", "F10 pressed: forcing pause UI show/bring to front");
            PauseUI.show(stage, settings, () -> game.setScreen(new MainMenuScreen(game)));
            // ensure modal overlay and window are in front
            stage.setKeyboardFocus(null);
        }
        
        // Update chat UI and stage
        chatUI.act(delta);
        stage.act(delta);
        stage.draw();

        // (debug indicator removed)
    }
    
    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
        gameClient.disconnect();
    }

    @Override
    public void dispose() {
        stage.dispose();
        gameClient.disconnect();
    }
}