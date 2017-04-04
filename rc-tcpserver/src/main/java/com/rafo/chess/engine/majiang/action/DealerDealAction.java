package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.room.RoomInstance;

/***
 * 给所有玩家发牌
 * 
 * @author Administrator
 */
public class DealerDealAction extends BaseMajongPlayerAction {

	public DealerDealAction(RoomInstance roomIns) {
		super(roomIns);
	}

	public void doAction() throws ActionRuntimeException {
		super.doAction();
	}

	@Override
	public int getActionType() {
		return IEMajongAction.ROOM_MATCH_DEAL;
	}

	@Override
	public int getPriority() {
		return 0;
	}
}
