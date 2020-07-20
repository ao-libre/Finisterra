package server.systems.account;

import com.artemis.Component;
import com.artemis.E;
import com.artemis.annotations.Wire;
import com.esotericsoftware.jsonbeans.Json;
import com.esotericsoftware.jsonbeans.JsonReader;
import com.esotericsoftware.jsonbeans.JsonValue;
import com.esotericsoftware.jsonbeans.OutputType;
import com.esotericsoftware.minlog.Log;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.jetbrains.annotations.NotNull;
import server.database.Account;
import server.database.Charfile;
import server.systems.network.ServerSystem;
import server.systems.world.WorldEntitiesSystem;
import server.systems.world.entity.factory.ComponentSystem;
import server.systems.world.entity.factory.EntityFactorySystem;
import server.utils.EntityJsonSerializer;
import shared.network.user.UserCreateResponse;
import shared.network.user.UserLoginResponse;
import shared.util.Messages;
import shared.util.UserSystemUtilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.*;

@Wire
public class UserSystem extends PassiveSystem {

    private EntityJsonSerializer entityJsonSerializer;
    private ServerSystem serverSystem;
    private WorldEntitiesSystem worldEntitiesSystem;
    private EntityFactorySystem entityFactorySystem;
    private AccountSystem accountSystem;
    private ComponentSystem componentSystem;
    private Json json;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @Override
    protected void initialize() {
        json = new Json();
        json.setOutputType(OutputType.minimal);
        json.setUsePrototypes(false);
    }

    public void login(int connectionId, String userName) {
        if (userExists(userName)) {
            // login
            try {
                Integer entityId = loadUser(userName).get(250, TimeUnit.MILLISECONDS);
                if (entityId != -1) {
                    serverSystem.sendTo(connectionId, UserLoginResponse.ok());
                    worldEntitiesSystem.login(connectionId, entityId);
                } else {
                    serverSystem.sendTo(connectionId,
                            UserLoginResponse.failed("No se pudo leer el personaje " + userName + ". Por favor contactate con soporte."));
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                Log.info("Failed to retrieve user from JSON file");
                e.printStackTrace();
                serverSystem.sendTo(connectionId,
                        UserLoginResponse.failed("Hubo un problema al leer el personaje " + userName));
            }

        } else {
            // don't exist (should never happen)
            // TODO remove from Account ?
            serverSystem.sendTo(connectionId,
                    UserLoginResponse.failed("Este personaje " + userName + " no existe!"));
        }
    }

    public void create(int connectionId, String name, int heroId, String userAcc, int index) {
        UserSystemUtilities userSystemUtilities = new UserSystemUtilities();

        if (userSystemUtilities.userNameIsNumeric(name)){
            serverSystem.sendTo(connectionId,
                    UserCreateResponse.failed(Messages.USERNAME_NOT_NUEMRIC));

        }else if (!userSystemUtilities.userNameIsNormalChar(name)) {
            serverSystem.sendTo(connectionId,
                    UserCreateResponse.failed(Messages.USERNAME_INVALID_CHAR));

        }else if (userSystemUtilities.userNameIsStartNumeric(name)) {
            serverSystem.sendTo(connectionId,
                    UserCreateResponse.failed(Messages.USERNAME_NOT_INIT_NUMBER));

        }else if (userSystemUtilities.userNameIsOfensive(name)) {
            serverSystem.sendTo(connectionId,
                    UserCreateResponse.failed(Messages.USERNAME_FORBIDDEN));

        }else if (userExists(name)) {
            // send user exists
            serverSystem.sendTo(connectionId,
                    UserCreateResponse.failed(Messages.USERNAME_ALREADY_EXIST));
        } else {
            // create and add to account
            int entityId = entityFactorySystem.create(name, heroId);
            saveUser(name);
            Account account = accountSystem.getAccount(userAcc);
            if (!account.getCharacters().get( index ).isBlank()) {
                try {
                    File oldUserFile = new File( Charfile.DIR_CHARFILES + account.getCharacters().get( index ) + ".json" );
                    oldUserFile.delete();
                    Log.info( "old file deleted " +account.getCharacters().get( index ) + ".json");
                }catch (Exception e){
                    Log.error("User System", "Error while creating a user", e);
                }
            }
            account.addCharacter( name, index );
            // send ok and login
            serverSystem.sendTo(connectionId, UserCreateResponse.ok());
            worldEntitiesSystem.login(connectionId, entityId);
        }
    }

    private boolean userExists(String userName) {
        File file = new File(Charfile.DIR_CHARFILES + userName + ".json");
        return file.isFile() && file.canRead();
    }

    public void save(@NotNull E e) {
        boolean canSave = e.hasCharacter() && e.hasName();
        if (canSave) {
            String name = e.getName().text;
            saveUser(name, e);
        }
    }

    public void save(int entityId) {
        E e = E.E(entityId);
        save(e);
    }

    private void saveUser(String name) {
        E user = E.withTag(name);
        saveUser(name, user);
    }

    private void saveUser(String name, E user) {
        executor.submit(() -> {
            Collection<Component> components = componentSystem.getComponents(user.id(), ComponentSystem.Visibility.SERVER);
            File userFile = new File(Charfile.DIR_CHARFILES + name + ".json");
            try (FileWriter writer = new FileWriter(userFile)) {
                json.setWriter(writer);
                entityJsonSerializer.write(json, components, null);
            } catch (IOException e) {
                e.printStackTrace();
                Log.info("Failed to write charfile " + name);
            }
        });
    }

    private @NotNull Future<Integer> loadUser(String name) {
        return executor.submit(() -> {
            File userFile = new File(Charfile.DIR_CHARFILES + name + ".json");
            // read components
            try {
                JsonValue jsonData = new JsonReader().parse(userFile);
                Collection<? extends Component> components = entityJsonSerializer.read(json, jsonData, null);
                // create user character in world
                return entityFactorySystem.create(components);
            } catch (Exception e) {
                e.printStackTrace();
                Log.info("Failed to retrieve user from json charfile");
                return -1;
            }
        });
    }
    public static void checkStorageDirectory() {
        File charfilesDir = new File(Charfile.DIR_CHARFILES);
        if (charfilesDir.isDirectory())
            charfilesDir.mkdirs();
    }
}
