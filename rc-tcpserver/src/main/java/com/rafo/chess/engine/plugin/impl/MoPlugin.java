package com.rafo.chess.engine.plugin.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.MahjongEngine;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.MoAction;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.engine.room.RoomManager;

/***
 * 发牌
 * @author Administrator
 * 
 */
public class MoPlugin extends AbstractPlayerPlugin<MoAction> implements IPluginCheckCanExecuteAction<MoAction>{

	@Override
	public void createCanExecuteAction(MoAction action) {

		if (action.getRoomInstance().getRoomStatus() == RoomInstance.RoomState.gameing.getValue()) {

			IPlayer player = action.getRoomInstance().getPlayerById(action.getPlayerUid());
			// 杠
			ActionManager.gangCheck(player, action);
			//测试代码
			if((player.getHandCards().getHandCards().size())>14 && ((MJCard)player.getHandCards().getHandCards().get(13)).getCardNum()==39)
				System.out.println("foo");

			// 胡
			ActionManager.huCheck(player, action);

			if (action.getRoomInstance().getCanExecuteActionSize() == 0) {
				ActionManager.daCheck((MJPlayer) player);
			}
		}
	}
	
	@Override
	public void doOperation(MoAction action) {
		RoomInstance<MJCard> room = action.getRoomInstance();
		MahjongEngine engine = (MahjongEngine) room.getEngine();
		MJPlayer p =(MJPlayer) room.getPlayerById(action.getPlayerUid());
		ArrayList<MJCard> cardPool = engine.getCardPool();
		if (p == null)
			return;
		if(cardPool.size()==0)
			return;
		MJCard card = cardPool.remove(0);
		card.setUid(p.getUid());
		action.setCard(card.getCardNum());
		p.getHandCards().getHandCards().add(card);
		//重置同一圈不能胡第二张的标记为false
		p.getPassNohu().noPass = false;
		p.getPassNohu().cardNum = 0;
		//重置同一圈不能胡第二张的标记为false
		p.getPassNopeng().noPass = false;
		p.getPassNopeng().cardNum = 0;

		createCanExecuteAction(action);

		ActionManager.checkQueCardStatus(p.getUid(), room);
	}

	@Override
	public boolean checkExecute(Object... objects) {
		MJPlayer nextp = (MJPlayer) objects[0];
		RoomInstance roomIns = RoomManager.getRoomInstnaceByRoomid(nextp.getRoomId());
		MoAction action = new MoAction(roomIns);
		action.setPlayerUid(nextp.getUid());
		action.setFromUid(nextp.getUid());
		action.setAutoRun(true);
		action.setCanDoType(gen.getCanDoType());
		action.setSubType(gen.getSubType());
		action.getRoomInstance().addCanExecuteAction(action);
		return true;
	}
}
