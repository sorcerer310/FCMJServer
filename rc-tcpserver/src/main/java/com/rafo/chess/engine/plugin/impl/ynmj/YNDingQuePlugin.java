package com.rafo.chess.engine.plugin.impl.ynmj;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.game.YNMJGameType;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.*;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import com.rafo.chess.engine.room.RoomInstance;

import java.util.ArrayList;
import java.util.HashMap;

/***
 * 
 * @author Administrator
 * 
 */
public class YNDingQuePlugin extends  AbstractPlayerPlugin<DingQueAction> implements IPluginCheckCanExecuteAction<DingQueAction> {

	@Override
	public void doOperation(DingQueAction action) {
		RoomInstance room = action.getRoomInstance();
		this.dingQue(action);
		
		HashMap<Integer, Integer> map = (HashMap<Integer, Integer>) room.getAttribute(RoomAttributeConstants.YN_GAME_QUE);

		if (map.size() == room.getPlayerArr().length) {
			StringBuffer sb = new StringBuffer();
			IPlayer[] players = room.getPlayerArr();
			for(IPlayer p : players){
				sb.append(""+map.get(p.getUid()));
				sb.append(",");
			}
			
			DefaultAction defaultAction = new DefaultAction(room);
			defaultAction.setSubType(YNMJGameType.PlayType.LackFinish);
			defaultAction.setCanDoType(YNMJGameType.PlayType.LackFinish);
			defaultAction.setStatus(1);
			defaultAction.setToBeCards(sb.toString());

			room.getEngine().getMediator().addCanExecuteAction(defaultAction);

			this.createCanExecuteAction(action);
		}
	}


	@Override
	public boolean checkExecute(Object... objects) {
		MJPlayer pTemp = (MJPlayer) objects[0];
		DealerDealAction action = (DealerDealAction) objects[1];

		RoomInstance room = action.getRoomInstance();
		DingQueAction dingQueAction = new DingQueAction(room);
		dingQueAction.setPlayerUid(pTemp.getUid());
		dingQueAction.setSubType(this.getGen().getSubType());
		dingQueAction.setCanDoType(this.getGen().getCanDoType());
		room.getEngine().getMediator().addCanExecuteAction(dingQueAction);

		room.addAttribute(RoomAttributeConstants.YN_GAME_QUE, new HashMap<Integer,Integer>());
		return true;
	}

	@Override
	public void createCanExecuteAction(DingQueAction action) {
		RoomInstance room = action.getRoomInstance();
		IPlayer[] players = room.getPlayerArr();
		for(IPlayer p : players){
			if(room.getBankerUid()==p.getUid()){
				ActionManager.huCheck(p, action);
				ActionManager.gangCheck(p, action);

				if (action.getRoomInstance().getCanExecuteActionSize() == 0) {
					ActionManager.daCheck((MJPlayer) p);
				}
			}
		}
	}

	@Override
	public boolean doPayDetail(PayDetailed pd, RoomInstance room, Calculator calculator) {
		return true;
	}


	private void dingQue(DingQueAction action) {
		RoomInstance room = action.getRoomInstance();

		HashMap<Integer, Integer> map = (HashMap<Integer, Integer>) room
				.getAttribute(RoomAttributeConstants.YN_GAME_QUE);

		int card = action.getCard();
		map.put(action.getPlayerUid(), action.getCard());

		ActionManager.checkQueCardStatus(action.getPlayerUid(), room);

		boolean dingqueStart = false;

		int step = room.getEngine().getMediator().getCurrentStep();
		ArrayList<IEPlayerAction> list2 = room.getEngine().getMediator().getCanExecuteActionByStep(step - 1);
		for (IEPlayerAction act : list2) {
			if (act.getPlayerUid() == action.getPlayerUid())
				continue;
			if(act.getActionType() != action.getActionType())
				continue;
			room.addCanExecuteAction((BaseMajongPlayerAction) act);
			dingqueStart = true;
		}

		if(dingqueStart){
			DefaultAction restAction = new DefaultAction(room);
			restAction.setActionType(action.getActionType());
			restAction.setPlayerUid(action.getPlayerUid());
			restAction.setSubType(action.getSubType());
			restAction.setCanDoType(action.getSubType());
			restAction.setStatus(1);
			restAction.setCard(card);
			restAction.setToBeCards(action.getToBeCards());
			room.getEngine().getMediator().addCanExecuteAction(restAction);
		}
	}
}
