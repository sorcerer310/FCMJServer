package com.rafo.chess.engine.plugin.impl.ynmj;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.GangAction;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;
import com.rafo.chess.engine.plugin.OptPluginFactory;
import com.rafo.chess.engine.room.RoomInstance;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/8.
 */
public abstract class YNPayPlugin extends AbstractPlayerPlugin<IEPlayerAction> {
    @Override
    public void doOperation(IEPlayerAction action) throws ActionRuntimeException {
        if(action.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU ){
            return;
        }

        if(!analysis(action)){
            return;
        }

        PayDetailed pay = this.payment(action);
        pay.setPayType(PayDetailed.PayType.Multiple);
        action.getRoomInstance().getEngine().getCalculator().addPayDetailed(pay);
    }

    public abstract boolean analysis(IEPlayerAction action) ;

}
