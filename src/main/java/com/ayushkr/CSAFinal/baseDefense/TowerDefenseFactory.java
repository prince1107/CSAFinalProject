package com.ayushkr.CSAFinal.baseDefense;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.AutoRotationComponent;
import com.almasb.fxgl.dsl.components.EffectComponent;
import com.almasb.fxgl.dsl.components.HealthIntComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.action.ActionComponent;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.components.IrremovableComponent;
import com.almasb.fxgl.entity.components.TimeComponent;
import com.almasb.fxgl.entity.state.StateComponent;
import com.almasb.fxgl.pathfinding.CellMoveComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import com.ayushkr.CSAFinal.baseDefense.components.BulletComponent;
import com.ayushkr.CSAFinal.baseDefense.components.EnemyComponent;
import com.ayushkr.CSAFinal.baseDefense.components.EnemyHealthViewComponent;
import com.ayushkr.CSAFinal.baseDefense.components.TowerComponent;
import com.ayushkr.CSAFinal.baseDefense.data.EnemyData;
import com.ayushkr.CSAFinal.baseDefense.data.TowerData;
import com.ayushkr.CSAFinal.baseDefense.ui.PowerDownIcon;
import javafx.beans.binding.Bindings;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.texture;
import static com.ayushkr.CSAFinal.baseDefense.EntityType.*;
import static com.ayushkr.CSAFinal.baseDefense.data.Config.*;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class TowerDefenseFactory implements EntityFactory {

    @Spawns("Enemy")
    public Entity spawnEnemy(SpawnData data) {
        EnemyData enemyData = data.get("enemyData");

        return entityBuilder(data)
                .type(ENEMY)
                .viewWithBBox("enemies/" + enemyData.imageName())
                .collidable()
                .with(new TimeComponent())
                .with(new EffectComponent())
                .with(new HealthIntComponent(enemyData.hp()))
                .with(new EnemyComponent(data.get("way"), enemyData))
                .with(new AutoRotationComponent())
                .with(new EnemyHealthViewComponent())
                .build();
    }

    @Spawns("Tower")
    public Entity spawnTower(SpawnData data) {
        TowerData towerData = data.get("towerData");

        return entityBuilder(data)
                .type(TOWER)
                .viewWithBBox(towerData.imageName())
                .collidable()
                .with(new TowerComponent(towerData))
                .zIndex(Z_INDEX_TOWER)
                .build();
    }

    @Spawns("Bullet")
    public Entity spawnBullet(SpawnData data) {
        String imageName = data.get("imageName");

        Node view = texture(imageName);
        view.setRotate(90);

        Entity tower = data.get("tower");
        Entity target = data.get("target");

        return entityBuilder(data)
                .type(BULLET)
                .viewWithBBox(view)
                .collidable()
                .with(new BulletComponent(tower, target))
                .with(new AutoRotationComponent())
                .zIndex(Z_INDEX_BULLET)
                .build();
    }

    @Spawns("towerBase")
    public Entity newTowerBase(SpawnData data) {
        var rect = new Rectangle(64, 64, Color.GREEN);
        rect.setOpacity(0.25);

        var cell = entityBuilder(data)
                .type(TOWER_BASE)
                .viewWithBBox(rect)
                .onClick(e -> {
                    FXGL.<TowerDefenseApp>getAppCast().onCellClicked(e);
                })
                .build();

        rect.fillProperty().bind(
                Bindings.when(cell.getViewComponent().getParent().hoverProperty())
                        .then(Color.DARKGREEN)
                        .otherwise(Color.GREEN)
        );

        return cell;
    }

    @Spawns("waypoint")
    public Entity newWaypoint(SpawnData data) {
        return entityBuilder(data)
                .type(WAY)
                .build();
    }

    // VISUAL EFFECTS

    @Spawns("visualEffectSlow")
    public Entity newVisualEffectSlow(SpawnData data) {
        var icon1 = new PowerDownIcon(Color.YELLOW);
        var icon2 = new PowerDownIcon(Color.ORANGE);

        var icon3 = new PowerDownIcon(Color.YELLOW);
        var icon4 = new PowerDownIcon(Color.ORANGE);

        var box = new VBox(-5, icon3, icon4);
        box.setTranslateX(64.0);
        box.setTranslateY(-25.0);

        return entityBuilder(data)
                .viewWithBBox(new VBox(-5, icon1, icon2))
                .viewWithBBox(box)
                .scale(0.3, 0.3)
                .build();
    }

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {

        return entityBuilder(data)
                .type(PLAYER)
                .bbox(new HitBox(new Point2D(5,5), BoundingShape.circle(12)))
                .bbox(new HitBox(new Point2D(10,25), BoundingShape.box(10, 17)))
                .with(new CollidableComponent(true))
                .with(new IrremovableComponent())
                .with(new PlayerComponent())
                .build();
    }
}
