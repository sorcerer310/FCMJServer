package com.rafo.chess.engine.plugin.impl;

import java.util.ArrayList;
import java.util.LinkedList;

import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.BaseMajongPlayerAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.majiang.action.TingAction;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;

/***
 * 杠后炮胡
 * 
 * @author Administrator
 * 
 */
public abstract class TingPlugin extends AbstractPlayerPlugin<TingAction>
		implements IPluginCheckCanExecuteAction<TingAction> {

	@Override
	public void createCanExecuteAction(TingAction action) {
		MJPlayer player = (MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());
		ActionManager.daCheck(player);
		LinkedList<BaseMajongPlayerAction> list = action.getRoomInstance().getCanExecuteActionList();
		for(BaseMajongPlayerAction actTemp : list){
			if(actTemp.getActionType()==IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT){
				actTemp.setCard(action.getCard());
				actTemp.setAutoRun(true);
			}
		}
	}

	@Override
	public void doOperation(TingAction action) {
		payment(action);
		MJPlayer player = (MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());
		player.setKouTing(true);
		String cards = action.getToBeCards();
		String[] arr = cards.split(",");
		ArrayList<MJCard> hands = player.getHandCards().getHandCards();
		for (MJCard c : hands) {
			int status = 1;
			boolean changeable = true;
			for (String outC : arr) {
				if (c.getCardNum() == Integer.parseInt(outC)) {
					changeable = false;
					continue;
				}
			}
			if (changeable)
				c.setStatus(status);
		}
		this.createCanExecuteAction(action);
	}

	public abstract boolean analysis(MJPlayer player);

	public boolean checkExecute(Object... objects) {
		MJPlayer player = (MJPlayer) objects[0];
		if (!analysis(player))
			return false;
		return true;
	}
}
