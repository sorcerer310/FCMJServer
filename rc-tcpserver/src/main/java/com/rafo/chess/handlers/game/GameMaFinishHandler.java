package com.rafo.chess.handlers.game;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.room.RoomHelper;
import com.rafo.chess.model.battle.WBBattleStepREQ;
import com.rafo.chess.core.GameExtension;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class GameMaFinishHandler extends BaseClientRequestHandler
{
    final static Logger logger = LoggerFactory.getLogger("play");

    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject)
    {
        GameExtension roomExt = (GameExtension) getParentExtension();
        
        roomExt.getGameService().closeMa(Integer.parseInt(user.getName()));
        
        
        
    }
}
