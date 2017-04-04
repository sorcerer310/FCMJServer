package com.rafo.chess.engine.majiang.action;

import java.util.ArrayList;
import java.util.List;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.plugin.OptPluginFactory;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.resources.DataContainer;
import com.rafo.chess.template.impl.PluginTemplateGen;

public class PassAction extends BaseMajongPlayerAction {

	public PassAction(RoomInstance<MJCard> roomIns) {
		super(roomIns);
	}

	@Override
	public int getActionType() {
		return IEMajongAction.PLAYER_ACTION_TYPE_CARD_GUO;
	}

	@SuppressWarnings("unchecked")
	public void doAction() throws ActionRuntimeException {
		int step = roomIns.getEngine().getMediator().getCurrentStep();
		ArrayList<IEPlayerAction> list = roomIns.getEngine().getMediator()
				.getCanExecuteActionByStep(step - 1);

		boolean valid = false;
		for (IEPlayerAction action : list) {
			if (action.getPlayerUid() == this.getPlayerUid()) {
				valid = true;
				break;
			}
		}

		if(valid) {
			this.setStatus(1);
			List<PluginTemplateGen> genList = (List<PluginTemplateGen>) DataContainer
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
		}
	}


	@Override
	public int getPriority() {
		return 0;
	}

	public boolean checkMySelf(int actionType, int card, int playerUid,
			int subType, String toBeCards) {
		return true;
	}
}
