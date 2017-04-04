package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.game.YNMJGameType;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.DealerLiujuAction;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.engine.room.RoomInstance.RoomState;
import com.rafo.chess.template.impl.PluginTemplateGen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/***
 * 抓到指定牌触发执行 conditionStr 牌池剩余多少张执行流局
 * 如果有人胡牌，第一个人胡了就退出
 * @author Administrator
 * 
 */
public class YXLiuJuPlugin implements IOptPlugin<DealerLiujuAction> {
	PluginTemplateGen gen;

	@Override
	public void doOperation(DealerLiujuAction action) throws ActionRuntimeException {
		if (!analysis(action)) {
			return;
		}

		action.getRoomInstance().getEngine().getMediator().addCanExecuteAction(action);

		action.getRoomInstance().setRoomStatus(RoomState.calculated.getValue());
		
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


		//流局目前不算分
		//杠分，三家出杠分，一家赢
//		ArrayList<IPlayer> allPlayer = room.getAllPlayer();
//		Map<Integer, Integer> gangScore = new HashMap<>();                                                              //杠+的分
//		Map<Integer, Integer> gangDelScore = new HashMap<>();                                                           //杠-的分，除了杠的人其他三家减
//		for (IPlayer p : allPlayer) {
//			int c = 0;
//			//计算当前玩家有几个杠
//			ArrayList<CardGroup> acg = p.getHandCards().getOpencards();
//			for (CardGroup cg : acg)
//				if (cg.getCardsList().size() == 4) {
//					//暗杠1个4分
//					if (cg.getGType() == 11) c += YNMJGameType.HuAttachType.OtherAnGang;
//						//明杠1个2分
//					else if (cg.getGType() == 13) c += YNMJGameType.HuAttachType.OtherMingGang;
//				}
//			//为有杠玩家增加杠分,杠分由3家出，所以乘以3
//			gangScore.put(p.getUid(), c * 3);
//			//其他玩家减少杠分
//			ArrayList<MJPlayer> amp = room.getAllPlayer();
//			for (IPlayer fp : amp) {
//				if (fp.getUid() != p.getUid()) {
//					//初始化当前玩家的减分为0
//					if (!gangDelScore.containsKey(fp.getUid())) gangDelScore.put(fp.getUid(), 0);
//					//为当前玩家减分
//					gangDelScore.put(fp.getUid(), (gangDelScore.get(fp.getUid()) + c));
//				}
//			}
//		}
//		//为每个玩家增加杠分
//		for(IPlayer p:allPlayer)
//			calculator.getUserBattleBalances().get(p.getUid()).setGangPoint(gangScore.get(p.getUid()) - gangDelScore.get(p.getUid()));
//
//		Calculator.gangScore = gangScore;
//		Calculator.gangDelScore = gangDelScore;


		return false;
	}
}
