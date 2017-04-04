package com.rafo.hall.handlers;

import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.gm.service.InviteService;
import com.rafo.hall.service.ActiveService;
import com.rafo.hall.utils.CmdsUtils;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class InviteHandler extends BaseClientRequestHandler{

	@Override
	public void handleClientRequest(User user, ISFSObject req) {
		// TODO Auto-generated method stub
		int inviteId=req.getInt("inviteId");
		int playerId=Integer.valueOf(user.getName());
		int status=InviteService.setInviteId(playerId, inviteId);
		
		if(status==1)
		{
			int card=InviteService.getInviteId(playerId);
			InviteService.addCardNum(playerId, 10);
			ISFSObject data=new SFSObject();
			RedisManager.getInstance().hSet("uid."+playerId, "card", ""+InviteService.getCardNum(Integer.valueOf(user.getName())));
			String cardstr = RedisManager.getInstance().hGet("uid." + playerId, "card");
			data.putInt("roomCard", Integer.valueOf(cardstr));
			send("SFS_EVENT_ACCOUNT_MODIFY", data, user);
		}
		
		ISFSObject result=new SFSObject();
		result.putInt("status", status);
		result.putInt("inviteId", inviteId);
		
		send(CmdsUtils.CMD_INVITE, result, user);
	}
	
}