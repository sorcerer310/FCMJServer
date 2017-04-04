package com.rafo.chess.handlers.room;

import com.rafo.chess.common.GlobalConstants;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.room.*;
import com.rafo.chess.model.account.LoginRoom;
import com.rafo.chess.model.account.LoginUser;
import com.rafo.chess.model.room.BGRoomEnterRES;
import com.rafo.chess.model.room.GBRoomCreateREQ;
import com.rafo.chess.model.room.GBRoomEnterREQ;
import com.rafo.chess.service.LoginService;
import com.rafo.chess.utils.CmdsUtils;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


public class RoomJoinEventHandler extends BaseServerEventHandler
{
	final static Logger logger = LoggerFactory.getLogger("room");

	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException {
		GameExtension roomExt = (GameExtension) getParentExtension();
		GBRoomEnterREQ message = new GBRoomEnterREQ();
		User user = (User)event.getParameter(SFSEventParam.USER);
		Room room = (Room)event.getParameter(SFSEventParam.ROOM);

		trace("user ["+user.getName()+"] start join room ["+ room.getName()+"] ,is owner?"+ user.getName().equals(room.getOwner().getName()));

		SFSObject extraObj = (SFSObject)event.getParameter(SFSEventParam.OBJECT);
		LoginUser loginUser = new LoginUser(user);
		if(extraObj == null){
			extraObj = new SFSObject();
			extraObj.putInt("roomid",Integer.parseInt(room.getName()));
		}

		Properties props = roomExt.getConfigProperties();
		int serverId = Integer.parseInt(props.getProperty("server.id").trim());

		assembleMessage(loginUser,extraObj,message,serverId);

		BGRoomEnterRES enterRES = null;
		if(room != null) {
			try{

				RoomInstance rafoRoom = roomExt.getGameService().getRoom();
				if(rafoRoom == null){
					SFSObject createRoomREQ = (SFSObject)room.getProperty("message");
					GBRoomCreateREQ createReq = GBRoomCreateREQ.fromSFSOBject(createRoomREQ);
					roomExt.initService(createReq, loginUser.getId());

					rafoRoom = roomExt.getGameService().getRoom();
					RoomHelper.storeRoom2Redis(getLoginRoom(rafoRoom, serverId));
				} else {
					int card = loginUser.getCard();
					SFSObject createRoomREQ = (SFSObject)room.getProperty("message");
					GBRoomCreateREQ createReq = GBRoomCreateREQ.fromSFSOBject(createRoomREQ);
					boolean flag=RoomHelper.checkCard(card, createReq.getCount());
					if(!flag)
					{
						ISFSObject resp=new SFSObject();
						resp.putInt("result",GlobalConstants.ROOM_CREATE_FAILED_ROOMCARD_NOT_ENOUGTH);
						resp.putUtfString("msg","left card not enough");
						roomExt.send(CmdsUtils.CMD_CREATROOM,resp,user);
						return;
					}
				}

				enterRES = roomExt.getRoomService().enterRoom(message.getName(), message.getID(), message.getIp(), message.getSex(), message.getHead());

				if(enterRES.getResult() != GlobalConstants.ROOM_ENTER_SUCCESS){
					roomExt.send(CmdsUtils.CMD_JOINROOM, enterRES.toRoomEnterResSFSObj(user), user);
					logger.debug(System.currentTimeMillis()+"\t"+user.getName()+"\t"+ "joinroom" +"\t"+
							user.getIpAddress()+"\t"+ room.getName() +"\t"+ 0 +"\t" + "fail"+"\t"+enterRES.getResult());
					return;
				}

				loginUser.setRoom(Integer.parseInt(room.getName()));
				LoginService.updateUserAttribute(loginUser.getId(), "room", room.getName());
				user.setProperty("sfsobj",loginUser.toSFSObject());
				logger.debug(System.currentTimeMillis()+"\t"+user.getName()+"\t"+ "joinroom" +"\t"+
						user.getIpAddress()+"\t"+ room.getName() +"\t"+ 0 +"\t" + "success"+"\t"+"success");

				for(User u:room.getUserList()){
					roomExt.send(CmdsUtils.CMD_JOINROOM, enterRES.toRoomEnterResSFSObj(u), u);
				}
			}catch (Exception e){
				e.printStackTrace();
				logger.debug(System.currentTimeMillis()+"\t"+user.getName()+"\t"+ "joinroom" +"\t"+
						user.getIpAddress()+"\t"+ room.getName() +"\t"+ 0 +"\t" + "failed"+"\t"+"system_err");
				BGRoomEnterRES res = RoomHelper.enterFailed(message,GlobalConstants.SYSTEM_ERROR);
				roomExt.send(CmdsUtils.CMD_JOINROOM, res.toRoomEnterResSFSObj(user), user);
			}

		}else {
			BGRoomEnterRES res = RoomHelper.enterFailed(message,GlobalConstants.ROOM_ENTER_FAILED_NUMBER_ERROR);
			roomExt.send(CmdsUtils.CMD_JOINROOM, res.toRoomEnterResSFSObj(user), user);
			logger.debug(System.currentTimeMillis()+"\t"+user.getName()+"\t"+ "joinroom" +"\t"+
					user.getIpAddress()+"\t"+ room.getName() +"\t"+ 0 +"\t" + "failed"+"\t"+"room_not_exists");
		}

		trace("user ["+user.getName()+"] joined room ["+ room.getName()+"]");

	}

	private void assembleMessage(LoginUser user,ISFSObject param,GBRoomEnterREQ message,int serverId){

		message.setID(user.getId());
		message.setRoomID(param.getInt("roomid"));
		message.setServerID(serverId);
		message.setAccountID(user.getId()+"");
		message.setHead(user.getHead());
		message.setIp(user.getIp());
		message.setName(user.getName());
		message.setSex(user.getSex());
	}


	private LoginRoom getLoginRoom(RoomInstance room, int serverId){
		LoginRoom loginRoom  = new LoginRoom();
		loginRoom.setPlayType(room.getRstempateGen().getTempId());
		loginRoom.setBattleTime(room.getCurrRounds());
		loginRoom.setCreateTime(room.getCreateTime());
		loginRoom.setInBattle(room.getRoomStatus() != RoomInstance.RoomState.Idle.getValue());
		loginRoom.setOwnerAccountID(String.valueOf(room.getOwnerId()));
		loginRoom.setRoomID(room.getRoomId());
		loginRoom.setRoomType((Integer)room.getAttribute(RoomAttributeConstants.GY_GAME_ROOM_COUNT));
		loginRoom.setRoundTotal(room.getTotalRound());
		loginRoom.setServerId(serverId);
		return loginRoom;
	}


}
