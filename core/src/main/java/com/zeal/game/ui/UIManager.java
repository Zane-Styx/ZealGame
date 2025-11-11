package com.zeal.game.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Simple UI manager that keeps a stack of dialogs. Opening a new dialog hides the previous one
 * (but keeps it on the stack). Closing the current dialog will restore the previous dialog.
 */
public final class UIManager {
    private UIManager() {}

    private static final Deque<Dialog> stack = new ArrayDeque<>();

    public static void openDialog(Dialog dialog, Stage stage) {
        if (dialog == null || stage == null) return;
        if (!stack.isEmpty()) {
            Dialog prev = stack.peek();
            // hide previous visually but keep it on the stack
            prev.setVisible(false);
        }
        stack.push(dialog);
        dialog.show(stage);
    }

    public static void closeCurrent() {
        if (stack.isEmpty()) return;
        // Ask the current dialog to hide; the dialog's hide() override should call dialogHidden
        Dialog cur = stack.peek();
        if (cur != null) {
            cur.hide();
        }
    }

    public static boolean isShowing() {
        return !stack.isEmpty() && stack.peek().isVisible();
    }

    public static void hideAll() {
        while (!stack.isEmpty()) closeCurrent();
    }

    /**
     * Called by a Dialog implementation when it has been hidden (for example, via hide()).
     * This method will remove the dialog from the stack and restore the previous dialog if any.
     */
    public static void dialogHidden(Dialog dialog) {
        if (dialog == null) return;
        if (stack.isEmpty()) return;
        // If the dialog on the top is the one hidden, pop and restore previous
        if (stack.peek() == dialog) {
            stack.pop();
            if (!stack.isEmpty()) {
                Dialog prev = stack.peek();
                prev.setVisible(true);
                prev.toFront();
            }
        } else {
            // otherwise just remove it if present
            stack.remove(dialog);
        }
    }
}

