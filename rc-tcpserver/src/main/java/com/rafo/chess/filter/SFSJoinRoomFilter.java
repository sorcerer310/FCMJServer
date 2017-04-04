package com.rafo.chess.filter;

import com.rafo.chess.common.GlobalConstants;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.exception.PersistException;
import com.rafo.chess.service.LoginService;
import com.rafo.chess.utils.CmdsUtils;
import com.smartfoxserver.v2.controllers.filter.SysControllerFilter;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.filter.FilterAction;

/**
 * Created by Administrator on 2016/9/22.
 */
public class SFSJoinRoomFilter extends SysControllerFilter {
    @Override
    public FilterAction handleClientRequest(User user, ISFSObject params) throws SFSException {

        String roomId = params.getUtfString("n");
        Room room = null ;
        if (roomId != null && roomId.trim().length() > 0) {
            String roomIdStr = String.format("%06d", Integer.parseInt(roomId));
            room = user.getZone().getRoomByName(roomIdStr);
            params.putUtfString("n",roomIdStr);
        }

        if(room == null){ //TODO: 是否有异常情况下，用户已经在房间了，又重复加入房间？
            try {
                LoginService.updateUserAttribute(Integer.parseInt(user.getName()), "room", "0");
            } catch (PersistException e) {
                e.printStackTrace();
            }

            SFSObject data = new SFSObject();
            data.putInt("result", GlobalConstants.ROOM_ENTER_FAILED_NUMBER_ERROR);
            user.getZone().getExtension().send("joinroom" , data, user);
            return FilterAction.HALT;
        }

        if(room.getSize().getUserCount()==4){
            SFSObject data = new SFSObject();
            data.putInt("result", GlobalConstants.ROOM_ENTER_FAILED_ROOM_FULL);
            user.getZone().getExtension().send(CmdsUtils.CMD_JOINROOM, data, user);
        }

        return FilterAction.CONTINUE;
    }
}