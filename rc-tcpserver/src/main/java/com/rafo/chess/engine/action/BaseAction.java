package com.rafo.chess.engine.action;

import java.util.List;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.gameModel.IECardModel;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.plugin.OptPluginFactory;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.resources.DataContainer;
import com.rafo.chess.template.impl.PluginTemplateGen;

public abstract class BaseAction<C extends IECardModel> implements
		IEPlayerAction {
	/** 步骤计数 */
	protected int step;
	/** 目标牌 */
	protected int card;
	/** 牌的来源 */
	protected int fromUid;
	/** 做动作的玩家id */
	protected int playerUid;
	// protected String params;
	/** 行为状态0未执行1执行 */
	protected int status;

	protected int pluginId;
	protected int subType;
	protected int canDoType;
	protected String toBeCards = "";

	protected RoomInstance<C> roomIns;
	
	protected boolean autoRun = false;
	protected boolean canPass = true;
	

	public BaseAction(RoomInstance<C> roomIns) {
		this.roomIns = roomIns;
	}

	public int getPluginId() {
		return pluginId;
	}

	public void setPluginId(int pluginId) {
		this.pluginId = pluginId;
	}

	public boolean isAutoRun() {
		return autoRun;
	}



	public void setAutoRun(boolean autoRun) {
		this.autoRun = autoRun;
	}



	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public int getCard() {
		return card;
	}

	public void setCard(int card) {
		this.card = card;
	}

	public int getFromUid() {
		return fromUid;
	}

	public void setFromUid(int fromUid) {
		this.fromUid = fromUid;
	}

	public int getPlayerUid() {
		return playerUid;
	}

	public void setPlayerUid(int playerUid) {
		this.playerUid = playerUid;
	}

	// public String getParams() {
	// return params;
	// }
	//
	// public void setParams(String params) {
	// this.params = params;
	// }

	@Override
	public int getStatus() {
		return this.status;
	}

	public int getSubType() {
		return subType;
	}

	public void setSubType(int subType) {
		this.subType = subType;
	}

	public String getToBeCards() {
		return toBeCards;
	}

	public void setToBeCards(String toBeCards) {
		this.toBeCards = toBeCards;
	}

	public int getCanDoType() {
		return canDoType;
	}

	public void setCanDoType(int canDoType) {
		this.canDoType = canDoType;
	}

	@Override
	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public RoomInstance<C> getRoomInstance() {
		return roomIns;
	}

	public boolean isCanPass() {
		return canPass;
	}

	public void setCanPass(boolean canPass) {
		this.canPass = canPass;
	}

	/***
	 * 游标检测
	 * 
	 * @return
	 */
	public boolean checkPlayerFocus() {
		MJPlayer player = (MJPlayer) roomIns.getPlayerById(this.playerUid);
		return player.getIndex() == roomIns.getFocusIndex();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doAction() throws ActionRuntimeException {
		this.setStatus(1);
		List<PluginTemplateGen> genList =  DataContainer
				.getInstance().getListDataByName("pluginTemplateGen");
		for (PluginTemplateGen gen : genList) {
			if (gen.getRoomSettingTempId() != roomIns.getRstempateGen().getTempId())
				continue;
			String[] actions = gen.getActionType().split(",");
			for (String str : actions) {
				if (Integer.parseInt(str) == this.getActionType()) {
					IOptPlugin plugin = OptPluginFactory.createOptPlugin(gen
							.getTempId());
					plugin.doOperation(this);
				}
			}
		}

		//如果有玩家和牌，进入结算界面
		if(roomIns.getRoomStatus()==RoomInstance.RoomState.hashu.getValue()){
			roomIns.setRoomStatus(RoomInstance.RoomState.calculated.getValue());
		}

	}
}
