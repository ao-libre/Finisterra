package game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import entity.character.info.Inventory.Item;
import game.handlers.ObjectHandler;
import game.screens.GameScreen;
import game.utils.Skins;
import game.utils.WorldUtils;
import shared.network.interaction.DropItem;
import shared.network.inventory.InventoryUpdate;
import shared.network.inventory.ItemActionRequest;
import shared.objects.types.Obj;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

import static com.artemis.E.E;

public class Inventory extends Window {

    static final int COLUMNS = 6;
    private static final int ROWS = 1;
    private static final int SIZE = COLUMNS * ROWS;

    private ArrayList<Slot> slots;
    private Optional<Slot> selected = Optional.empty();
    private Optional<Slot> dragging = Optional.empty();
    private Optional<Slot> origin = Optional.empty();
    private boolean over;

    Inventory() {
        super("", Skins.COMODORE_SKIN, "black");
        setMovable(false);
        slots = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            Slot newSlot = new Slot();
            slots.add(newSlot);
            add(slots.get(i)).width(Slot.SIZE).height(Slot.SIZE);
        }

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selected.ifPresent(slot -> slot.setSelected(false));
                selected = getSlot(x, y);
                selected.ifPresent(slot -> {
                    slot.setSelected(true);
                    slot.getItem().ifPresent(item -> {
                        if (getTapCount() >= 2) {
                            GameScreen.getClient().sendToAll(new ItemActionRequest(slots.indexOf(slot)));
                        }
                    });
                });
            }

            private Optional<Slot> getSlot(float x, float y) {
                return Stream.of(getChildren().items)
                        .filter(Slot.class::isInstance)
                        .filter(actor -> {
                            if (x > actor.getX() && x < actor.getWidth() + actor.getX()) {
                                return y > actor.getY() && y < actor.getHeight() + actor.getY();
                            }
                            return false;
                        })
                        .map(Slot.class::cast).findFirst();
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                boolean result = super.touchDown(event, x, y, pointer, button);
                if (result) {
                    origin = getSlot(x, y);
                }
                return result;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);
                if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                    dragging = origin;
                } else {
                    dragging = Optional.empty();
                }
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                if (dragging.isPresent()) {
                    Item[] userItems = E(GameScreen.getPlayer()).getInventory().items;
                    // notify server
                    Optional<Slot> slot = getSlot(x, y);
                    if (slot.isPresent()) {
                        Slot target = slot.get();
                        InventoryUpdate update = new InventoryUpdate(E(GameScreen.getPlayer()).getNetwork().id);
                        int targetIndex = slots.indexOf(target);
                        int originIndex = slots.indexOf(dragging.get());
                        Item originItem = userItems[originIndex];
                        if (userItems[targetIndex] != null) {
                            update.add(targetIndex, originItem);
                            update.add(originIndex, userItems[targetIndex]);
                            swap(userItems, originIndex, targetIndex);
                        } else {
                            update.add(targetIndex, originItem);
                            update.remove(originIndex);
                            userItems[targetIndex] = originItem;
                            userItems[originIndex] = null;
                        }
                        GameScreen.getClient().sendToAll(update);
                        updateUserInventory();
                    } else {
                        WorldUtils.mouseToWorldPos().ifPresent(worldPos -> GameScreen.getClient().sendToAll(new DropItem(E(GameScreen.getPlayer()).getNetwork().id, draggingIndex(), worldPos)));
                    }
                }
                dragging = Optional.empty();
            }

            final <T> void swap(T[] a, int i, int j) {
                T t = a[i];
                a[i] = a[j];
                a[j] = t;
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                over = isOver();
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                over = isOver();
            }
        });
    }

    public void updateUserInventory() {
        Item[] userItems = E(GameScreen.getPlayer()).getInventory().items;
        for (int i = 0; i < SIZE; i++) {
            Item item = i < userItems.length ? userItems[i] : null;
            slots.get(i).setItem(item);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        dragging.ifPresent(slot -> slot.getItem().ifPresent(item -> {
            Optional<Obj> object = ObjectHandler.getObject(item.objId);
            object.ifPresent(obj -> {
                TextureRegion graphic = ObjectHandler.getGraphic(obj);
                int x1 = Gdx.input.getX() - (graphic.getRegionWidth() / 2) - 4;
                int y1 = Gdx.graphics.getHeight() - Gdx.input.getY() - (graphic.getRegionHeight() / 2);
                Vector2 tempPosition = stageToLocalCoordinates(new Vector2(x1, y1));
                batch.draw(graphic, tempPosition.x, tempPosition.y);
            });
        }));
    }

    public Optional<Slot> getSelected() {
        return selected;
    }

    public int selectedIndex() {
        assert(selected.isPresent());
        return slots.indexOf(selected.get());
    }

    private int draggingIndex() {
        assert(dragging.isPresent());
        return slots.indexOf(dragging.get());
    }

    public boolean isOver() {
        return over;
    }
}
