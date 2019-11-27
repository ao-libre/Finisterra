package server.systems.manager;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.esotericsoftware.minlog.Log;
import server.database.ServerDescriptorReader;
import shared.model.Spell;
import shared.model.readers.DescriptorsReader;
import shared.util.SharedResources;
import shared.util.SpellJson;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * Spell Logic
 */
public class SpellManager extends BaseSystem {
    private static DescriptorsReader reader = new ServerDescriptorReader();
    private static Map<Integer, Spell> spells = new HashMap<>();

    public SpellManager() {
        init();
    }

    @Override
    protected void processSystem() {
    }

    public void init() {
        Log.info("Server initialization", "Loading spells...");
        SpellJson.load(spells, Gdx.files.internal(SharedResources.SPELLS_JSON_FILE));
    }

    public int getId(Spell spell) {
        return spells.entrySet().stream().filter(entry -> entry.getValue().equals(spell)).map(Map.Entry::getKey).findFirst().get();
    }

    public Optional<Spell> getSpell(int id) {
        return Optional.ofNullable(spells.get(id));
    }

    public Map<Integer, Spell> getSpells() {
        return spells;
    }

}
