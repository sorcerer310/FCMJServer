package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.GangAction;
import com.rafo.chess.engine.plugin.impl.BuGangPlugin;
import com.rafo.chess.engine.room.RoomInstance;

/***
 * 永修补杠
 * @author Administrator
 * 
 */
public class YXGangBuGangPlugin extends BuGangPlugin{

	@Override
	public void doOperation(GangAction action) throws ActionRuntimeException {
		super.doOperation(action);

	}

	@Override
	public boolean doPayDetail(PayDetailed pd, RoomInstance room, Calculator calculator) {
		if (!pd.isValid() || pd.getFromUid() == null || pd.getFromUid().length !=1) {
			return false;
		}

		int rate = pd.getRate();
		calculator.addCardBalance(pd.getToUid(), pd.getFromUid()[0], this.getGen().getZimoFlag(), rate, pd);

		calculator.getBattleCensuss().get(pd.getToUid()).addKong();
		return true;
	}
}
