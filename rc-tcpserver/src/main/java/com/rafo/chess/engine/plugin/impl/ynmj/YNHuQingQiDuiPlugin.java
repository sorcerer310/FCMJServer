package com.rafo.chess.engine.plugin.impl.ynmj;

import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;

import java.util.ArrayList;
import java.util.HashMap;

/***
 * 抓到指定牌触发执行
 * 
 * @author Administrator
 */
public class YNHuQingQiDuiPlugin extends YNHuPlugin {

	public boolean checkHu(MJPlayer player, ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList) {
		if(groupList.size()>0)
			return false;
		if (!this.oneCorlor(handCards, groupList))
			return false;
		if (groupList.size() == 0)
			return false;
		int size = handCards.size();
		int[] handcardsTemp = new int[size];
		for (int i = 0; i < handCards.size(); i++) {
			handcardsTemp[i] = handCards.get(i).getCardNum();
		}
		// 七对都是手牌
		HashMap<Integer, Integer> map = arrayHandsCardCount(handcardsTemp);
		for (int i : map.values()) {
			if (i % 2 != 0) {
				return false;
			}
		}
		return true;
	}

}
