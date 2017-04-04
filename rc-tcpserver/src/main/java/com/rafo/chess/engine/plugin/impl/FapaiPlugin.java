package com.rafo.chess.engine.plugin.impl;

import java.util.ArrayList;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.MahjongEngine;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.DealerDealAction;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.template.impl.PluginTemplateGen;
import com.rafo.chess.template.impl.RoomSettingTemplateGen;

/***
 * 发牌
 * 
 * @author Administrator
 * 
 */
public abstract class FapaiPlugin implements IOptPlugin<DealerDealAction>, IPluginCheckCanExecuteAction {

	PluginTemplateGen gen = null;
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void doOperation(DealerDealAction action) {

		RoomInstance room = action.getRoomInstance();
		MahjongEngine engine = (MahjongEngine) room.getEngine();
		RoomSettingTemplateGen roomGen = room.getRstempateGen();
		ArrayList<IPlayer> players = room.getAllPlayer();

		for (IPlayer player : players) {
			MJPlayer p = (MJPlayer) player;
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
		
		
		MJPlayer player = (MJPlayer)room.getPlayerById(room.getBankerUid());
		room.setFocusIndex(player.getIndex());
		ActionManager.moCheck(player);
		
	}

	public PluginTemplateGen getGen() {
		return gen;
	}

	public void setGen(PluginTemplateGen gen) {
		this.gen = gen;
	}

}
