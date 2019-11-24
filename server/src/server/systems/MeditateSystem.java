package server.systems;

import com.artemis.Aspect;
import com.artemis.E;
import com.artemis.annotations.Wire;
import entity.character.states.Meditating;
import entity.character.status.Mana;
import entity.world.CombatMessage;
import graphics.Effect;
import graphics.Effect.EffectBuilder;
import server.systems.manager.WorldManager;
import shared.interfaces.Constants;
import shared.network.notifications.ConsoleMessage;
import shared.network.notifications.EntityUpdate.EntityUpdateBuilder;
import shared.network.notifications.RemoveEntity;
import shared.network.sound.SoundNotification;
import shared.util.Messages;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.artemis.E.E;

@Wire
public class MeditateSystem extends IntervalFluidIteratingSystem {

    private static final float MANA_RECOVERY_PERCENT = 0.3f;
    private static Map<Integer, Integer> userMeditations = new HashMap<>();
    private WorldManager worldManager;

    public MeditateSystem(float timer) {
        super(Aspect.all(Meditating.class, Mana.class), timer);
    }

    @Override
    protected void process(E player) {
        Mana mana = player.getMana();
        EntityUpdateBuilder update = EntityUpdateBuilder.of(player.id());
        EntityUpdateBuilder notify = EntityUpdateBuilder.of(player.id());
        if (mana.min < mana.max) {
            int manaMin = mana.min;
            int prob = ThreadLocalRandom.current().nextInt(2);
            if (prob == 1) {
                // meditar
                mana.min += mana.max * MANA_RECOVERY_PERCENT;
                mana.min = Math.min(mana.min, mana.max);
                int recoveredMana = mana.min - manaMin;
                CombatMessage manaMessage = CombatMessage.magic("+" + recoveredMana);
                update.withComponents(mana);
                notify.withComponents(manaMessage);
                ConsoleMessage consoleMessage = ConsoleMessage.info(Messages.MANA_RECOVERED, Integer.toString(recoveredMana));
                worldManager.sendEntityUpdate(player.id(), consoleMessage);
            }
        }

        if (mana.min >= mana.max) {
            notify.remove(Meditating.class);
            ConsoleMessage consoleMessage = ConsoleMessage.info(Messages.MEDITATE_STOP);
            worldManager.sendEntityUpdate(player.id(), consoleMessage);
            stopMeditationEffect(player.id());
        }

        if (!update.isEmpty()) {
            worldManager.notifyUpdate(player.id(), update.build());
        }

        if (!notify.isEmpty()) {
            worldManager.notifyUpdate(player.id(), notify.build());
        }
    }

    public void toggle(int userId) {
        E player = E(userId);
        boolean meditating = player.isMeditating();

        ConsoleMessage consoleMessage;
        EntityUpdateBuilder update = EntityUpdateBuilder.of(userId);

        if (meditating) {
            stopMeditationEffect(userId);
            consoleMessage = ConsoleMessage.info(Messages.MEDITATE_STOP);
            update.remove(Meditating.class);
        } else {
            E entity = E(userId);
            Mana mana = entity.getMana();
            if (mana != null && mana.min == mana.max) {
                consoleMessage = ConsoleMessage.info(Messages.MANA_FULL);
            } else {
                int e = world.create();
                Effect effect = new EffectBuilder().withParticle(Constants.MEDITATE_NW_FX).attachTo(userId).build();
                worldManager.notifyUpdate(userId, EntityUpdateBuilder.of(e).withComponents(effect).build());
                userMeditations.put(userId, e);
                player.meditating();
                consoleMessage = ConsoleMessage.info(Messages.MEDITATE_START);
                update.withComponents(player.getMeditating());
                getWorldManager().notifyUpdate(player.id () , new SoundNotification (18));
            }
        }
        worldManager.sendEntityUpdate(userId, consoleMessage);
        worldManager.notifyUpdate(userId, update.build());
    }

    private void stopMeditationEffect(int userId) {
        Integer entityId = userMeditations.get(userId);
        worldManager.notifyUpdate(userId, new RemoveEntity(entityId));
        userMeditations.remove(userId);
        E(entityId).deleteFromWorld();
        E(userId).removeMeditating();
        getWorldManager().notifyUpdate(userId , new SoundNotification (18, SoundNotification.SoundState.STOPPED));
    }

    private WorldManager getWorldManager() {
        return world.getSystem(WorldManager.class);
    }

}
