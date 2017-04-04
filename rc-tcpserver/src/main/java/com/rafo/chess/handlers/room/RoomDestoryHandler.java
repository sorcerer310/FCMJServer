package com.rafo.chess.handlers.room;

import com.rafo.chess.common.GlobalConstants;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.engine.vote.VoteResultType;
import com.rafo.chess.model.room.BGVoteDestroyRES;
import com.rafo.chess.model.room.GBVoteDestroyREQ;
import com.rafo.chess.engine.room.RafoRoomService;
import com.rafo.chess.engine.room.RoomHelper;
import com.rafo.chess.utils.CmdsUtils;
import com.smartfoxserver.v2.annotations.MultiHandler;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.extensions.SFSExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@MultiHandler
public class RoomDestoryHandler extends BaseClientRequestHandler
{
	final static Logger logger = LoggerFactory.getLogger("room");

	@Override
	public void handleClientRequest(User user, ISFSObject isfsObject) {
		GameExtension gameExt = (GameExtension) getParentExtension();
		RafoRoomService roomService = gameExt.getRoomService();
		String command = isfsObject.getUtfString(SFSExtension.MULTIHANDLER_REQUEST_ID);

		Room room = gameExt.getParentRoom();
		GBVoteDestroyREQ destoryREQ = new GBVoteDestroyREQ();
		destoryREQ.setAccountID(user.getName());
		destoryREQ.setRoomID(Integer.parseInt(room.getName()));

		ISFSObject resp = new SFSObject();

		if(command.equals(CmdsUtils.CMD_ROOM_DESTROY)){
			destoryREQ.setVoteResult(VoteResultType.START.value());
		}else {
			int voteResult = isfsObject.getInt("voteResult");
			destoryREQ.setVoteResult(voteResult);
		}

		toLog(logger,user,room.getName(),command,"vote",destoryREQ.getVoteResult()+"");

		if(room == null){
			resp.putInt("result", GlobalConstants.ROOM_DESTORY_FAILED_ROOM_NUM_ERROR);
			gameExt.send(CmdsUtils.CMD_ROOM_DESTROY_VOTE_RESP,resp,user);
			toLog(logger,user,room.getName(),"destroyroom","failed","room_num_err");
			return;
		}else if(!isContainUser(room,user)){
			resp.putInt("result", GlobalConstants.ROOM_DESTORY_FAILED_NOT_IN_ROOM);
			gameExt.send(CmdsUtils.CMD_ROOM_DESTROY_VOTE_RESP,resp,user);
			toLog(logger,user,room.getName(),"destroyroom","failed","user_not_in_room");
			return;
		}

		GameExtension extension = (GameExtension)room.getExtension();
		int uc = extension.getRoomService().getRoom().getAllPlayer().size();
		int max_uc = extension.getRoomService().getRoom().getPlayerArr().length;
		int roomStatus = extension.getRoomService().getRoom().getRoomStatus();
		if(uc < max_uc && roomStatus == RoomInstance.RoomState.Idle.getValue()
				&& room.getOwner().getName().equals(user.getName())
				&& destoryREQ.getVoteResult()==VoteResultType.START.value()){
			try {
				RoomHelper.destroyRoom(gameExt);
				resp.putInt("result", GlobalConstants.ROOM_DESTORY_SUCCESS);
				gameExt.send(CmdsUtils.CMD_ROOM_DESTROY_VOTE_RESP,resp,room.getUserList());
				toLog(logger,user,room.getName(),"destroyroom","success","success");
			} catch (Exception e) {
				resp.putInt("result", GlobalConstants.SYSTEM_ERROR);
				gameExt.send(CmdsUtils.CMD_ROOM_DESTROY_VOTE_RESP,resp,user);
				toLog(logger,user,room.getName(),"destroyroom","failed","system_err");
			}
		}else{
			List<BGVoteDestroyRES> datas = roomService.voteDestoryRoom(destoryREQ);

			if(datas.size()>0){
				if(datas.get(0).getResult() == GlobalConstants.WC_VOTE_DESTROY_SUCCESS){
					try{
						RoomHelper.destroy(room,gameExt,resp);
						toLog(logger,user,room.getName(), "destroyroom", "success","success");
					}catch (Exception e){
						resp.putInt("result", GlobalConstants.SYSTEM_ERROR);
						gameExt.send(CmdsUtils.CMD_ROOM_DESTROY_VOTE_RESP,resp,user);
						toLog(logger,user,room.getName(),"destroyroom","failed","system_err");
					}
				}else{
					for(BGVoteDestroyRES one:datas){
						CmdsUtils.sendMessage(gameExt, CmdsUtils.CMD_ROOM_DESTROY_VOTE_RESP, one.toSFSObject(), one.getAccountID());
					}
				}

			}

		}

	}

	private boolean isContainUser(Room room,User user){
		for(User u:room.getUserList()){
			if(u.getName()==user.getName()){
				return true;
			}
		}
		return false;
	}

	private void  toLog(Logger logger,User user,String roomId,String cmd,String isErr,String msg){
		logger.debug(System.currentTimeMillis()+"\t"+user.getName()+"\t"+ cmd +"\t"+
				user.getIpAddress()+"\t"+ roomId +"\t"+ 0 +"\t" + isErr+"\t"+msg);
	}

}