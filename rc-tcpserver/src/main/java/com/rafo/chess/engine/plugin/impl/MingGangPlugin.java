package com.rafo.chess.engine.plugin.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.gameModel.factory.GameModelFactory;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.GangAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.engine.room.RoomManager;

/***
 * 
 * @author Administrator
 * 
 */
public abstract class MingGangPlugin extends GangPlugin{

	@Override
	public void createCanExecuteAction(GangAction action) {
		MJPlayer player = (MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());
		ActionManager.moCheck(player);
	}
	
	@Override
	public void doOperation(GangAction action) throws ActionRuntimeException {
		if (action.getSubType()==gen.getSubType()) {
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
				if(cardlist.size()==3){
					break;
				}
			}
			MJCard card = action.getRoomInstance().getEngine().getOutCardPool().getLast();
			if (cardlist.size()!=3||card.getCardNum()!=action.getCard()) {
				throw new ActionRuntimeException("gang is faild...", action.getActionType(), action.getPlayerUid());
			}
			action.getRoomInstance().getEngine().getOutCardPool().remove(card);
			card.setUid(action.getPlayerUid());
			cardlist.add(card);
			CardGroup cardGroup = new CardGroup(gen.getSubType(), cardlist, action.getFromUid());
			player.getHandCards().getOpencards().add(cardGroup);
			//测试注释代码
//			PayDetailed pay = payment(action);
//			pay.setPayType(PayDetailed.PayType.Multiple);
			player.setOpen(true);

			//测试代码，向牌库中增加指定的牌，为杠完下一张摸的牌
//			ArrayList<MJCard> cardPool = action.getRoomInstance().getEngine().getCardPool();
//			MJCard c = new MJCard();
//			c.setCardNum(26);
//			c.setUid(0);
//			c.setStatus(0);
//			cardPool.add(0,c);

			this.createCanExecuteAction(action);
		}
	}

	@Override
	public boolean checkExecute(Object... objects) { //YB听牌后再杠
		MJPlayer pTemp = (MJPlayer) objects[0];

		IEPlayerAction act = (IEPlayerAction) objects[1];
		//如果牌池小于等于4张牌，不允许杠
//		if(act.getRoomInstance().getEngine().getCardPool().size() <= 4){
//			return false;
//		}

		int cardNum = act.getCard();
		IEPlayerAction daAction = act;
		//如果听牌，此处没用到，没有听牌
//		if(act.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING){
//			daAction = act.getRoomInstance().getEngine().getMediator().getDoneActionByStep(act.getStep() -1);
//			if(daAction != null && daAction.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT &&
//					daAction.getPlayerUid() != act.getPlayerUid()){
//				cardNum = daAction.getCard();
//			}else{
//				return false;
//			}
//		}
		//统计手牌中所有种类牌的数量
		HashMap<Integer, Integer> map = pTemp.getHandCards().getCardCountFromHands();
		for (int num : map.keySet()) {
			int count = map.get(num);																					//判断每种牌再手牌中的数量
			if (count == 3 && daAction.getPlayerUid() != pTemp.getUid() && num == cardNum) {							//如果当前牌的数量为3，执行打牌动作玩家与当前判断玩家不为同一人，且牌面值相同，执行以下动作

				List<Integer> cards = new ArrayList<>();
				for(int i=0;i<count;i++){
					cards.add(num);
				}

				GangPlugin.GangCheckState state = jiaozuiWithOutGang(pTemp, cards, daAction);
				if(state == GangPlugin.GangCheckState.Invalid){
					continue;
				}else if(state == GangPlugin.GangCheckState.CanTing){
					return false;
				}

				GangAction gangAct = new GangAction(daAction.getRoomInstance(), pTemp.getUid(), daAction.getPlayerUid(),
						num, gen.getSubType());
				gangAct.setCanDoType(gen.getCanDoType());
				if(act.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING) {
					gangAct.setCanPass(false);
				}
				RoomManager.getRoomInstnaceByRoomid(pTemp.getRoomId()).addCanExecuteAction(gangAct);
			}
		}
		return true;
	}

	@Override
	public boolean doPayDetail(PayDetailed pd, RoomInstance room, Calculator calculator) {
		if(super.doPayDetail(pd, room, calculator)){
			calculator.getBattleCensuss().get(pd.getToUid()).addKong();
			return true;
		}

		return false;
	}

	/**
	 * 如果未听牌，且未开门，去掉手里的杠牌，判断是否能叫嘴，如果能叫嘴，则暂时不杠出去，发出听牌的消息
	 * 如果听牌，去掉杠牌，判断是否影响听牌时候的牌型，如果不影响，则可以杠；否则，不能杠
	 * @param player
	 * @param cards
	 * @return
/*	 */
	public GangCheckState jiaozuiWithOutGang(MJPlayer player, List<Integer> cards, IEPlayerAction act){
		ArrayList<MJCard> tempCards = new ArrayList<>();
		for(Integer card : cards){
			MJCard c = (MJCard) GameModelFactory.createCard(card, GameModelFactory.CardType.CARD_MAJIANG.getFlag());
			tempCards.add(c);
		}

		MJPlayer tempPlayer = player;

		//如果是点杠，杠听判断的时候，没开门需要设置为开门状态，因为此时的听牌不能为扣听
		if(!player.isOpen()){
			try {
				tempPlayer = player.clone();
			} catch (CloneNotSupportedException e) {
				return GangCheckState.Invalid;
			}

			if(act.getPlayerUid() != player.getUid()){
				tempPlayer.setOpen(true);
			}
		}

		CardGroup cardGroup = new CardGroup(gen.getSubType(), tempCards, act.getFromUid());
		int jiaozui = player.isJiaozui();
		boolean canTing = ActionManager.tingWithoutGangCheck(tempPlayer, act, cardGroup);

		if(player.isKouTing() || player.isTing()){ //已经扣听或听，判断是否影响牌型
			if(jiaozui != player.isJiaozui()){
				player.setJiaozui(jiaozui); //恢复原来的牌型
				return GangCheckState.Invalid;
			}else{
				return GangCheckState.CanGang;
			}
		}

		if(canTing){
			player.setJiaozui(tempPlayer.isJiaozui());
		}

		return canTing? GangCheckState.CanTing : GangCheckState.CanGang;

	}
}
