package com.rafo.chess.engine.plugin.impl;

import java.util.ArrayList;
import java.util.LinkedList;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.BaseMajongPlayerAction;
import com.rafo.chess.engine.majiang.action.PassAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;

/***
 * 过
 * @author Administrator
 * 
 */
public class PassPlugin extends AbstractPlayerPlugin<PassAction> implements IPluginCheckCanExecuteAction<PassAction> {

	@Override
	public void doOperation(PassAction action) {
		MJPlayer player = (MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());
		LinkedList<BaseMajongPlayerAction> list = action.getRoomInstance().getLastCanExecuteActionList();
		//获得所有可执行的动作
		for (BaseMajongPlayerAction act : list) {
			//如果动作类型为和，设置不和
			if (act.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU) {
				player.setPassNohu(true);
			}
			//听暂未使用
			else if(act.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING){
				player.setJiaozui(-1); //reset jiaozui
			}
		}
		this.createCanExecuteAction(action);
	}

	@Override
	public boolean checkExecute(Object... objects) {
		return true;
	}

	@Override
	public void createCanExecuteAction(PassAction action) {
		int step = action.getRoomInstance().getEngine().getMediator().getCurrentStep();
		//获得上一步可执行的动作
		ArrayList<IEPlayerAction> list = action.getRoomInstance().getEngine().getMediator()
				.getCanExecuteActionByStep(step - 1);
		for (IEPlayerAction act : list) {
			//判断上一步动作与当前步动作是否都为自己，如果是越过
			if (act.getPlayerUid() == action.getPlayerUid())
				continue;
			action.getRoomInstance().addCanExecuteAction((BaseMajongPlayerAction) act);
		}

		//在这里处理碰完不杠，不能打牌的问题
		if (action.getRoomInstance().getCanExecuteActionSize() == 0) {
			MJPlayer player = (MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());
			if (action.getPlayerUid() == action.getFromUid()) {
				// 目前轮到自己，过牌之后发一个打牌操作
				ActionManager.daCheck(player);
			} else {
				MJPlayer p = (MJPlayer)action.getRoomInstance().getPlayerArr()[action.getRoomInstance().nextFocusIndex()];
				ActionManager.moCheck(p);
			}
		}
	}
}
