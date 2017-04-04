package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.game.YNMJGameType;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * 七星点灯,七星点灯需要自摸才能和，如果点炮算七星在手
 * Created by fengchong on 2017/3/2.
 */
public class YXHuQiXingDianDengPlugin extends YXHuPlugin {
    @Override
    public boolean checkHu(MJPlayer player, ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList) {
        //测试代码
//        if(handCards.get(handCards.size()-1).getCardNum()==44)
//            System.out.println("aaa");

        //1:是否开门
        if(player.isOpen())
            return false;

        //2:和的牌必须是字
        if(handCards.get(handCards.size()-1).getCardNum()<41)
            return false;

        //3:判断字牌不为7张或有重复的，返回false
        ArrayList<MJCard> ziCards = this.getSameTypeCards(handCards, MJCard.MJCardType.ZI);
        if(ziCards.size()!=7 || isRepeat(ziCards))
            return false;

        //4:必须自摸
        if(!this.isZiMo(handCards,player))
            return false;

        //5:非字牌包含三种花色，每种花色最多三张，同花色的牌之间大于2即可
        List<MJCard> l_wan = getSameTypeCards(handCards, MJCard.MJCardType.WAN);
        List<MJCard> l_tong = getSameTypeCards(handCards, MJCard.MJCardType.TONG);
        List<MJCard> l_tiao = getSameTypeCards(handCards, MJCard.MJCardType.TIAO);

        if(l_wan.size()>3 || l_tiao.size()>3 || l_tong.size()>3)
            return false;

        if(!isIntervalCard(l_wan) || !isIntervalCard(l_tong) || !isIntervalCard(l_tiao))
            return false;


        player.getHuAttachType().clear();

        //七星点灯自摸才胡，一定算门前清？？？门清需要 自摸、不开门、夹或单吊
        //门前清
//        if(this.isMenQianQing(handCards,groupList,player)) {
//        player.getHuAttachType().add(YNMJGameType.HuAttachType.MenQianQing);
//            player.getHuAttachType().add(YNMJGameType.HuAttachType.ZiMo);
//        }

        //单独加自摸
        if(this.isZiMo(handCards,player)) {
            player.getHuAttachType().add(YNMJGameType.HuAttachType.MenQianQing);
            player.getHuAttachType().add(YNMJGameType.HuAttachType.ZiMo);
        }


        return true;
    }
}
