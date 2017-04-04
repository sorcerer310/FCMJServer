package com.rafo.chess.engine.plugin.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.ChiAction;
import com.rafo.chess.engine.majiang.action.DaAction;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.engine.room.RoomManager;

/***
 * 发牌
 * @author Administrator
 */
public class ChiPlugin extends AbstractPlayerPlugin<ChiAction> implements IPluginCheckCanExecuteAction<ChiAction> {
	public void createCanExecuteAction(ChiAction action) {
		MJPlayer player = (MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());
		ActionManager.gangCheck(player, action);
		ActionManager.tingCheck(player, action);
		if(action.getRoomInstance().getCanExecuteActionSize()==0)
			ActionManager.daCheck(player);
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public void doOperation(ChiAction action) throws ActionRuntimeException {
		// 移除杠牌,放入手牌
		LinkedList<MJCard> pool = action.getRoomInstance().getEngine().getOutCardPool();
		MJCard card = pool.getLast();
		if (card.getCardNum() == action.getCard()) {
			pool.remove(card);
		}
		String[] arr = action.getToBeCards().split(",");
		MJPlayer player = (MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());
		ArrayList<MJCard> gList = new ArrayList<MJCard>();
		gList.add(card);
		for (String carStr : arr) {
			ArrayList<MJCard> hands = player.getHandCards().getHandCards();
			Iterator<MJCard> it = hands.iterator();
			while (it.hasNext()) {
				MJCard cTemp = it.next();
				if (cTemp.getCardNum() != Integer.parseInt(carStr))
					continue;
				gList.add(cTemp);
				it.remove();
				break;
			}
		}
		if (gList.size() != 3)
			throw new ActionRuntimeException("chi is faild...", action.getActionType(), action.getPlayerUid());
		CardGroup cardGroup = new CardGroup(gen.getSubType(), gList, action.getFromUid());
		player.getHandCards().getOpencards().add(cardGroup);
		player.setOpen(true);

//		payment(action);
//		PayDetailed pay = this.payment(action);
//		if(pay!=null){
//			pay.getCards().clear();
//			for(MJCard c : gList){
//				pay.addCard(c.getCardNum());
//			}
//		}

		this.createCanExecuteAction(action);
	}

	@Override
	public boolean checkExecute(Object... objects) {
		MJPlayer pTemp = (MJPlayer) objects[0];
		DaAction act = (DaAction) objects[1];
		if (pTemp.isKouTing() || pTemp.isTing()) {
			return false;
		}
		// 只能吃上一家牌,获得上家id
		int lastIndex = pTemp.getIndex() - 1;
		lastIndex = lastIndex < 0 ? lastIndex + 4 : lastIndex;
		RoomInstance room = act.getRoomInstance();
		if (room.getPlayerArr()[lastIndex].getUid() != act.getPlayerUid())
			return false;

		int cNum = act.getCard();
		// 字不能吃
		if (cNum / 10 > 3)
			return false;

		HashMap<Integer, Integer> countMap = pTemp.getHandCards().getCardCountFromHands();
		//判断吃右边的牌
		if (countMap.containsKey(cNum - 2) && countMap.containsKey(cNum - 1)) {
			ChiAction chiAct = new ChiAction(act.getRoomInstance());
			chiAct.setCard(act.getCard());
			chiAct.setPlayerUid(pTemp.getUid());
			chiAct.setFromUid(act.getPlayerUid());
			chiAct.setToBeCards((cNum - 2) + "," + (cNum - 1));
			chiAct.setSubType(gen.getSubType());
			chiAct.setCanDoType(gen.getCanDoType());
			RoomManager.getRoomInstnaceByRoomid(pTemp.getRoomId()).addCanExecuteAction(chiAct);
		}
		//判断吃左边的牌
		if (countMap.containsKey(cNum + 2) && countMap.containsKey(cNum + 1)) {
			ChiAction chiAct = new ChiAction(act.getRoomInstance());
			chiAct.setCard(act.getCard());
			chiAct.setPlayerUid(pTemp.getUid());
			chiAct.setFromUid(act.getPlayerUid());
			chiAct.setToBeCards((cNum + 1) + "," + (cNum + 2));
			chiAct.setSubType(gen.getSubType());
			chiAct.setCanDoType(gen.getCanDoType());
			RoomManager.getRoomInstnaceByRoomid(pTemp.getRoomId()).addCanExecuteAction(chiAct);
		}
		//判断吃中间的牌
		if (countMap.containsKey(cNum - 1) && countMap.containsKey(cNum + 1)) {
			ChiAction chiAct = new ChiAction(act.getRoomInstance());
			chiAct.setCard(act.getCard());
			chiAct.setPlayerUid(pTemp.getUid());
			chiAct.setFromUid(act.getPlayerUid());
			chiAct.setToBeCards((cNum - 1) + "," + (cNum + 1));
			chiAct.setCanDoType(gen.getCanDoType());
			chiAct.setSubType(gen.getSubType());
			chiAct.setCanDoType(gen.getCanDoType());
			RoomManager.getRoomInstnaceByRoomid(pTemp.getRoomId()).addCanExecuteAction(chiAct);
		}
		return true;
	}
}
