package com.rafo.chess.engine.plugin.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.MahjongEngine;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.DaAction;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.engine.room.RoomManager;

/***
 * 打牌
 * 
 * @author Administrator
 * 
 */
public class DaPlugin extends AbstractPlayerPlugin<DaAction>implements IPluginCheckCanExecuteAction<DaAction> {

	private final Logger logger = LoggerFactory.getLogger("play");
	@Override
	public void createCanExecuteAction(DaAction act) {
		MJPlayer player = (MJPlayer)act.getRoomInstance().getPlayerById(act.getPlayerUid());
		if(player!=null)
		{
			logger.debug("-----da:"+ ";uid:" + player.getNickName() + ";state:" + player.isOffline());
		}
		
		// 判断其他人可能产生那种行为
		RoomInstance room = act.getRoomInstance();
		int currentIndex = room.getFocusIndex();
		int roomSize = room.getPlayerArr().length;
		for (int i = 0; i < roomSize; i++) {
			MJPlayer p = (MJPlayer) room.getPlayerArr()[currentIndex];
			if (p.getUid() == act.getPlayerUid()) {
				currentIndex = ++currentIndex == roomSize ? 0 : currentIndex;
				continue;
			}
			// 杠
			ActionManager.gangCheck(p, act);
			// 碰
			ActionManager.pengCheck(p, act);
			// 吃
			ActionManager.chiCheck(p, act);
			// 胡
			ActionManager.huCheck(p, act);

			currentIndex = ++currentIndex == roomSize ? 0 : currentIndex;
		}
		//游标下移
		if (act.getRoomInstance().getCanExecuteActionSize() == 0) {
			int index = act.getRoomInstance().nextFocusIndex();
			MJPlayer nextPlayer = (MJPlayer) act.getRoomInstance().getPlayerArr()[index];
			ActionManager.moCheck(nextPlayer);			
			
			logger.debug("摸牌:"+ ";uid:" + nextPlayer.getNickName() + ";state:" + player.isOffline() + "-----");
		}
	}
	
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void doOperation(DaAction action) {
		RoomInstance room = action.getRoomInstance();
		MahjongEngine engine = (MahjongEngine) room.getEngine();
		MJPlayer player = (MJPlayer) room.getPlayerById(action.getPlayerUid());
		ArrayList<MJCard> cards = player.getHandCards().getHandCards();
		Iterator<MJCard> it = cards.iterator();
		while (it.hasNext()) {
			MJCard temp = it.next();
			if (temp.getCardNum() == action.getCard()) {
				it.remove();
				LinkedList<MJCard> outPool = engine.getOutCardPool();
				outPool.add(temp);
				break;
			}
		}
		if (player.isKouTing()) {
			ArrayList<MJCard> list = player.getHandCards().getHandCards();
			for (MJCard c : list) {
				if (c.getStatus() == 0)
					c.setStatus(1);
			}
		}
		this.createCanExecuteAction(action);
	}


	@Override
	public boolean checkExecute(Object... objects) {
		MJPlayer player = (MJPlayer) objects[0];
		RoomInstance roomIns = RoomManager.getRoomInstnaceByRoomid(player.getRoomId());
		DaAction daAct = new DaAction(roomIns);
		daAct.setPlayerUid(player.getUid());
		daAct.setFromUid(player.getUid());
		daAct.setSubType(gen.getSubType());
		daAct.setCanDoType(gen.getCanDoType());
		roomIns.addCanExecuteAction(daAct);
		return true;
	}
}
