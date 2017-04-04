package com.rafo.chess.engine.plugin.impl.ynmj;

import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;

import java.util.ArrayList;
import java.util.HashMap;

/***
 * 豪七对
 * 
 * @author Administrator
 */
public class YNHuLongQiDuiPlugin extends YNHuPlugin {

	public boolean checkHu(MJPlayer player, ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList) {
		if(groupList.size()>0)
			return false;
		int size = handCards.size();
		int[] handcardsTemp = new int[size];

		for (int i = 0; i < handCards.size(); i++) {
			handcardsTemp[i] = handCards.get(i).getCardNum();
		}
		// 龙七对都是手牌
		if(groupList.size()>0)
			return false;
		HashMap<Integer, Integer> map = arrayHandsCardCount(handcardsTemp);
		boolean hasFour = false;
		for (int i : map.values()) {
			if (i % 2 != 0) {
				return false;
			}
			if (i == 4)
				hasFour = true;
		}
		return hasFour;
	}
}
