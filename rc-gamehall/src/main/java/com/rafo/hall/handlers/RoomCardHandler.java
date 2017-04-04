package com.rafo.hall.handlers;

import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.gm.service.InviteService;
import com.rafo.hall.service.ActiveService;
import com.rafo.hall.utils.CmdsUtils;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class RoomCardHandler extends BaseClientRequestHandler{

	@Override
	public void handleClientRequest(User user, ISFSObject req) {
		// TODO Auto-generated method stub
//		int cardNum=InviteService.getCardNum(Integer.valueOf(user.getName()));
		int status=InviteService.getInviteId(Integer.valueOf(user.getName()));
		
		String card = RedisManager.getInstance().hGet("uid." + user.getName(), "card");
		
		ISFSObject result=new SFSObject();
		result.putInt("cardNum", Integer.valueOf(card));
		result.putInt("inviteId", status);
		
		send(CmdsUtils.CMD_CARDNUM , result, user);
		
	}
	
}
