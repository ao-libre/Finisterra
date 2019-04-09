package entity.world;

import com.artemis.Component;
import com.artemis.annotations.DelayedComponentRemoval;

import java.io.Serializable;

@DelayedComponentRemoval
public class CombatMessage extends Component implements Serializable {

    public static float DEFAULT_TIME = 2;
    public static float DEFAULT_ALPHA = DEFAULT_TIME;
    public static float DEFAULT_OFFSET = 20;
    public Kind kind;

    public String text;
    public float time = DEFAULT_TIME;
    public float alpha = DEFAULT_ALPHA;
    public float offset = DEFAULT_OFFSET;

    public CombatMessage() {
    }

    public CombatMessage(String text) {
        this(text, Kind.PHYSICAL);
    }

    public CombatMessage(String text, Kind kind) {
        this.text = text;
        this.kind = kind;
    }

    public enum Kind {
        MAGIC,
        PHYSICAL
    }
}
