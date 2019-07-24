package game.handlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader.ParticleEffectParameter;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import game.loaders.*;
import game.loaders.ObjectsLoader.ObjectParameter;
import game.utils.Resources;
import game.utils.Skins.AOSkin;
import model.descriptors.*;
import model.textures.AOAnimation;
import model.textures.AOImage;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import shared.model.Spell;
import shared.objects.types.Obj;
import shared.objects.types.Type;
import shared.util.SharedResources;

import javax.sound.midi.Sequencer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static game.loaders.DescriptorsLoader.*;
import static game.loaders.DescriptorsLoader.DescriptorParameter.descriptor;
import static game.loaders.GenericLoader.GenericParameter.bodiesGenericParameter;
import static game.utils.Resources.GAME_DESCRIPTORS_PATH;

public class DefaultAOAssetManager extends AssetManager implements AOAssetManager {

    private static final Class<HashMap<Integer, BodyDescriptor>> BODIES_CLASS;
    private static final Class<ArrayList<AOImage>> IMAGE_CLASS;
    private static final Class<ArrayList<AOAnimation>> ANIMATION_CLASS;
    private static final Class<HashMap<Integer, Obj>> OBJS_CLASS;
    private static final Class<HashMap<Integer, Spell>> SPELLS_CLASS;
    private static final Class<ArrayList<Descriptor>> DESCRIPTORS_CLASS;

    private Map<Integer, AOImage> images;
    private Map<Integer, AOAnimation> animations;

    static {
        HashMap<Integer, BodyDescriptor> integerBodyDescriptorHashMap = new HashMap<>();
        BODIES_CLASS = (Class<HashMap<Integer, BodyDescriptor>>) integerBodyDescriptorHashMap.getClass();

        ArrayList<AOImage> integerGraphicHashMap = new ArrayList<>();
        IMAGE_CLASS = (Class<ArrayList<AOImage>>) integerGraphicHashMap.getClass();

        ArrayList<AOAnimation> integerAnimationHashMap = new ArrayList<>();
        ANIMATION_CLASS = (Class<ArrayList<AOAnimation>>) integerAnimationHashMap.getClass();

        HashMap<Integer, Obj> integerObjHashMap = new HashMap<>();
        OBJS_CLASS = (Class<HashMap<Integer, Obj>>) integerObjHashMap.getClass();

        HashMap<Integer, Spell> integerSpellHashMap = new HashMap<>();
        SPELLS_CLASS = (Class<HashMap<Integer, Spell>>) integerSpellHashMap.getClass();

        ArrayList<Descriptor> descriptors = new ArrayList<>();
        DESCRIPTORS_CLASS = (Class<ArrayList<Descriptor>>) descriptors.getClass();
    }

    public DefaultAOAssetManager() {
        setLoader(Sequencer.class, new MidiLoader());
        setLoader(ANIMATION_CLASS, ANIMATIONS + JSON_EXTENSION, new AnimationLoader());
        setLoader(IMAGE_CLASS, IMAGES + JSON_EXTENSION, new ImageLoader());
        setLoader(BODIES_CLASS, BODIES + JSON_EXTENSION, new GenericLoader<>());
        setLoader(OBJS_CLASS, new ObjectsLoader());
        setLoader(SPELLS_CLASS, SharedResources.SPELLS_FILE + JSON_EXTENSION, new SpellsLoader());
        setLoader(DESCRIPTORS_CLASS, new DescriptorsLoader());
        setLoader(AOSkin.class, new AOSkinLoader());
    }

    @Override
    public void load() {
        loadObjects();
        loadSpells();
        loadDescriptors();
        loadParticles();
        loadSounds();
        loadMusic();
        loadSkins();
        loadFonts();
    }

    @Override
    public AssetManager getAssetManager() {
        return this;
    }

    private void loadFonts() {
        // TODO
    }

    private void loadSkins() {
        load(Resources.GAME_SKIN_FILE, AOSkin.class);
    }

    @Override
    public Pixmap getPixmap(String key) {
        return null;
    }

    @Override
    public Texture getTexture(int key) {
        String fileName = Resources.GAME_GRAPHICS_PATH + key + Resources.GAME_GRAPHICS_EXTENSION;
        if (!isLoaded(fileName)) {
            loadTexture(fileName);
            finishLoadingAsset(fileName);
        }
        return get(fileName);
    }

    @Override
    public AOImage getImage(int id) {
        if (images == null) {
            this.images = getImages();
        }
        return images.get(id);
    }

    @Override
    public AOAnimation getAnimation(int id) {
        if (animations == null) {
            this.animations = getAnimations()
                    .stream()
                    .collect(Collectors.toMap(AOAnimation::getId, animation -> animation));

        }
        return animations.get(id);
    }

    @Override
    public BitmapFont getFont(String key) {
        return null;
    }

    @Override
    public TextureAtlas getTextureAtlas(String key) {
        return null;
    }

    @Override
    public AOSkin getSkin() {
        return get(Resources.GAME_SKIN_FILE);
    }

    @Override
    public Music getMusic(int key) {
        if (Gdx.files.internal(Resources.GAME_MUSIC_PATH + key + Resources.GAME_MUSIC_EXTENSION).exists()) {
            return get(Resources.GAME_MUSIC_PATH + key + Resources.GAME_MUSIC_EXTENSION);
        } else {
            return null;
        }
    }

    @Override
    public Sound getSound(int key) {
        if (Gdx.files.internal(Resources.GAME_SOUNDS_PATH + key + Resources.GAME_SOUNDS_EXTENSION).exists()) {
            return get(Resources.GAME_SOUNDS_PATH + key + Resources.GAME_SOUNDS_EXTENSION);
        } else {
            return null;
        }
    }

    @Override
    public Sequencer getMidi(int key) {
        if (Gdx.files.internal(Resources.GAME_MIDI_PATH + key + Resources.GAME_MIDI_EXTENSION).exists()) {
            return get(Resources.GAME_MIDI_PATH + key + Resources.GAME_MIDI_EXTENSION);
        } else {
            return null;
        }

    }

    @Override
    public ParticleEffect getParticle(String particle) {
        return get(Resources.GAME_FXS_PATH + particle);
    }

    @Override
    public Map<Integer, BodyDescriptor> getBodies() {
        return get(GAME_DESCRIPTORS_PATH + BODIES + JSON_EXTENSION);
    }

    @Override
    public Map<Integer, AOImage> getImages() {
        if (images == null) {
            List<AOImage> list = get(GAME_DESCRIPTORS_PATH + IMAGES + JSON_EXTENSION);
            this.images = list
                        .stream()
                        .collect(Collectors.toMap(AOImage::getId, image -> image));
        }
        return images;
    }

    @Override
    public List<AOAnimation> getAnimations() {
        return get(GAME_DESCRIPTORS_PATH + ANIMATIONS + JSON_EXTENSION);
    }

    @Override
    public Map<Integer, Obj> getObjs() {
        Map<Integer, Obj> objs = new HashMap<>();
        Arrays.stream(Type.values()).forEach(type -> {
            String fileName = SharedResources.OBJECTS_FOLDER + type.name().toLowerCase() + JSON_EXTENSION;
            if (Gdx.files.internal(fileName).exists()) {
                objs.putAll(get(fileName));
            }
        });
        return objs;
    }

    @Override
    public Map<Integer, Spell> getSpells() {
        return get(SharedResources.SPELLS_JSON_FILE);
    }

    @Override
    public List<ShieldDescriptor> getShields() {
        return get(GAME_DESCRIPTORS_FOLDER + SHIELDS + JSON_EXTENSION);
    }

    @Override
    public List<FXDescriptor> getFXs() {
        return get(GAME_DESCRIPTORS_FOLDER + FXS + JSON_EXTENSION);
    }

    @Override
    public List<HeadDescriptor> getHeads() {
        return get(GAME_DESCRIPTORS_FOLDER + HEADS + JSON_EXTENSION);
    }

    @Override
    public List<HelmetDescriptor> getHelmets() {
        return get(GAME_DESCRIPTORS_FOLDER + HELMETS + JSON_EXTENSION);
    }

    @Override
    public List<WeaponDescriptor> getWeapons() {
        return get(GAME_DESCRIPTORS_FOLDER + WEAPONS + JSON_EXTENSION);
    }

    private void loadTexture(String fileName) {
        TextureParameter param = new TextureParameter();
        param.minFilter = TextureFilter.Linear;
        param.magFilter = TextureFilter.Linear;
        param.genMipMaps = true;
        param.wrapU = Texture.TextureWrap.Repeat;
        param.wrapV = Texture.TextureWrap.Repeat;
        load(fileName, Texture.class, param);
    }

    private void loadObjects() {
        Arrays.stream(Type.values()).forEach(type -> {
            ObjectParameter param = new ObjectParameter(type);
            String fileName = SharedResources.OBJECTS_FOLDER + type.name().toLowerCase() + JSON_EXTENSION;
            if (Gdx.app.getFiles().internal(fileName).exists()) {
                load(fileName, OBJS_CLASS, param);
            }
        });
    }

    private void loadSpells() {
        load(SharedResources.SPELLS_JSON_FILE, SPELLS_CLASS);
    }

    private void loadDescriptors() {
        load(GAME_DESCRIPTORS_FOLDER + WEAPONS + JSON_EXTENSION, DESCRIPTORS_CLASS, descriptor(WeaponDescriptor.class));
        load(GAME_DESCRIPTORS_FOLDER + SHIELDS + JSON_EXTENSION, DESCRIPTORS_CLASS, descriptor(ShieldDescriptor.class));
        load(GAME_DESCRIPTORS_FOLDER + HEADS + JSON_EXTENSION, DESCRIPTORS_CLASS, descriptor(HeadDescriptor.class));
        load(GAME_DESCRIPTORS_FOLDER + HELMETS + JSON_EXTENSION, DESCRIPTORS_CLASS, descriptor(HelmetDescriptor.class));
        load(GAME_DESCRIPTORS_FOLDER + FXS + JSON_EXTENSION, DESCRIPTORS_CLASS, descriptor(FXDescriptor.class));
        load(GAME_DESCRIPTORS_FOLDER + IMAGES + JSON_EXTENSION, IMAGE_CLASS);
        load(GAME_DESCRIPTORS_FOLDER + ANIMATIONS + JSON_EXTENSION, ANIMATION_CLASS);
        load(GAME_DESCRIPTORS_FOLDER + BODIES + JSON_EXTENSION, BODIES_CLASS, bodiesGenericParameter());
    }

    private void loadParticles() {
        load(Resources.GAME_FXS_PATH + "meditate1.party", ParticleEffect.class);
        ParticleEffectParameter params = new ParticleEffectParameter();
        params.imagesDir = Gdx.files.internal(Resources.GAME_PARTICLES_PATH);
        load(Resources.GAME_FXS_PATH + "aura1.party", ParticleEffect.class, params);
    }

    private void loadMusic() {
        Reflections reflections = new Reflections("", new ResourcesScanner());
        Set<String> mp3Files = reflections.getResources(Pattern.compile(".*\\.mp3"));
        Set<String> midiFiles = reflections.getResources(Pattern.compile(".*\\.mid"));
        mp3Files.forEach(mp3 -> load(mp3, Music.class));
        midiFiles.forEach(midi -> load(midi, Sequencer.class));
    }

    private void loadSounds() {
        Reflections reflections = new Reflections("", new ResourcesScanner());
        Set<String> sounds = reflections.getResources(Pattern.compile(".*\\.wav"));
        sounds.forEach(sound -> load(sound, Sound.class));
    }
}
