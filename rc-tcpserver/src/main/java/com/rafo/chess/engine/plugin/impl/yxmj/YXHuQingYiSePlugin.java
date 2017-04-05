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

		//永修规则，门前清一定要自摸
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
			super.computeScoreYX(pd,room,calculator);
		}
		return false;
	}


}