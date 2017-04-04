package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.game.YNMJGameType;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.plugin.OptPluginFactory;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.engine.room.RoomInstance.RoomState;
import com.rafo.chess.resources.DataContainer;
import com.rafo.chess.template.impl.PluginTemplateGen;

import java.util.List;

/***
 * 抢杠胡
 * 
 * @author Administrator
 */
public class QiangGangHuAction extends BaseMajongPlayerAction   {

	public QiangGangHuAction(RoomInstance roomIns) {
		super(roomIns);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doAction() throws ActionRuntimeException {
		List<PluginTemplateGen> genList = (List<PluginTemplateGen>) DataContainer
				.getInstance().getListDataByName("pluginTemplateGen");
		for (PluginTemplateGen gen : genList) {
			if (gen.getRoomSettingTempId() != roomIns.getRstempateGen().getTempId())
				continue;
			String[] actions = gen.getActionType().split(",");
			for (String str : actions) {
				if (Integer.parseInt(str) == this.getActionType() &&
						gen.getSubType() == this.getSubActionType()) {
					@SuppressWarnings("rawtypes")
					IOptPlugin plugin = OptPluginFactory.createOptPlugin(gen
							.getTempId());
					plugin.doOperation(this);
				}
			}
		}
		// 输出一个发送到客户端的刘局的action
	}
	
	
	@Override
	public int getActionType() {
		return IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU;
	}
	
	public int getSubActionType()
	{
		return YNMJGameType.PlayType.R_QiangGang;
	}


	@Override
	public int getPriority() {
		return 0;
	}


	
}
