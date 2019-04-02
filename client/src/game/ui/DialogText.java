package game.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import game.utils.Skins;

public class DialogText extends Table {

    private TextField textf;

    DialogText() {
        row().colspan(1).expandX().fillX();
        textf = new TextField("", Skins.COMODORE_SKIN, "transparent");
        setVisible(false);
    }

    public String getMessage() {
        return textf != null ? textf.getText() : "";
    }

    public void toggle() {
        setVisible(!isVisible());
        if (isVisible()) {
            add(textf).fillX();
            row().colspan(1).expandX().fillX();
            getStage().setKeyboardFocus(textf);
        } else {
            getStage().unfocus(textf);
            textf.setText("");
            removeActor(textf);
        }
    }

}
