package com.rafo.chess.engine.majiang;

import com.rafo.chess.engine.action.AbstractActionMediator;
import com.rafo.chess.engine.majiang.action.*;
import com.rafo.chess.engine.room.RoomInstance;

public class MahjongActionMediator extends AbstractActionMediator {

	public MahjongActionMediator(RoomInstance roomIns) {
		super(roomIns);
	}

	@Override
	public void registerAction() {
		actionMapper.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN, MoAction.class);
		actionMapper.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT, DaAction.class);
		actionMapper.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_CHI, ChiAction.class);
		actionMapper.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_PENG, PengAction.class);
		actionMapper.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG, GangAction.class);
		actionMapper.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU, HuAction.class);
		actionMapper.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING, TingAction.class);
//		actionMapper.put(IEMajongAction.ROOM_MATCH_QUE, DingQueAction.class);
	}

	@Override
	public int getActionType() {
		return -1;
	}
}
