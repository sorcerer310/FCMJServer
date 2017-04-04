package com.rafo.hall.handlers;

import com.rafo.chess.common.model.record.BWRoomRecordRES;
import com.rafo.chess.common.service.record.RecordService;
import com.rafo.hall.utils.CmdsUtils;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

/**
 * Created by Administrator on 2016/9/22.
 */
public class RoomRecordHandler extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject) {
        BWRoomRecordRES res = RecordService.getRoomRecord(Integer.parseInt(user.getName()));

        getParentExtension().send(CmdsUtils.CMD_ROOMRECORD , res.toSFSObject() , user);
    }
}
