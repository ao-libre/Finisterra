package server.manager;

import com.artemis.E;
import com.artemis.World;
import com.esotericsoftware.minlog.Log;
import entity.Heading;
import server.core.Server;
import server.database.model.attributes.Attributes;
import server.database.model.constants.Constants;
import server.database.model.modifiers.Modifiers;
import shared.interfaces.CharClass;
import shared.interfaces.Hero;
import shared.interfaces.Race;
import shared.network.notifications.RemoveEntity;
import shared.objects.types.*;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static com.artemis.E.E;

public class WorldManager extends DefaultManager {

    public WorldManager(Server server) {
        super(server);
    }

    @Override
    public void init() {

    }

    public int createEntity(String name, int heroId) {
        int player = getWorld().create();

        E entity = E(player);
        // set position
        setEntityPosition(entity);
        // set head and body
        setHeadAndBody(name, entity);
        // set class
        setClassAndAttributes(heroId, entity);
        // set inventory
        setInventory(player);

        return player;
    }

    private void setClassAndAttributes(int heroId, E entity) {
        Hero hero = Hero.values()[heroId];
        entity.charHeroHeroId(heroId);
        CharClass charClass = CharClass.values()[hero.getClassId()];
        // calculate HP
        calculateHP(charClass, entity);
        // calculate MANA
        calculateMana(entity, charClass);
        // set stamina
        entity.staminaMax(100);
        entity.staminaMin(100);
        // set body and head
        Race race = Race.values()[hero.getRaceId()];
        setNakedBody(entity, race);
        setHead(entity, race);
    }

    private void setHead(E entity, Race race) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int headIndex = 0;
        switch (race) {
            case HUMAN:
                headIndex = random.nextInt(1, 51 + 1);
                break;
            case DROW:
                headIndex = random.nextInt(201, 221 + 1);
                break;
            case ELF:
                headIndex = random.nextInt(101, 122 + 1);
                break;
            case GNOME:
                headIndex = random.nextInt(401, 416 + 1);
                break;
            case DWARF:
                headIndex = random.nextInt(301, 319 + 1);
        }
        entity.headIndex(headIndex);
    }

    void setNakedBody(E entity, Race race) {
        int bodyIndex = 1;
        switch (race) {
            case GNOME:
                bodyIndex = 222;
                break;
            case DWARF:
                bodyIndex = 53;
                break;
            case ELF:
                bodyIndex = 210;
                break;
            case DROW:
                bodyIndex = 32;
                break;
            case HUMAN:
                bodyIndex = 21;
        }
        entity.bodyIndex(bodyIndex);
    }

    private void calculateMana(E entity, CharClass heroClass) {
        float intelligenceAttr = Attributes.INTELLIGENCE.of(heroClass);
        float manaMod = Modifiers.MANA.of(heroClass);
        Float manaBase = Constants.getMana(heroClass);
        int maxMana = (int) (intelligenceAttr * manaMod * manaBase);
        entity.manaMax(maxMana);
        entity.manaMin(maxMana);
    }

    private void calculateHP(CharClass heroClass, E entity) {
        float heroStrength = Attributes.STRENGTH.of(heroClass);
        float heroHealthMod = Modifiers.HEALTH.of(heroClass);
        Float hpBase = Constants.getHp(heroClass);
        int maxHP = (int) (heroStrength * heroHealthMod * hpBase);
        entity.healthMax(maxHP);
        entity.healthMin(maxHP);
    }

    private void setInventory(int player) {
        E(player).inventory();
        addItem(player, Type.HELMET);

        addItem(player, Type.ARMOR);
        addItem(player, Type.WEAPON);
        addItem(player, Type.SHIELD);
        addPotion(player, PotionKind.HP);
        addPotion(player, PotionKind.MANA);
    }

    private void setHeadAndBody(String name, E entity) {
        entity
                .headingCurrent(Heading.HEADING_NORTH)
                .character()
                .nameText(name);
    }

    private void setEntityPosition(E entity) {
        entity
                .worldPosX(25)
                .worldPosY(25)
                .worldPosMap(1);
    }

    private void addPotion(int player, PotionKind kind) {
        Set<Obj> objs = getServer().getObjectManager().getTypeObjects(Type.POTION);
        objs.stream() //
                .map(PotionObj.class::cast) //
                .filter(potion -> {
                    PotionKind potionKind = potion.getKind();
                    return potionKind != null && potionKind.equals(kind);
                }) //
                .findFirst() //
                .ifPresent(obj -> E(player).getInventory().add(obj.getId()));
    }

    private void addItem(int player, Type type) {
        Set<Obj> objs = getServer().getObjectManager().getTypeObjects(type);
        objs.stream()
                .filter(obj -> {
                    if (obj instanceof ObjWithClasses) {
                        int heroId = E(player).getCharHero().heroId;
                        Hero hero = Hero.values()[heroId];
                        CharClass clazz = CharClass.values()[hero.getClassId()];
                        Set<CharClass> forbiddenClasses = ((ObjWithClasses) obj).getForbiddenClasses();
                        boolean supported = forbiddenClasses.size() == 0 || !forbiddenClasses.contains(clazz);
                        if (supported && obj instanceof ArmorObj) {
                            Race race = Race.values()[hero.getRaceId()];
                            if (race.equals(Race.GNOME) || race.equals(Race.DWARF)) {
                                supported = ((ArmorObj) obj).isDwarf();
                            } else if (((ArmorObj) obj).isWomen()) {
                                supported = false; // TODO
                            }
                        }
                        return supported;
                    } else if (obj.getType().equals(Type.POTION)) {
                        PotionObj potion = (PotionObj) obj;
                        return potion.getKind().equals(PotionKind.HP) || potion.getKind().equals(PotionKind.MANA);
                    }
                    return true;
                })
                .findFirst()
                .ifPresent(obj -> E(player).getInventory().add(obj.getId()));
    }

    public void registerItem(int id) {
        getServer().getMapManager().updateEntity(id);
    }

    public void registerEntity(int connectionId, int id) {
        getServer().getNetworkManager().registerUserConnection(id, connectionId);
        getServer().getMapManager().updateEntity(id);
    }

    public void unregisterEntity(int playerToDisconnect) {
        getWorld().delete(playerToDisconnect);
    }

    void sendEntityRemove(int user, int entity) {
        if (getServer().getNetworkManager().playerHasConnection(user)) {
            getServer().getNetworkManager().sendTo(getServer().getNetworkManager().getConnectionByPlayer(user), new RemoveEntity(entity));
        }
    }

    void sendEntityUpdate(int user, Object update) {
        if (getServer().getNetworkManager().playerHasConnection(user)) {
            getServer().getNetworkManager().sendTo(getServer().getNetworkManager().getConnectionByPlayer(user), update);
        }
    }

    public void notifyToNearEntities(int entityId, Object update) {
        getServer().getMapManager().getNearEntities(entityId).forEach(nearPlayer -> {
            Log.info("Notifying near player " + nearPlayer + ". Update: " + update);
            sendEntityUpdate(nearPlayer, update);
        });
    }

    public void notifyUpdate(int entityId, Object update) {
        sendEntityUpdate(entityId, update);
        notifyToNearEntities(entityId, update);
    }

    private World getWorld() {
        return getServer().getWorld();
    }
}