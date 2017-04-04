package com.rafo.chess.engine.plugin.impl.ynmj;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.majiang.MJPlayer;

/***
 * 单钓
 * 手里还剩一张牌
 * @author Administrator
 * 
 */
public class YNDanDiaoPlugin extends YNPayPlugin{

	@Override
	public boolean analysis(IEPlayerAction action) {
		MJPlayer player = (MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());

		if(player.getHandCards().getHandCards().size() == 2){
			return true;
		}
		return false;
	}
}
