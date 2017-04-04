package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.action.BaseAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.room.RoomInstance;


public abstract class BaseMajongPlayerAction extends BaseAction<MJCard> {
	public BaseMajongPlayerAction(RoomInstance<MJCard> roomIns) {
		super(roomIns);
	}

	public BaseMajongPlayerAction(RoomInstance<MJCard> roomIns, int playerUid, int fromUid, int card) {
		super(roomIns);
		this.playerUid = playerUid;
		this.fromUid = fromUid;
		this.card = card;
	}

	public BaseMajongPlayerAction(RoomInstance<MJCard> roomIns, int playerUid, int fromUid, int card, int subType) {
		super(roomIns);
		this.playerUid = playerUid;
		this.fromUid = fromUid;
		this.card = card;
		this.subType = subType;
	}

	public BaseMajongPlayerAction(RoomInstance<MJCard> roomIns, int playerUid, int fromUid, int card, int subType, String toBeCards) {
		super(roomIns);
		this.playerUid = playerUid;
		this.fromUid = fromUid;
		this.card = card;
		this.subType = subType;
		this.toBeCards = toBeCards;
	}

	public void doAction() throws ActionRuntimeException {
		super.doAction();
	}
	
	public boolean checkMySelf(int actionType, int card, int playerUid,
			int subType, String toBeCards){
		if (this.getActionType() != actionType)
			return false;
		if (this.getPlayerUid() != playerUid)
			return false;
		if (this.getSubType() != subType)
			return false;
		if(!this.toBeCards.equals(toBeCards))
			return false;
		if(this.card !=card)
			return false;
		return true;
	}
	
}
