package com.rafo.chess.engine.majiang;

import com.rafo.chess.engine.gameModel.IEHandCardsContainer;
import com.rafo.chess.engine.gameModel.IPlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MJPlayer implements IPlayer, Cloneable {
	private int uid;
	private int index;																									//玩家在桌上的索引0-3对应东南西北
	private PlayState state;
	private boolean banker;
	private int score;
	private IEHandCardsContainer container;
	private int roomId;
	/** 如果叫嘴，是叫嘴牌行flag -1 未计算 0计算后未叫嘴 大于1是叫嘴的牌行 */
	private int jiaozui = -1;
	/** 是否扣听,扣听为不开门的听 */
	private boolean isKouTing = false;
	/** 是否听 */
	private boolean isTing = false;
	private boolean offline;
	private boolean isPassNohu = false;
	private String ip;
	private int seatNo;
	private int sex;
	private String head;
	private String nickName;
	/** 是否开门*/
	private boolean isOpen = false;
	private boolean seated = false;
	//定缺，缺的哪一门
	private int que;
	//获得和的除牌型外的其他加分项
	private Set<Integer> huAttachType = new HashSet<>();
	public Set<Integer> getHuAttachType() {return huAttachType;}
	//保存现在是否进入全求人包三家一轮，永修使用
	private boolean quanQiuRenChargeAll = false;
	public boolean isQuanQiuRenChargeAll() {return quanQiuRenChargeAll;}
	public void setQuanQiuRenChargeAll(boolean quanQiuRenChargeAll) {this.quanQiuRenChargeAll = quanQiuRenChargeAll;}

	/**
	 * 保存抢杠小胡状态
	 * flag			记录当前是否可以抢杠小胡
	 */
	private boolean qiangGangPingHuFlag = false;
	public boolean isQiangGangPingHuFlag() {return qiangGangPingHuFlag;}
	public void setQiangGangPingHuFlag(boolean qiangGangPingHuFlag) {this.qiangGangPingHuFlag = qiangGangPingHuFlag;}

	/**
	 * 保存杠上开花状态
	 */
	private boolean gangShangKaiHuaFlag = false;
	public boolean isGangShangKaiHuaFlag() {return gangShangKaiHuaFlag;}
	public void setGangShangKaiHuaFlag(boolean gangShangKaiHuaFlag) {this.gangShangKaiHuaFlag = gangShangKaiHuaFlag;}

	private String sameIp;
	private List<Integer> sameIpAccIDs = new ArrayList<Integer>();

	public boolean isOpen() {
		return isOpen;
	}
	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}

	public void reset(){
		container.cleanHands();
		jiaozui=-1;
		state= PlayState.Battle;
		isKouTing = false;
		isTing = false;
		isPassNohu = false;
		banker = false;
		isOpen = false;
	}
	
	public boolean isPassNohu() {
		return isPassNohu;
	}

	public void setPassNohu(boolean isPassNohu) {
		this.isPassNohu = isPassNohu;
	}

	public MJPlayer() {
		container = new MJHandCardsContainer();
	}

	public boolean isKouTing() {
		return isKouTing;
	}

	public void setKouTing(boolean isTing) {
		this.isKouTing = isTing;
	}

	public int isJiaozui() {
		return jiaozui;
	}

	public void setJiaozui(int jiaozui) {
		this.jiaozui = jiaozui;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public boolean isBanker() {
		return banker;
	}

	public void setBanker(boolean banker) {
		this.banker = banker;
	}

	@Override
	public int getUid() {
		return uid;
	}

	@Override
	public int getIndex() {
		return index;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public IEHandCardsContainer getHandCards() {
		return container;
	}

	@Override
	public PlayState getPlayState() {
		return state;
	}

	@Override
	public void setPlayerState(PlayState state) {
		this.state = state;

	}

	@Override
	public boolean isOffline() {
		return offline;
	}

	@Override
	public void setOffline(boolean offline) {
		this.offline = offline;
	}

	@Override
	public boolean isSeated() {
		return this.seated;
	}

	@Override
	public void setSeated(boolean seated) {
		this.seated = seated;
	}

	@Override
	public int getScore() {
		return score;
	}

	@Override
	public void setScore(int score) {
		this.score = score;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getSeatNo() {
		return seatNo;
	}

	public void setSeatNo(int seatNo) {
		this.seatNo = seatNo;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public String getHead() {
		return head;
	}

	public void setHead(String head) {
		this.head = head;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public boolean isTing() {
		return isTing;
	}

	public void setTing(boolean ting) {
		isTing = ting;
	}

	public int getQue() {
		return que;
	}

	public void setQue(int que) {
		this.que = que;
	}

	public boolean needResetSameIps(List<Integer> accountIDs){
		if(sameIp == null || !sameIp.equals(ip))
			return true;

		if(accountIDs.size() > sameIpAccIDs.size())
			return true;

		return false;
	}

	public void setSameIp(List<Integer> accountIDs){
		sameIp = ip;
		sameIpAccIDs.clear();
		for(Integer accountID : accountIDs)
			sameIpAccIDs.add(accountID);
	}

	public void resetSameIp(String ip, Integer accountID){
		if(sameIp == null || !sameIp.equals(ip))
			sameIpAccIDs.clear();

		sameIp = ip;
		if(!sameIpAccIDs.contains(accountID))
			sameIpAccIDs.add(accountID);
	}

	@Override
	public MJPlayer clone() throws CloneNotSupportedException {

		
		return (MJPlayer) super.clone();
	}
	
	//是否查看过翻码界面
	private boolean bIsLookMa = false;
	public boolean IsLookMa()
	{
		return bIsLookMa;
	}
	public void SetLookMa()
	{
		bIsLookMa = true;
	}



}
