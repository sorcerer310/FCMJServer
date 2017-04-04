package com.rafo.chess.handlers.game;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.model.battle.WBBattleStartREQ;
import com.rafo.chess.core.GameExtension;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameStartHandler extends BaseClientRequestHandler
{
	final static Logger logger = LoggerFactory.getLogger("play");

	@Override
	public void handleClientRequest(User user, ISFSObject isfsObject) {
		GameExtension gameExt = (GameExtension) getParentExtension();
		//send();
		WBBattleStartREQ msg = new WBBattleStartREQ();
		msg.setRoomId(Integer.parseInt(user.getLastJoinedRoom().getName()));
		msg.setAccountId(user.getName());
		trace(user.getName() + "========== WBBattleStartREQAction ============");

		try {
			gameExt.getGameService().ready(Integer.parseInt(user.getName()));
			gameExt.getRoomService().checkVoteStatus(Integer.parseInt(user.getName()));
		} catch (ActionRuntimeException e) {
			logger.error("game_start_error\t"+ user.getLastJoinedRoom().getName() + "\t" + user.getName() + "\t" + user.getIpAddress() + "\t" + isfsObject.toJson());
			gameExt.getGameService().sendFailedStatus(Integer.parseInt(user.getName()));
		}

	}

}
