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
 * <p>
 * 判断规则：
 * 1:如果有顺子开门必须为清一色，否则不胡一条龙
 * 2:判断同花色牌是否大于9张，如果大于9张将
 * <p>
 * Created by fengchong on 2017/3/2.
 */
public class YXHuYiTiaoLongPlugin extends YXHuPlugin {

    @Override
    public boolean checkHu(MJPlayer player, ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList) {

        //如果不为一条龙，返回false
        if(!this.isYiTiaoLong(handCards,groupList))
            return false;

        player.getHuAttachType().clear();
        //门前清
        if (this.isMenQianQing(handCards, groupList, player)) {
            player.getHuAttachType().add(YNMJGameType.HuAttachType.MenQianQing);
            player.getHuAttachType().add(YNMJGameType.HuAttachType.ZiMo);
        }
        //自摸
        if (this.isZiMo(handCards, player))
            player.getHuAttachType().add(YNMJGameType.HuAttachType.ZiMo);

        //清一色
        if (this.oneCorlor(handCards, groupList))
            player.getHuAttachType().add(YNMJGameType.HuAttachType.QingYiSe);

        return true;
    }

}
