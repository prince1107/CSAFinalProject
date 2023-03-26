package com.ayushkr.CSAFinal.baseDefense.ui;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.ayushkr.CSAFinal.baseDefense.data.TowerData;
import com.ayushkr.CSAFinal.baseDefense.TowerDefenseApp;
import com.ayushkr.CSAFinal.baseDefense.data.Vars;
import javafx.scene.layout.HBox;

import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.getip;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class TowerSelectionBox extends HBox {

    private Entity cell;

    public TowerSelectionBox(List<TowerData> towerData) {
        setSpacing(5);

        towerData.forEach(data -> {
            var icon = new TowerIcon(data);
            icon.bindToMoney(getip(Vars.MONEY));
            icon.setOnMouseClicked(e -> {
                if (cell != null) {
                    FXGL.<TowerDefenseApp>getAppCast().onTowerSelected(cell, data);
                }
            });

            getChildren().add(icon);
        });
    }

    public void setCell(Entity cell) {
        this.cell = cell;
    }

    public Entity getCell() {
        return cell;
    }
}
