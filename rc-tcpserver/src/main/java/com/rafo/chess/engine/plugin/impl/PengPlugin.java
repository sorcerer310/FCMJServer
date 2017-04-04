package com.rafo.chess.engine.plugin.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.gameModel.IEHandCardsContainer;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.DaAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.majiang.action.PengAction;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;
import com.rafo.chess.engine.room.RoomManager;

/***
 * 碰牌
 * @author Administrator
 * 
 */
public class PengPlugin extends AbstractPlayerPlugin<PengAction> implements IPluginCheckCanExecuteAction<PengAction> {

	@Override
	public void doOperation(PengAction action) throws ActionRuntimeException {
		LinkedList<MJCard> pool = action.getRoomInstance().getEngine()
				.getOutCardPool();
		MJCard card = pool.getLast();																					//获得打出的牌最后一张
		if (card.getCardNum() == action.getCard()) {
			pool.remove(card);
		}

		ArrayList<MJCard> list = new ArrayList<MJCard>();
		list.add(card);
		//动作触发玩家
		MJPlayer player = (MJPlayer) action.getRoomInstance()
				.getPlayerById(action.getPlayerUid());
		ArrayList<MJCard> hands = player.getHandCards().getHandCards();
		Iterator<MJCard> it = hands.iterator();
		while(it.hasNext()){
			MJCard cTemp = it.next();
			if(cTemp.getCardNum()==action.getCard()){
				list.add(cTemp);
				it.remove();
				if(list.size()==3)
					break;
			}
		}
		if(list.size()!=3){
			throw new ActionRuntimeException("peng is faild...", action.getActionType(), action.getPlayerUid());
		}
		CardGroup cardGroup = new CardGroup(gen.getSubType(), list, action.getFromUid());
		player.getHandCards().getOpencards().add(cardGroup);
		player.setOpen(true);
		//此处不执行payment
//		payment(action);
		this.createCanExecuteAction(action);
	}

	@Override
	public boolean checkExecute(Object... objects) {
		DaAction act = (DaAction) objects[1];
		MJPlayer player = (MJPlayer) objects[0];
		if (player.isKouTing() || player.isTing()) {
			return false;
		}
		int cardNum = act.getCard();
		IEHandCardsContainer<MJCard> container = player.getHandCards();
		ArrayList<MJCard> cards = container.getHandCards();
		// 检查手里的牌数
		int count = 0;
		for (MJCard card : cards) {
			if (card.getCardNum() == cardNum) {
				count++;
			}
		}
		if (count < 2) {
			return false;
		}
		PengAction pengAct = new PengAction(act.getRoomInstance());
		pengAct.setCard(act.getCard());
		pengAct.setPlayerUid(player.getUid());
		pengAct.setFromUid(act.getPlayerUid());
		pengAct.setSubType(gen.getSubType());
		pengAct.setCanDoType(gen.getCanDoType());
		int gangDrawCard = findGangHouDrawCard(player, act);
		if(gangDrawCard > 0 && gangDrawCard == act.getCard()){
			pengAct.setGanghou(true);
		}
		RoomManager.getRoomInstnaceByRoomid(player.getRoomId()).addCanExecuteAction(pengAct);
		return true;
	}

	protected int findGangHouDrawCard(MJPlayer pTemp, IEPlayerAction act){
		int step = act.getStep();
		IEPlayerAction moAction = act.getRoomInstance().getEngine().getMediator().getDoneActionByStep(--step);
		if(moAction == null){
			return -1;
		}

		if(moAction.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING
				|| moAction.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_GUO) {
			moAction = act.getRoomInstance().getEngine().getMediator().getDoneActionByStep(--step);
			if (moAction == null || moAction.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN) {
				return -1;
			}
		}

		IEPlayerAction gangAction = act.getRoomInstance().getEngine().getMediator().getDoneActionByStep(--step);
		if(gangAction == null || gangAction.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG){
			return -1;
		}

		return moAction.getCard();
	}

	@Override
	public void createCanExecuteAction(PengAction action) {
		MJPlayer player = (MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());
		ActionManager.gangCheck(player, action);
		if(action.getRoomInstance().getCanExecuteActionSize()==0)
			ActionManager.daCheck(player);
	}
}
