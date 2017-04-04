package com.rafo.chess.engine.majiang.action;

import java.util.List;

import com.rafo.chess.engine.action.IEActionExecutor;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.plugin.OptPluginFactory;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.resources.DataContainer;
import com.rafo.chess.template.impl.PluginTemplateGen;

/**
 * 用来定庄
 * 该类执行pluginTemplateGen.xls配置文件中所有插件的动作，包括所有和的动作
 */
public abstract class BaseMajongDealerAction implements IEActionExecutor {
	RoomInstance<MJCard> roomIns;
	protected int step;
	protected int status;

	public BaseMajongDealerAction(RoomInstance<MJCard> roomIns) {
		this.roomIns = roomIns;
	}

	@Override
	public RoomInstance<MJCard> getRoomInstance() {
		return roomIns;
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
				if (Integer.parseInt(str) == this.getActionType()) {
					@SuppressWarnings("rawtypes")
					IOptPlugin plugin = OptPluginFactory.createOptPlugin(gen.getTempId());
					plugin.doOperation(this);
				}
			}
		}
		// 输出一个发送到客户端的流局的action
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}
