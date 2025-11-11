package com.zeal.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.zeal.game.network.ChatMessage;
import com.zeal.game.network.client.GameClient;

public class ChatUI extends Actor {
    private final TextField chatInput;
    private final ScrollPane chatScrollPane;
    private final Table chatTable;
    private final Array<Label> messages;
    private final GameClient gameClient;
    private final Skin skin;
    private boolean isVisible;
    private static final int MAX_MESSAGES = 50;

    public ChatUI(Stage stage, Skin skin, GameClient gameClient) {
        this.skin = skin;
        this.gameClient = gameClient;
        this.messages = new Array<>();
        
        // Set up chat table
        chatTable = new Table();
        chatTable.setVisible(false);
        chatTable.setPosition(10, 10);
        chatTable.setSize(400, 200);
        chatTable.setDebug(true); // Temporary: show table bounds
        
        // Create message area
        Table messageArea = new Table();
        chatScrollPane = new ScrollPane(messageArea, skin);
        chatScrollPane.setFadeScrollBars(false);
        
        // Create chat input
        chatInput = new TextField("", skin);
        chatInput.setMessageText("Press ENTER to send, ESC to close");
        
        // Layout
        chatTable.add(chatScrollPane).expand().fill().pad(5).row();
        chatTable.add(chatInput).expandX().fillX().pad(5);
        
        stage.addActor(chatTable);
        
        // Set up network listener
        gameClient.setMessageListener(this::addMessage);
    }

    public void toggleVisibility() {
        isVisible = !isVisible;
        chatTable.setVisible(isVisible);
        if (isVisible) {
            chatInput.setText("");
            chatInput.setDisabled(false);
            Stage stage = chatInput.getStage();
            // Do not replace the global input processor here. The screen maintains an InputMultiplexer
            // so we only set keyboard focus to the text field.
            if (stage != null) {
                stage.setKeyboardFocus(chatInput);
            }
            // Debug message to confirm toggle
            Gdx.app.log("ChatUI", "Chat window opened");
        } else {
            // Clear keyboard focus when hiding
            Stage stage = chatInput.getStage();
            if (stage != null) stage.setKeyboardFocus(null);
            Gdx.app.log("ChatUI", "Chat window closed");
        }
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void update() {
        if (isVisible) {
            if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
                toggleVisibility();
            } else if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
                String message = chatInput.getText().trim();
                if (!message.isEmpty()) {
                    gameClient.sendMessage(message);
                    chatInput.setText("");
                }
            }
        } else if (Gdx.input.isKeyJustPressed(Keys.SLASH)) {
            toggleVisibility();
        }
    }

    public void addMessage(ChatMessage chatMessage) {
        Label messageLabel = new Label(chatMessage.toString(), skin);
        messageLabel.setWrap(true);
        
        Table messageArea = (Table) chatScrollPane.getActor();
        messageArea.add(messageLabel).expandX().fillX().pad(2).row();
        
        messages.add(messageLabel);
        if (messages.size > MAX_MESSAGES) {
            messages.first().remove();
            messages.removeIndex(0);
        }
        
        chatScrollPane.layout();
        chatScrollPane.scrollTo(0, 0, 0, 0);
        
        // Debug message to confirm message added
        Gdx.app.log("ChatUI", "Message added: " + chatMessage.toString());
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        update();
    }
}