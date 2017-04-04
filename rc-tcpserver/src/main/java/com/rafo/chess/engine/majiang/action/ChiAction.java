package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.room.RoomInstance;

public class ChiAction extends BaseMajongPlayerAction {

	public ChiAction(RoomInstance<MJCard> roomIns) {
		super(roomIns);
	}

	@Override
	public int getActionType() {
		return IEMajongAction.PLAYER_ACTION_TYPE_CARD_CHI;
	}

	@Override
	public int getPriority() {
		return IEMajongAction.PRIORITY_CHI;
	}

}
