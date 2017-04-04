package com.rafo.chess.engine.plugin.impl.ynmj;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.action.GangAction;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;

/**
 * Created by Administrator on 2016/11/10.
 */
public class YNGangPlugin extends AbstractPlayerPlugin<GangAction> implements IPluginCheckCanExecuteAction<GangAction> {

    @Override
    public boolean checkExecute(Object... objects) {
        return false;
    }

    @Override
    public void createCanExecuteAction(GangAction action) {
    }

    @Override
    public void doOperation(GangAction action) throws ActionRuntimeException {
    }



}
