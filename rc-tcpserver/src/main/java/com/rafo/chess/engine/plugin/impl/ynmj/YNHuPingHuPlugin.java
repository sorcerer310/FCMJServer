package com.rafo.chess.engine.plugin.impl.ynmj;

import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.engine.room.RoomManager;

import java.util.ArrayList;
import java.util.HashMap;

/***
 * 抓到指定牌触发执行
 * 
 * @author Administrator
 */
public class YNHuPingHuPlugin extends YNHuPlugin {

	public boolean checkHu(MJPlayer player, ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList) {


		int size = handCards.size();
		int[] cardsTemp = new int[size];
		for (int i = 0; i < handCards.size(); i++) {
			cardsTemp[i] = handCards.get(i).getCardNum();
		}

		return this.isHu(cardsTemp);
	}


}
