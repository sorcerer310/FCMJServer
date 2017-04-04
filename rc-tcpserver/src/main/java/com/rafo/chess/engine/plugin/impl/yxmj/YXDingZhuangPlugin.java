package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.DealerDingZhuangAction;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.template.impl.PluginTemplateGen;

/***
 * 永修定庄
 * 下一个人接庄
 * @author Administrator
 * 
 */
public class YXDingZhuangPlugin implements IOptPlugin<DealerDingZhuangAction> {
	PluginTemplateGen gen = null;

	@Override
	public void doOperation(DealerDingZhuangAction act) {
		RoomInstance<MJCard> room = act.getRoomInstance();
//		room.addRound(); //局数加一

		//黄庄，不换庄家
//		if( room.getLastWinner().size() ==0 && room.getCurrRounds()>1 ){
//			return;
//		}

		MJPlayer banker = (MJPlayer) room.getPlayerById(room.getBankerUid());
		if (banker == null) {
			banker = (MJPlayer) room.getPlayerArr()[0];
			room.addRound(); //局数加一
		}
		//庄家输了 或者 黄庄，才换庄
		else if(room.getLastWinner().size() > 0 && room.getLastWinner().get(0) != banker.getUid()
				|| room.getLastWinner().size()==0 ) {
			int nextIndex = banker.getIndex() + 1;
			nextIndex = nextIndex > 3 ? nextIndex - 4 : nextIndex;
			banker = (MJPlayer) room.getPlayerArr()[nextIndex];
			room.addRound(); //局数加一
		}
		banker.setBanker(true);

		//获得上一句坐庄的玩家，把该玩家的坐庄标志设为false
		if(room.getBankerUid() > 0 && room.getBankerUid() != banker.getUid()){ //reset is banker
			MJPlayer lastBanker = (MJPlayer) room.getPlayerById(room.getBankerUid());
			if(lastBanker!= null){
				lastBanker.setBanker(false);
			}
		}
		room.setBankerUid(banker.getUid());
	}

	@Override
	public PluginTemplateGen getGen() {
		return gen;
	}

	@Override
	public void setGen(PluginTemplateGen gen) {
		this.gen = gen;
	}

	@Override
	public boolean doPayDetail(PayDetailed pd, RoomInstance room, Calculator calculator) {
		return false;
	}

}
