package com.rafo.chess.engine.room;

import com.rafo.chess.common.db.RedisManager;

import java.util.HashMap;
@SuppressWarnings("rawtypes")
public class RoomManager {
	/** 房间号 */
//	private static AtomicInteger roomid = new AtomicInteger(1000000);
	private static HashMap<Integer, RoomInstance> roomMapper = new HashMap<Integer, RoomInstance>();

	
	public static RoomInstance getRoomInstnaceByRoomid(int roomId){
		return roomMapper.get(roomId);
	}
	
	/***
	 * 创建一个房间
	 * 
	 * @return
	 */
	public static RoomInstance createRoom(int roomId, int roomSettingTempId,int uId) {
		RoomInstance room = new RoomInstance(roomId,roomSettingTempId,uId);
		long recordId = RedisManager.getInstance().incr("game_room_recordId");
        if(recordId > 10000000){
			recordId = RedisManager.getInstance().incr("game_room_recordId", recordId);
		}
		room.setRecordId((int) recordId);
		room.setRoomId(roomId);
		roomMapper.put(room.getRoomId(), room);
		return room;
	}
	/***
	 * 创建一个房间
	 * 
	 * @return
	 */
	public static boolean destroyRoom(int roomId) {
		roomMapper.remove(roomId);
		return true;
	}
}
