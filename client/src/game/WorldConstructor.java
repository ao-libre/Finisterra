package game;

import com.artemis.*;
import com.artemis.managers.UuidEntityManager;
import game.handlers.DefaultAOAssetManager;
import game.screens.ScreenEnum;
import game.screens.ScreenManager;
import game.systems.PlayerSystem;
import game.systems.actions.PlayerActionSystem;
import game.systems.anim.IdleAnimationSystem;
import game.systems.anim.MovementAnimationSystem;
import game.systems.camera.CameraFocusSystem;
import game.systems.camera.CameraMovementSystem;
import game.systems.camera.CameraShakeSystem;
import game.systems.camera.CameraSystem;
import game.systems.input.InputSystem;
import game.systems.map.MapManager;
import game.systems.map.TiledMapSystem;
import game.systems.network.*;
import game.systems.physics.AttackAnimationSystem;
import game.systems.physics.MovementProcessorSystem;
import game.systems.physics.MovementSystem;
import game.systems.physics.PlayerInputSystem;
import game.systems.render.BatchBeginSystem;
import game.systems.render.BatchEndSystem;
import game.systems.render.BatchSystem;
import game.systems.render.chars.PrerenderCharCache;
import game.systems.render.world.*;
import game.systems.resources.*;
import game.systems.screen.MouseSystem;
import game.systems.screen.ScreenSystem;
import game.systems.sound.SoundSytem;
import game.systems.ui.UserInterfaceSystem;
import game.systems.ui.action_bar.ActionBarSystem;
import game.systems.ui.action_bar.systems.InventorySystem;
import game.systems.ui.action_bar.systems.SpellSystem;
import game.systems.ui.console.ConsoleSystem;
import game.systems.ui.dialog.DialogSystem;
import game.systems.ui.stats.StatsSystem;
import game.systems.ui.user.UserSystem;
import game.systems.world.ClearSystem;
import game.systems.world.NetworkedEntitySystem;
import game.systems.world.WorldSystem;
import game.utils.CursorSystem;
import net.mostlyoriginal.api.system.render.ClearScreenSystem;
import shared.systems.IntervalSystem;

import java.util.Arrays;

import static com.artemis.WorldConfigurationBuilder.Priority.HIGH;

public class WorldConstructor {

    private static final int LOGIC = 10;
    private static final int PRE_ENTITY_RENDER_PRIORITY = 6;
    private static final int ENTITY_RENDER_PRIORITY = 5;
    private static final int POST_ENTITY_RENDER_PRIORITY = 4;
    private static final int DECORATION_PRIORITY = 3;
    private static final int UI = 0;

    private static WorldConfiguration getWorldConfiguration(
            Config config,
            ScreenManager screenManager,
            DefaultAOAssetManager assetManager,
            MusicSystem musicSystem
    ) {
        return new WorldConfigurationBuilder()
                // Sistemas de uso global (no necesitan prioridad porque son pasivos)
                .with(screenManager)

                // register all screens
                .with(Arrays.stream(ScreenEnum.values())
                        .map(ScreenEnum::get)
                        .map(BaseSystem.class::cast)
                        .toArray(BaseSystem[]::new))

                // Network system (no necesita prioridad porque es asincrónico, funciona por callbacks)
                .with(new ClientSystem(),
                        new ClientResponseProcessor(),
                        new GameNotificationProcessor())

                // Sistemas de alta prioridad
                .with(HIGH,
                        new TimeSync(),
                        new SuperMapper(),
                        new LocalReferenceSystem(),
                        new ClearSystem())

                .with(LOGIC,
                        new IntervalSystem(),

                        // Player component.movement
                        new PlayerInputSystem(),
                        new MovementProcessorSystem(),
                        new MovementAnimationSystem(),
                        new IdleAnimationSystem(),
                        new MovementSystem(),
                        new PlayerSystem(),

                        // Camera
                        new CameraSystem(),
                        new CameraFocusSystem(),
                        new CameraMovementSystem(),
                        new CameraShakeSystem(),

                        // Logic systems
                        new NetworkedEntitySystem(),
                        new AttackAnimationSystem(),
                        new SoundSytem(),
                        new TiledMapSystem(),
                        new AnimationsSystem(),
                        new DescriptorsSystem(),
                        new MessageSystem(),
                        new MapSystem(),
                        new ObjectSystem(),
                        new ParticlesSystem(),
                        new SoundsSystem(),
                        new SpellsSystem(),
                        new FontsSystem(),
                        new PlayerActionSystem(),
                        new InputSystem(),
                        new ScreenSystem(),
                        new WorldSystem())

                // Rendering
                .with(PRE_ENTITY_RENDER_PRIORITY,
                        new BatchSystem(),
                        new BatchBeginSystem(),
                        new PrerenderCharCache(),
                        new ClearScreenSystem(),
                        new MapGroundRenderingSystem(),
                        new ObjectRenderingSystem(),
                        new TargetRenderingSystem(),
                        new NameRenderingSystem())

                .with(ENTITY_RENDER_PRIORITY,
                        new EffectRenderingSystem(),
                        new CharacterRenderSystem(),
                        new MapMiddleLayerRenderingSystem(),
                        new WorldRenderingSystem())

                .with(POST_ENTITY_RENDER_PRIORITY,
                        new CombatRenderingSystem(),
                        new DialogRenderingSystem(),
                        new MapLastLayerRenderingSystem())

                .with(DECORATION_PRIORITY,
                        new StateRenderingSystem(),
                        new CharacterStatesRenderingSystem(),
                        new BatchEndSystem())

                // UI
                .with(UI,
                        new MouseSystem(),
                        new CursorSystem(),
                        new InventorySystem(),
                        new SpellSystem(),
                        new ActionBarSystem(),
                        new ConsoleSystem(),
                        new DialogSystem(),
                        new StatsSystem(),
                        new UserSystem(),
                        new UserInterfaceSystem())

                // Otros sistemas
                .with(new MapManager(),
                        new UuidEntityManager())
// @todo Habilitar información de perfileo en UI
//              .with(new ProfilerSystem())
                .build()
                .register(config)
                .register(assetManager)
                .register(musicSystem);
    }

    /**
     * Construye el Artemis World, inicializa e inyecta sistemas.
     * Este método es bloqueante.
     */
    public static World create(
            Config config,
            ScreenManager screenManager,
            DefaultAOAssetManager assetManager,
            MusicSystem musicSystem
    ) {
        return new World(getWorldConfiguration(config, screenManager, assetManager, musicSystem));
    }
}
