package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.game.YNMJGameType;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ChiAction;
import com.rafo.chess.engine.majiang.action.DaAction;
import com.rafo.chess.engine.plugin.impl.ChiPlugin;

import java.util.ArrayList;

/**
 * 永修吃插件
 * Created by fengchong on 2017/3/1.
 */
public class YXChiPlugin extends ChiPlugin {
    @Override
    public boolean checkExecute(Object... objects) {
        //执行如果碰或杠了一种颜色，不能吃另一种颜色检查操作
        MJPlayer player = (MJPlayer) objects[0];
        DaAction act = (DaAction) objects[1];
        ArrayList<CardGroup> groupList = player.getHandCards().getOpencards();
        if (groupList.size() > 0) {
            for (CardGroup c : groupList) {
                if (c.getCardsList().get(0).getCardNum() < 40 && c.getCardsList().get(0).getCardNum() / 10 != act.getCard() / 10)
                    return false;
                else if(c.getCardsList().get(0).getCardNum()>40)
                    return false;
            }
        }

        //先执行基础吃操作
        return super.checkExecute(objects);
    }

    @Override
    public void doOperation(ChiAction action) throws ActionRuntimeException {
        super.doOperation(action);

        MJPlayer player = (MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());
        //判断手里是否剩一张牌，如果剩一张，则进入全求人包三家的一圈
        if(player.getHandCards().getOpencards().size()==4) {
            //继续判断手中的两张牌是否为同花色，并且间隔小于等于2
            ArrayList<MJCard> hands = player.getHandCards().getHandCards();
            if (hands.size() != 2) return;
            if (hands.get(0).getCardNum() / 10 == hands.get(1).getCardNum() / 10 && Math.abs(hands.get(0).getCardNum() - hands.get(1).getCardNum()) <= 2)
                player.setQuanQiuRenChargeAll(true);
        }
    }
}
