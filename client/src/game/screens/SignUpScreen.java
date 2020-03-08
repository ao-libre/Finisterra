package game.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import game.AOGame;
import game.ClientConfiguration;
import game.handlers.AOAssetManager;
import game.handlers.MusicHandler;
import game.network.ClientResponseProcessor;
import game.network.GameNotificationProcessor;
import game.systems.network.ClientSystem;
import net.mostlyoriginal.api.network.marshal.common.MarshalState;
import shared.network.account.AccountCreationRequest;
import shared.network.lobby.JoinLobbyRequest;
import shared.util.Messages;

import static game.utils.Resources.CLIENT_CONFIG;

public class SignUpScreen extends AbstractScreen {

    private ClientSystem clientSystem;

    private TextField usernameField;
    private TextField passwordField1, passwordField2;
    private TextField emailField;

    public SignUpScreen(ClientSystem clientSystem) {
        this.clientSystem = clientSystem;
    }

    @Override
    void createContent() {
        /* Tabla de sign up */
        Window signUpTable = new Window("", getSkin()); //@todo usar tabla
        Label usernameLabel = new Label("Username:", getSkin());
        this.usernameField = new TextField("", getSkin());
        Label emailLabel = new Label("Email:", getSkin());
        this.emailField = new TextField("", getSkin());
        Label passwordLabel1 = new Label("Password:", getSkin());
        this.passwordField1 = new TextField("", getSkin());
        this.passwordField1.setPasswordCharacter('*');
        this.passwordField1.setPasswordMode(true);
        Label passwordLabel2 = new Label("Repeat password:", getSkin());
        this.passwordField2 = new TextField("", getSkin());
        this.passwordField2.setPasswordCharacter('*');
        this.passwordField2.setPasswordMode(true);

        TextButton registerButton = new TextButton("Register account", getSkin());
        registerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((TextButton)actor).isPressed()) {
                    //@todo validar username, email, password. hashear password.
                    clientSystem.getKryonetClient().sendToAll(new AccountCreationRequest());
                }
            }
        });

        signUpTable.getColor().a = 0.8f;
        signUpTable.add(usernameLabel).padRight(5);
        signUpTable.add(this.usernameField).width(250).row();
        signUpTable.add(emailLabel).padTop(5).padRight(5);
        signUpTable.add(this.emailField).padTop(5).width(250).row();
        signUpTable.add(passwordLabel1).padTop(5).padRight(5);
        signUpTable.add(this.passwordField1).padTop(5).width(250).row();
        signUpTable.add(passwordLabel2).padTop(5).padRight(5);
        signUpTable.add(this.passwordField2).padTop(5).width(250).row();
        signUpTable.add();
        signUpTable.add(registerButton).padTop(20);

        /* Tabla principal */
        getMainTable().add(signUpTable).width(500).height(300).pad(10);
        getStage().setKeyboardFocus(this.usernameField);
    }

    @Override
    protected void keyPressed(int keyCode) {
    }
}
