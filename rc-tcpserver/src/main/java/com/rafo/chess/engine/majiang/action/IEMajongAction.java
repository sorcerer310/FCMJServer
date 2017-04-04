package com.rafo.chess.engine.majiang.action;

public interface IEMajongAction {
	/**抓*/
	public static final int PLAYER_ACTION_TYPE_CARD_GETIN = 1;
	/**打*/
	public static final int PLAYER_ACTION_TYPE_CARD_PUTOUT = 2;
	/**吃*/
	public static final int PLAYER_ACTION_TYPE_CARD_CHI = 3;
	/**碰*/
	public static final int PLAYER_ACTION_TYPE_CARD_PENG = 4;
	/**杠*/
	public static final int PLAYER_ACTION_TYPE_CARD_GANG = 5;
	/**胡*/
	public static final int PLAYER_ACTION_TYPE_CARD_HU = 6;
	/**听*/
	public static final int PLAYER_ACTION_TYPE_CARD_TING = 7;
	/**过*/
	public static final int PLAYER_ACTION_TYPE_CARD_GUO = 8;
	/**选庄*/
	public static final int ROOM_GAME_START_BANKER = 100;
	/**流局*/
	public static final int ROOM_MATCH_LIUJU=101;  
	/**发牌*/
	public static final int ROOM_MATCH_DEAL = 102;
	/**定缺*/
	public static final int ROOM_MATCH_QUE = 103;

	public static final int PRIORITY_HU = 5;
	public static final int PRIORITY_TING = 4;
	public static final int PRIORITY_GANG = 3;
	public static final int PRIORITY_PENG = 2;
	public static final int PRIORITY_CHI = 1;
	public static final int PRIORITY_NORMAL = 0;
	//所有用户都会发送消息，不参与优先级判断
	public static final int PRIORITY_COMMON = -1;
}
