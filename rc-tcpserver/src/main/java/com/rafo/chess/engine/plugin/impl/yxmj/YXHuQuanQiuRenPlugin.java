package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.game.YNMJGameType;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.engine.room.RoomManager;

import java.util.ArrayList;

/**
 * 全求人，如果玩家手中只剩一张牌，并且别人点炮，叫做全求人
 * Created by fengchong on 2017/3/2.
 */
public class YXHuQuanQiuRenPlugin extends YXHuPlugin {
    @Override
    public boolean checkHu(MJPlayer player, ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList) {
        //1:如果开门牌中有暗杠，不算全求人，如果吃牌了只能胡清一色
        for(CardGroup cg:groupList){
            if(cg.getGType()==YNMJGameType.PlayType.CealedKong)
                return false;
            if(cg.getCardsList().get(0).getCardNum() != cg.getCardsList().get(1).getCardNum() && !this.oneCorlor(handCards,groupList)) {
                return false;
            }
        }

        //3:如果手牌不为2张，返回false
        if(handCards.size()!=2)
            return false;

        //4:判断和牌是否为别人打出
        ArrayList<IEPlayerAction> lAction = RoomManager.getRoomInstnaceByRoomid(player.getRoomId()).getEngine().getMediator().getDoneActionList();
        IEPlayerAction iepa = lAction.get(lAction.size()-1);


        //5:别人打出的牌与handCards的牌不一致不和
        if(iepa.getCard()!=handCards.get(handCards.size()-1).getCardNum()
                //动作执行者为自己不和
                || iepa.getPlayerUid()==player.getUid()
                //打出的牌与自己手里的牌不一致不和
                || iepa.getCard()!=handCards.get(0).getCardNum())
            return false;

        player.getHuAttachType().clear();
        //清一色
        if(this.oneCorlor(handCards,groupList))
            player.getHuAttachType().add(YNMJGameType.HuAttachType.QingYiSe);

        //自摸
        if(this.isZiMo(handCards,player))
            player.getHuAttachType().add(YNMJGameType.HuAttachType.ZiMo);
        //判断对对胡
        if(this.isDuiDuiHu(handCards,groupList))
            player.getHuAttachType().add(YNMJGameType.HuAttachType.DuiDuiHu);

        return true;
    }

    @Override
    public boolean doPayDetail(PayDetailed pd, RoomInstance room, Calculator calculator) {
        //结算细节对象无效并结算对象不为null返回false
        if(!pd.isValid() && pd.getFromUid()!=null) return false;

        MJPlayer player = (MJPlayer) room.getPlayerById(pd.getToUid());

        //判断是否清一色也达成包三家条件，如果达成，清一色的掏钱，全求人不掏钱
        boolean isZiMo = player.getHuAttachType().contains(YNMJGameType.HuAttachType.ZiMo);
        int chargeid = isQingYiSeChargeAll(player,pd,isZiMo);
        if(chargeid>0)
            pd.setFromUid(new int[]{chargeid});

        //全求人包三家判断
        //全求人的包三家，设置状态数据在YXChiPlugin、YXPengPlugin、YXMoPlugin中设置。杠的判断也在YXMoPlugin中设置
        //如果赢家player对象的isQuanQiuRenChargeAll==true，则全求人包庄
        if(player.isQuanQiuRenChargeAll() || chargeid>0 ){
            //只结算一个包三家。
            computeScoreYXChargeAll(pd, room, calculator);
        }else{
            //此处要执行父类的doPayDetail，因为该函数中包括对抢杠包三家的判断
            super.doPayDetail(pd,room,calculator);
        }
        return false;
    }

}
