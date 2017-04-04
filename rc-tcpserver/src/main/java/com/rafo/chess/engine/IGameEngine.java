package com.rafo.chess.engine;

import java.util.ArrayList;
import java.util.LinkedList;

import com.rafo.chess.engine.action.AbstractActionMediator;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.gameModel.IECardModel;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.gameModel.factory.GameModelFactory;
import com.rafo.chess.engine.room.RoomInstance;

/***
 * 玩法引擎的通用借口
 * 
 * @author Administrator
 */
public interface IGameEngine<C extends IECardModel> {
	/**
	 * 扩展的初始化方法
	 */
	public abstract void init();
	/***
	 * 销毁房间
	 */
	public boolean destroy();
	/***
	 * 洗牌
	 */
	public void shuffle();
	/**
	 * 开始游戏
	 */
	public boolean startGame() throws ActionRuntimeException;

	public RoomInstance getRoomIns();

	public AbstractActionMediator getMediator();

	public Calculator getCalculator();

	public abstract ArrayList<C> getCardPool();

	/**
	 * 获得打出的牌牌池
	 * @return	获得打出的牌集合
	 */
	public LinkedList<C> getOutCardPool();
	// 加码/翻码摸的牌
	public ArrayList<Integer> getMaCards();
	// 加码/翻码的匹配结果
	public ArrayList<ArrayList<Integer>> getMaResult();
	// 四家翻码得分
	public ArrayList<Integer> getMaScores();
	public void executeAction(int actionType, int card, int playerUid,
			int subType, String toBeCards) throws ActionRuntimeException;

	public void clean();

	/**
	 * 定庄，包括摇骰子，换位置等逻辑
	 * @return
	 */
	public void dingzhuang() throws ActionRuntimeException;
}
