package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.GangAction;
import com.rafo.chess.engine.plugin.impl.GangPlugin;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.engine.room.RoomManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/***
 * 永修暗杠
 * @author Administrator
 * 
 */
public class YXGangAnGangPlugin extends GangPlugin {

	@Override
	public boolean doPayDetail(PayDetailed pd, RoomInstance room, Calculator calculator) {
		if(super.doPayDetail(pd, room, calculator)){
			//抢杠胡不算杠,李志刚
//			if ( !room.IsBankerQiang() )
//			{
//				calculator.getBattleCensuss().get(pd.getToUid()).addCealedKong();
//			}
			return true;
		}

		return false;
	}

	@Override
	public boolean checkExecute(Object... objects) {
		MJPlayer pTemp = (MJPlayer) objects[0];
		IEPlayerAction act = (IEPlayerAction) objects[1];

		//不限制最后一圈不让杠
//		if(act.getRoomInstance().getEngine().getCardPool().size() <= 4){
//			return false;
//		}

		int gangHouDrawCard = findGangHouDrawCard(pTemp, act);
		HashMap<Integer, Integer> map = pTemp.getHandCards().getCardCountFromHands();

		for (int num : map.keySet()) {
			int count = map.get(num);
			if (count == 4 && pTemp.getUid() == act.getPlayerUid()) {
				List<Integer> cards = new ArrayList<>();
				for(int i=0;i<count;i++){
					cards.add(num);
				}

				GangAction gangAct = new GangAction(act.getRoomInstance(), pTemp.getUid(), act.getPlayerUid(),
						num, gen.getSubType());
				gangAct.setCanDoType(gen.getCanDoType());
				if(gangHouDrawCard > 0 && cards.contains(gangHouDrawCard)) {
					gangAct.setGanghou(true);
				}
				RoomManager.getRoomInstnaceByRoomid(pTemp.getRoomId()).addCanExecuteAction(gangAct);
			}
		}
		return true;
	}
}
