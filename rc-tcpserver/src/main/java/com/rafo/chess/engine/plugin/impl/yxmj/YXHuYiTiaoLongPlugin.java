package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.game.YNMJGameType;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.plugin.impl.HuPlugin;
import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 一条龙,可以开门，手中有龙就可以和，开门的牌可以碰或杠不能有吃，有吃只能和清一色
 * Created by fengchong on 2017/3/2.
 */
public class YXHuYiTiaoLongPlugin extends YXHuPlugin {
    @Override
    public boolean checkHu(MJPlayer player, ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList) {
        //1:判断开门的牌如果有顺子必须为清一色，否则返回失败
        for(CardGroup cg:groupList){
            ArrayList<MJCard> amjc = cg.getCardsList();
            if(amjc.get(0).getCardNum()!=amjc.get(1).getCardNum() && !this.oneCorlor(handCards,groupList))
                return false;
        }

        //2:判断手牌中某一花色的牌数量为9 或 11 或 12 或 14
        //9张牌表示有一条龙，11张表示一条龙加一对，12张表示一条龙加顺子或刻子
        // ，14张表示一条龙、一对、一顺子或一刻子
        ArrayList<MJCard> lLong = null;                                                                                 //引用一条龙的集合
        HashMap<MJCard.MJCardType, ArrayList<MJCard>> hmCard = splitHandCards(handCards);

        Iterator<MJCard.MJCardType> itCardKey = hmCard.keySet().iterator();
        while (itCardKey.hasNext()) {
            MJCard.MJCardType mjct = itCardKey.next();
            int c = hmCard.get(mjct).size();
            if (c == 9 || c == 11 || c == 12 || c == 14) {
                lLong = hmCard.remove(mjct);
                break;
            }
        }
        if (lLong == null) return false;

        //3:判断龙牌集合中是否有一条龙
        ArrayList<Integer> save_long = new ArrayList<>();                                                                 //保存龙集合的容器
        for(int i=1;i<=9;i++){
            //从集合中移除龙牌的其中一张
            boolean removeflag = false;
            Iterator<MJCard> itmjc = lLong.iterator();
            while(itmjc.hasNext()){
                MJCard mjc = itmjc.next();
                if(mjc.getCardNum()%10==i){
                    //从包含龙的集合牌中移除一张龙牌
                    lLong.remove(mjc);
                    //将该龙牌加入到一个集合中备用
                    save_long.add(mjc.getCardNum());
                    removeflag=true;
                    break;
                }
            }
            //如果没有移除操作，表示该集合中缺少龙牌某一张，直接返回false
            if(!removeflag)
                return false;
        }
        //4:判断龙牌集合剩下的牌型如果为3N+2，则为和牌
        int[] remainCards = new int[0];
        //增加其他剩余的牌到数组中
        Iterator<ArrayList<MJCard>> itRemainCard = hmCard.values().iterator();
        while (itRemainCard.hasNext()){
            ArrayList<MJCard> almjc = itRemainCard.next();
            for(MJCard mjc:almjc)
                remainCards = ArrayUtils.add(remainCards,mjc.getCardNum());
        }
        //5:增加去雕龙的手牌到集合中，判断是否和
        for(MJCard mjc:lLong)
            remainCards = ArrayUtils.add(remainCards,mjc.getCardNum());
        if(!isHu(remainCards) )
            return false;

        //6:判断是【青龙在手】还是【青龙点睛】,如果是【青龙在手】必须胡夹或者单吊
        //【青龙在手】的特点是最后一张牌不为一条龙中的牌
        int lastCard = handCards.get(handCards.size()-1).getCardNum();
        if(!save_long.contains(lastCard) && !this.isjia)
            return false;

        player.getHuAttachType().clear();
        //门前清
        if(this.isMenQianQing(handCards,groupList,player)) {
            player.getHuAttachType().add(YNMJGameType.HuAttachType.MenQianQing);
            player.getHuAttachType().add(YNMJGameType.HuAttachType.ZiMo);
        }
        //自摸
        if(this.isZiMo(handCards,player))
            player.getHuAttachType().add(YNMJGameType.HuAttachType.ZiMo);

        //清一色
        if(this.oneCorlor(handCards,groupList))
            player.getHuAttachType().add(YNMJGameType.HuAttachType.QingYiSe);

//        player.getHuAttachType().add(YNMJGameType.HuAttachType.MenQianQing);

        return true;
    }
}
