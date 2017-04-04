package com.rafo.chess.common.service.record;


import com.rafo.chess.common.Constants;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.common.model.record.*;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.*;

public class RecordService {

    private static final long serialVersionUID = 1452350439730871003L;
    private static Logger logger = LoggerFactory.getLogger(RecordService.class);

    /**
     * 根据UID获取历史记录
     * @param playerID
     * @return
     */
    public static BWRoomRecordRES getRoomRecord(int playerID){

        BWRoomRecordRES builder = new BWRoomRecordRES();
        Jedis jedis = null;
        try{
            jedis = RedisManager.getInstance().getRedis();
            String roomsKey = "player_rooms_" + playerID;
            if(jedis.exists(roomsKey)){
                builder.setResult(Constants.ROOM_RECORD_SUCCESS);
                Set<String> rooms = jedis.smembers(roomsKey);

                SFSArray roomStatics = new SFSArray();

                List<SFSObject> statics = new ArrayList<>();
                for(String roomIdRecordId : rooms){
                    byte[] roomStatistic = jedis.get(getRoomStatisticCacheKey(roomIdRecordId).getBytes());
                    if(roomStatistic != null && roomStatistic.length>0) {
                        statics.add(SFSObject.newFromBinaryData(roomStatistic));
                    }
                }

                //按照时间排序
                Collections.sort(statics, new Comparator<SFSObject>() {
                    @Override
                    public int compare(SFSObject o1, SFSObject o2) {
                        try{
                            return o2.getInt("startTime").compareTo( o1.getInt("startTime"));
                        }catch (Exception e){
                            return 0;
                        }
                    }
                });

                int limit = 50;
                int count = 0;
                for(SFSObject rs : statics){
                    if(count >= limit){
                        break;
                    }
                    roomStatics.addSFSObject(rs);
                    count++;
                }

                builder.setRoomStatistics(roomStatics);
            }else{
                builder.setResult(Constants.ROOM_RECORD_FAILED_NODATA);
            }
        }catch(Exception e){
            e.printStackTrace();
            logger.error("get room record error, playerID={}", playerID);
            builder.setResult(Constants.ROOM_RECORD_FAILED);
        }finally {
            if(jedis != null){
                jedis.close();
            }
        }

        return builder;
    }

    public static BWRoundRecordRES getRoundRecords(int roomID, int recordId, int playerID) {
        BWRoundRecordRES builder = new BWRoundRecordRES();
        builder.setAccountID(String.valueOf(playerID));

        Jedis jedis = null;
        try{
            jedis = RedisManager.getInstance().getRedis();
            byte[] roundDataKey = getRoundDataCacheKey(roomID+"_"+recordId).getBytes();
            if(jedis.exists(roundDataKey)) {
                Set<byte[]> roundDatas = jedis.smembers(roundDataKey);
                SFSArray data = new SFSArray();
                List<SFSObject> rounds = new ArrayList<>();
                for (byte[] roundData : roundDatas) {
                    rounds.add(SFSObject.newFromBinaryData(roundData));
                }

                //按照round排序
                Collections.sort(rounds, new Comparator<SFSObject>() {
                    @Override
                    public int compare(SFSObject o1, SFSObject o2) {
                        try{
                            return o1.getInt("id") - o2.getInt("id");
                        }catch (Exception e){
                            return 0;
                        }
                    }
                });

                for(SFSObject round : rounds){
                    data.addSFSObject(round);
                }

                builder.setRoundData(data);
            }else{
                builder.setResult(Constants.ROUND_RECORD_FAILED_NODATA);
                return builder;
            }
        }finally {
            if(jedis != null){
                jedis.close();
            }
        }

        builder.setResult(Constants.ROUND_RECORD_SUCCESS);
        return builder;
    }

    /**
     * 存储每一局打牌信息
     * @param roomId
     * @param startTime
     * @param info
     * @param battleData
     */
    public static void saveRoundData(int roomId, int startTime, int roundStartTime, int recordId, List<PlayerPointInfoPROTO> info, SFSObject battleData){
        //用户房间历史 key: player_rooms_playerId  value: roomid_recordid
        //玩家房间历史信息 room_stat_roomId_recordid : startTime
        //玩家每一局历史信息 room_round_roomId_recordid: {round, time, playerPoint, round1, round2, round3}

        Jedis jedis = null;
        try {
            jedis = RedisManager.getInstance().getRedis();

            int expireTime = 3*24*60*60;
            //用户的房间列表
            String roomRecordID = roomId+"_"+recordId;
            for(PlayerPointInfoPROTO ppi : info){
                String roomsKey = "player_rooms_"+ppi.getPlayerID();
                jedis.sadd(roomsKey, roomRecordID);
                jedis.expire(roomsKey, expireTime);
            }

            RoomStatisticsPROTO roomStatistic = new RoomStatisticsPROTO();
            roomStatistic.setRoomID(roomId);
            roomStatistic.setStartTime(startTime);
            roomStatistic.setPlayerInfo(info);
            roomStatistic.setRecordID(recordId);

            //当前房间的战局
            byte[] roomStaticKey = getRoomStatisticCacheKey(roomRecordID).getBytes();
            jedis.set(roomStaticKey, roomStatistic.toSFSObject().toBinary());
            jedis.expire(roomStaticKey, expireTime);

            RoundDataPROTO roundData =  new RoundDataPROTO();
            roundData.setId(battleData.getInt("battleTime"));
            roundData.setStartTime(roundStartTime);
            roundData.setPlayerInfo(info);
            roundData.setBattleData(battleData);

            //每一局的打牌明细
            byte[] roundDataKey = getRoundDataCacheKey(roomRecordID).getBytes();
            jedis.sadd(roundDataKey, roundData.toSFSObject().toBinary());
            jedis.expire(roundDataKey, expireTime);

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!= null){
                jedis.close();
            }
        }
    }

    private static String getRoomStatisticCacheKey(String roomRecordID){
        return "room_stat_" + roomRecordID;
    }

    private static String getRoundDataCacheKey(String roomRecordID){
        return "room_round_" + roomRecordID;
    }

    /**
     private void addPlayerRoundCount(RoomStatistic roomStatistic)
     {
     List<Integer> playerIDs = get4Players(roomStatistic);
     for (Integer playerID : playerIDs)
     {
     String totalStr = RedisManager.getInstance().get(RedisKeyConstants.PLAYER_ROUND_INFO + playerID);
     if(totalStr != null)
     {
     RedisManager.getInstance().incr(RedisKeyConstants.PLAYER_ROUND_INFO + playerID);
     }
     }
     }

     public void saveRoundToDB(int roomID, int ownerID, int roomType, Boolean isFinish, RoomStatistic roomStatistic, RoundRecord roundRecord)
     {
     addPlayerRoundCount(roomStatistic);
     int currRoundCount = 0;
     List<BattleStatistic> battleStatistics = new ArrayList<>(); // 玩家历史战斗数据
     List<BattleStatisticBean> beans = new ArrayList<BattleStatisticBean>();
     Map<BattleStatistic, BattleStatisticBean> data2Bean = new HashMap<BattleStatistic, BattleStatisticBean>();

     boolean hasRecord = false;
     getStatisticsFromDBByRoomID(roomID, battleStatistics, beans, data2Bean); // 获得所有房间号为roomID的相关数据，再行筛选
     if(battleStatistics.size() == 0) // 该房间中的某玩家没有写过，则代表该房间没有被写过
     {
     currRoundCount = createBean(roomID, ownerID, roomType, isFinish, roomStatistic, roundRecord);
     }

     else
     {
     for (BattleStatistic battleStatistic : battleStatistics)
     {
     if(battleStatistic.getRoomID() != roomID)
     {
     continue;
     }

     if(battleStatistic.getRoomStatisticsData().getStartTime() != roomStatistic.getStartTime())
     {
     continue;
     }
     List<Integer> dbPlayerIDs = get4Players(battleStatistic.getRoomStatisticsData());
     List<Integer> playerIDs = get4Players(roomStatistic);
     if(!isSamePlayers(dbPlayerIDs, playerIDs))
     {
     continue;
     }

     hasRecord = true;
     // 至此，房间号，四个人，以及房间创建时间一致，可以认为是指定的记录
     currRoundCount = updateBean(roomID, ownerID, roomType, isFinish, roomStatistic, roundRecord, battleStatistic, data2Bean);
     }
     if(hasRecord == false)
     {
     currRoundCount = createBean(roomID, ownerID, roomType, isFinish, roomStatistic, roundRecord);
     }
     }

     sendRecordLog(roomID, roomStatistic, ownerID, isFinish,  roomType, roundRecord, currRoundCount);
     }

     public void changeRoomByVote(Room room)
     {
     List<BattleStatistic> battleStatistics = new ArrayList<>(); // 玩家历史战斗数据
     List<BattleStatisticBean> beans = new ArrayList<BattleStatisticBean>();
     Map<BattleStatistic, BattleStatisticBean> data2Bean = new HashMap<BattleStatistic, BattleStatisticBean>();

     getStatisticsFromDBByRoomID(room.getRoomID(), battleStatistics, beans, data2Bean); // 获得所有房间号为roomID的相关数据，再行筛选
     if(battleStatistics.size() == 0) // 该房间中的某玩家没有写过，则代表该房间没有被写过
     {
     return; // 无记录需要更改
     }
     else
     {
     BattleStatistic latestStatic = null;
     int i = 0;
     for (BattleStatistic battleStatistic : battleStatistics)
     {
     if(battleStatistic.getRoomID() != room.getRoomID())
     {
     continue;
     }
     List<Integer> dbPlayerIDs = get4Players(battleStatistic.getRoomStatisticsData());
     List<Integer> playerIDs = new ArrayList<>();
     Map<String, Player> account2Player = room.getPlayers();
     for (Player player : account2Player.values())
     {
     playerIDs.add(player.getID());
     }

     if(!isSamePlayers(dbPlayerIDs, playerIDs))
     {
     continue;
     }

     if(i == 0)
     {
     latestStatic = battleStatistics.get(0);
     ++i;
     continue;
     }
     if(latestStatic.getRoomStatisticsData().getStartTime() <= battleStatistic.getRoomStatisticsData().getStartTime())
     {
     latestStatic = battleStatistic;
     }
     }

     if(latestStatic == null)
     return;

     // 至此，房间号，四个人，以及房间创建时间一致，可以认为是指定的记录
     *//*			IBattleStatisticBeanDao dao = DBCS.getExector(IBattleStatisticBeanDao.class);
			BattleStatisticBean bean = data2Bean.get(latestStatic);
			bean.setIsFinish((byte)2);
			dao.updateBattleStatisticBean(bean);*//*
		}
	}

	private BGRecordLogSYN sendRecordLog(Integer roomID, RoomStatistic roomStatistic, int ownerID, Boolean isFinish, int roomType, RoundRecord roundRecord, int currRoundCount)
	{
		BGRecordLogSYN builder = new BGRecordLogSYN();
		builder.setRoomID(roomID);
		builder.setRoomStartTime(roomStatistic.getStartTime());
		builder.setRoomType(roomType);
		builder.addAllPlayerIDs(get4Players(roomStatistic));
		builder.setOwnerID(ownerID);
		builder.setFinished(isFinish);
		builder.setRoundRecordStartTime(roundRecord.getStartTime());
		builder.setCurrRoundCount(currRoundCount);

*//*		ServerService serverService = ServiceContainer.getInstance().getPublicService(ServerService.class);
		Transmitter.getInstance().write(serverService.getGameNode(), GlobalConstants.DEFAULT_CALLBACK, builder.build());*//*
		return builder;
	}

	private int updateBean(int roomID, int ownerID, int roomType,
		Boolean isFinish, RoomStatistic roomStatistic, RoundRecord roundRecord,
		BattleStatistic battleStatistic, Map<BattleStatistic, BattleStatisticBean> data2Bean)
	{
		BattleStatisticBean bean = data2Bean.get(battleStatistic);
		bean.setRoomID(roomID);
		List<Integer> playerIDs = get4Players(roomStatistic);
		bean.setPlayerID1(playerIDs.get(0));
		bean.setPlayerID2(playerIDs.get(1));
		bean.setPlayerID3(playerIDs.get(2));
		bean.setPlayerID4(playerIDs.get(3));
		bean.setOwnerID(ownerID);
		bean.setRoomType(roomType);
		bean.setIsFinish(isFinish == true ? (byte)1 : (byte)0);
		bean.setRoom_statistic(roomStatistic.toRoomStatisticsPROTO().toByteArray());

*//*		List<RoundRecord> roundRecords = battleStatistic.getRoundRecordDatas();
		roundRecords.add(roundRecord);
		RoundDatas round_datas = this.getRoundDataProto(roundRecords);
		bean.setRound_datas(round_datas.toByteArray());

		IBattleStatisticBeanDao dao = DBCS.getExector(IBattleStatisticBeanDao.class);
		dao.updateBattleStatisticBean(bean);

		return roundRecords.size();*//*
		return 0;
	}

	private int createBean(int roomID,int ownerID, int roomType, Boolean isFinish, RoomStatistic roomStatistic, RoundRecord roundRecord)
	{
		List<Integer> playerIDs = get4Players(roomStatistic);
		BattleStatisticBean bean = new BattleStatisticBean();
		bean.setRoomID(roomID);
		bean.setPlayerID1(playerIDs.get(0));
		bean.setPlayerID2(playerIDs.get(1));
		bean.setPlayerID3(playerIDs.get(2));
		bean.setPlayerID4(playerIDs.get(3));
		bean.setOwnerID(ownerID);
		bean.setRoomType(roomType);
		bean.setIsFinish(isFinish == true ? (byte)1 : (byte)0);
//		bean.setRoom_statistic(roomStatistic.toRoomStatisticsPROTO().toByteArray());

		List<RoundRecord> roundRecords = new ArrayList<>();
		roundRecords.add(roundRecord);
//		RoundDatas round_datas = this.getRoundDataProto(roundRecords);
//		bean.setRound_datas(round_datas.toByteArray());

//		IBattleStatisticBeanDao dao = DBCS.getExector(IBattleStatisticBeanDao.class);
//		dao.insertBattleStatisticBean(bean);*//*
		//记录玩家对房间的信息
		RedisManager.getInstance().sadd(RedisKeyConstants.PLAYERID_TO_ROOMIDS + playerIDs.get(0), roomID + "");
		RedisManager.getInstance().sadd(RedisKeyConstants.PLAYERID_TO_ROOMIDS + playerIDs.get(1), roomID + "");
		RedisManager.getInstance().sadd(RedisKeyConstants.PLAYERID_TO_ROOMIDS + playerIDs.get(2), roomID + "");
		RedisManager.getInstance().sadd(RedisKeyConstants.PLAYERID_TO_ROOMIDS + playerIDs.get(3), roomID + "");
		//记录房间信息
		RedisManager.getInstance().lpush(RedisKeyConstants.ROOMID_TO_ROOM_INFO + roomID, bean);
		return 1;
	}

	private boolean isStatisti4Player(int roomID, int playerID, BattleStatistic battleStatistic)
	{
		if(battleStatistic.getRoomID() == roomID)
		{
			if(battleStatistic.getPlayerID1() == playerID ||
				battleStatistic.getPlayerID2() == playerID ||
				battleStatistic.getPlayerID3() == playerID ||
				battleStatistic.getPlayerID4() == playerID)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}

	*//*
	 * 从数据库取出playerID对应的beans与BattleStatistic
	 *//*
	private int getStatisticsFromDBByPlayerID(int playerID, List<BattleStatistic> battleStatistics,
		List<BattleStatisticBean> beans, Map<BattleStatistic, BattleStatisticBean> data2Bean)
	{
		beans.clear();
		Set<String> roomIds = RedisManager.getInstance().smembers(RedisKeyConstants.PLAYERID_TO_ROOMIDS + playerID);
		List<BattleStatisticBean> tempBeans = new ArrayList<BattleStatisticBean>();
		if(!roomIds.isEmpty()) {
			for(String roomID : roomIds) {
				tempBeans.addAll(getRoomStatisticBeanBy(Integer.valueOf(roomID)));
			}
		}
		// 数据库对象为null
		if (tempBeans.isEmpty())
		{
			return 0;
		}
		beans.addAll(tempBeans);
		unmarshal(battleStatistics, beans, data2Bean);
//		logger.error("----getStatisticsFromDBByPlayerID---");
//		logger.error("battleStatistics size : " + battleStatistics.size());
		logger.error("beans size : " + beans.size());
		logger.error("data2Bean size : " + data2Bean.size());
		return 0;
	}

	private int getStatisticsFromDBByRoomID(int roomID, List<BattleStatistic> battleStatistics,
		List<BattleStatisticBean> beans, Map<BattleStatistic, BattleStatisticBean> data2Bean)
	{
		beans.clear();
		//从redis取出记录
		List<BattleStatisticBean> tempBeans = getRoomStatisticBeanBy(roomID);
		// 数据库对象为空
		if (tempBeans.isEmpty())
		{
			return 0;
		}
		beans.addAll(tempBeans);
		unmarshal(battleStatistics, beans, data2Bean);

		return 0;
	}

	*//**
     * 根据房间id取出房间记录信息
     * @param roomID
     * @return
     *//*
	public List<BattleStatisticBean> getRoomStatisticBeanBy(int roomID) {
		List<BattleStatisticBean> tempBeans = new ArrayList<BattleStatisticBean>();
		// 从redis取出记录
		int size = RedisManager.getInstance().llen(RedisKeyConstants.ROOMID_TO_ROOM_INFO + roomID)
				.intValue();
		for (int i = 0; i < size; i++) {
			BattleStatisticBean bsBean = (BattleStatisticBean) RedisManager
					.lpop(RedisKeyConstants.ROOMID_TO_ROOM_INFO + roomID);
			tempBeans.add(bsBean);
		}
		return tempBeans;
	}

	private int unmarshal(List<BattleStatistic> battleStatistics,
		List<BattleStatisticBean> beans, Map<BattleStatistic, BattleStatisticBean> data2Bean)
	{
		battleStatistics.clear();
		data2Bean.clear();
		if(beans.size() == 0)
		{
			return -1;
		}
		try
		{
			for (BattleStatisticBean bean : beans)
			{
				BattleStatistic battleStatistic = new BattleStatistic(
					bean.getUID(), bean.getRoomID(), bean.getPlayerID1(), bean.getPlayerID2(),
					bean.getPlayerID3(), bean.getPlayerID4(),
					bean.getOwnerID(), bean.getRoomType(), bean.getIsFinish() == 0 ? false :true,
					getRoomStatistics(bean), getRoundRecords(bean));
				battleStatistics.add(battleStatistic);
				data2Bean.put(battleStatistic, bean);
			}
			return 0;
		}
		catch(Exception e)
		{
			logger.error("ummarshal exception");
		}
		return -1;
	}

	private  RoomStatistic getRoomStatistics(BattleStatisticBean bean)
	{
		try
		{
			byte[] roomStatisticsBuffer = bean.getRoom_statistic();
			RoomStatisticsPROTO proto = RoomStatisticsPROTO.parseFrom(roomStatisticsBuffer);
			return new RoomStatistic(proto);

		}
		catch(Exception e)
		{
			logger.error("ummarshal room statistics exception, roomID={}", bean.getRoomID());
		}
		return null;
	}

	private RoundDatas getRoundDataProto(List<RoundRecord> roundRecords)
	{

		RoundDatas builder = new RoundDatas();

		for (RoundRecord roundRecord : roundRecords)
		{
			builder.addRoundDatas(roundRecord.toRoundDataPROTO());
		}
		return builder;
	}

	private List<Integer> get4Players(RoomStatistic roomStatistic)
	{
		List<PlayerPointInfo> playerPointInfos = roomStatistic.getPlayerPointInfo();
		List<Integer> playerIDs = new ArrayList<>();
		for (PlayerPointInfo playerPointInfo : playerPointInfos)
		{
			playerIDs.add(playerPointInfo.getPlayerID());
		}

		Collections.sort(playerIDs);
		return playerIDs;
	}

	private List<RoundRecord> getRoundRecords(BattleStatisticBean bean)
	{
*//*		try
		{
			List<RoundRecord> roundRecords = new ArrayList<>();
			byte[] roundDatasBuffer = bean.getRound_datas();
			RoundDatas roundDatas = RoundDatas.parseFrom(roundDatasBuffer);
			for (RoundDataPROTO proto : roundDatas.getRoundDatas())
			{
				RoundRecord roundRecord = new RoundRecord(proto);
				roundRecords.add(roundRecord);
			}
			return roundRecords;
		}
		catch(Exception e)
		{
			logger.error("ummarshal round data exception, roomID={}", bean.getRoomID());
		}*//*
		return new ArrayList<>();
	}

	private boolean isSamePlayers(List<Integer> list1, List<Integer> list2)
	{
		return list1.containsAll(list2);
	}
*/

}
