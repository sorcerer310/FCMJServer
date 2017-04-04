package com.rafo.chess.engine.plugin.impl.ynmj;

import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.plugin.impl.PengPlugin;
import com.rafo.chess.engine.room.RoomInstance;

/***
 * 发牌
 * @author Administrator
 * 
 */
public class YNPengPlugin extends PengPlugin {

	@Override
	public boolean doPayDetail(PayDetailed pd, RoomInstance room, Calculator calculator) {
		if(!pd.isValid()){
			return false;
		}
		calculator.addCardBalance(pd.getToUid(), 0, this.getGen().getZimoFlag(), 0, pd); //用于牌行显示
		return true;
	}
}
