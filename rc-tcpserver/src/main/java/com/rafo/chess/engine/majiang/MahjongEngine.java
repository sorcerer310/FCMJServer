package com.rafo.chess.engine.majiang;

import com.rafo.chess.engine.AbstractGameEngine;
import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.action.BaseMajongPlayerAction;
import com.rafo.chess.engine.majiang.action.DealerDingZhuangAction;
import com.rafo.chess.engine.majiang.action.DealerDealAction;
import com.rafo.chess.engine.majiang.action.DefaultAction;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import com.rafo.chess.engine.room.RoomInstance;

import java.util.*;

/***
 * 麻将,引擎，程序的生命周期在此
 * 
 * @author Administrator
 */
public class MahjongEngine extends AbstractGameEngine<MJCard> {

	public MahjongEngine(RoomInstance roomIns) {
		super(roomIns);
	}

	@Override
	public void init() {
		mediator = new MahjongActionMediator(roomIns);
		roomIns.addAttribute(RoomAttributeConstants.ROOM_BASE_RATE, 1);
	}

	@Override
	public boolean startGame() throws ActionRuntimeException{
		this.roomIns.addAttribute(RoomAttributeConstants.YN_GAME_QUE, new HashMap<Integer,Integer>());
		this.roomIns.setRoomStatus(RoomInstance.RoomState.gameing.getValue());		
		//洗牌tack
		this.shuffle();
		//发牌
		DealerDealAction tackCardsAction = new DealerDealAction(roomIns);
		tackCardsAction.doAction();
		logger.debug("game start by round :"+this.roomIns.getCurrRounds());
		return true;
	}

	@Override
	public void executeAction(int actionType, int card, int playerUid,
			int subType, String toBeCards) throws ActionRuntimeException {
		//执行动作代理中的所有可执行动作
		mediator.executeAction(actionType, card, playerUid, subType, toBeCards);
	}

	public void clean(){
		this.cardPool.clear();
		this.outCardPool.clear();
		this.maArrayList.clear();
		this.jiamaResultArrayList.clear();
		this.maScores.clear();
		this.mediator = new MahjongActionMediator(roomIns);
	}

	@Override
	public void dingzhuang() throws ActionRuntimeException{
		// 4个玩家都准备好了开局, 清空牌池
		ArrayList<MJPlayer> players = roomIns.getAllPlayer();
		for (MJPlayer player : players) {
			player.reset();
		}
		roomIns.getEngine().clean();
		//清空结算
		calculator.clean();

		//庄
		DealerDingZhuangAction dingzhuang = new DealerDingZhuangAction(roomIns);
		dingzhuang.doAction();
		roomIns.getLastWinner().clear();
	}

}
