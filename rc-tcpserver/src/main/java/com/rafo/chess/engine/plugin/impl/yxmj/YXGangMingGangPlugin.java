package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.GangAction;
import com.rafo.chess.engine.plugin.impl.MingGangPlugin;

/***
 * 永修明杠
 * @author Administrator
 * 
 */
public class YXGangMingGangPlugin extends MingGangPlugin{

    @Override
    public void doOperation(GangAction action) throws ActionRuntimeException {
        super.doOperation(action);

//        MJPlayer player = (MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());
//        判断手里是否剩一张牌，如果剩一张，则进入全求人包三家的一圈
//        if(player.getHandCards().getOpencards().size()==4)
//            player.setQuanQiuRenChargeAll(true);
    }
}
