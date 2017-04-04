package com.rafo.hall.handlers;

import com.rafo.chess.common.model.record.BWRoundRecordRES;
import com.rafo.chess.common.service.record.RecordService;
import com.rafo.hall.utils.CmdsUtils;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

/**
 * Created by Administrator on 2016/9/23.
 */
public class RoundRecordHandler extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject) {
        Integer recordID = isfsObject.getInt("recordID");
        Integer roomID = isfsObject.getInt("roomID");

        BWRoundRecordRES res = RecordService.getRoundRecords(roomID, recordID, Integer.parseInt(user.getName()));
        send(CmdsUtils.CMD_ROUNDRECORD , res.toSFSObject() , user);
    }
}
