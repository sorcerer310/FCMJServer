package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.room.RoomInstance;


public class PengAction extends BaseMajongPlayerAction {
	private boolean isGanghou; //是否是杠后，延边补杠时需要判断

	public PengAction(RoomInstance<MJCard> roomIns) {
		super(roomIns);
	}

	@Override
	public int getActionType() {
		return IEMajongAction.PLAYER_ACTION_TYPE_CARD_PENG;
	}

	@Override
	public int getPriority() {
		return IEMajongAction.PRIORITY_PENG;
	}

	public boolean isGanghou() {
		return isGanghou;
	}

	public void setGanghou(boolean ganghou) {
		isGanghou = ganghou;
	}
}
