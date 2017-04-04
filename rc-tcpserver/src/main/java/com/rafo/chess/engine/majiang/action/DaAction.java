package com.rafo.chess.engine.majiang.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import com.rafo.chess.engine.room.RoomInstance;

/***
 * 摸
 * 
 * @author Administrator
 * 
 */
public class DaAction extends BaseMajongPlayerAction {

	public DaAction(RoomInstance<MJCard> roomIns) {
		super(roomIns);
	}

	public void doAction() throws ActionRuntimeException {
		MJPlayer p = (MJPlayer) roomIns.getPlayerById(this.playerUid);
		// 停牌状态，不能打状态为1的牌
		if (p.isKouTing()) {
			boolean daCard = false;
			ArrayList<MJCard> list = p.getHandCards().getHandCards();
			for (MJCard c : list) {
				if (c.getCardNum() == this.card) {
					if (c.getStatus() == 0) {
						daCard = true;
					}
				}
			}
			if (!daCard) {
				throw new ActionRuntimeException(
						"card is not able to execute...[ actionType = "
								+ this.getActionType() + ",uid="
								+ this.getPlayerUid()+"]", this.getActionType(),
						this.getPlayerUid());
			}
		}
		super.doAction();
	}

	@Override
	public int getActionType() {
		return IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	public boolean checkMySelf(int actionType, int card, int playerUid,
							   int subType, String toBeCards){
		if(actionType != this.getActionType() || this.playerUid != playerUid || card <= 0){
			return false;
		}

		//手牌里需要包含打的这张牌
		MJPlayer player = (MJPlayer)roomIns.getPlayerById(playerUid);
		ArrayList<MJCard> handCards = player.getHandCards().getHandCards();
		boolean containCard = false;
		for(MJCard c : handCards){
			if(c.getCardNum() == card){
				containCard = true;
				break;
			}
		}

		if(!containCard){
			return false;
		}

		//如果有缺牌，不能打非缺那一门的牌

		if (ActionManager.checkQueCardStatus(playerUid, roomIns) && ActionManager.isValidCard(player, this)) {
			return false;
		}

		this.setCard(card);
		return true;
	}
}
