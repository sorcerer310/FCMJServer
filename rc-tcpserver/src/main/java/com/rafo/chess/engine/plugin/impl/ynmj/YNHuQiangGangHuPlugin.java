package com.rafo.chess.engine.plugin.impl.ynmj;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.game.YBMJGameType;
import com.rafo.chess.engine.game.YNMJGameType;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.room.RoomInstance;

import java.util.ArrayList;

/***
 * 杠后炮
 * 
 * @author Administrator
 * 
 */
public class YNHuQiangGangHuPlugin extends AbstractPlayerPlugin<HuAction> {
	@Override
	public void doOperation(HuAction action) {
		if (analysis(action)) 
		{
			// 找到杠牌产生的结果对象
			ArrayList<PayDetailed> payList = action.getRoomInstance().getEngine().getCalculator().getPayDetailList();
			IEPlayerAction last1 = action.getRoomInstance().getEngine().getMediator().getDoneActionByStep(action.getStep() - 2);

			// debug
			ArrayList<PayDetailed> payList2 = action.getRoomInstance().getEngine().getCalculator().getPayDetailList();
			ArrayList<IEPlayerAction> last2 = action.getRoomInstance().getEngine().getMediator().getDoneActionList();
			int a = 1;
			
			//PayDetailed pay = this.payment(action);
			//pay.setPayType(PayDetailed.PayType.Multiple);

			for (PayDetailed pd : payList) {
				if(last1.getStep() == pd.getStep()){ //杠分数失效
					pd.setValid(false);
				}
			}
		}

	}

	public boolean analysis(HuAction action) {
		int step = action.getStep();
		IEPlayerAction last1 = action.getRoomInstance().getEngine().getMediator().getDoneActionByStep(step - 1);
		IEPlayerAction last2 = action.getRoomInstance().getEngine().getMediator().getDoneActionByStep(step - 2);
		if (last1 == null || last2 == null)
			return false;

		// 判断上一步是杠,并且是补杠
		if (last2.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG
				&& last2.getSubType() == YNMJGameType.PlayType.Kong) {
			return true;
		}
		
		return false;
	}
}
