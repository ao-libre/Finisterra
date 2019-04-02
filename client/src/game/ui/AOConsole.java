package game.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import game.utils.Colors;
import game.utils.Fonts;

import java.util.LinkedList;

public class AOConsole extends Actor {

    public static final float MAX_MESSAGES = 6;
    private LinkedList<Actor> messages = new LinkedList<>();

    AOConsole() {
        super();
    }

    public void addMessage(String message) {
        addMessage(message, new Label.LabelStyle(Fonts.CONSOLE_FONT, Colors.GREY));
    }

    private void addMessage(String message, Label.LabelStyle style) {
        Label label = new Label(message, style);
        label.setX(getX());
        if (messages.size() >= MAX_MESSAGES) {
            messages.pollLast();
        }
        messages.offerFirst(label);
        setY();
    }

    private void setY() {
        for (int i = 0; i < messages.size(); i++) {
            messages.get(i).setY(getY() + i * 16);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (messages.isEmpty()) {
            return;
        }
        // draw background
        messages.forEach(message -> {
            float index = MAX_MESSAGES - messages.indexOf(message);
            float alpha = index / MAX_MESSAGES;
            message.draw(batch, parentAlpha * alpha);
        });
    }
}
