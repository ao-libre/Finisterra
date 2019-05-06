package entity.character.parts;

import com.artemis.Component;

import java.io.Serializable;

public class Body extends Component implements Serializable {

    public int index;

    public Body() {
    }

    public Body(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
