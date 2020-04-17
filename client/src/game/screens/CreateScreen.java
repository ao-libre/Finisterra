package game.screens;

import com.artemis.annotations.Wire;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import game.systems.network.ClientSystem;
import shared.interfaces.Hero;

@Wire
public class CreateScreen extends AbstractScreen {
    private ClientSystem clientSystem;
    private ScreenManager screenManager;

    public CreateScreen() {
    }

    @Override
    protected void keyPressed(int keyCode) {

    }

    public ClientSystem getClientSystem() {
        return clientSystem;
    }

    @Override
    public Skin getSkin() {
        return super.getSkin();
    }

    @Override
    public void createUI() {
        Window createWindow = new Window("", getSkin());

        Label nameLabel = new Label("Name:", getSkin());
        createWindow.add(nameLabel).row();

        TextField name = new TextField("", getSkin());
        createWindow.add(name).row();

        Label heroLabel = new Label("Hero: ", getSkin());
        createWindow.add(heroLabel).row();

        SelectBox<Hero> heroSelectBox = new SelectBox<>(getSkin());
        Array<Hero> heros = new Array<>();
        Hero.getHeroes().forEach(heros::add);
        heroSelectBox.setItems(heros);
        createWindow.add(heroSelectBox).row();


        TextButton registerButton = new TextButton("Create", getSkin());
        registerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // send request to create user
                //clientSystem.send(new UserCreateRequest(name.getText(), heroSelectBox.getSelected().ordinal()));
                registerButton.setDisabled(true);
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        registerButton.setDisabled(false);
                    }
                }, 2);
            }
        });
        createWindow.add(registerButton).row();

        TextButton goBackButton = new TextButton("Go Back", getSkin());
        goBackButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screenManager.to(ScreenEnum.LOGIN);
            }
        });
        createWindow.add(goBackButton).row();

        getMainTable().add(createWindow);
    }
}
