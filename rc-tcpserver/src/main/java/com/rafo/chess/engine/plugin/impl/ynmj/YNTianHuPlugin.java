package com.rafo.chess.engine.plugin.impl.ynmj;

import com.rafo.chess.engine.action.IEPlayerAction;

/***
 * 天胡,
 * @author Administrator
 * 
 */
public class YNTianHuPlugin extends YNPayPlugin {

	@Override
	public boolean analysis(IEPlayerAction action) {
		if(action.getRoomInstance().getEngine().getOutCardPool().size() == 0){
			return true;
		}

		return false;
	}
}
