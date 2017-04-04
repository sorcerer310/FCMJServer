package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.game.YNMJGameType;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.room.RoomInstance;

/**
 * 杠上开花
 * Created by Administrator on 2016/11/10.
 */
public class YXHuGangShangKaiHuaPlugin extends AbstractPlayerPlugin<IEPlayerAction> {

    @Override
    public void doOperation(IEPlayerAction action) throws ActionRuntimeException {
        //如果动作类型不为和返回
        if (action.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU)
            return;
        //获得房间对象
        RoomInstance roomIns = action.getRoomInstance();
        int step = action.getStep();

        IEPlayerAction last1 = roomIns.getEngine().getMediator().getDoneActionByStep(step);
        //如果最后一步不为 过
        if(last1.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_GUO){
            //从房间的完成动作中取最后动作
            last1 = roomIns.getEngine().getMediator().getDoneActionByStep(step);
        }

        // 自摸或打牌操作
        if (last1 == null || (last1.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN
                && last1.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT)) {
            // return;
        }
        //如果最后动作类型为出牌
        if(last1.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT){
            step -= 1;
        }

        // 杠牌操作
        IEPlayerAction last2 = roomIns.getEngine().getMediator().getDoneActionByStep(step = (step - 2));
        if (last2 == null || last2.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG) {
            return;
        }

        if(last2.getPlayerUid() != action.getPlayerUid()){
            return;
        }


//        MJPlayer player = (MJPlayer) roomIns.getPlayerById(action.getPlayerUid());
        //判断如果为清一色，什么牌都可以和
//        action.
        //如果不是清一色，只能和夹得单钓

        //增加杠上开花附加分项
        ((MJPlayer)roomIns.getPlayerById(action.getPlayerUid())).getHuAttachType().add(YNMJGameType.HuAttachType.GangShangKaiHua);

        //由于修改了payment方法，计分方法不一样了，所以此处不执行payment方法
//        PayDetailed pay = this.payment(action);
//        pay.setPayType(PayDetailed.PayType.Multiple);
    }

    @Override
    public boolean doPayDetail(PayDetailed pd, RoomInstance room, Calculator calculator) {
        //自摸现在不在这里增加附加项
//        if(pd.getFromUid().length > 1) {
//            calculator.addCardBalance(pd.getToUid(), this.getGen().getZimoFlag(), pd.getCards(), 0);
//        }
        return true;
    }
}
