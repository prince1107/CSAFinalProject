package com.ayushkr.CSAFinal.baseDefense.data;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public record EnemyData(
        int hp,
        int reward,
        double moveSpeed,
        double interval,
        String imageName
) { }
