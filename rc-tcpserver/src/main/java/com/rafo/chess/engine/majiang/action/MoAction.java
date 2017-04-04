package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.room.RoomInstance;

/***
 * 抓拍
 * 
 * @author Administrator
 * 
 */
public class MoAction extends BaseMajongPlayerAction {
	public MoAction(RoomInstance<MJCard> roomIns) {
		super(roomIns);
		this.autoRun = true;
	}

	public void doAction() throws ActionRuntimeException {
		if (roomIns.getRoomStatus() == RoomInstance.RoomState.gameing
				.getValue()) {
			DealerLiujuAction liujuAction = new DealerLiujuAction(roomIns);
			liujuAction.setPlayerUid(this.getPlayerUid());
			liujuAction.setStep(this.getStep());
			liujuAction.doAction();
		}
		super.doAction();
	}

	@Override
	public int getActionType() {
		return IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN;
	}

	@Override
	public int getPriority() {
		return IEMajongAction.PRIORITY_COMMON;
	}

}
