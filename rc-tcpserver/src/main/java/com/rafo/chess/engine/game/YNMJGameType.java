package com.rafo.chess.engine.game;

import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import com.rafo.chess.engine.room.RoomInstance;

import java.util.HashMap;
import java.util.Map;

/**
 * 云南游戏类型
 */
public class YNMJGameType {

    private static Map<Integer, Integer> playType2ActionType = new HashMap<>();
    private static Map<Integer, Integer> huPlugin2HuType = new HashMap<>();
    static {
        //playType 对应 Action 映射
        playType2ActionType.put(PlayType.Discard, IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT);
        playType2ActionType.put(PlayType.Pong, IEMajongAction.PLAYER_ACTION_TYPE_CARD_PENG);
        playType2ActionType.put(PlayType.Kong, IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG);
        playType2ActionType.put(PlayType.DotKong, IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG);
        playType2ActionType.put(PlayType.CealedKong, IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG);
        playType2ActionType.put(PlayType.Hu, IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU);
        playType2ActionType.put(PlayType.Pass, IEMajongAction.PLAYER_ACTION_TYPE_CARD_GUO);
        playType2ActionType.put(PlayType.Ting, IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING);
        playType2ActionType.put(PlayType.Chi, IEMajongAction.PLAYER_ACTION_TYPE_CARD_CHI);
        playType2ActionType.put(PlayType.Lack, IEMajongAction.ROOM_MATCH_QUE);

        //胡牌类型
        huPlugin2HuType.put(152, HuType.PingHU);
        huPlugin2HuType.put(153, HuType.QiDuiHu);
        huPlugin2HuType.put(154, HuType.DaDuiZiHu);
        huPlugin2HuType.put(155, HuType.LongQiDuiHu);
        huPlugin2HuType.put(156, HuType.QingYiSeHu);
        huPlugin2HuType.put(157, HuType.QingDaduiHU);
        huPlugin2HuType.put(158, HuType.QingQiDuiHu);
        huPlugin2HuType.put(159, HuType.QingLongBeiHu);
    }


    public class PlayType {
        //--基本操作        1+
        public static final int Idle = 0;
        public static final int Deal = 1; // 发牌
        public static final int Draw = 2; // 摸牌
        public static final int Discard = 3; // 打牌
        public static final int CanChi = 4; //可吃
        public static final int Chi = 5;
        public static final int CanPong = 6; // 可碰
        public static final int Pong = 7; // 碰
        public static final int CanKong = 8; // 可补杠
        public static final int Kong = 9; // 补杠
        public static final int CanCealedKong = 10; // 可暗杠
        public static final int CealedKong = 11; // 暗杠
        public static final int CanDotKong = 12; // 可点杠
        public static final int DotKong = 13; // 点杠
        public static final int CanTing = 14; // 可听牌
        public static final int Ting = 15; // 听
        public static final int CanKouTing = 16; //可扣听
        public static final int KouTing = 17; //扣听
        public static final int CanHu = 18; // 可胡
        public static final int Hu = 19; // 胡牌
        public static final int He = 20;
        public static final int Pass = 21; // 过
        public static final int HaiDiLao = 22; // 海底捞

        //--特殊操作        100+
        public static final int LackStart = 100; // 开始定缺
        public static final int Lack = 101; // 定缺
        public static final int LackFinish = 102; // 定缺结束

        //结算界面使用---
        //--基本结算 300+
        public static final int R_QiangGang      = 300; //--抢杠
        public static final int R_GangShangHu    = 301; //--杠上胡
        public static final int R_GangHouPao     = 302; //--杠后炮
        public static final int R_WinSelf        = 303; //--自摸
        public static final int R_DiscardOther   = 304; //--给人点炮
        public static final int R_DotKongOther   = 305; //--给人点扛
        public static final int R_ShouBaYi       = 306; //--手把一
        public static final int R_HaiDiHu		 = 307;  //--海底胡
    }

    //玩儿法类型
    public class RoomPlayType {
        public static final int RED = 1;
        public static final int PIAO = 1<<1;
        public static final int QIDUI = 1<<2;
        public static final int YISE = 1<<3;
    }

    public static class HuType {
        public static final int PingHU = 1;
        public static final int DaDuiZiHu = 2;
        public static final int QiDuiHu = 3;
        public static final int LongQiDuiHu = 4;
        public static final int QingYiSeHu = 5;
        public static final int QingDaduiHU = 6;
        public static final int QingQiDuiHu = 7;
        public static final int QingLongBeiHu = 8;
    }

    public static class ResultType {
        public static final int None = 1; // --未叫牌
        public static final int Jiao = 2; // --叫牌
        public static final int KouTing = 3; // --扣听
        public static final int Ting = 4; // --听
        public static final int Hu = 5; // --胡
    }
    /**
     * 和牌附加加分类型
     * 161:门前清
     * 160:抢杠
     * 156:清一色
     * 157:全求人
     * 154:对对和
     */
    public static class HuAttachType{
        public static final int PingHu = 152;
        public static final int QiDui = 153;
        public static final int DuiDuiHu = 154;
        public static final int YiTiaoLong = 155;
        public static final int QingYiSe= 156;
        public static final int QuanQiuRen = 157;
        public static final int ZiYiSe = 158;
        public static final int GangShangKaiHua = 159;
        public static final int QiangGang = 160;
        public static final int MenQianQing = 161;
        public static final int QiXingDianDeng = 162;
        public static final int QiXingZaiShou = 163;
        public static final int ZiMo = 164;
        public static final int YiTaiFeng = 171;                                                                        //一本风(或叫做一台风)
        public static final int ErTaiFeng = 172;
        public static final int SanTaiFeng = 173;
        public static final int SiTaiFeng = 174;
        public static final int JieSuan10 = 175;                                                                        //结算10分
        public static final int JieSuan20 = 176;                                                                        //结算20分
        public static final int JieSuan30 = 177;                                                                        //结算30分

        private static HashMap<Integer,Integer> huZiMoAttachScore = new HashMap<>();                                    //保存自摸分数
        private static HashMap<Integer,Integer> huDianPaoAttachScore = new HashMap<>();                                 //保存点炮分数
        public static HashMap<Integer, Integer> getHuZiMoAttachScore() {return huZiMoAttachScore;}
        public static HashMap<Integer, Integer> getHuDianPaoAttachScore() {return huDianPaoAttachScore;}

        public static final int OtherJieSuanXianJia = 5;                                                                //结算闲家
        public static final int OtherJieSuanZhuangJia = 10;                                                             //结算庄家
        public static final int OtherJieSuanPlus10 = 10;                                                                //结算加10
        public static final int OtherJieSuanPlus20 = 20;                                                                //结算加20
        public static final int OtherJieSuanPlus30 = 30;                                                                //结算加30
        public static final int OtherFeiBiHuMingGang = 2;                                                               //非闭胡明杠2分
        public static final int OtherFeiBiHuAnGang = 4;                                                                 //非闭胡暗杠4分
        public static final int OtherBiHuMingGang = 5;                                                                  //闭胡明杠5分
        public static final int OtherBiHuAnGang = 10;                                                                   //闭胡暗杠10分

        public static final int OtherBuGane = 2;                                                                        //补杠2分


        static{
            huZiMoAttachScore.put(MenQianQing,10);                                                                      //门清
            huZiMoAttachScore.put(QiDui,20);                                                                            //七对
            huZiMoAttachScore.put(YiTiaoLong,20);                                                                       //一条龙
            huZiMoAttachScore.put(QiXingDianDeng,20);                                                                   //七星点灯
            huZiMoAttachScore.put(QiXingZaiShou,10);                                                                    //七星在手
            huZiMoAttachScore.put(QingYiSe,30);                                                                         //清一色
            huZiMoAttachScore.put(GangShangKaiHua,20);                                                                  //杠上开花
            huZiMoAttachScore.put(QiangGang,20);                                                                        //抢杠和
            huZiMoAttachScore.put(DuiDuiHu,10);                                                                         //对对和
            huZiMoAttachScore.put(QuanQiuRen,0);                                                                        //全求人
            huZiMoAttachScore.put(ZiYiSe,50);                                                                           //字一色
            huZiMoAttachScore.put(PingHu,5);                                                                            //平和如果庄家分数要*2
            huZiMoAttachScore.put(YiTaiFeng,0);                                                                         //一台风，台风分单独结算
            huZiMoAttachScore.put(ErTaiFeng,0);                                                                         //一台风，台风分单独结算
            huZiMoAttachScore.put(SanTaiFeng,0);                                                                        //一台风，台风分单独结算
            huZiMoAttachScore.put(SiTaiFeng,0);                                                                         //一台风，台风分单独结算


            huDianPaoAttachScore.put(MenQianQing,0);                                                                    //门清
            huDianPaoAttachScore.put(QiDui,20);                                                                         //七对
            huDianPaoAttachScore.put(YiTiaoLong,20);                                                                    //一条龙
            huDianPaoAttachScore.put(QiXingDianDeng,20);                                                                //七星点灯
            huDianPaoAttachScore.put(QiXingZaiShou,20);                                                                 //七星在手
            huDianPaoAttachScore.put(QingYiSe,30);                                                                      //清一色
            huDianPaoAttachScore.put(GangShangKaiHua,20);                                                               //杠上开花
            huDianPaoAttachScore.put(QiangGang,20);                                                                     //抢杠和
            huDianPaoAttachScore.put(DuiDuiHu,10);                                                                      //对对和
            huDianPaoAttachScore.put(QuanQiuRen,20);                                                                    //全求人
            huDianPaoAttachScore.put(ZiYiSe,50);                                                                        //字一色
            huDianPaoAttachScore.put(PingHu,5);                                                                         //平和如果庄家分数要*2
            huDianPaoAttachScore.put(YiTaiFeng,0);                                                                      //一台风，台风分单独结算
            huDianPaoAttachScore.put(ErTaiFeng,0);                                                                      //一台风，台风分单独结算
            huDianPaoAttachScore.put(SanTaiFeng,0);                                                                     //一台风，台风分单独结算
            huDianPaoAttachScore.put(SiTaiFeng,0);                                                                      //一台风，台风分单独结算
        }

        /**
         * 增加显示本风的附加项
         * @param player    要增加本风项目的玩家
         * @param c         增加的本风数量
         */
        public static void addHuTypeBenFeng(MJPlayer player, int c){
            switch (c){
                case 1:
                    player.getHuAttachType().add(YiTaiFeng);
                    break;
                case 2:
                    player.getHuAttachType().add(ErTaiFeng);
                    break;
                case 3:
                    player.getHuAttachType().add(SanTaiFeng);
                    break;
                case 4:
                    player.getHuAttachType().add(SiTaiFeng);
                    break;
                default:
                    break;
            }
        }

        /**
         * 获得明杠分数
         * @param room  根据房间类型返回不同的分数
         */
        public static int getMingGang(RoomInstance room){
            return room.getAttribute(RoomAttributeConstants.YB_PLAY_TYPE)==0?OtherBiHuMingGang:OtherFeiBiHuMingGang;

        }

        /**
         * 获得暗杠分数
         * @param room  根据房间类型返回不同的分数
         * @return
         */
        public static int getAnGang(RoomInstance room){
            return room.getAttribute(RoomAttributeConstants.YB_PLAY_TYPE)==0?OtherBiHuAnGang:OtherFeiBiHuAnGang;

        }
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
