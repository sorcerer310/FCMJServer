package com.rafo.chess.engine.game;

import com.rafo.chess.engine.majiang.action.IEMajongAction;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/17.
 */
public class GYMJGameType {

    private static Map<Integer, Integer> playType2ActionType = new HashMap<>();
    private static Map<Integer, Integer> playType2ActionSubType = new HashMap<>();
    private static Map<Integer, Integer> actionType2CanDoPlayType = new HashMap<>();
    private static Map<Integer, Integer> actionType2PlayType = new HashMap<>();
    private static Map<Integer, Integer> huPlugin2HuType = new HashMap<>();
    static {
        actionType2CanDoPlayType.put(9, PlayType.NormalChicken);
        actionType2CanDoPlayType.put(10, PlayType.ChargeChicken);
        actionType2CanDoPlayType.put(11, PlayType.DutyChicken);
        actionType2CanDoPlayType.put(12, PlayType.CanDotKong);
        actionType2CanDoPlayType.put(13, PlayType.CanCealedKong);
        actionType2CanDoPlayType.put(14, PlayType.CanKong);
        actionType2CanDoPlayType.put(19, PlayType.CanReadyHand);
        actionType2CanDoPlayType.put(23, PlayType.Draw);
        actionType2CanDoPlayType.put(24, PlayType.CanPong);
        actionType2CanDoPlayType.put(25, PlayType.He);
        actionType2CanDoPlayType.put(27, PlayType.Deal);


        actionType2PlayType.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN, PlayType.Draw);
        actionType2PlayType.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT, PlayType.Draw);
        actionType2PlayType.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_PENG, PlayType.CanPong);
        actionType2PlayType.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING, PlayType.CanReadyHand);
        actionType2PlayType.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU, PlayType.CanHu);
        actionType2PlayType.put(IEMajongAction.ROOM_MATCH_LIUJU, PlayType.He);

        actionType2PlayType.put(9, PlayType.NormalChicken);
        actionType2PlayType.put(10, PlayType.ChargeChicken);
        actionType2PlayType.put(11, PlayType.DutyChicken);
        actionType2PlayType.put(12, PlayType.DotKong);
        actionType2PlayType.put(13, PlayType.CealedKong);
        actionType2PlayType.put(14, PlayType.Kong);
        actionType2PlayType.put(15, PlayType.GangShangHu);
        actionType2PlayType.put(16, PlayType.GangHouPao);
        actionType2PlayType.put(17, PlayType.QiangGang);
        actionType2PlayType.put(18, PlayType.HardReadHand);
        actionType2PlayType.put(19, PlayType.ReadyHand);
        actionType2PlayType.put(20, PlayType.FlopChicken);
        actionType2PlayType.put(22, PlayType.Discard);
        actionType2PlayType.put(23, PlayType.Draw);
        actionType2PlayType.put(24, PlayType.Pong);
        actionType2PlayType.put(25, PlayType.He);
        actionType2PlayType.put(27, PlayType.Deal);
        actionType2PlayType.put(PlayType.DotKongOther, PlayType.DotKongOther);
        actionType2PlayType.put(PlayType.KillReadyHand, PlayType.KillReadyHand);
        actionType2PlayType.put(PlayType.BeKilledReadHand, PlayType.BeKilledReadHand);
        actionType2PlayType.put(PlayType.DiscardOther, PlayType.DiscardOther);

        playType2ActionSubType.put(PlayType.Kong, 14);
        playType2ActionSubType.put(PlayType.DotKong, 12);
        playType2ActionSubType.put(PlayType.CealedKong, 13);

        playType2ActionType.put(PlayType.Discard, IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT);
        playType2ActionType.put(PlayType.Pong, IEMajongAction.PLAYER_ACTION_TYPE_CARD_PENG);
        playType2ActionType.put(PlayType.Kong, IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG);
        playType2ActionType.put(PlayType.DotKong, IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG);
        playType2ActionType.put(PlayType.CealedKong, IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG);
        playType2ActionType.put(PlayType.Hu, IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU);
        playType2ActionType.put(PlayType.Pass, IEMajongAction.PLAYER_ACTION_TYPE_CARD_GUO);
        playType2ActionType.put(PlayType.ReadyHand, IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING);


        //胡牌类型
        huPlugin2HuType.put(-1, HuType.Unknow.ordinal());
        huPlugin2HuType.put(0, HuType.Ting.ordinal());
        huPlugin2HuType.put(1, HuType.PingHU.ordinal());
        huPlugin2HuType.put(2, HuType.DaDuiZiHu.ordinal());
        huPlugin2HuType.put(3, HuType.LongQiDuiHu.ordinal());
        huPlugin2HuType.put(4, HuType.QingYiSeHu.ordinal());
        huPlugin2HuType.put(5, HuType.QingQiDuiHu.ordinal());
        huPlugin2HuType.put(6, HuType.QingDaduiHU.ordinal());
        huPlugin2HuType.put(7, HuType.QingLongBeiHu.ordinal());
    }



    public class PlayType {
        public static final int Idle = 0;
        public static final int Deal = 1; // 发牌
        public static final int Draw = 2; // 摸牌
        public static final int Discard = 3; // 打牌
        public static final int CanPong = 4; // 可碰
        public static final int Pong = 5; // 碰
        public static final int CanKong = 6; // 可明杠
        public static final int Kong = 7; // 明杠
        public static final int CanCealedKong = 8; // 可暗杠
        public static final int CealedKong = 9; // 暗杠
        public static final int CanDotKong = 10; // 可点杠
        public static final int DotKong = 11; // 点杠
        public static final int CanHu = 12; // 可胡
        public static final int Hu = 13; // 胡牌
        public static final int He = 14;
        public static final int CanReadyHand = 15; // 可听牌
        public static final int ReadyHand = 16; // 听牌(软听)
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
        public static final int CanChi = 32; // 被杀报
        public static final int Chi = 33; // 被杀报
    }

    public enum HuType
    {
        Unknow, // 未叫嘴
        Ting,   // 叫嘴
        PingHU,
        DaDuiZiHu,
        QiDuiHu,
        LongQiDuiHu,
        QingYiSeHu,
        QingDaduiHU,
        QingQiDuiHu,
        QingLongBeiHu,
    }

    public static int getCanDoPlayType(int actionType, int actionSubType){
        System.out.println("getCanDoPlayType : " + actionType + " : " + actionSubType);
        if(actionType == IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU){
            return PlayType.CanHu; //胡牌暂不比较subType
        }
        return  actionSubType > 0 ?
                actionType2CanDoPlayType.get(actionSubType) :
                actionType2PlayType.get(actionType);
    }

    public static int getPlayType(int actionType){
        return  actionType2PlayType.get(actionType);
    }

    public static int[] getActionTypeByPlayType(int playType){
        int[] actionType = {0,0};
        actionType[0] = playType2ActionType.get(playType);
        if(playType2ActionSubType.get(playType) != null){
            actionType[1] = playType2ActionSubType.get(playType);
        }
        return actionType;
    }

    public static int getWinType(int winType){
        return huPlugin2HuType.get(winType);
    }
}
