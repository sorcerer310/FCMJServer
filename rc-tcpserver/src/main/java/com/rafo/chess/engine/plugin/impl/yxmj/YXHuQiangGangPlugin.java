package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.game.YNMJGameType;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;

import java.util.ArrayList;

/**
 * 抢杠和，这里不做和操作，只是将抢杠后的杠分取消了
 * Created by fengchong on 2017/3/2.
 */
public class YXHuQiangGangPlugin extends AbstractPlayerPlugin<HuAction> {
    @Override
    public void doOperation(HuAction action) {
        if (analysis(action))
        {
            // 找到杠牌产生的结果对象
            ArrayList<PayDetailed> payList = action.getRoomInstance().getEngine().getCalculator().getPayDetailList();
            IEPlayerAction last1 = action.getRoomInstance().getEngine().getMediator().getDoneActionByStep(action.getStep() - 2);

            //PayDetailed pay = this.payment(action);
            //pay.setPayType(PayDetailed.PayType.Multiple);

            //此处抢杠成功，为用户增加抢杠的分数
            ((MJPlayer)action.getRoomInstance().getPlayerById(action.getPlayerUid())).getHuAttachType().add(YNMJGameType.HuAttachType.QiangGang);

//            for (PayDetailed pd : payList) {
//                if(last1.getStep() == pd.getStep()){ //杠分数失效
//                    pd.setValid(false);
//                }
//            }
        }
    }

    public boolean analysis(HuAction action) {
        int step = action.getStep();
        IEPlayerAction last1 = action.getRoomInstance().getEngine().getMediator().getDoneActionByStep(step - 1);
        IEPlayerAction last2 = action.getRoomInstance().getEngine().getMediator().getDoneActionByStep(step - 2);
        if (last1 == null || last2 == null)
            return false;

        // 判断上一步是杠,并且是补杠.原来判断是last2，不知道为什么。
        if (last1.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG
                && last1.getSubType() == YNMJGameType.PlayType.Kong) {
            return true;
        }

        return false;
    }
}
