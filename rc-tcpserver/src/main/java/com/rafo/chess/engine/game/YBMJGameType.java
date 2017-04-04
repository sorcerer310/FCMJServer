package com.rafo.chess.engine.game;

import com.rafo.chess.engine.majiang.action.IEMajongAction;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/17.
 */
public class YBMJGameType {

    private static Map<Integer, Integer> playType2ActionType = new HashMap<>();
    private static Map<Integer, Integer> huPlugin2HuType = new HashMap<>();
    //客户端每种Action的优先级排序
    public static Map<Integer, Integer> clientActionPriority = new HashMap<>();
    static {
        //playType 对应 Action 映射
        playType2ActionType.put(PlayType.Discard, IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT);
        playType2ActionType.put(PlayType.Pong, IEMajongAction.PLAYER_ACTION_TYPE_CARD_PENG);
        playType2ActionType.put(PlayType.Kong, IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG);
        playType2ActionType.put(PlayType.DotKong, IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG);
        playType2ActionType.put(PlayType.ChuanXinGang, IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG);
        playType2ActionType.put(PlayType.XuanFengGang, IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG);
        playType2ActionType.put(PlayType.SanJianKe, IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG);
        playType2ActionType.put(PlayType.CealedKong, IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG);
        playType2ActionType.put(PlayType.Hu, IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU);
        playType2ActionType.put(PlayType.Pass, IEMajongAction.PLAYER_ACTION_TYPE_CARD_GUO);
        playType2ActionType.put(PlayType.KouTing, IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING);
        playType2ActionType.put(PlayType.ReadyHand, IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING);
        playType2ActionType.put(PlayType.Chi, IEMajongAction.PLAYER_ACTION_TYPE_CARD_CHI);

        //胡牌类型
        huPlugin2HuType.put(-1, HuType.Unknow.ordinal());
        huPlugin2HuType.put(16, HuType.Kouting.ordinal());
        huPlugin2HuType.put(42, HuType.Ting.ordinal());
        huPlugin2HuType.put(152, HuType.PingHU.ordinal());
        huPlugin2HuType.put(153, HuType.QiDuiHu.ordinal());
        huPlugin2HuType.put(154, HuType.DaDuiZiHu.ordinal());
        huPlugin2HuType.put(155, HuType.LongQiDuiHu.ordinal());
        huPlugin2HuType.put(156, HuType.QingYiSeHu.ordinal());
        huPlugin2HuType.put(157, HuType.QingDaduiHU.ordinal());
        huPlugin2HuType.put(158, HuType.QingQiDuiHu.ordinal());
        huPlugin2HuType.put(159, HuType.QingLongBeiHu.ordinal());
    }



    public class PlayType {
        public static final int Idle = 0;
        public static final int Deal = 1; // 发牌
        public static final int Draw = 2; // 摸牌
        public static final int Discard = 3; // 打牌
        public static final int CanPong = 4; // 可碰
        public static final int Pong = 5; // 碰
        public static final int CanKong = 6; // 可补杠
        public static final int Kong = 7; // 补杠
        public static final int CanCealedKong = 8; // 可暗杠
        public static final int CealedKong = 9; // 暗杠
        public static final int CanDotKong = 10; // 可点杠
        public static final int DotKong = 11; // 点杠
        public static final int CanHu = 12; // 可胡
        public static final int Hu = 13; // 胡牌
        public static final int He = 14;
        public static final int CanKouTing = 15; // 可扣听牌（未开门）
        public static final int KouTing = 16; // 扣听
        public static final int OffLine = 17; // 离线
        public static final int Pass = 18; // 过

        public static final int ChargeChicken = 19; // 冲锋鸡
        public static final int DutyChicken = 20; // 责任鸡
        // 以下的不参与出牌操作，用于结算
        public static final int NormalChicken = 21; // 普通鸡
        public static final int FlopChicken = 22; // 翻牌鸡
        public static final int KillReadyHand = 23; // 杀胡
        public static final int QiangGang = 24; // 抢杠
        public static final int GangShangHu = 25; // 杠上胡
        public static final int GangHouPao = 26; // 杠后炮
        public static final int WinSelf = 27; // 自摸
        public static final int DiscardOther = 28; // 点炮
        public static final int DotKongOther = 29; // 点明杠
        public static final int HardReadHand = 30; // 硬听
        public static final int BeKilledReadHand = 31; // 被杀报
        public static final int CanChi = 33; //可吃
        public static final int Chi = 34;
        public static final int CanXuanFengGang = 35; //东南西北杠
        public static final int XuanFengGang = 36;
        public static final int CanSanJianKe = 37; //中发白杠
        public static final int SanJianKe = 38;
        public static final int CanChuanXinGang = 39;
        public static final int ChuanXinGang = 40;
        public static final int CanReadyHand = 41; //听牌（开门后）
        public static final int ReadyHand = 42; //

        public static final int KaiMenGang = 81;  // 开门杠
        public static final int DaGang = 82; //大杠
        public static final int ShouBaYi = 83; // 手把一
        public static final int PiaoKouQing = 84; // 飘扣听
        public static final int YiSeKouQing = 85; // 一色扣听
        public static final int YiSePiaoKouQing = 86; // 一色飘扣听
        public static final int KouTingAnGang = 87; //扣听暗杠
        public static final int GangHouDianGang = 88; //杠后
        public static final int GangHouAnGang = 89; //杠后

        public static final int HaiDi = 100;

        public static final int Unknow = 150;
        public static final int Ting = 151;
        public static final int PingHU = 152;
        public static final int QiDuiHu = 153;
        public static final int DaDuiZiHu = 154;
        public static final int LongQiDuiHu = 155;
        public static final int QingYiSeHu = 156;
        public static final int QingDaduiHU = 157;
        public static final int QingQiDuiHu = 158;
        public static final int QingLongBeiHu = 159;

        public static final int LOST_EAST = 301; //失分项，东北西南
        public static final int LOST_NORTH = 302;
        public static final int LOST_WEST = 303;
        public static final int LOST_SOUTH = 304;
    }

    //玩儿法类型
    public class RoomPlayType {
        public static final int RED = 1;
        public static final int PIAO = 1<<1;
        public static final int QIDUI = 1<<2;
        public static final int YISE = 1<<3;
    }

    public enum HuType {
        Unknow, // 未叫嘴
        Kouting,   // 叫嘴
        Ting,
        PingHU,
        DaDuiZiHu,
        QiDuiHu,
        LongQiDuiHu,
        QingYiSeHu,
        QingDaduiHU,
        QingQiDuiHu,
        QingLongBeiHu,
    }


    public static int[] getActionTypeByPlayType(int playType){
        int[] actionType = {0,0};
        actionType[0] = playType2ActionType.get(playType);
        actionType[1] = playType;
        return actionType;
    }

    public static int getWinType(int winType){
        if(huPlugin2HuType.get(winType) == null){
            return 0;
        }
        return huPlugin2HuType.get(winType);
    }
}
