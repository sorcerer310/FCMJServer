package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.game.YNMJGameType;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;

import java.util.ArrayList;
import java.util.HashMap;

/***
 * 七对子
 * @author Administrator
 */
public class YXHuQiDuiPlugin extends YXHuPlugin {

	@Override
	public boolean checkHu(MJPlayer player, ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList) {
		//1:如果开门返回失败
		if(groupList.size()>0)
			return false;

		int[] handcardsTemp = this.list2intArray(handCards);

		// 七对都是手牌
		HashMap<Integer, Integer> map = arrayHandsCardCount(handcardsTemp);
		for (int i : map.values()) {
			if (i % 2 != 0) {
				return false;
			}
		}

		player.getHuAttachType().clear();
		//清一色
		if(this.oneCorlor(handCards,groupList))
			player.getHuAttachType().add(YNMJGameType.HuAttachType.QingYiSe);
		//门前清


		if(this.isQiDuiMenQianQing(handCards,groupList,player)) {
			player.getHuAttachType().add(YNMJGameType.HuAttachType.MenQianQing);
			player.getHuAttachType().add(YNMJGameType.HuAttachType.ZiMo);
		}
		return true;
	}

	public boolean isQiDuiMenQianQing(ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList, MJPlayer player) {
		//如果不是自摸，不算门前清
		if (!isZiMo(handCards, player))
			return false;
		//开门不算门前清
		if (player.isOpen())
			return false;

		return true;
	}
}
