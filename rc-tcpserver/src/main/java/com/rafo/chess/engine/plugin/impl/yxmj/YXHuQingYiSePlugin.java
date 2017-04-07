package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.game.YNMJGameType;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.engine.room.RoomInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/***
 * 清一色要求和别的和法同用
 * 清一色包三家规则:
 * 1: 赢家至少3个开门,且有同一玩家至少供3个开门 		自摸:供了3个开门的玩家包三家	点炮:供了3个开门的玩家包三家
 * 2: 赢家4个开门,且没有同一玩家至少供3个开门			自摸:供第2个开门的玩家包三家	点炮:供了第4个开门的玩家包三家
 * 3: 赢家3个开门,且没有同一玩家至少供3个开门			自摸:与普通自摸一样结算			点炮:由点炮的玩家包三家
 * 4: 赢家3个以下开门								自摸:与普通自摸一样结算			点炮:与普通点炮一样结算
 * @author fc
 */
public class YXHuQingYiSePlugin extends YXHuPlugin {
	@Override
	public boolean checkHu(MJPlayer player, ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList) {
		//判断是否为清一色
		if (!this.oneCorlor(handCards, groupList))
			return false;
		//转换牌型
		int[] cardsTemp = this.list2intArray(handCards);
		//判断是否和牌
		if(!this.isHu(cardsTemp))
			return false;

		player.getHuAttachType().clear();

		//门前清一定要自摸
		if(this.isMenQianQing(handCards,groupList,player)) {
			player.getHuAttachType().add(YNMJGameType.HuAttachType.MenQianQing);
			player.getHuAttachType().add(YNMJGameType.HuAttachType.ZiMo);
		}

		//自摸单独算
		if(this.isZiMo(handCards,player) && !player.getHuAttachType().contains(YNMJGameType.HuAttachType.ZiMo)){
			player.getHuAttachType().add(YNMJGameType.HuAttachType.ZiMo);
		}

		//增加全求人,如果收里有暗杠，不算全求人
		if(this.isQuanQiuRen(handCards,groupList,player)){
			player.getHuAttachType().add(YNMJGameType.HuAttachType.QuanQiuRen);
		}

		//对对胡
		if(this.isDuiDuiHu(handCards,groupList))
			player.getHuAttachType().add(YNMJGameType.HuAttachType.DuiDuiHu);

		//一条龙
		if(this.isYiTiaoLong(handCards,groupList))
			player.getHuAttachType().add(YNMJGameType.HuAttachType.YiTiaoLong);

		return true;
	}

	@Override
	public boolean doPayDetail(PayDetailed pd, RoomInstance room, Calculator calculator) {

		//结算细节对象无效并结算对象为null返回false
		if (!pd.isValid() && pd.getFromUid() != null) {
			return false;
		}

		MJPlayer player = (MJPlayer) room.getPlayerById(pd.getToUid());
		//清一色包三家判断
		//如果isChargeAll返回的输家id不为0，并且胡牌为点炮，走包三家结算，否则走父类普通结算
		boolean isZiMo = player.getHuAttachType().contains(YNMJGameType.HuAttachType.ZiMo);								//玩家是否自摸
		int chargepid =this.isQingYiSeChargeAll(player,pd,isZiMo);														//判定由哪个玩家包三家，如果chargepid为0则正常胡
		if(chargepid > 0){
			pd.setFromUid(new int[]{chargepid});
			computeScoreYXChargeAll(pd,room,calculator);
		} else{
			//此处要执行父类的doPayDetail，因为该函数中包括对抢杠包三家的判断
			super.doPayDetail(pd,room,calculator);
		}
		return false;
	}
}