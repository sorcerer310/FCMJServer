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

		if(this.oneCorlor(handCards,groupList))
			player.getHuAttachType().add(YNMJGameType.HuAttachType.QingYiSe);

		if(this.isMenQianQing(handCards,groupList,player)) {
			player.getHuAttachType().add(YNMJGameType.HuAttachType.MenQianQing);
			player.getHuAttachType().add(YNMJGameType.HuAttachType.ZiMo);
		}
		return true;
	}
}
