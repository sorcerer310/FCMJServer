package com.rafo.chess.engine.plugin;

import com.rafo.chess.engine.game.YNMJGameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rafo.chess.engine.majiang.MJPlayer;

import java.util.ArrayList;
import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.template.impl.PluginTemplateGen;

/***
 * 玩家行为的插件基类
 * 
 * @author Administrator
 * 
 */
public abstract class AbstractPlayerPlugin<A extends IEPlayerAction> implements
		IOptPlugin<A> {
	protected PluginTemplateGen gen;

	public PluginTemplateGen getGen() {
		return gen;
	}

	public void setGen(PluginTemplateGen gen) {
		this.gen = gen;
	}

	private final Logger logger = LoggerFactory.getLogger("play");

	@SuppressWarnings("unchecked")
	public PayDetailed payment(A action){
		//初始化一些必要的基础数据
//		int actSubType = action.getSubType();																			//和牌牌型的子类型
		int actSubType = gen.getSubType();																				//和牌牌型的子类型
		int payType = action.getFromUid()==action.getPlayerUid()?0:1;													//是否为自摸，0为自摸，1为点炮
		int rate = payType==0? YNMJGameType.HuAttachType.getHuZiMoAttachScore().get(actSubType)							//根据自摸还是点炮取牌型和牌基础值
							:YNMJGameType.HuAttachType.getHuDianPaoAttachScore().get(actSubType);

		logger.debug("AbstractPlayerPlugin.payment:[actSubType:"+actSubType+",payType:"+payType+",rate:"+rate+"]");

		//如果牌型为平和，且输家或赢家中包含庄家，分数要乘2
		if(actSubType == YNMJGameType.HuAttachType.PingHu
				&& (action.getPlayerUid()==action.getRoomInstance().getBankerUid()
					|| action.getFromUid()==action.getRoomInstance().getBankerUid()))
			rate*=2;

		PayDetailed ratePay = new PayDetailed();
		ratePay.setCard(action.getCard());
		ratePay.setPlugin(this);
		ratePay.setStep(action.getStep());
		ratePay.setRate(rate);

		if(payType==0){
			//获得行为拥有者
			int toUid = action.getPlayerUid();
			//设置支付对象的目的用户,赢家1人
			ratePay.setToUid(toUid);

			ArrayList<IPlayer> players = action.getRoomInstance().getAllPlayer();
			ArrayList<Integer> fromIds = new ArrayList<>();
			//设置支付对象来源用户，输家其他人
			for (IPlayer player : players) {
				if (player.getUid() == toUid ||
						action.getRoomInstance().getLastWinner().contains(player.getUid()))
					continue;
				fromIds.add(player.getUid());
			}
			ratePay.setFromUid(fromIds);
		}else if(payType==1){
			int toUid = action.getPlayerUid();
			//设置支付对象为动作执行者
			ratePay.setToUid(toUid);
			int[] fromIds = new int[1];
			fromIds[0] = action.getFromUid();
			//设置支付对象来源用户，输家
			ratePay.setFromUid(fromIds);
		}

		ratePay.setType(action.getActionType());
		ratePay.setSubType(gen.getSubType());
		action.getRoomInstance().getEngine().getCalculator().addPayDetailed(ratePay);
		return ratePay;
	}

	/**
	 * 为当前动作生成一个支付对象
	 * 0自摸所有人支付，1点炮的人支付
	 * */
	@SuppressWarnings("unchecked")
	public PayDetailed payment_old(A action) {
		String str = gen.getEffectStr();
		if (str == null || str.equals(""))
			return null;
		String[] arr = str.split(",");
		//effectStr第二个字段为结算基础值
		int rate = Integer.parseInt(arr[1]);
		PayDetailed ratePay = new PayDetailed();
		ratePay.setCard(action.getCard());
		ratePay.setPlugin(this);
		ratePay.setStep(action.getStep());
		ratePay.setRate(rate);
		//第一个值为支付类型分不同的情况
		int payType = Integer.parseInt(arr[0]);
		//当支付类型为2时
		if (payType == 2) {
			//如果牌的拥有者与行为拥有者为同一人，支付类型设置为0，否则设置为1
			if (action.getFromUid() == action.getPlayerUid()) {
				payType = 0;
			} else {
				payType = 1;
			}
			//如果行为类型为和牌
			//此处设置了抢杠和
//			if(action.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU)
//			{
//				HuAction huAction = (HuAction)action;
//				if(huAction.isQiang())
//				{
//					//ratePay.setRate(ratePay.getRate() * 3);
//					ratePay.setRate(ratePay.getRate() * 2);		//2.1	抢杠胡从原来的6分更改为4分。
//				}
//			}
		}
		//如果支付类型为0,0为自摸
		if (payType == 0) {
			//获得行为拥有者
			int toUid = action.getPlayerUid();
			//设置支付对象的目的用户,赢家1人
			ratePay.setToUid(toUid);

			ArrayList<IPlayer> players = action.getRoomInstance().getAllPlayer();
			ArrayList<Integer> fromIds = new ArrayList<>();
			//设置支付对象来源用户，输家其他人
			for (IPlayer player : players) {
				if (player.getUid() == toUid ||
						action.getRoomInstance().getLastWinner().contains(player.getUid()))
					continue;
				fromIds.add(player.getUid());
			}
			ratePay.setFromUid(fromIds);

		}
		//否则如果支付类型为其他，一般为1点炮
		else {
			int toUid = action.getPlayerUid();
			//设置支付对象为动作执行者
			ratePay.setToUid(toUid);
			int[] fromIds = new int[1];
			fromIds[0] = action.getFromUid();
			//设置支付对象来源用户，输家
			ratePay.setFromUid(fromIds);
		}
		ratePay.setType(action.getActionType());
		ratePay.setSubType(gen.getSubType());
		action.getRoomInstance().getEngine().getCalculator().addPayDetailed(ratePay);
		return ratePay;
	}

	@Override
	public boolean doPayDetail(PayDetailed pd, RoomInstance room, Calculator calculator) {
		//如果结算对象无效或结算用户为null返回false
		if (!pd.isValid() || pd.getFromUid() == null) {
			return false;
		}
		//获得基础结算值
		int rate = pd.getRate();
		ArrayList fromPlayers = new ArrayList();
		for (int uid : pd.getFromUid()) {
			IPlayer player = room.getPlayerById(uid);
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
			calculator.addCardBalance(delP.getUid(), pd.getToUid(), 0, -rate, pd);
		}

		if(pd.bFromMa)
		{			
			logger.debug("加码计价:" );
			for (int uid : pd.getFromUid()) {
				MJPlayer p = (MJPlayer) room.getPlayerById(uid);
				logger.debug( p.getNickName() + "出" + addscore + "分");
			}
		}

		if(payNum == room.getPlayerArr().length - 1){
			calculator.addCardBalance(pd.getToUid(), 0, this.getGen().getZimoFlag(), addscore, pd);
		}else{
			calculator.addCardBalance(pd.getToUid(),pd.getFromUid()[0], this.getGen().getDianedFlag(), addscore, pd);
		}
		//暂时只加番数，没有乘以赔付的用户数

		return true;
	}

}
