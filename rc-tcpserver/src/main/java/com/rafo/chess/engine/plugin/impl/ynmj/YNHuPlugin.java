package com.rafo.chess.engine.plugin.impl.ynmj;

import java.util.List;
import java.util.ArrayList;
import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.game.YBMJGameType;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.engine.plugin.impl.HuPlugin;
import com.rafo.chess.engine.room.RoomInstance;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * 抓到指定牌触发执行
 * 
 * @author Administrator
 */
public abstract class YNHuPlugin extends HuPlugin {
	private final Logger logger = LoggerFactory.getLogger("play");
	@Override
	public void createCanExecuteAction(HuAction action) {
		MJPlayer p = (MJPlayer)action.getRoomInstance().getPlayerArr()[action.getRoomInstance().nextFocusIndex()];
		ActionManager.moCheck(p);
	}

	@Override
	public void doOperation(HuAction action) throws ActionRuntimeException {
		if (!this.analysis(action))
			return;

		super.doOperation(action);
		MJPlayer player = (MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());
		player.setJiaozui(gen.getTempId());
		RoomInstance roomIns = action.getRoomInstance();
		// 计算其他的胡牌的人
		int step = roomIns.getEngine().getMediator().getCurrentStep();
		ArrayList<IEPlayerAction> actionList = roomIns.getEngine().getMediator().getCanExecuteActionByStep(step - 1);

		if(actionList == null){
			return;
		}

		for (IEPlayerAction act : actionList) {
			if (act.getActionType() != action.getActionType()) {
				continue;
			}
			if (act.getPlayerUid() == action.getPlayerUid())
				continue;
			if (act.getStatus() == 1) {
				continue;
			}
			int index = roomIns.getPlayerById(act.getPlayerUid()).getIndex();
			roomIns.setFocusIndex(index);

			int nextStep = roomIns.getEngine().getMediator().getNextStep();
			act.setStep(nextStep);
			act.setStatus(1);
			roomIns.getEngine().getMediator().getDoneActionList().add(act);
			act.doAction();
		}

/*		if( (roomIns.getLastWinner().size() + 1) == roomIns.getAllPlayer().size()) {
			action.getRoomInstance().setRoomStatus(RoomInstance.RoomState.calculated.getValue());
		}else{
			this.createCanExecuteAction(action);
		}*/
		if(!isMaRoom(roomIns))
			action.getRoomInstance().setRoomStatus(RoomInstance.RoomState.calculated.getValue());
		else {
			action.getRoomInstance().setRoomStatus(RoomInstance.RoomState.jiama.getValue());
		}
	}

	@Override
	public boolean doPayDetail(PayDetailed pd, RoomInstance room, Calculator calculator) {
		if (!pd.isValid() && pd.getFromUid() != null) {
			return false;
		}

		logger.debug("十三doPayDetail胡牌:");
		// 把加码的帐都算到被抢杠人的头上
		if(room.IsBankerQiang() && pd.bFromMa)//相关变量每局结束还原
		{
			logger.debug("jiama:把加码的帐都算到被抢杠人的头上");
			List<Integer> id = new ArrayList<>();
			id.add( room.getBankerQiangUid() );
			pd.setFromUid(id);
		}

		int rate = pd.getRate();
		ArrayList fromPlayers = new ArrayList();
		for (int uid : pd.getFromUid()) {
			IPlayer player = room.getPlayerById(uid);
			if (player == null)
				continue;
			fromPlayers.add(player);
		}
		int payNum = fromPlayers.size();
		if (payNum == 0)
			return false;
		// 加分的
		int addscore = payNum * rate;
		// 减分的
		for (int uid : pd.getFromUid()) {
			IPlayer delP = room.getPlayerById(uid);
			if(pd.getFromUid().length > 1) {
				calculator.addCardBalance(delP.getUid(), pd.getToUid(), 0, -rate, pd);
			}
		}

		if (payNum > 1) {
			calculator.getBattleCensuss().get(pd.getToUid()).addWinSelf(); // 自摸
			if(room.getEngine().getCardPool().size() >= 4) {
				calculator.addCardBalance(pd.getToUid(), 0, this.getGen().getZimoFlag(), addscore, pd);
			}
		} else if (pd.getFromUids().length == 1) {
				calculator.getBattleCensuss().get(pd.getToUid()).addWinOther(); // 接炮
				calculator.getBattleCensuss().get(pd.getFromUid()[0]).addDiscardOther(); // 点炮
				calculator.addCardBalance(pd.getToUid(), pd.getFromUid()[0], this.getGen().getDianedFlag(), addscore, pd);
				calculator.addCardBalance(pd.getFromUid()[0], pd.getToUid(), this.getGen().getDianFlag(), -pd.getRate(), pd);
		}

		calculator.getUserBattleBalances().get(pd.getToUid()).setWinType(pd.getSubType());
		calculator.getUserBattleBalances().get(pd.getToUid()).setHu(true);
		//暂时为兼容原来的显示
		calculator.addCardBalance(pd.getToUid(), this.getGen().getSubType(), pd.getCards(), addscore);
		return false;
	}


}
