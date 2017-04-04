package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.action.BaseAction;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.room.RoomInstance;

/**
 * Created by Administrator on 2016/10/19.
 */
public class DefaultAction extends BaseMajongPlayerAction {

    public DefaultAction(RoomInstance<MJCard> roomIns) {
        super(roomIns);
        this.status = 1;
    }

    private int actionType = 0;

    @Override
    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType){
        this.actionType = actionType;
    }

    @Override
    public int getPriority() {
        return IEMajongAction.PRIORITY_COMMON;
    }

    @Override
    public boolean checkMySelf(int actionType, int card, int playerUid, int subType, String toBeCards) {

        if(actionType!=this.actionType)
            return false;
        if(playerUid!=this.playerUid)
            return false;

        if(this.card==0&&card>0){
            this.card = card;
        }
        return true;
    }

}
