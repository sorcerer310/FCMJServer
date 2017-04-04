package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.game.YNMJGameType;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.DaAction;
import com.rafo.chess.engine.majiang.action.PengAction;
import com.rafo.chess.engine.plugin.impl.PengPlugin;
import com.rafo.chess.engine.room.RoomInstance;

import java.util.ArrayList;

/***
 * 永修碰插件
 * @author Administrator
 * 
 */
public class YXPengPlugin extends PengPlugin {



	@Override
	public boolean doPayDetail(PayDetailed pd, RoomInstance room, Calculator calculator) {
		if(!pd.isValid()){
			return false;
		}
		calculator.addCardBalance(pd.getToUid(), 0, this.getGen().getZimoFlag(), 0, pd); //用于牌行显示
		return true;
	}

	@Override
	public boolean checkExecute(Object... objects) {
		//如果吃了一种颜色不能碰另一种颜色
		MJPlayer player = (MJPlayer)objects[0];
		DaAction act = (DaAction) objects[1];
		ArrayList<CardGroup> groupList = player.getHandCards().getOpencards();
		if(groupList.size()>0)
		{
			for(CardGroup c:groupList)
			{
				if(c.getCardsList().get(0).getCardNum()<40&&c.getCardsList().get(0).getCardNum()/10!=act.getCard()/10&&c.getGType()== YNMJGameType.PlayType.Chi)
					return false;
			}
		}

		return super.checkExecute(objects);
	}

	@Override
	public void doOperation(PengAction action) throws ActionRuntimeException {
		super.doOperation(action);

		MJPlayer player = (MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());
		//判断手里是否剩一张牌，如果剩一张，则进入全求人包三家的一圈
		if(player.getHandCards().getOpencards().size()==4){
			//继续判断手中的两张牌是否为同花色，并且间隔小于等于2
			ArrayList<MJCard> hands = player.getHandCards().getHandCards();
			if(hands.size()!=2) return;
			if(hands.get(0).getCardNum()/10 == hands.get(1).getCardNum()/10 && Math.abs(hands.get(0).getCardNum()-hands.get(1).getCardNum())<=2)
				player.setQuanQiuRenChargeAll(true);
		}
	}
}
