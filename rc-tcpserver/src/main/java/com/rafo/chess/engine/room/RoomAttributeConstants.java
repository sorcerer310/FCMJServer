package com.rafo.chess.engine.room;

public class RoomAttributeConstants {

	/** 房间局数设置	0:4局  1:8局  2:16局 */
	public final static String GY_GAME_ROOM_COUNT = "gameType";
	/** 新玩儿法类型	0：闭和 1：非闭和 */
	public final static String YB_PLAY_TYPE = "gamePlayType";
	/** 加码类型		0：不加码 1:加码10:加码20 3:加码30 */
	public final static String YB_MA_TYPE = "gameMaType";


	/** 房间基础番*/
	public final static String ROOM_BASE_RATE="baseRate";
	/** 房间骰子*/
	public final static String ROOM_ROOL_THE_DICE="roolTheDice";
	/** 首局定座位时的用户和筛子信息 **/
	public final static String ROOM_SEAT_DICE="playerSeatDice";

	public final static String ROOM_RED_DICE="readDice";
	/** 筛子定位*/
	public final static String ROOM_ROOL_THE_DICE_CHOOSE_SIT="roolTheDiceChooseSit";

	/** 是否缺一门 */
	public final static String ROOM_QUEYIMEN="queYiMen";


	//创建房间时的玩儿法
	/** 带红点 */
	public final static String ROOM_PLAY_TYPE_RED = "playtype_red";
	/** 带飘 */
	public final static String ROOM_PLAY_TYPE_PIAO = "playtype_piao";
	/** 带七小对 */
	public final static String ROOM_PLAY_TYPE_QIDUI = "playtype_qidui";
	/** 带一色 */
	public final static String ROOM_PLAY_TYPE_YISE = "playtype_yise";

	/** 定缺 */
	public final static String YN_GAME_QUE = "que";
	
	//局数消耗钻石
	/** 4局 钻石x2 */
	public final static int ROOM_ZUANSHI_4 = 1;
	/** 8局 钻石x3 */
	public final static int ROOM_ZUANSHI_8 = 2;
	/** 16局 钻石x5 */
	public final static int ROOM_ZUANSHI_16 = 4;
}
