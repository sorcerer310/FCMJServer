package com.rafo.chess.engine.plugin.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.game.YBMJGameType;
import com.rafo.chess.engine.gameModel.factory.GameModelFactory;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.GangAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;
import com.rafo.chess.engine.room.RoomManager;

/***
 * 
 * @author Administrator
 * 
 */
public abstract class GangPlugin extends AbstractPlayerPlugin<GangAction> implements IPluginCheckCanExecuteAction<GangAction> {

	
	@Override
	public void createCanExecuteAction(GangAction action) {
		MJPlayer player = (MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());
		ActionManager.moCheck(player);
	}
	
	@Override
	public void doOperation(GangAction action) throws ActionRuntimeException {
		if (action.getSubType() == gen.getSubType()) {
			// 移除手牌,加入杠牌
			MJPlayer player = (MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());
			ArrayList<MJCard> hands = player.getHandCards().getHandCards();
			ArrayList<MJCard> cardlist = new ArrayList<MJCard>();
			Iterator<MJCard> it = hands.iterator();
			while (it.hasNext()) {
				MJCard c = it.next();
				if (c.getCardNum() == action.getCard()) {
					it.remove();
					cardlist.add(c);
				}
			}
			if (cardlist.size()!=4) {
				throw new ActionRuntimeException("gang is faild...", action.getActionType(), action.getPlayerUid());
			}
			CardGroup cardGroup = new CardGroup(gen.getSubType(), cardlist, action.getFromUid());
			player.getHandCards().getOpencards().add(cardGroup);
			if(action.isGanghou()){
				cardGroup.setSubType(YBMJGameType.PlayType.GangHouAnGang);
			}
			//此处不在杠的时候计分，在最后结算的时候计算杠分
//			payment(action);
			this.createCanExecuteAction(action);
		}
	}

	@Override
	public boolean checkExecute(Object... objects) {
		MJPlayer pTemp = (MJPlayer) objects[0];
		IEPlayerAction act = (IEPlayerAction) objects[1];
		//如果牌池里的牌小于4，不能杠
//		if(act.getRoomInstance().getEngine().getCardPool().size() <= 4){
//			return false;
//		}

		HashMap<Integer, Integer> map = pTemp.getHandCards().getCardCountFromHands();
		for (int num : map.keySet()) {
			int count = map.get(num);
			if (count == 4 && pTemp.getUid() == act.getPlayerUid()) {
				GangAction gangAct = new GangAction(act.getRoomInstance(), pTemp.getUid(), act.getPlayerUid(),
						num, gen.getSubType());
				gangAct.setCanDoType(gen.getCanDoType());
				RoomManager.getRoomInstnaceByRoomid(pTemp.getRoomId()).addCanExecuteAction(gangAct);
			} 
		}
		return true;
	}

	/**
	 * 如果未听牌，且未开门，去掉手里的杠牌，判断是否能叫嘴，如果能叫嘴，则暂时不杠出去，发出听牌的消息
	 * 如果听牌，去掉杠牌，判断是否影响听牌时候的牌型，如果不影响，则可以杠；否则，不能杠
	 * @param player
	 * @param cards
	 * @return
	 */
	public GangCheckState jiaozuiWithOutGang(MJPlayer player, List<Integer> cards, IEPlayerAction act){

		ArrayList<MJCard> tempCards = new ArrayList<>();
		for(Integer card : cards){
			MJCard c = (MJCard) GameModelFactory.createCard(card, GameModelFactory.CardType.CARD_MAJIANG.getFlag());
			tempCards.add(c);
		}

		CardGroup cardGroup = new CardGroup(gen.getSubType(), tempCards, act.getFromUid());
		int jiaozui = player.isJiaozui();
		boolean canTing = ActionManager.tingWithoutGangCheck(player, act, cardGroup);

		if(player.isKouTing() || player.isTing()){ //已经扣听或听，判断是否影响牌型
			if(jiaozui != player.isJiaozui()){
				player.setJiaozui(jiaozui); //恢复原来的牌型
				return GangCheckState.Invalid;
			}else{
				return GangCheckState.CanGang;
			}
		}

		if(!canTing && !player.isOpen()){ //如果不能听,并且未开门,不能暗杠
			return GangCheckState.Invalid;
		}

		return canTing? GangCheckState.CanTing : GangCheckState.CanGang;
	}

	protected enum GangCheckState{
		Invalid, CanGang, CanTing
	}

	protected int findGangHouDrawCard(MJPlayer pTemp, IEPlayerAction act){
		if( pTemp.getUid() != act.getPlayerUid()){
			return -1;
		}

		if(act.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN
				&& act.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING){
			return -1;
		}

		IEPlayerAction moAction = act;
		int step = act.getStep();
		if(act.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING
				|| moAction.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_GUO){
			moAction = act.getRoomInstance().getEngine().getMediator().getDoneActionByStep(--step);
			if(moAction == null || moAction.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN){
				return -1;
			}
		}

		IEPlayerAction gangAction = act.getRoomInstance().getEngine().getMediator().getDoneActionByStep(--step);
		if(gangAction != null && gangAction.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG
				&& gangAction.getPlayerUid() == pTemp.getUid() ){
			return moAction.getCard();
		}

		return -1;
	}
}
