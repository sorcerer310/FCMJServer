package com.rafo.chess.engine.room;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.rafo.chess.engine.plugin.impl.HuPlugin;
import com.rafo.chess.engine.vote.VoteResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rafo.chess.engine.IGameEngine;
import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.gameModel.IECardModel;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.gameModel.IPlayer.PlayState;
import com.rafo.chess.engine.gameModel.factory.GameModelFactory;
import com.rafo.chess.engine.majiang.MahjongEngine;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.BaseMajongPlayerAction;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.vote.VoteExecutor;
import com.rafo.chess.resources.DataContainer;
import com.rafo.chess.template.impl.RoomSettingTemplateGen;

public class RoomInstance<C extends IECardModel> {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	/** 房间号 */
	private int roomId;
	/** 进入房间的密码 */
	private int password;
	/** 引擎配置 */
	private RoomSettingTemplateGen rstempateGen;
	/** 当前局数 */
	private int currRounds;
	/** 房间状态 */
	private int roomStatus;
	/** 房主 */
	private int ownerId;
	/** 创建时间 */
	private long createTime;
	/** 引擎 */
	private IGameEngine engine;
	/** 玩家的坐序 */
	private IPlayer[] playerArr = null;
	/** 玩家id的映射 */
	private HashMap<Integer, IPlayer> playerMap = new HashMap<Integer, IPlayer>();
	/** 投票器 */
	private VoteExecutor voteExecutor = new VoteExecutor();
	/** 游标 */
	private int focusIndex = 0;
	/** 上局的赢家 */
	private List<Integer> lastWinner = new ArrayList<>();
	/** 庄家id */
	private int bankerUid;

	/** 抢杠胡，是否设置专家的状态 */
	private boolean bBankerQiang=false;
	/** 抢杠胡设置的庄家id */
	private int bankerQiangUid=0;

	public void setIsBankerQiang(boolean bQiang) {bBankerQiang = bQiang;}                                               //被抢杠的人
	public boolean IsBankerQiang()
	{
		return bBankerQiang;
	}
	public void SetBankerQiang(boolean b)
	{
		bBankerQiang=b;
	}

	public void setBankerQiangUid(int nID)
	{
		bankerQiangUid=nID;
	}
	public int getBankerQiangUid()
	{
		return bankerQiangUid;
	}

	/**
	 * 战绩ID，redis递增值，与roomId组成战绩的ID，防止同样的roomId把战绩覆盖
	 */
	private int recordId;

    private ConcurrentHashMap<String, VoteResultType> voteDestroyResult = new ConcurrentHashMap<String, VoteResultType>();

    //永修规则，门前清包三家
    //记录了包三家的玩家id
    private int player_menQianQingChargeAll = 0;


    /***
	 * 房间属性
	 */
	private HashMap<String, Object> attributeMap = new HashMap<String, Object>();

	public void addAttribute(String key, Object value) {
		attributeMap.put(key, value);
	}

	public Object getAttribute(String key) {
		return attributeMap.get(key);
	}

	public RoomInstance(int roomId, int rstempId, int ownerId) {
		this.roomId = roomId;
		this.ownerId = ownerId;
		this.createTime = System.currentTimeMillis()/1000;
		RoomSettingTemplateGen roomGen = (RoomSettingTemplateGen) DataContainer
				.getInstance().getDataByNameAndId("RoomSettingTemplateGen",rstempId);
		this.rstempateGen = roomGen;
		playerArr = new IPlayer[roomGen.getPlayerNum()];
		engine = new MahjongEngine(this);
		engine.init();
		logger.debug("room is creating ,roomid [" + roomId + "], owner[" + this.ownerId + "]");
	}


	/***
	 * 取得拥有优先级坐高操作的玩家的集合,将操作从canExecuteActionList转移到canExecutePlayerActionList
	 *
	 * @return
	 */
	public LinkedList<BaseMajongPlayerAction> getCanExecuteActionListByPriority() {
		/** 发送给客户端的一个玩家的操作 */
		LinkedList<BaseMajongPlayerAction> canExecutePlayerActionList = new LinkedList<BaseMajongPlayerAction>();
		if (this.getRoomStatus() == RoomState.gameing.getValue()) {
			/** 上一步是否有执行完的抓拍的操作 */
			ArrayList<IEPlayerAction> lastlist = this.engine.getMediator()
					.getCanExecuteActionByStep(
							this.engine.getMediator().getCurrentStep() - 1);
			for (IEPlayerAction action : lastlist) {
				if (action.getStatus() != 1)
					continue;
				if (action.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN)
					continue;
				if (action.getPlayerUid() != action.getPlayerUid())
					continue;
				canExecutePlayerActionList.add((BaseMajongPlayerAction) action);
			}

		}

		ArrayList<IEPlayerAction> list = this.engine.getMediator()
				.getCanExecuteActionByStep(
						this.engine.getMediator().getCurrentStep());
		if (list == null)
			return canExecutePlayerActionList;
		IEPlayerAction actionTemp = null;
		for (IEPlayerAction action : list) {
			if (actionTemp == null) {
				actionTemp = action;
				continue;
			}
			if (action.getPriority() < actionTemp.getPriority())
				continue;
			actionTemp = action;
		}

		Iterator<IEPlayerAction> it = list.iterator();
		while (it.hasNext()) {
			BaseMajongPlayerAction action = (BaseMajongPlayerAction) it.next();

			if(action.getPriority() == IEMajongAction.PRIORITY_COMMON){
				canExecutePlayerActionList.add(action);
				continue;
			}

			boolean isQiang = false;
			if(action.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU)
			{
				HuAction huAction = (HuAction)action;
				if(huAction.isQiang())
					isQiang = true;
			}

			if(!isQiang)
			{
				if (action.getPlayerUid() != actionTemp.getPlayerUid())
					continue;
			}

			canExecutePlayerActionList.add(action);
		}
		return canExecutePlayerActionList;
	}

	public int getCurrentTurnPlayerId() {
		return playerArr[this.getFocusIndex()].getUid();
	}

	public int getBankerUid() {
		return bankerUid;
	}

	public void setBankerUid(int bankerUid) {
		this.bankerUid = bankerUid;
	}

	public void addCanExecuteAction(BaseMajongPlayerAction action) {
/*		if (this.roomStatus == RoomState.calculated.getValue())
			return;*/
		this.engine.getMediator().addCanExecuteAction(action);
	}
	public void addCanExecuteActionAtStep(int step , BaseMajongPlayerAction action) {
		if (this.roomStatus == RoomState.calculated.getValue())
			return;
		this.engine.getMediator().addCanExecuteActionByStep(step,action);
	}

	public List<Integer> getLastWinner() {
		return lastWinner;
	}

	public void setLastWinner(List<Integer> lastWinner) {
		this.lastWinner = lastWinner;
	}
	public void addLastWinner(int lastWinner) {
		this.lastWinner.add(lastWinner);
	}


	public int getFocusIndex() {
		return focusIndex;
	}

	public void setFocusIndex(int focusIndex) {
		this.focusIndex = focusIndex;
	}

	public int nextFocusIndex() {
		focusIndex = ++focusIndex == 4 ? 0 : focusIndex;
		return focusIndex;
	}

	public IPlayer[] getPlayerArr() {
		return playerArr;
	}

	public HashMap<Integer, IPlayer> getPlayerMap() {
		return playerMap;
	}

	public VoteExecutor getVoteExecutor() {
		return voteExecutor;
	}

	public void setVoteExecutor(VoteExecutor voteExecutor) {
		this.voteExecutor = voteExecutor;
	}

	/***
	 * 修改玩家状态
	 *
	 * @param uid
	 * @param state
	 */
	public void changePlayerState(int uid, PlayState state) {
		IPlayer player = playerMap.get(uid);
		if (player == null)
			return;
		player.setPlayerState(state);
	}

	/***
	 * @param uid
	 * @return
	 */
	public synchronized boolean leaveRoom(int uid) {
		boolean res = false;
		IPlayer player = getPlayerById(uid);
		if (player == null) {
			return res;
		}
		for (int i = 0; i < playerArr.length; i++) {
			if (playerArr[i] != null && playerArr[i].getUid() == player.getUid()) {
				playerArr[i] = null;
				playerMap.remove(uid);
				res = true;
			}
		}
		return res;
	}

	/***
	 * @param uid
	 * @return
	 */
	public synchronized boolean joinRoom(int uid) {
		IPlayer player = getPlayerById(uid);
		if (player != null) {
			return false;
		}
		for (int i = 0; i < playerArr.length; i++) {
			if (playerArr[i] == null) {
				player = (IPlayer) GameModelFactory.createPlayer(rstempateGen
						.getCardType());
				player.setUid(uid);
				playerArr[i] = player;
				player.setIndex(i);
				player.setPlayerState(PlayState.Idle);
				player.setRoomId(roomId);
				playerMap.put(uid, player);
				return true;
			}
		}
		return false;
	}

	public synchronized boolean joinRoom(MJPlayer player) {
		IPlayer p = getPlayerById(player.getUid());
		if (p != null) {
			return false;
		}

		for (int i = 0; i < playerArr.length; i++) {
			if (playerArr[i] == null) {
				playerArr[i] = player;
				player.setIndex(i);
				player.setPlayerState(PlayState.Idle);
				player.setRoomId(roomId);
				playerMap.put(player.getUid(), player);
				return true;
			}
		}
		return false;
	}

	public IPlayer getPlayerById(int uid) {
		return playerMap.get(uid);
	}

	public ArrayList<IPlayer> getAllPlayer() {
		ArrayList<IPlayer> list = new ArrayList<IPlayer>();
		list.addAll(playerMap.values());
		return list;
	}

	public IGameEngine<C> getEngine() {
		return engine;
	}

	public void setEngine(IGameEngine<C> engine) {
		this.engine = engine;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public int getPassword() {
		return password;
	}

	public void setPassword(int password) {
		this.password = password;
	}

	public RoomSettingTemplateGen getRstempateGen() {
		return rstempateGen;
	}

	public void setRstempateGen(RoomSettingTemplateGen rstempateGen) {
		this.rstempateGen = rstempateGen;
	}

	public int getCurrRounds() {
		return currRounds;
	}

	public void setCurrRounds(int currRounds) {
		this.currRounds = currRounds;
	}

	public int getRoomStatus() {
		return roomStatus;
	}

	public void setRoomStatus(int roomStatus) {
		this.roomStatus = roomStatus;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	/***
	 * 扣钻石
	 *
	 * @return
	 */
	public int getTicketCount() {
		int gameType = (Integer) attributeMap
				.get(RoomAttributeConstants.GY_GAME_ROOM_COUNT);
		switch(gameType)
		{
		case 0:return RoomAttributeConstants.ROOM_ZUANSHI_4;
		case 1:return RoomAttributeConstants.ROOM_ZUANSHI_8;
		case 2:return RoomAttributeConstants.ROOM_ZUANSHI_16;
		default:return RoomAttributeConstants.ROOM_ZUANSHI_4;
		}
	}

	/**
	 * 回合数
	 *
	 * @return
	 */
	public int getTotalRound() {
		//运营要求，4，8，16所以干脆单独写
		//return rstempateGen.getTicket()
		//		* (1 + (Integer) attributeMap
		//				.get(RoomAttributeConstants.GY_GAME_ROOM_COUNT));
		int nType = (Integer) attributeMap.get(RoomAttributeConstants.GY_GAME_ROOM_COUNT);
		switch (nType) {
		case 0:
			return 4;
		case 1:
			return 8;
		case 2:
			return 16;
		default:
			break;
		}
		return 4;
	}

	/** 房间状态 */
	public enum RoomState {
		Idle(0),
		seating(1), //首局定位置
		gameing(2),
		calculated(3),
		jiama(4),	// 加码翻码
		//增加了一个有玩家和牌的状态
		hashu(5);
		RoomState(int state) {
			this.state = state;
		}

		private int state;

		public int getValue() {
			return state;
		}

		public RoomState getState(int state) {
			for (RoomState s : values()) {
				if (s.getValue() == state)
					return s;
			}
			return null;
		}
	}

	public boolean isFull() {
		return playerMap.size() == rstempateGen.getPlayerNum();
	}

	public int getCanExecuteActionSize() {
		ArrayList<IEPlayerAction> list = this.engine.getMediator()
				.getCanExecuteActionByStep(
						this.getEngine().getMediator().getCurrentStep());

		if (list == null)
			return 0;

		int count = 0;
		for(IEPlayerAction action : list){
			if(action.getStatus() == 0){
				count ++;
			}
		}

		return count;
	}

	/***
	 * 取得上一步可执行的操作
	 * @return
	 */
	public LinkedList<BaseMajongPlayerAction> getLastCanExecuteActionList() {
		LinkedList<BaseMajongPlayerAction> list = new LinkedList<BaseMajongPlayerAction>();
		ArrayList<IEPlayerAction> listtemp = this.engine.getMediator()
				.getCanExecuteActionByStep(
						this.engine.getMediator().getCurrentStep()-1);
		if (listtemp == null)
			return list;
		for (IEPlayerAction action : listtemp) {
			list.add((BaseMajongPlayerAction) action);
		}
		return list;
	}

	public LinkedList<BaseMajongPlayerAction> getCanExecuteActionList() {
		LinkedList<BaseMajongPlayerAction> list = new LinkedList<BaseMajongPlayerAction>();
		ArrayList<IEPlayerAction> listtemp = this.engine.getMediator()
				.getCanExecuteActionByStep(
						this.engine.getMediator().getCurrentStep());
		if (listtemp == null)
			return list;
		for (IEPlayerAction action : listtemp) {
			list.add((BaseMajongPlayerAction) action);
		}
		return list;
	}

	public LinkedList<BaseMajongPlayerAction> getCanExecuteActionList(int step) {
		LinkedList<BaseMajongPlayerAction> list = new LinkedList<BaseMajongPlayerAction>();
		ArrayList<IEPlayerAction> listtemp = this.engine.getMediator().getCanExecuteActionByStep(step);
		if (listtemp == null)
			return list;
		for (IEPlayerAction action : listtemp) {
			list.add((BaseMajongPlayerAction) action);
		}
		return list;
	}

	public void addRound(){
		this.currRounds ++;
	}

	public int getRecordId() {
		return recordId;
	}

	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}

	/**
	 * 房间和牌插件,每局结算的时候，把所有返回true的和牌插件加入其中，供结算时使用
	 */
	private ArrayList<HuPlugin> huPlugins = new ArrayList<>();

	public ArrayList<HuPlugin> getHuPlugins(){
		return huPlugins;
	}
}
