package com.rafo.hall.handlers;

import com.rafo.chess.common.db.RedisManager;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2016/9/27.
 */
public class LogoutHandler extends BaseServerEventHandler {

    private final Logger logger = LoggerFactory.getLogger("login");
    @Override
    public void handleServerEvent(ISFSEvent isfsEvent) throws SFSException {
        User user = (User) isfsEvent.getParameter(SFSEventParam.USER);
        RedisManager.getInstance().hSet("uid." + user.getName() , "status" , "0");
        logger.debug("logout\t" + user.getName());
    }
}