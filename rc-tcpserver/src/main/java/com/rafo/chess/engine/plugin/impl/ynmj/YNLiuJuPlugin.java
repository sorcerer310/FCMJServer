package com.rafo.chess.engine.plugin.impl.ynmj;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.game.YBMJGameType;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.DealerLiujuAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.majiang.action.MoAction;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.engine.room.RoomInstance.RoomState;
import com.rafo.chess.template.impl.PluginTemplateGen;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/***
 * 抓到指定牌触发执行 conditionStr 牌池剩余多少张执行流局
 * 如果有人胡牌，第一个人胡了就退出
 * @author Administrator
 * 
 */
public class YNLiuJuPlugin implements IOptPlugin<DealerLiujuAction> {
	PluginTemplateGen gen;

	@Override
	public void doOperation(DealerLiujuAction action) throws ActionRuntimeException {
		if (!analysis(action)) {
			return;
		}
		
		action.getRoomInstance().getEngine().getMediator().addCanExecuteAction(action);

		action.getRoomInstance().setRoomStatus(RoomInstance.RoomState.calculated.getValue());		
		
		//流局 -- 清掉积分
		action.getRoomInstance().getEngine().getCalculator().clean();
	}

	public boolean analysis(DealerLiujuAction action) {
		RoomInstance room = action.getRoomInstance();
		ArrayList<MJCard> list = room.getEngine().getCardPool();

		int countLimit = Integer.parseInt(gen.getConditionStr());
		return list.size() == countLimit;
	}

	@Override
	public PluginTemplateGen getGen() {
		return gen;
	}

	@Override
	public void setGen(PluginTemplateGen gen) {
		this.gen = gen;
	}

	@Override
	public boolean doPayDetail(PayDetailed pd, RoomInstance room, Calculator calculator) {
		if (!pd.isValid() || pd.getFromUid() == null) {
			return false;
		}
		int rate = pd.getRate();

		ArrayList fromPlayers = new ArrayList();
		for (int uid : pd.getFromUid()) {
			IPlayer player = room.getPlayerById(uid);
			fromPlayers.add(player);
		}
		int payNum = fromPlayers.size();
		if (payNum == 0)
			return false;
		/*// 加分的
		int addscore = payNum * rate;
		MJPlayer toP = (MJPlayer) room.getPlayerById(pd.getToUid());
		calculator.getUserBattleBalances().get(toP.getUid()).addPoint(addscore);

		if (toP.isKouTing()) {
			calculator.getUserBattleBalances().get(toP.getUid()).setWinType(YBMJGameType.PlayType.KouTing);
		}else if(toP.isTing()){
			calculator.getUserBattleBalances().get(toP.getUid()).setWinType(YBMJGameType.PlayType.ReadyHand);
		} else {
			calculator.getUserBattleBalances().get(toP.getUid()).setWinType(-1);
		}

		// 减分的
		for (int uid : pd.getFromUid()) {
			IPlayer delP = room.getPlayerById(uid);
			calculator.getUserBattleBalances().get(delP.getUid()).addPoint(-rate);
		}*/
		return false;
	}
}
