package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.room.RoomInstance;

public class GangAction extends BaseMajongPlayerAction{

	private boolean isGanghou; //是否是杠后，用于前端特殊展示

	public GangAction(RoomInstance<MJCard> roomIns) {
		super(roomIns);
	}

	public GangAction(RoomInstance roomInstance, int uid, int playerUid, int card, int subType) {
		super(roomInstance, uid, playerUid, card, subType);
	}

	public void doAction() throws ActionRuntimeException{
		super.doAction();
	}
	
	@Override
	public int getActionType() {
		return IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG;
	}
	
	@Override
	public int getPriority() {
		return IEMajongAction.PRIORITY_GANG;
	}

	public boolean isGanghou() {
		return isGanghou;
	}

	public void setGanghou(boolean ganghou) {
		isGanghou = ganghou;
	}

	public boolean checkMySelf(int actionType, int card, int playerUid,
							   int subType, String toBeCards){
		if (this.getActionType() != actionType)
			return false;
		if (this.getPlayerUid() != playerUid)
			return false;
		if (this.getSubType() != subType)
			return false;
		if(this.card !=card)
			return false;
		return true;
	}
	
}
