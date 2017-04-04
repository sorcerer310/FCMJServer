package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.game.YNMJGameType;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * 七星在手
 * Created by fengchong on 2017/3/2.
 */
public class YXHuQiXingZaiShouPlugin extends YXHuPlugin {
    @Override
    public boolean checkHu(MJPlayer player, ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList) {
        //测试代码
//        if(handCards.get(handCards.size()-1).getCardNum()==44)
//            System.out.println("aaa");

        //1:是否开门,开门返回false
        if(player.isOpen())
            return false;

        //由于7星点灯点炮不算7星点灯，要走7星在手这里，所以这里不能限制七星在手只胡非字牌
        //2:胡牌不能是字牌
//        if(handCards.get(handCards.size()-1).getCardNum()>40)
//            return false;

        //3:判断字牌不为7张或有重复的返回false
        ArrayList<MJCard> ziCards = this.getSameTypeCards(handCards, MJCard.MJCardType.ZI);
        if(ziCards.size()!=7 || isRepeat(ziCards))
            return false;

        //4:非字牌包含3种花色，且每种花色至少2张，符合[1、4、7]，[2、5、8]，[3、6、9]
        List<MJCard> l_tiao = getSameTypeCards(handCards, MJCard.MJCardType.TIAO);
        List<MJCard> l_wan = getSameTypeCards(handCards, MJCard.MJCardType.WAN);
        List<MJCard> l_tong = getSameTypeCards(handCards, MJCard.MJCardType.TONG);

        if(l_wan.size()>3 || l_tiao.size()>3 || l_tong.size()>3)
            return false;

        if(!isIntervalCard(l_wan) || !isIntervalCard(l_tong) || !isIntervalCard(l_tiao))
            return false;

        player.getHuAttachType().clear();
        //门前清,七星在手如果自摸算门清？门清需要 自摸、不开门、夹或单吊
//        if(this.isMenQianQing(handCards,groupList,player)) {
        if(this.isZiMo(handCards,player)){
            player.getHuAttachType().add(YNMJGameType.HuAttachType.MenQianQing);
            player.getHuAttachType().add(YNMJGameType.HuAttachType.ZiMo);
        }

        return true;
    }
}
