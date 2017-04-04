package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.DaAction;
import com.rafo.chess.engine.plugin.impl.DaPlugin;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.engine.room.RoomManager;

/***
 * 打牌
 * 
 * @author Administrator
 * 
 */
public class YXDaPlugin extends DaPlugin {
    @Override
    public void doOperation(DaAction action) {
        super.doOperation(action);
        RoomInstance room = action.getRoomInstance();
        MJPlayer player = (MJPlayer) room.getPlayerById(action.getPlayerUid());

        //打牌时判断，如果玩家有杠上开花状态，重置该状态
        if(player.isGangShangKaiHuaFlag())
            player.setGangShangKaiHuaFlag(false);
    }
}
