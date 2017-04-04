package com.rafo.chess.engine.plugin.impl.ynmj;

import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.engine.room.RoomInstance.RoomState;

/***
 * 海底捞
 * 
 * @author Administrator
 * 
 */
public class YNHaiDiLaoPlugin extends AbstractPlayerPlugin<HuAction> {

	@Override
	public void doOperation(HuAction action) {
		if (action.getRoomInstance().getRoomStatus() == RoomState.calculated.getValue()
				&& action.getRoomInstance().getEngine().getCardPool().size() <= 1) {
			PayDetailed pay = this.payment(action);
			if(pay != null){
				pay.setPayType(PayDetailed.PayType.Multiple);
			}
		}
	}

	@Override
	public boolean doPayDetail(PayDetailed pd, RoomInstance room, Calculator calculator) {
		if(pd.getFromUid().length > 1) {
			calculator.addCardBalance(pd.getToUid(), this.getGen().getZimoFlag(), pd.getCards(), 0);
		}
		return true;
	}
}
