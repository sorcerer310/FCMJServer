package com.rafo.chess.utils;

import com.rafo.chess.engine.game.YBMJGameType;

/**
 * Created by Administrator on 2016/9/8.
 */
public class Constants {
    public final static String EXTENSION_ID = "__lib__";
    public final static String EXTENSIONS_CLASS = "com.rafo.chess.core.GameExtension";

    public final static  int[] lostScoreType = new int[]{
        YBMJGameType.PlayType.LOST_EAST,
        YBMJGameType.PlayType.LOST_NORTH,
        YBMJGameType.PlayType.LOST_WEST,
        YBMJGameType.PlayType.LOST_SOUTH
    };
}
