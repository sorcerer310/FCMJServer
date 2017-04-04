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
public class YNHuQingDaDuiPlugin extends YNHuPlugin {

	public boolean checkHu(MJPlayer player, ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList) {
		if (!this.oneCorlor(handCards, groupList))
			return false;

		int size = handCards.size();
		int[] handcardsTemp = new int[size];

		for (int i = 0; i < handCards.size(); i++) {
			handcardsTemp[i] = handCards.get(i).getCardNum();
		}
		// 手牌没有顺子
		HashMap<Integer, Integer> map = arrayHandsCardCount(handcardsTemp);
		int duiCount = 0;
		for (int count : map.values()) {
			if (count == 1) {
				return false;
			}
			if (count == 2) {
				duiCount++;
			}
			if (count == 4) {
				return false;
			}
		}
		if (duiCount != 1)
			return false;
		// 亮牌没有顺子
		for (CardGroup cg : groupList) {
			if(cg.getCardsList().get(0).getCardNum()!=cg.getCardsList().get(1).getCardNum())
				return false;
		}
		return true;
	}
}
