package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.majiang.action.MoAction;
import com.rafo.chess.engine.plugin.impl.MoPlugin;

import java.util.ArrayList;

/***
 * 发牌
 * @author Administrator
 */
public class YXMoPlugin extends MoPlugin {

    @Override
    public void createCanExecuteAction(MoAction action) {

/*        Map<Integer, Integer> map = (HashMap<Integer, Integer>) action.getRoomInstance()
                .getAttribute(RoomAttributeConstants.YN_GAME_QUE);
        if(map.size() != action.getRoomInstance().getPlayerArr().length){
            return;
        }*/

        super.createCanExecuteAction(action);
    }

    @Override
    public void doOperation(MoAction action) {
        super.doOperation(action);

        MJPlayer player =(MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());
        IEPlayerAction last = action.getRoomInstance().getEngine().getMediator().getDoneActionByStep(action.getStep() - 1);

        //-----------永修全求人解除状态-------------
        //此状态与chi、peng、gang互相配合改变状态
        //由于全求人包三家只有一轮奏效，所以在下次摸牌的时候重置全求人包三家状态
        //再之后该玩家不能吃、碰、杠，所以isQuanQiuRenChargeAll状态再不能被置为true
        if(player.isQuanQiuRenChargeAll())
            player.setQuanQiuRenChargeAll(false);
        //-----------永修全求人解除状态--------------

        //-----------永修全求人杠后进入状态-----------
        //永修全求人杠的进入状态，吃、碰状态再其他类里写的
        //判断摸之前是否为杠并且开门的牌为4组 进入全求人状态。
        if(player.getHandCards().getOpencards().size()==4 && last.getActionType()== IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG){

            //继续判断手中的两张牌是否为同花色，并且间隔小于等于2
            ArrayList<MJCard> hands = player.getHandCards().getHandCards();
            if(hands.size()!=2) return;
            if(hands.get(0).getCardNum()/10 == hands.get(1).getCardNum()/10 && Math.abs(hands.get(0).getCardNum()-hands.get(1).getCardNum())<=2)
                player.setQuanQiuRenChargeAll(true);
        }
        //-----------永修全求人杠后进入状态-----------

        //-----------永修抢杠小胡-------------
        //杠后该玩家摸两次牌以后才重置抢杠小胡标志
        if(player.isQiangGangFlag())
                player.setQiangGangFlag(false);
        //-----------永修抢杠小胡-------------

        //-----------永修杠上开花小胡-------------
        //如果上一步是杠，设置杠上开花状态.重置状态再DaPlugin中
        if(last!=null && last.getActionType()==IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG) {
            player.setGangShangKaiHuaFlag(true);
            //设置完杠上开花后检查是否胡
            ActionManager.huCheck(player,action);
        }

        //-----------永修杠上开花小胡-------------
    }
}
