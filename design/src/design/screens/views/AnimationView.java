package design.screens.views;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import design.designers.AnimationDesigner;
import design.designers.AnimationDesigner.AnimationParameters;
import design.editors.AnimationEditor;
import game.screens.WorldScreen;
import model.textures.AOAnimation;
import model.textures.BundledAnimation;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

import static launcher.DesignCenter.SKIN;

public class AnimationView extends View<AOAnimation, AnimationDesigner> implements WorldScreen {

    public AnimationView() {
        super(new AnimationDesigner(new AnimationParameters()));
    }

    @Override
    Preview<AOAnimation> createPreview() {
        return new AnimationPreview();
    }

    @Override
    Preview<AOAnimation> createItemView() {
        AnimationItem animationItem = new AnimationItem();

        return animationItem;
    }

    @Override
    protected void sort(Array<AOAnimation> items) {
        items.sort(Comparator.comparingInt(AOAnimation::getId));
    }

    @Override
    protected void keyPressed(int keyCode) {

    }

    class AnimationItem extends Editor<AOAnimation> {

        private AOAnimation animation;
        private Actor view;

        public AnimationItem() {
            super(SKIN);
        }

        @NotNull
        @Override
        protected Table getTable() {
            return AnimationEditor.getTable(current);
        }

        @Override
        protected AOAnimation getCopy(AOAnimation to) {
            return new AOAnimation(to);
        }

        @Override
        void save() {
            getDesigner().add(current);
        }

    }

    class AnimationPreview extends Preview<AOAnimation> {

        private final Image image;
        private final Label label;
        private AOAnimation animation;
        private BundledAnimation bundledAnimation;

        public AnimationPreview() {
            super(SKIN);
            label = new Label("", SKIN);
            add(label).row();
            image = new Image();
            add(image);
        }

        @Override
        public void show(AOAnimation animation) {
            this.animation = animation;
            label.setText(animation.getId());
            bundledAnimation = getAnimationHandler().getAnimation(animation.getId());
            TextureRegion graphic = bundledAnimation.getGraphic();
            setSize(graphic.getRegionWidth(), graphic.getRegionHeight());
        }

        @Override
        public AOAnimation get() {
            return animation;
        }

        @Override
        public void act(float delta) {
            if (animation != null) {
                bundledAnimation.setAnimationTime(bundledAnimation.getAnimationTime() + delta);
                TextureRegion graphic = bundledAnimation.getGraphic();
                if (graphic.isFlipY()) {
                    graphic.flip(false, true);
                }
                TextureRegionDrawable drawable = new TextureRegionDrawable(graphic);
                image.setDrawable(drawable);
            }
            super.act(delta);
        }
    }
}
