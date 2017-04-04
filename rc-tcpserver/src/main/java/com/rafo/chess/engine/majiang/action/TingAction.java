package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.room.RoomInstance;

import java.util.Map;

public class TingAction extends BaseMajongPlayerAction {

	//记录每个牌的胡牌类型，Ting之后不用再去判断一次胡牌类型
	private Map<Integer,Integer> cardHuType;

	public TingAction(RoomInstance<MJCard> roomIns) {
		super(roomIns);
	}

	@Override
	public int getActionType() {
		return IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING;
	}

	public void doAction() throws ActionRuntimeException {
		MJPlayer player = (MJPlayer) roomIns.getPlayerById(this.playerUid);
		if (player.isKouTing()) {
			throw new ActionRuntimeException(" has been tingState ...[ actionType = "
								+ this.getActionType() + ",uid="
								+ this.getPlayerUid()+"]",
					this.getActionType(), this.getPlayerUid());
		}
		super.doAction();
	}


	@Override
	public int getPriority() {
		return IEMajongAction.PRIORITY_TING;
	}
	public boolean checkMySelf(int actionType, int card, int playerUid,
			int subType, String toBeCards){
		if (this.getActionType() != actionType)
			return false;
		if (this.getPlayerUid() != playerUid)
			return false;

		if (actionType == IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING) {
			if(cardHuType!=null){
				if(card<=0 || !cardHuType.containsKey(card)){
					return false;
				}
			}else if(card > 0){
				return false;
			}
		} else if (!this.getToBeCards().equals(toBeCards)) {
			return false;
		}
		if (actionType != IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT
				&& actionType != IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING) { // 打牌听牌不判断action里的牌
			if (this.getCard() != card)
				return false;
		} else {
			this.setCard(card);
		}
		return true;
	}

	public Map<Integer, Integer> getCardHuType() {
		return cardHuType;
	}

	public void setCardHuType(Map<Integer, Integer> cardHuType) {
		this.cardHuType = cardHuType;
	}
}
