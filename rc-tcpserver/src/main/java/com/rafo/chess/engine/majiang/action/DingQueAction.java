package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.room.RoomInstance;

/**
 * Created by Administrator on 2016/11/28.
 */
public class DingQueAction extends BaseMajongPlayerAction {

    public DingQueAction(RoomInstance<MJCard> roomIns) {
        super(roomIns);
    }

    @Override
    public int getActionType() {
        return IEMajongAction.ROOM_MATCH_QUE;
    }

    @Override
    public int getPriority() {
        return IEMajongAction.PRIORITY_COMMON;
    }

    @Override
    public boolean checkMySelf(int actionType, int card, int playerUid, int subType, String toBeCards) {

        if(actionType != this.getActionType())
            return false;
        if(playerUid != this.playerUid)
            return false;

        if(this.card==0 && card>0){
            this.card = card;
        }
        return true;
    }

}