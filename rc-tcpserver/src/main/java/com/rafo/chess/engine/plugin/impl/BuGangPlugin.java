package com.rafo.chess.engine.plugin.impl;

import java.util.*;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.GangAction;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.engine.room.RoomManager;

/***
 * 补杠
 * @author Administrator
 */
public abstract class BuGangPlugin extends GangPlugin{
	@Override
	public void createCanExecuteAction(GangAction action) {
		if (action.getRoomInstance().getCanExecuteActionSize() == 0) {//
			MJPlayer player = (MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());
			ActionManager.moCheck(player);
		}
	}

	@Override
	public void doOperation(GangAction action) throws ActionRuntimeException {
		if (action.getSubType() == gen.getSubType()) {
			RoomInstance roomIns = action.getRoomInstance();
			// 移除碰牌,加入杠牌
			MJPlayer player = (MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());
			ArrayList<CardGroup> grouplist = player.getHandCards().getOpencards();
			Iterator<CardGroup> it = grouplist.iterator();
			boolean gangOk = false;

			int pengUid = 0;
			while (it.hasNext()) {
				CardGroup cg = it.next();
				ArrayList<MJCard> cardlist = cg.getCardsList();
				if(cardlist.size() != 3)
					continue;
				if (action.getCard() != cardlist.get(0).getCardNum()
						|| action.getCard() != cardlist.get(1).getCardNum()
						|| action.getCard() != cardlist.get(2).getCardNum() )
					continue;

				gangOk = true;
				it.remove();

				//让碰失效
				ArrayList<PayDetailed> payDetaileds = action.getRoomInstance().getEngine().getCalculator().getPayDetailList();
				for(PayDetailed payDetailed : payDetaileds){
					if(payDetailed.getSubType() == cg.getGType()
							&& cg.getCardsList().get(0).getCardNum() == payDetailed.getCard()){
						payDetailed.setValid(false);
						pengUid = payDetailed.getFromUid()[0];
						break;
					}
				}

				ArrayList<MJCard> hands = player.getHandCards().getHandCards();
				Iterator<MJCard> it2 = hands.iterator();
				while (it2.hasNext()) {
					MJCard c = it2.next();
					if (c.getCardNum() == action.getCard()) {
						it2.remove();
						cardlist.add(c);
						CardGroup cardGroup = new CardGroup(gen.getSubType(), cardlist, pengUid);
						player.getHandCards().getOpencards().add(cardGroup);
						break;
					}
				}
				break;
			}
			if (!gangOk) {
				throw new ActionRuntimeException("gang is faild...", action.getActionType(), action.getPlayerUid());
			}

			// 补杠判断其他人是否能胡
			ArrayList<IPlayer> list = roomIns.getAllPlayer();
//			Boolean isOtherHu = false;

			// 设置抢杠平胡标志为true，杠玩家摸牌数量为0，
			// 当摸牌数量为1时设置flag为false，结束抢杠平胡状态
			player.setQiangGangFlag(true);

			for (IPlayer other : list) {
				if (other.getUid() == action.getPlayerUid())
					continue;

				ActionManager.huCheck(other,action);

				//乐成的代码
//				int step = roomIns.getEngine().getMediator().getCurrentStep();
//				if(ActionManager.huCheck(other, action))
//				{
//					ArrayList<IEPlayerAction> acList = roomIns.getEngine().getMediator()
//							.getCanExecuteActionByStep(step);
//
//					for(int i = 0; i < acList.size(); ++i)
//					{
//						HuAction huAction = (HuAction)acList.get(i);
//						huAction.setQiang(true);
//						huAction.setQiangGangOwner(action.getPlayerUid());
//						huAction.setQiangGangBei(action.getFromUid());
//
//						//设置房间状态
//						//IPlayer player = act.getRoomInstance().getPlayerById(act.getPlayerUid());
//						// 设置房间的抢状态和ID
//						 action.getRoomInstance().setIsBankerQiang(true);
//						 action.getRoomInstance().setBankerQiangUid( action.getPlayerUid() );
//					}
//
//					isOtherHu = true;
//				}
			}

//			payment(action);
			this.createCanExecuteAction(action);
		}
	}

	@Override
	public boolean checkExecute(Object... objects) {
		MJPlayer pTemp = (MJPlayer) objects[0];
		IEPlayerAction act = (IEPlayerAction) objects[1];
		if ( act.getPlayerUid() != pTemp.getUid() ) {
			return false;
		}

		HashMap<Integer,Integer> map = pTemp.getHandCards().getCardCountFromHands();
		ArrayList<CardGroup> groupList = pTemp.getHandCards().getOpencards();

		//碰完后检查当前要补的牌是否同刚碰的牌相同，如果相同，下圈才能补杠，否则可以补杠其他碰牌
		// 注释掉允许碰完马上补杠。
//		if(act.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_PENG){
//			boolean flag = false;
//			for(Map.Entry<Integer,Integer> cardCount:map.entrySet()){
//				//如果某张牌再手中数量大于1，该牌不能补杠。
//				//或者当前牌与碰的牌相同都不可以补，越过
//				if(cardCount.getValue()>1 || cardCount.getKey()==act.getCard()) continue;
//				int cardNum = cardCount.getKey();
//				for(CardGroup cg:groupList){
//					ArrayList<MJCard> cl = cg.getCardsList();
//					if(cg.getCardsList().size()==3 && cl.get(0).getCardNum()==cardNum && cl.get(1).getCardNum()==cardNum
//							&& cl.get(2).getCardNum()==cardNum){
//						//如果有牌可以补不是上一步的碰，设置flag为true
//						flag = true;
//					}
//				}
//			}
//			//如果判断没有牌可以补不是上一步的碰，直接返回false
//			if(!flag) return flag;
//		}

//		if(act.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_PENG){
//			return false;
//		}


//		if (act.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN && act.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_PENG)
//			return false;

		/*if(act.getRoomInstance().getEngine().getCardPool().size() <= 4){
			return false;
		}*/

		int gangHouDrawCard = findGangHouDrawCard(pTemp, act);
//		HashMap<Integer, Integer> map = pTemp.getHandCards().getCardCountFromHands();

		for(Map.Entry<Integer, Integer> cardCount : map.entrySet()) {
			if(cardCount.getValue() >1){
				continue;
			}
			int cardNum = cardCount.getKey();

			for (CardGroup cg : groupList) {
				if (cg.getCardsList().size() == 3 
						&& cg.getCardsList().get(0).getCardNum() == cardNum
						&& cg.getCardsList().get(1).getCardNum() == cardNum
						&& cg.getCardsList().get(2).getCardNum() == cardNum
						) {

					GangAction gangAct = new GangAction(act.getRoomInstance(), pTemp.getUid(), act.getPlayerUid(),
							cardNum, gen.getSubType());
					gangAct.setCanDoType(gen.getCanDoType());
					if(gangHouDrawCard > 0 && cardNum == gangHouDrawCard) {
						gangAct.setGanghou(true);
					}
					RoomManager.getRoomInstnaceByRoomid(pTemp.getRoomId()).addCanExecuteAction(gangAct);
				}
			}
		}

		return true;
	}
}
