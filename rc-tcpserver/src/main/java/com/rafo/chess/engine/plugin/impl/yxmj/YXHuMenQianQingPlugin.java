package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.game.YNMJGameType;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import com.rafo.chess.engine.room.RoomManager;

import java.util.ArrayList;

/**
 * 门前清,必须自摸，只能夹和
 * Created by fengchong on 2017/3/2.
 */
public class YXHuMenQianQingPlugin extends YXHuPlugin {
    @Override
    public boolean checkHu(MJPlayer player, ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList) {
        //测试代码
        if( handCards.get(handCards.size()-1).getCardNum()==28)
            System.out.println("aaa");
        //测试代码

        //如果开门不算门清
        if(player.isOpen())
            return false;

        MJCard lastcard = handCards.get(handCards.size() - 1);

        //如果闭和，其他人点炮不和，自摸算门清和
        if(RoomManager.getRoomInstnaceByRoomid(player.getRoomId()).getAttribute(RoomAttributeConstants.YB_PLAY_TYPE)==0){
            if(lastcard.getUid() != player.getUid())
                return false;
        }

        //如果非闭和，自摸算门清胡，点炮算小和
        if(RoomManager.getRoomInstnaceByRoomid(player.getRoomId()).getAttribute(RoomAttributeConstants.YB_PLAY_TYPE)==1) {
            if (lastcard.getUid()!= player.getUid())
                return false;
        }

//        int[] cards = this.arraySort(this.list2intArray(handCards));
        int[] cards = this.list2intArray(handCards);
        boolean flag = this.isHu(cards) && this.isjia;
        //如果flag为true附加其他加分项
        if(!flag)
            return false;

        //附加属性清空
        player.getHuAttachType().clear();
        //自摸
        if(this.isZiMo(handCards,player))
            player.getHuAttachType().add(YNMJGameType.HuAttachType.ZiMo);
        //清一色
        if(this.oneCorlor(handCards,groupList))
            player.getHuAttachType().add(YNMJGameType.HuAttachType.QingYiSe);

        return true;
    }
}
