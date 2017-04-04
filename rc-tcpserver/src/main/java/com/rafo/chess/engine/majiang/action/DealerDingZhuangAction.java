package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.room.RoomInstance;

/***
 * 给所有玩家发牌
 * @author Administrator
 */
public class DealerDingZhuangAction extends BaseMajongDealerAction{

	public DealerDingZhuangAction(RoomInstance roomIns) {
		super(roomIns);
	}

	@Override
	public int getActionType() {
		return IEMajongAction.ROOM_GAME_START_BANKER;
	}


}
