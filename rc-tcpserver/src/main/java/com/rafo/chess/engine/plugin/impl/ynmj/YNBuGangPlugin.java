package com.rafo.chess.engine.plugin.impl.ynmj;

import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.plugin.impl.BuGangPlugin;
import com.rafo.chess.engine.room.RoomInstance;

/***
 * 补杠
 * 
 * @author Administrator
 * 
 */
public class YNBuGangPlugin extends BuGangPlugin{


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
