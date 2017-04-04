package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.game.YNMJGameType;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.room.RoomInstance;

import java.util.ArrayList;
import java.util.HashMap;

/***
 * 对对和,可以开门碰牌，全部为碰或刻子，最后手中剩两对儿和牌
 * @author Administrator
 */
public class YXHuDuiDuiHuPlugin extends YXHuPlugin {

	@Override
	public boolean checkHu(MJPlayer player, ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList) {
		//转换牌数据为数组
		int[] handcardsTemp = this.list2intArray(handCards);

		// 手牌没有顺子
		HashMap<Integer, Integer> map = this.arrayHandsCardCount(handcardsTemp);
		int duiCount = 0;
		for (int count : map.values()) {
			if (count == 1) return false;
			if (count == 2) duiCount++;
			if (count == 4) return false;
		}
		if (duiCount != 1)
			return false;
		//开门的牌不为碰返回false
		for(CardGroup cg:groupList){
			ArrayList<MJCard> amjc = cg.getCardsList();
			//判断如果开门牌不为碰牌，返回false
			if(amjc.get(0).getCardNum()!=amjc.get(1).getCardNum())
				return false;
		}

		//附加属性清空
		player.getHuAttachType().clear();
		//清一色属性
		if(this.oneCorlor(handCards,groupList))
			player.getHuAttachType().add(YNMJGameType.HuAttachType.QingYiSe);

		//门前清一定为自摸
		if(this.isMenQianQing(handCards,groupList,player)) {
			player.getHuAttachType().add(YNMJGameType.HuAttachType.MenQianQing);
			player.getHuAttachType().add(YNMJGameType.HuAttachType.ZiMo);
		}

		//自摸项单独加
		if(this.isZiMo(handCards,player))
			player.getHuAttachType().add(YNMJGameType.HuAttachType.ZiMo);

		//全求人
		if(this.isQuanQiuRen(handCards,groupList,player))
			player.getHuAttachType().add(YNMJGameType.HuAttachType.QuanQiuRen);

		return true;
	}

	@Override
	public boolean doPayDetail(PayDetailed pd, RoomInstance room, Calculator calculator) {
		return super.doPayDetail(pd, room, calculator);
	}
}
