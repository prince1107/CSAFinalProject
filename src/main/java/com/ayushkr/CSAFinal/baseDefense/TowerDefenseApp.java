package com.ayushkr.CSAFinal.baseDefense;

import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.input.view.KeyView;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.quest.Quest;
import com.almasb.fxgl.quest.QuestService;
import com.almasb.fxgl.quest.QuestState;
import com.ayushkr.CSAFinal.baseDefense.components.EnemyComponent;
import com.ayushkr.CSAFinal.baseDefense.data.*;
import com.ayushkr.CSAFinal.baseDefense.ui.*;
import com.ayushkr.CSAFinal.baseDefense.ui.scene.TowerDefenseGameMenu;
import com.ayushkr.CSAFinal.baseDefense.ui.scene.TowerDefenseMainMenu;
import javafx.beans.binding.Bindings;
import javafx.geometry.Point2D;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import com.almasb.fxgl.input.UserAction;
import javafx.scene.input.KeyCode;
import com.almasb.fxgl.input.virtual.VirtualButton;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;
import static com.ayushkr.CSAFinal.baseDefense.EntityType.WAY;
import static com.ayushkr.CSAFinal.baseDefense.data.Config.*;

/**
 * This is an example of a tower defense game.
 *
 * TODO:
 * - bonus buffs (runes)
 * - tower level up (upgrades)
 * - level end scene
 * - player progression currency, so that towers are unlocked via the "shop"
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class TowerDefenseApp extends GameApplication {

    private List<TowerData> towerData;

    private LevelData currentLevel;

    private TowerSelectionBox towerSelectionBox;
    private WaveIcon waveIcon;
    private Entity player;

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Left") {
            @Override
            protected void onAction() {
                player.getComponent(PlayerComponent.class).left();
            }
        }, KeyCode.A);

        getInput().addAction(new UserAction("Right") {
            @Override
            protected void onAction() {
                player.getComponent(PlayerComponent.class).right();
            }
        }, KeyCode.D);

        getInput().addAction(new UserAction("Up") {
            @Override
            protected void onAction() {
                player.getComponent(PlayerComponent.class).up();
            }
        }, KeyCode.W);

        getInput().addAction(new UserAction("Down") {
            @Override
            protected void onAction() {
                player.getComponent(PlayerComponent.class).down();
            }
        }, KeyCode.S);

        getInput().addAction(new UserAction("Use") {
            @Override
            protected void onActionBegin() {
                System.out.println("E");
            }
        }, KeyCode.E);
    }


    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("FXGL Tower Defense");
        settings.setVersion("0.3");
        settings.setWidth(25 * 64);
        settings.setHeight(14 * 64);
        settings.getCSSList().add("main.css");
        settings.setIntroEnabled(false);
        settings.setMainMenuEnabled(true);
        settings.setGameMenuEnabled(true);
        settings.addEngineService(QuestService.class);
        settings.addEngineService(CurrencyService.class);
        settings.setSceneFactory(new SceneFactory() {
            @Override
            public FXGLMenu newMainMenu() {
                return new TowerDefenseMainMenu();
            }

            @Override
            public FXGLMenu newGameMenu() {
                return new TowerDefenseGameMenu();
            }
        });
        settings.setApplicationMode(ApplicationMode.DEVELOPER);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put(Vars.NUM_ENEMIES, 0);
        vars.put(Vars.MONEY, STARTING_MONEY);
        vars.put(Vars.PLAYER_HP, STARTING_HP);
        vars.put(Vars.CURRENT_WAVE, 0);

        vars.put(Vars.NUM_TOWERS, 0);
    }

    public void setCurrentLevel(LevelData currentLevel) {
        this.currentLevel = currentLevel;
    }

    @Override
    protected void initGame() {
        initVarListeners();

        loadTowerData();

        getGameWorld().addEntityFactory(new TowerDefenseFactory());

        // construct UI objects
        towerSelectionBox = new TowerSelectionBox(towerData);
        waveIcon = new WaveIcon();
        player = null;

        // player must be spawned after call to nextLevel, otherwise player gets removed
        // before the update tick _actually_ adds the player to game world
        player = spawn("player", 50, 50);

        set("player", player);
        loadCurrentLevel();
    }

    private void initVarListeners() {
        getWorldProperties().<Integer>addListener(Vars.NUM_ENEMIES, (old, newValue) -> {
            if (newValue == 0) {
                onWaveEnd();
            }
        });

        getWorldProperties().<Integer>addListener(Vars.PLAYER_HP, (old, newValue) -> {
            if (newValue == 0) {
                gameOver();
            }
        });

        getWorldProperties().<Integer>addListener(Vars.CURRENT_WAVE, (old, newValue) -> {
            Animations.playWaveIconAnimation(waveIcon);
        });

        // TODO: add FXGL API for limiting values of properties / vars?
        getWorldProperties().<Integer>addListener(Vars.MONEY, (old, newValue) -> {
            if (newValue > MAX_MONEY) {
                set(Vars.MONEY, MAX_MONEY);
            }
        });
    }

    private void loadTowerData() {
        List<String> towerNames = List.of(
                "tower1.json",
                "tower2.json",
                "tower3.json",
                "tower4.json",
                "tower5.json",
                "tower6.json"
        );

        towerData = towerNames.stream()
                .map(name -> getAssetLoader().loadJSON("towers/" + name, TowerData.class).get())
                .toList();
    }

    private void loadCurrentLevel() {
        if (player != null) {
            player.setZIndex(Integer.MAX_VALUE);
        }
        set(Vars.CURRENT_WAVE, 0);

        waveIcon.setMaxWave(currentLevel.maxWaveIndex());

        runOnce(() -> {
            initQuests(currentLevel);
        }, Duration.seconds(0.1));

        setLevelFromMap("tmx/" + currentLevel.map());

        getGameWorld().getEntitiesFiltered(e -> e.isType("TiledMapLayer"))
                .forEach(e -> {
                    e.getViewComponent().addOnClickHandler(event -> {
                        towerSelectionBox.setVisible(false);
                    });
                });

        scheduleNextWave();
    }

    private void initQuests(LevelData level) {
        getService(QuestService.class).questsProperty().forEach(quest -> getService(QuestService.class).removeQuest(quest));

        var quest = new Quest("Objectives");

        quest.stateProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("QUEST: " + oldValue + " -> " + newValue);
        });

        level.quests()
                .forEach(data -> {
                    quest.addIntObjective(data.desc(), data.varName(), (int) data.varValue());
                });

        getQuestService().startQuest(quest);

        var vbox = new VBox(10);
        vbox.getChildren().addAll(
                getUIFactoryService().newText(quest.getName(), Color.ANTIQUEWHITE, 24.0)
        );

        quest.objectivesProperty().forEach(obj -> {
            var text = getUIFactoryService().newText("");
            text.textProperty().bind(
                    Bindings.when(obj.stateProperty().isEqualTo(QuestState.COMPLETED))
                            .then("- " + obj.getDescription() + " \u2713")
                            .otherwise("- " + obj.getDescription())
            );
            text.fillProperty().bind(
                    Bindings.when(obj.stateProperty().isEqualTo(QuestState.COMPLETED))
                            .then(Color.GREEN)
                            .otherwise(Color.WHITE)
            );

            vbox.getChildren().add(text);
        });

        addUINode(vbox, getAppWidth() - 200, 10);
    }

    private void scheduleNextWave() {
        waveIcon.scheduleWave(WAVE_PREP_TIME, () -> nextWave());
    }

    private void nextWave() {
        if (geti(Vars.CURRENT_WAVE) < currentLevel.maxWaveIndex()) {
            FXGL.inc(Vars.CURRENT_WAVE, 1);

            currentLevel.waves(geti(Vars.CURRENT_WAVE))
                    .forEach(wave -> {
                        spawnWave(wave);
                    });
        }
    }

    private void spawnWave(WaveData wave) {
        var wayEntity = getGameWorld().getSingleton(e ->
                e.isType(WAY) && e.getString("name").equals(wave.way())
        );

        EnemyData data = getAssetLoader().loadJSON("enemies/" + wave.enemy(), EnemyData.class).get();

        Polyline p = wayEntity.getObject("polyline");
        var way = Way.fromPolyline(p, wayEntity.getX(), wayEntity.getY());

        for (int i = 0; i < wave.amount(); i++) {
            runOnce(() -> {
                spawnWithScale(
                        "Enemy",
                        new SpawnData()
                                .put("way", way)
                                .put("enemyData", data),
                        Duration.seconds(0.45),
                        Interpolators.ELASTIC.EASE_OUT()
                );

            }, Duration.seconds(i * data.interval()));
        }

        FXGL.inc(Vars.NUM_ENEMIES, wave.amount());
    }

    private void onWaveEnd() {
        currentLevel.waves(geti(Vars.CURRENT_WAVE))
                .forEach(wave -> {
                    FXGL.inc(Vars.MONEY, wave.reward());
                });

        if (geti(Vars.CURRENT_WAVE) < currentLevel.maxWaveIndex()) {
            scheduleNextWave();
        } else {
            gameOver();
        }
    }

    @Override
    protected void initUI() {
        towerSelectionBox.setVisible(false);

        addUINode(towerSelectionBox);

        addUINode(new MoneyIcon(), 10, 10);
        addUINode(new HPIcon(), 10, 90);

        addUINode(waveIcon, 10, 250);

        addUINode(new EnemiesIcon(), 10, 170);
    }

    public void onCellClicked(Entity cell) {
        // if we already have a tower on this tower base, ignore call
        if (cell.getProperties().exists("tower"))
            return;

        towerSelectionBox.setCell(cell);
        towerSelectionBox.setVisible(true);

        var x = cell.getX() > getAppWidth() / 2.0 ? cell.getX() - 250 : cell.getX();

        towerSelectionBox.setTranslateX(x);
        towerSelectionBox.setTranslateY(cell.getY());
    }

    public void onTowerSelected(Entity cell, TowerData data) {
        if (geti(Vars.MONEY) - data.cost() >= 0) {
            towerSelectionBox.setVisible(false);

            FXGL.inc(Vars.MONEY, -data.cost());

            var tower = spawnWithScale(
                    "Tower",
                    new SpawnData(cell.getPosition()).put("towerData", data),
                    Duration.seconds(0.85),
                    Interpolators.ELASTIC.EASE_OUT()
            );

            cell.setProperty("tower", tower);

            FXGL.inc(Vars.NUM_TOWERS, +1);
        }
    }

    public void onEnemyKilled(Entity enemy) {
        inc(Vars.MONEY, enemy.getComponent(EnemyComponent.class).getData().reward());
        FXGL.inc(Vars.NUM_ENEMIES, -1);
    }

    public void onEnemyReachedEnd(Entity enemy) {
        FXGL.inc(Vars.PLAYER_HP, -1);
        FXGL.inc(Vars.NUM_ENEMIES, -1);
    }

    private void gameOver() {
        showMessage("Level Complete!", getGameController()::gotoMainMenu);
    }



    public static void main(String[] args) {
        launch(args);
    }
}
