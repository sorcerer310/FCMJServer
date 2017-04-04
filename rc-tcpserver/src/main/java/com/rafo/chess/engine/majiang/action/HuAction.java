package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.room.RoomInstance;

public class HuAction extends BaseMajongPlayerAction {

	public HuAction(RoomInstance<MJCard> roomIns) {
		super(roomIns);
	}

	public void doAction()throws ActionRuntimeException {
		super.doAction();
	}

	@Override
	public int getActionType() {
		return IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU;
	}

	@Override
	public int getPriority() {
		return IEMajongAction.PRIORITY_HU;
	}

	public boolean checkMySelf(int actionType, int card, int playerUid,
							   int subType, String toBeCards){
		if(actionType == this.getActionType() && card == this.getCard() && playerUid == this.getPlayerUid()){
			return true;
		}
		return false;
	}
	
	boolean bQiang = false;
	int nQiangGangOwner = 0;
	int nQiangGangBei = 0;		//被抢杠的人
	// 抢杠胡
	public boolean isQiang()
	{
		return bQiang;
	}
	
	public void setQiang(boolean bQ)
	{
		bQiang = bQ;
	}
	
	public void setQiangGangOwner(int uId)
	{
		nQiangGangOwner = uId;
	}
	
	public void setQiangGangBei(int uId)		//被抢杠的人
	{
		nQiangGangBei = uId;
	}
	
	public int getQiangGangOwner()
	{
		return nQiangGangOwner;
	}
	
	
}
