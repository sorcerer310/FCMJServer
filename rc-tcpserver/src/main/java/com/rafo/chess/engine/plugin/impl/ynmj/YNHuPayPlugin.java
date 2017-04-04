package com.rafo.chess.engine.plugin.impl.ynmj;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.IEMajongAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/8.
 */
public class YNHuPayPlugin extends YNPayPlugin {

    @Override
    public boolean analysis(IEPlayerAction action) {
        return true;
    }


}
