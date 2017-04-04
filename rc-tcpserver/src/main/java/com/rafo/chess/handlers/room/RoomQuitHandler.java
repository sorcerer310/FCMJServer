package com.rafo.chess.handlers.room;

import com.rafo.chess.common.GlobalConstants;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.model.room.BGRoomQuitRES;
import com.rafo.chess.model.room.GBRoomQuitREQ;
import com.rafo.chess.model.account.LoginUser;
import com.rafo.chess.service.LoginService;
import com.rafo.chess.utils.CmdsUtils;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RoomQuitHandler extends BaseClientRequestHandler
{
	final static Logger logger = LoggerFactory.getLogger("room");

	@Override
	public void handleClientRequest(User user, ISFSObject isfsObject) {
		GameExtension gameExt = (GameExtension) getParentExtension();
		GBRoomQuitREQ req = new GBRoomQuitREQ();
		req.setRoomID(Integer.parseInt(gameExt.getParentRoom().getName()));
		req.setAccountID(user.getName());

		try{
			List<BGRoomQuitRES> data = gameExt.getRoomService().roomQuit(req);

			if(data.get(0).getResult()== GlobalConstants.ROOM_QUIT_SUCCESS){
				LoginUser loginUser = new LoginUser(user);
				loginUser.setRoom(0);
				LoginService.updateUserAttribute(loginUser.getId(), "room", "0");
				getApi().leaveRoom(user,gameExt.getParentRoom());
				user.setProperty("sfsobj",loginUser.toSFSObject());
				for(BGRoomQuitRES one:data){
					CmdsUtils.sendMessage(gameExt, CmdsUtils.CMD_ROOM_QUIT,one.toSFSObject(), one.getAccountID());
				}

				logger.debug(System.currentTimeMillis()+"\t"+user.getName()+"\t"+ "roomquit" +"\t"+
						user.getIpAddress()+"\t"+ gameExt.getParentRoom().getName() +"\t"+ 0 +"\t" + "success"+"\t"+"success");

			}else{
				for(BGRoomQuitRES one:data){
					CmdsUtils.sendMessage(gameExt, CmdsUtils.CMD_ROOM_QUIT,one.toSFSObject(), one.getAccountID());
				}

				logger.debug(System.currentTimeMillis()+"\t"+user.getName()+"\t"+ "roomquit" +"\t"+
						user.getIpAddress()+"\t"+ gameExt.getParentRoom().getName() +"\t"+ 0 +"\t" + "failed"+"\t"+ data.get(0).getResult());
			}
		}catch (Exception e){
			logger.debug(System.currentTimeMillis()+"\t"+user.getName()+"\t"+ "roomquit" +"\t"+
					user.getIpAddress()+"\t"+ gameExt.getParentRoom().getName() +"\t"+ 0 +"\t" + "failed"+"\t"+"system_err");
		}


	}
}