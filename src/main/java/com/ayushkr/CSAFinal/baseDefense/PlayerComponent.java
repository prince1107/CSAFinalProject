package com.ayushkr.CSAFinal.baseDefense;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.image;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class PlayerComponent extends Component {

    private AnimatedTexture texture;

    private AnimationChannel animIdle, animWalk;

    private int jumps = 2;
    private double speed = 0;

    public PlayerComponent() {

        Image image = image("player.png");

        animIdle = new AnimationChannel(image, 4, 32, 42, Duration.seconds(1), 1, 1);
        animWalk = new AnimationChannel(image, 4, 32, 42, Duration.seconds(0.66), 0, 3);

        texture = new AnimatedTexture(animIdle);
        texture.loop();
    }

    @Override
    public void onAdded() {
        entity.getTransformComponent().setScaleOrigin(new Point2D(16, 21));
        entity.getViewComponent().addChild(texture);
    }

    @Override
    public void onUpdate(double tpf) {
        speed = tpf * 60;
    }

    public void up() {
        getEntity().setRotation(270);
        move(0, -5 * speed);
    }

    public void down() {
        getEntity().setRotation(90);
        move(0, 5 * speed);
    }

    public void left() {
        getEntity().setRotation(180);
        move(-5 * speed, 0);
    }

    public void right() {
        getEntity().setRotation(0);
        move(5 * speed, 0);
    }

    private Vec2 velocity = new Vec2();

    public void move(double dx, double dy) {
        if (!getEntity().isActive())
            return;

        velocity.set((float) dx, (float) dy);

        int length = Math.round(velocity.length());

        velocity.normalizeLocal();
        for (int i = 0; i < length; i++) {
            entity.translate(velocity.x, velocity.y);
        }
    }
}
