package com.rafo.hall.handlers;

import com.rafo.chess.common.db.RedisManager;
import com.rafo.hall.service.ActiveService;
import com.rafo.hall.utils.CmdsUtils;
import com.rafo.hall.vo.ActiveListVO;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2016/11/21.
 */
public class ActiveListHandler extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject) {
        send(CmdsUtils.CMD_ActiveRequestLIST , ActiveService.get(user) , user);
    }
}
