package com.rafo.chess.engine;

import java.util.ArrayList;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rafo.chess.engine.action.AbstractActionMediator;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.gameModel.IECardModel;
import com.rafo.chess.engine.gameModel.factory.GameModelFactory;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.template.impl.RoomSettingTemplateGen;
import com.rafo.chess.utils.MathUtils;

/***
 * 玩法引擎抽象类
 * 
 * @author Administrator
 */
public abstract class AbstractGameEngine<C extends IECardModel> implements
		IGameEngine<C> {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	/** 牌池，未使用的牌 */
	protected ArrayList<C> cardPool = new ArrayList<C>();
	/** 打出的牌 */
	protected LinkedList<C> outCardPool = new LinkedList<C>();
	/** 行为管理的中介,由子类初始化 */
	protected AbstractActionMediator mediator = null;
	/** 房间实例 */
	protected RoomInstance roomIns = null;
	/** 结算器*/
	protected Calculator calculator ;
	// 加码/翻码摸的牌
	protected ArrayList<Integer> maArrayList = new ArrayList<Integer>();
	// 四家加码结果
	protected ArrayList<ArrayList<Integer>> jiamaResultArrayList = new ArrayList<ArrayList<Integer>>();	// 固定顺序:庄家、下家、对家、上家
	// 四家翻码得分
	protected ArrayList<Integer> maScores = new ArrayList<Integer>();

 	public AbstractGameEngine(RoomInstance roomIns) {
		this.roomIns = roomIns;
		this.calculator = new Calculator(roomIns);
		init();
	}
	public Calculator getCalculator() {
		return calculator;
	}
	public ArrayList<C> getCardPool() {
		return cardPool;
	}

	public LinkedList<C> getOutCardPool() {
		return outCardPool;
	}
	
	// 加码/翻码牌
	public ArrayList<Integer> getMaCards()
	{
		return maArrayList;
	}
	// 加码/翻码结果
	public ArrayList<ArrayList<Integer>> getMaResult()
	{
		return jiamaResultArrayList;
	}
	// 加码/翻码得分
	public ArrayList<Integer> getMaScores()
	{
		return maScores;
	}

	public AbstractActionMediator getMediator() {
		return mediator;
	}

	public RoomInstance getRoomIns() {
		return roomIns;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void shuffle() {
		RoomSettingTemplateGen gen = roomIns.getRstempateGen();
		String cards = gen.getCardNumPool();
		ArrayList<C> cardList = new ArrayList<C>();
		for (String card : cards.split(",")) {
			Integer num = Integer.parseInt(card);
//			if((int)roomIns.getAttribute(RoomAttributeConstants.YB_PLAY_TYPE) == 0)
//			{
//				//不带风就是 东西南北中发白都没有
//				if(num > 40 && num < 48)
//					continue;
//			}
			
			for(int i = 0;i<4;i++)
			{
				C c = (C) GameModelFactory.createCard(num, gen.getCardType());
				cardList.add(c);
			}
			
		}
		cardPool = new ArrayList<C>();
		while (cardList.size() > 0) {
			int index = MathUtils.random(0, cardList.size() - 1);
			C iecm = cardList.remove(index);
			cardPool.add(iecm);
		}
	}
	@Override
	public boolean destroy() {
		return false;
	}

	public void clean(){
		this.cardPool.clear();
		this.outCardPool.clear();
		this.maArrayList.clear();
		this.jiamaResultArrayList.clear();
		this.maScores.clear();
	}
}
