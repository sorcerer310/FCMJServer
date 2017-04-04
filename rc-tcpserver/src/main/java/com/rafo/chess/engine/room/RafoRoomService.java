package com.rafo.chess.engine.room;

import com.rafo.chess.common.GlobalConstants;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.vote.VoteExecutor;
import com.rafo.chess.engine.vote.VoteResultType;
import com.rafo.chess.exception.PersistException;
import com.rafo.chess.model.room.*;
import com.rafo.chess.utils.CmdsUtils;
import com.rafo.chess.utils.DateTimeUtil;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.RoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.smartfoxserver.v2.exceptions.SFSVariableException;
import com.smartfoxserver.v2.extensions.SFSExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class RafoRoomService {

	private final Logger logger = LoggerFactory.getLogger(RafoRoomService.class);
	private RoomInstance room ;
	private ConcurrentHashMap<Integer, Long> room2VoteStartTimes = new ConcurrentHashMap<>();
	private static Long ONE_MINS = 60 * 1000L;
	private SFSExtension roomExt;

	public SFSExtension getRoomExt() {
		return roomExt;
	}

	public RafoRoomService(SFSExtension roomExt){
		this.roomExt = roomExt;
	}


	public RoomInstance getRoom(){
		return room;
	}


	public void createRoom(GBRoomCreateREQ req, int uid) {
		this.room = RoomManager.createRoom(req.getRoomID(), 2, uid);
		this.room.addAttribute(RoomAttributeConstants.GY_GAME_ROOM_COUNT, req.getCount());								//设置局数
		this.room.addAttribute(RoomAttributeConstants.YB_PLAY_TYPE, req.getType());										//设置玩法，是否闭和
		this.room.addAttribute(RoomAttributeConstants.YB_MA_TYPE, req.getMaType());										//设置加码的值
		//int red = (req.getType() & YBMJGameType.RoomPlayType.RED) == YBMJGameType.RoomPlayType.RED ? 1 : 0;
		//int piao = (req.getType() & YBMJGameType.RoomPlayType.PIAO) == YBMJGameType.RoomPlayType.PIAO ? 1 : 0;
		//int qidui = (req.getType() & YBMJGameType.RoomPlayType.QIDUI) == YBMJGameType.RoomPlayType.QIDUI ? 1 : 0;
		//int yise = (req.getType() & YBMJGameType.RoomPlayType.YISE) == YBMJGameType.RoomPlayType.YISE ? 1 : 0;

		//this.room.addAttribute(RoomAttributeConstants.ROOM_PLAY_TYPE_RED,  red);
		//this.room.addAttribute(RoomAttributeConstants.ROOM_PLAY_TYPE_PIAO, piao);
		//this.room.addAttribute(RoomAttributeConstants.ROOM_PLAY_TYPE_QIDUI, qidui);
		//this.room.addAttribute(RoomAttributeConstants.ROOM_PLAY_TYPE_YISE, yise);


		//this.room.addAttribute(RoomAttributeConstants.YB_PLAY_TYPE, req.getType());

		this.room.addAttribute(RoomAttributeConstants.ROOM_RED_DICE, 0);
		this.room.addAttribute(RoomAttributeConstants.ROOM_QUEYIMEN, 0);
	}

	public BGRoomEnterRES enterFailed(int playerId, int errorCode) {
		BGRoomEnterRES res = new BGRoomEnterRES();
		res.setResult(errorCode);
		res.setRoomID(room.getRoomId());
		res.setApplierAccountID(String.valueOf(playerId));
		res.setApplierID(playerId);
		return res;
	}

	//1. enter room
	public BGRoomEnterRES enterRoom(String name, int playerID, String ip, String sex, String head){

		if (!room.getPlayerMap().containsKey(playerID) && this.room.getRstempateGen().getPlayerNum()  == room.getPlayerMap().size()){
			return enterFailed( playerID, GlobalConstants.ROOM_ENTER_FAILED_ROOM_FULL);
		}

		MJPlayer applier = (MJPlayer)room.getPlayerById(playerID);

		if(applier == null) {
			boolean isBanker = room.getPlayerMap().size() == 0;
			applier = new MJPlayer();
			applier.setUid(playerID);
			applier.setIp(ip);
			applier.setSex(Integer.parseInt(sex));
			applier.setNickName(name);
			applier.setHead(head);
			applier.setScore(0);
			applier.setBanker(isBanker);

			room.joinRoom(applier);
		}else{
			applier.setOffline(false);
		}

		// 检测相同IP,如果有相同IP，将状态设置为IDLE
		if(room.getRoomStatus() == RoomInstance.RoomState.Idle.ordinal()){
			List<Integer> same_ips = new ArrayList<Integer>();
			List<MJPlayer> players = room.getAllPlayer();
			for(MJPlayer player : players) {
				if(player.getUid() == applier.getUid() || player.isOffline())
					continue;

				if(player.getIp().equals(applier.getIp()))
					same_ips.add(player.getUid());
			}

			if(same_ips.size() > 0 && same_ips.size() < 3){
				if(applier.needResetSameIps(same_ips)){
					applier.setSameIp(same_ips);

					for(MJPlayer player : players) {
						player.setPlayerState(IPlayer.PlayState.Idle);

						if(same_ips.contains(player.getUid()))
							player.resetSameIp(applier.getIp(), applier.getUid());
					}
				}
			}
		}

		BGRoomEnterRES res = new BGRoomEnterRES();
		ArrayList<IPlayer> players = room.getAllPlayer();
		for (IPlayer iplayer : players) // 将房间内所有玩家的信息填充到协议中
		{
			MJPlayer p = (MJPlayer)iplayer;
			PlayerInfoSSPROTO playerSSInfo = new PlayerInfoSSPROTO();
			playerSSInfo.setAccountID(String.valueOf(p.getUid()));
			playerSSInfo.setChair(p.getIndex());
			playerSSInfo.setName(p.getNickName());
			playerSSInfo.setHead(p.getHead());
			playerSSInfo.setSex(String.valueOf(p.getSex()));
			playerSSInfo.setRoom(room.getRoomId());
			playerSSInfo.setPlayerID(p.getUid());
			playerSSInfo.setIp(p.getIp());
			playerSSInfo.setStatus(p.getPlayState().ordinal());
			playerSSInfo.setOffline(p.isOffline());
			res.addPlayerInfo(playerSSInfo);
		}
		res.setApplierAccountID(String.valueOf(applier.getUid()));
		res.setApplierID(applier.getUid());
		res.setRoomID(room.getRoomId());
		res.setRoomType((Integer)room.getAttribute(RoomAttributeConstants.GY_GAME_ROOM_COUNT));
		res.setPlayType((Integer)room.getAttribute(RoomAttributeConstants.YB_PLAY_TYPE));
		res.setMaType((Integer)room.getAttribute(RoomAttributeConstants.YB_MA_TYPE));
		res.setCurrentBattleCount(room.getCurrRounds());
		res.setResult(GlobalConstants.ROOM_ENTER_SUCCESS);


		// 检查房间状态，通知进入房间的人房间状态
		// 检查房间状态，通知进入房间的人房间状态
		if(room.getVoteExecutor().hasVoteApply())
		{
			BGVoteDestroyRES builder = new BGVoteDestroyRES();
			setVoteBuilder(builder, room, GlobalConstants.WC_VOTE_DESTROY_VOTING);
			builder.setAccountID(String.valueOf(playerID));
			res.setBgVoteDestroyRES(builder);
		}

		return res;
	}


	private BGRoomDestoryRES destroyRoomError(GBRoomDestoryREQ message, int errorCode){
		BGRoomDestoryRES res = new BGRoomDestoryRES();
		res.setResult(errorCode);
		res.setAccountID(message.getAccountID());
		res.setRoomID(message.getRoomID());
		return res;
	}

	/*
	 * 服务器主动销毁房间，只有在房间总结算的时候会调用
	 */
	public BGAutoDestroySYN autoDestroyRoom(int roomID){
		BGAutoDestroySYN builder = new BGAutoDestroySYN();

		if (room.getRoomStatus() != RoomInstance.RoomState.Idle.getValue())
		{
			logger.error("autoDestroyRoom room is not normal, roomID={}", roomID);
			builder.setResult(GlobalConstants.BG_AUTO_DESTROY_FAILED_NO_IN_ROOM);
			return builder;
		}

		Set<Integer> accountIDs = new HashSet<>(room.getPlayerMap().keySet());

		builder.setOwnerAccountID(String.valueOf(room.getOwnerId()));
		builder.setRoomID(roomID);
		builder.setRoomType((Integer)room.getAttribute(RoomAttributeConstants.GY_GAME_ROOM_COUNT));
		builder.setIp(((MJPlayer)room.getPlayerById(room.getOwnerId())).getIp());

		for (Integer accountID : accountIDs)
		{
			builder.addAccountIDs(String.valueOf(accountID));
		}
		builder.setResult(GlobalConstants.BG_AUTO_DESTROY_SUCCESS);

		try {
			SFSExtension extension = this.getRoomExt();
			com.smartfoxserver.v2.entities.Room sfsRoom = extension.getParentRoom();
			RoomHelper.destroyRoom(extension);
			SFSObject obj = new SFSObject();
			this.getRoomExt().send(CmdsUtils.SFS_EVENT_FORCE_DESTORY_ROOM, obj, sfsRoom.getUserList());
		} catch (PersistException e) {
			e.printStackTrace();
		} catch (SFSVariableException e) {
			e.printStackTrace();
		}

		return builder;
	}


	public List<BGVoteDestroyRES> voteDestoryRoom(GBVoteDestroyREQ message) {
		BGVoteDestroyRES builder = new BGVoteDestroyRES();

		int accountID = Integer.parseInt(message.getAccountID());
		int roomID = message.getRoomID();
		builder.setRoomID(roomID);
		List<BGVoteDestroyRES> results = new ArrayList<>();

		VoteExecutor voteExecutor = room.getVoteExecutor();
		if (voteExecutor.hasVoted(accountID)) // 玩家已经投过票
		{
			logger.error("Player has already voted " + accountID);
			builder.setAccountID(message.getAccountID());
			builder = setVoteBuilder(builder, room, GlobalConstants.WC_VOTE_DESTROY_FAILED_HAS_VOTED);
			results.add(builder);
			return results;
		}

		VoteResultType voteResult = VoteResultType.valueOf(message.getVoteResult());
		if (voteResult == VoteResultType.START && !voteExecutor.isFirstApplyDestroy()) // 该房间已经存在一次申请
		{
			logger.error("Apply has already existed " + accountID);
			builder.setAccountID(message.getAccountID());
			builder = setVoteBuilder(builder, room, GlobalConstants.WC_VOTE_DESTROY_FAILED_EXISTED);
			results.add(builder);
			return results;
		}

		if (voteResult == VoteResultType.START) {
			room2VoteStartTimes.put(roomID, System.currentTimeMillis()); // 用于记录申请解散
			builder.setRemainTime(ONE_MINS + "");
			try {
				RoomVariable isVote = new SFSRoomVariable("isVote", true);
				isVote.setHidden(true);
				this.getRoomExt().getParentRoom().setVariable(isVote);
				RoomVariable voteTime = new SFSRoomVariable("voteTime", (new Long(System.currentTimeMillis()).intValue())/1000);
				voteTime.setHidden(true);
				this.getRoomExt().getParentRoom().setVariable(voteTime);
			} catch (SFSVariableException e) {
				e.printStackTrace();
			}
		}

		voteExecutor.addVoteResult(accountID, voteResult); // 添加投票记录之后，需要进行结果检查

		Set<Integer> accountIDs = new HashSet<>(room.getPlayerMap().keySet());

		if (voteResult == VoteResultType.REFUSE) // 有一人拒绝，则取消
		{
			builder = setVoteBuilder(builder, room, GlobalConstants.WC_VOTE_DESTROY_FAILED_REFUSED);
			try {
				RoomVariable isVote = new SFSRoomVariable("isVote", false);
				this.getRoomExt().getParentRoom().setVariable(isVote);
				this.getRoomExt().getParentRoom().removeVariable("voteTime");

			} catch (SFSVariableException e) {
				e.printStackTrace();
			}
			voteExecutor.cancelDestroy();
		}
//		else if (voteExecutor.isCouldDestroy(room.getAllPlayer().size())) // 可以解散房间，之后房间会被垃圾回收，所以不用清空房间的投票记录
		else if (voteExecutor.isCouldDestroy((room.getAllPlayer().size()-1)))
		{
			builder = setVoteBuilder(builder, room, GlobalConstants.WC_VOTE_DESTROY_SUCCESS);
			room2VoteStartTimes.remove(roomID);

			// 通知Game，写解散房间Log
			sendVoteDestroyOKLog(roomID);
		}else {
			builder = setVoteBuilder(builder, room, GlobalConstants.WC_VOTE_DESTROY_VOTING);
		}

		for (Integer reciverAccountID : accountIDs) {
			builder.setAccountID(String.valueOf(reciverAccountID));
			try {
				results.add((BGVoteDestroyRES) builder.clone());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}

		return results;
	}

	/*
	 * 定时检查投票解散房间，对于超时的，自动投票为同意
	 */
	public List<BGVoteDestroyRES> checkVoteTime(Long nowTime){
		Set<Integer> roomIDS = room2VoteStartTimes.keySet();
		List<BGVoteDestroyRES> results = new ArrayList<>();
		for (Integer roomID : roomIDS){
			if(nowTime - room2VoteStartTimes.get(roomID) >= ONE_MINS)
			{
				// 超时，直接销毁房间，然后发送给各个人。
				room2VoteStartTimes.remove(roomID);
				if(room == null)
					continue;

				Set<Integer> accountIDs = new HashSet<>(room.getPlayerMap().keySet());
				sendVoteDestroyOKLog(roomID);

				for (Integer reciverAccountID : accountIDs){
					BGVoteDestroyRES builder = new BGVoteDestroyRES();
					builder = setVoteBuilder(builder, room, GlobalConstants.WC_VOTE_DESTROY_SUCCESS);
					builder.setAccountID(String.valueOf(reciverAccountID));
					results.add(builder);
				}
			}
		}
		return results;
	}

	private BGVoteDestroyOKLogSYN sendVoteDestroyOKLog(int roomID){
		BGVoteDestroyOKLogSYN builder = new BGVoteDestroyOKLogSYN();
		builder.setRoomID(roomID);
		return builder;
	}

	public List<BGRoomQuitRES> roomQuit(GBRoomQuitREQ req){
		Integer playerId = Integer.parseInt(req.getAccountID());
		List<BGRoomQuitRES> results = new ArrayList<>();
		if(room.getPlayerById(playerId) == null)
		{
			logger.error("roomQuit error, not in room, accountID={}, roomID={}", req.getAccountID(), req.getRoomID());
			results.add(quitRoomError(req,GlobalConstants.ROOM_QUIT_FAILED_NOT_IN_ROOM));
			return results;
		}

		if(room.getOwnerId() ==  playerId){
			logger.error("roomQuit error, owner can not exit! accountID={}, roomID={}", req.getAccountID(), req.getRoomID());
			results.add(quitRoomError(req, GlobalConstants.ROOM_QUIT_FAILED_IS_OWNER));
			return results;
		}

		if(room.getRoomStatus() != RoomInstance.RoomState.Idle.getValue()){
			logger.error("roomQuit error, player in battle! accountID={}, roomID={}", req.getAccountID(), req.getRoomID());
			results.add(quitRoomError(req, GlobalConstants.ROOM_QUIT_FAILED_IN_BATTLE));
			return results;
		}

		// 处理退出房间问题
		Set<Integer> accountIDs = new HashSet<Integer>(room.getPlayerMap().keySet());
		IPlayer player = room.getPlayerById(playerId);
		room.leaveRoom(playerId);

		BGRoomQuitRES builder = new BGRoomQuitRES();

		builder.setResult(GlobalConstants.ROOM_QUIT_SUCCESS);
		builder.setRoomID(req.getRoomID());
		builder.setQuitterID(player.getUid());
		builder.setQuitterAccountID(String.valueOf(player.getUid()));

		for (Integer  accountID : accountIDs)
		{
			builder.setAccountID(String.valueOf(accountID));
			try {
				results.add((BGRoomQuitRES)builder.clone());
			} catch (CloneNotSupportedException e) {
			}
		}
		return results;
	}

	private BGRoomQuitRES quitRoomError(GBRoomQuitREQ req, int errorCode){
		BGRoomQuitRES builder = new BGRoomQuitRES();
		builder.setResult(errorCode);
		builder.setAccountID(req.getAccountID());
		builder.setRoomID(req.getRoomID());
		builder.setQuitterID(-1);
		builder.setQuitterAccountID(req.getAccountID());
		return builder;
	}


	/*
	 * 设置投票返回信息的值
	 */
	private BGVoteDestroyRES setVoteBuilder(BGVoteDestroyRES builder, RoomInstance room, int result){

		Map<Integer, VoteResultType> voteResults = room.getVoteExecutor().getVoteRecord();
		for (Map.Entry<Integer, VoteResultType> voteRecord : voteResults.entrySet()){
			IPlayer player = room.getPlayerById(voteRecord.getKey());
			VoteInfoPROTO voteInfo = new VoteInfoPROTO();
			voteInfo.setPlayerID(player.getUid());
			voteInfo.setVoteResult(voteRecord.getValue().value());
			builder.addPlayerVoteInfo(voteInfo);
		}

		Long remainTime;
		Long startTime = room2VoteStartTimes.get(room.getRoomId());
		if(startTime == null){
			remainTime = 0L;
		}else{
			remainTime = ONE_MINS - DateTimeUtil.getDateDiff(new Date().getTime(), startTime);
			if(remainTime < 0)
				remainTime = 0L;
		}

		builder.setRemainTime(remainTime.toString());
		builder.setRoomID(room.getRoomId());
		builder.setResult(result);

		return builder;
	}


	public void checkVoteStatus(int playerId){
		RoomVariable isVote = roomExt.getParentRoom().getVariable("isVote");
		if(isVote != null) {
			if (isVote.getBoolValue()) {
				MJPlayer player = (MJPlayer) room.getPlayerById(playerId);
				GBVoteDestroyREQ destoryREQ = new GBVoteDestroyREQ();
				destoryREQ.setAccountID(String.valueOf(player.getUid()));
				destoryREQ.setRoomID(Integer.parseInt(roomExt.getParentRoom().getName()));

				VoteExecutor voteExecutor = room.getVoteExecutor();

				if(voteExecutor.getVoteRecord().containsKey(player.getUid())){
					VoteResultType voteResultType = voteExecutor.getVoteRecord().get(player.getUid());
					destoryREQ.setVoteResult(voteResultType.value());
				}else{
					destoryREQ.setVoteResult(VoteResultType.START.value());
				}

				BGVoteDestroyRES builder = new BGVoteDestroyRES();
				builder.setRoomID(room.getRoomId());

				BGVoteDestroyRES res = setVoteBuilder(builder, room, GlobalConstants.WC_VOTE_DESTROY_VOTING);
				res.setAccountID(String.valueOf(playerId));
				CmdsUtils.sendMessage(roomExt, CmdsUtils.CMD_ROOM_DESTROY_VOTE_RESP, res.toSFSObject(), String.valueOf(playerId));
			}
		}
	}
}