package com.rafo.chess.engine.plugin.impl.ynmj;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.MahjongEngine;
import com.rafo.chess.engine.majiang.action.*;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;
import com.rafo.chess.engine.plugin.OptPluginFactory;
import com.rafo.chess.engine.plugin.impl.FapaiPlugin;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.template.impl.PluginTemplateGen;
import com.rafo.chess.template.impl.RoomSettingTemplateGen;

import java.util.*;

/***
 * 发牌
 * 
 * @author Administrator
 * 
 */
public class YNFaPaiPlugin extends FapaiPlugin {

	PluginTemplateGen gen = null;
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void doOperation(DealerDealAction action) {

		RoomInstance room = action.getRoomInstance();
		MahjongEngine engine = (MahjongEngine) room.getEngine();
		RoomSettingTemplateGen roomGen = room.getRstempateGen();
		ArrayList<IPlayer> players = room.getAllPlayer();

		Map<Integer, Integer[]> userCards = new HashMap<>();

/*		Integer[] dCards = {11, 15, 14, 14, 18, 36, 26, 19, 39, 21, 46, 25, 47 };
		Integer[] bCards = {21, 25, 21, 25, 25, 25, 27, 28, 22, 45, 46, 47, 47 };
		Integer[] cCards = {21, 15, 16, 15, 31, 31, 31, 29, 29, 45, 45, 45, 29 };
		Integer[] banker = {14, 14, 16, 16, 17, 17, 41, 41, 42, 43, 44, 41, 41 };*/

		//杠后
		Integer[] dCards = {11, 12, 13, 23, 24, 25, 35, 36, 37, 11, 27, 27, 27 };
		Integer[] bCards = {26, 26, 23, 37, 38, 38, 39, 39, 11, 11, 16, 16, 17 };
		Integer[] cCards = {14, 15, 15, 26, 31, 31, 31, 31, 29, 21, 22, 23, 12 };
		Integer[] banker = {11, 12, 14, 16, 26, 19, 18, 18, 18, 18, 14, 14, 14 };

		List<Integer[]> otherCards =  new ArrayList<>();
		otherCards.add(bCards);
		otherCards.add(cCards);
		otherCards.add(dCards);
		int index = 0;

		for (IPlayer player : players) {
			MJPlayer p = (MJPlayer) player;
			if(p.getUid() == room.getBankerUid()){
				userCards.put(p.getUid(), banker);
			}else {
				userCards.put(p.getUid(), otherCards.get(index));
				index++;
			}
			int count = roomGen.getInitHandCardCount();
			ArrayList<MJCard> cardPool = engine.getCardPool();
			if (cardPool.size() < count) {
				return;
			}
			if (player == null)
				return;
			ArrayList cards = new ArrayList<MJCard>();
			for (int i = 0; i < count; i++) {
				MJCard card = cardPool.remove(0);
				card.setUid(player.getUid());
				cards.add(card);
			}
			player.getHandCards().addHandCards(cards);
		}

		//reCreateHandCards(room, userCards, room.getAllPlayer());

		MJPlayer player = (MJPlayer)room.getPlayerById(room.getBankerUid());
		room.setFocusIndex(player.getIndex());
		ActionManager.moCheck(player);
		try {
			room.getEngine().getMediator().doAutoRunAction();
		} catch (ActionRuntimeException e) {
			e.printStackTrace();
		}

		this.createCanExecuteAction(action);

	}

	@Override
	public boolean doPayDetail(PayDetailed pd, RoomInstance room, Calculator calculator) {
		return false;
	}

	public static void reCreateHandCards(RoomInstance room , Map<Integer, Integer[]> userCards, ArrayList<IPlayer> players){
		for(IPlayer player : players) {
			ArrayList<MJCard> handCards = player.getHandCards().getHandCards();
			ArrayList<MJCard> cardPool = room.getEngine().getCardPool();
			for (MJCard card : handCards) {
				cardPool.add(card);
			}
		}
		ArrayList<MJCard> cardPool = room.getEngine().getCardPool();
		for(IPlayer player : players) {
			ArrayList<MJCard> handCards = player.getHandCards().getHandCards();
			Integer[] newCards = userCards.get(player.getUid());
			int i=0;
			for(int card : newCards){
				Iterator<MJCard> cs = cardPool.iterator();
				while(cs.hasNext()){
					MJCard c = cs.next();
					if(c.getCardNum() == card){
						handCards.set(i, c);
						cs.remove();
						i++;
						break;
					}
				}
			}

		}
	}

	@Override
	public boolean checkExecute(Object... objects) {
		return true;
	}

	@Override
	public void createCanExecuteAction(IEPlayerAction action) { //定缺
		RoomInstance room = action.getRoomInstance();
		if((int)room.getAttribute(RoomAttributeConstants.ROOM_QUEYIMEN) != 1) {
			return;
		}

		List<IOptPlugin> pluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.ROOM_MATCH_QUE, room.getRstempateGen().getTempId());

		if(pluginList.size() != 1){
			return;
		}

		//取消玩家操作
		int step = room.getEngine().getMediator().getCurrentStep();
		List<IEPlayerAction> nextCandoAction = room.getEngine().getMediator().getCanExecuteActionByStep(step);
		if(nextCandoAction != null) {
			nextCandoAction.clear();
		}

		IPlayer[] players = room.getPlayerArr();
		//添加开始定缺操作
		for (IPlayer playerTemp : players) {
			IPluginCheckCanExecuteAction plugin = (IPluginCheckCanExecuteAction) pluginList.get(0);
			plugin.checkExecute(playerTemp, action);
		}
	}
}
