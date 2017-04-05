package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.game.YNMJGameType;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.room.RoomInstance;

import java.util.ArrayList;

/**
 * 字一色，只要都是字就可以和，可以开门碰或杠字
 * Created by fengchong on 2017/3/2.
 */
public class YXHuZiYiSePlugin extends YXHuPlugin {
    @Override
    public boolean checkHu(MJPlayer player, ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList) {
        //开门牌不能万 饼 条
        for (CardGroup cg : groupList) {
            for (MJCard mjc : cg.getCardsList()) {
                if (mjc.getCardNum() < 40)
                    return false;
            }
        }

        int[] hCard = this.list2intArray(handCards);
        //如果手牌发现除了字以外的牌不算字一色不能和
        for (int c : hCard)
            if (c < 40)
                return false;

        //先清空附加加分项
        player.getHuAttachType().clear();

        //判断是否为对对和
        if (this.isDuiDuiHu(handCards, groupList))
            player.getHuAttachType().add(YNMJGameType.HuAttachType.DuiDuiHu);

        //门清一定为自摸
        if (this.isMenQianQing(handCards, groupList, player)) {
            player.getHuAttachType().add(YNMJGameType.HuAttachType.MenQianQing);
            player.getHuAttachType().add(YNMJGameType.HuAttachType.ZiMo);
        }
        //单独判断自摸
        if (this.isZiMo(handCards, player))
            player.getHuAttachType().add(YNMJGameType.HuAttachType.ZiMo);

        //增加全求人
        if (this.isQuanQiuRen(handCards, groupList, player))
            player.getHuAttachType().add(YNMJGameType.HuAttachType.QuanQiuRen);

        return true;
    }

    @Override
    public boolean doPayDetail(PayDetailed pd, RoomInstance room, Calculator calculator) {

        //字一色包三家判断
        MJPlayer player = (MJPlayer) room.getPlayerById(pd.getToUid());
        //如果isZiYiSeChargeAll返回的输家不为0，并且胡牌为点炮，走包三家结算，否则走普通结算
        boolean isZiMo = player.getHuAttachType().contains(YNMJGameType.HuAttachType.ZiMo);								//玩家是否自摸
        int chargepid = this.isZiYiSeChargeAll(player,pd, isZiMo);
        if (chargepid > 0 ) {
            pd.setFromUid(new int[]{chargepid});
            computeScoreYXChargeAll(pd, room, calculator);
        } else {
            super.computeScoreYX(pd, room, calculator);
        }

        return false;
    }
}
