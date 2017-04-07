package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.game.YNMJGameType;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.engine.plugin.impl.HuPlugin;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.engine.room.RoomManager;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/***
 *
 * @author Administrator
 */
public abstract class YXHuPlugin extends HuPlugin {
    private final Logger logger = LoggerFactory.getLogger("cacl");

    @Override
    public void createCanExecuteAction(HuAction action) {
        MJPlayer p = (MJPlayer) action.getRoomInstance().getPlayerArr()[action.getRoomInstance().nextFocusIndex()];
        ActionManager.moCheck(p);
    }

    @Override
    public void doOperation(HuAction action) throws ActionRuntimeException {
        if (!this.analysis(action))
            return;

        super.doOperation(action);
        //获得和牌的用户
        MJPlayer player = (MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());
        player.setJiaozui(gen.getTempId());
        RoomInstance roomIns = action.getRoomInstance();

        //--------------此部分为一炮多响---------------
        // 计算其他的胡牌的人
        int step = roomIns.getEngine().getMediator().getCurrentStep();
        ArrayList<IEPlayerAction> actionList = roomIns.getEngine().getMediator().getCanExecuteActionByStep(step - 1);

        if (actionList == null) {
            return;
        }

        for (IEPlayerAction act : actionList) {
            if (act.getActionType() != action.getActionType()) continue;
            if (act.getPlayerUid() == action.getPlayerUid()) continue;
            if (act.getStatus() == 1) continue;

            int index = roomIns.getPlayerById(act.getPlayerUid()).getIndex();
            roomIns.setFocusIndex(index);

            int nextStep = roomIns.getEngine().getMediator().getNextStep();
            act.setStep(nextStep);
            act.setStatus(1);
            roomIns.getEngine().getMediator().getDoneActionList().add(act);
            act.doAction();
        }

        //加码已不使用
//        if (!isMaRoom(roomIns))
//            action.getRoomInstance().setRoomStatus(RoomInstance.RoomState.calculated.getValue());
//        else {
//            action.getRoomInstance().setRoomStatus(RoomInstance.RoomState.jiama.getValue());
//        }
        //--------------此部分为一炮多响---------------


        //此处将界面转为结算界面
        action.getRoomInstance().setRoomStatus(RoomInstance.RoomState.calculated.getValue());
    }

    @Override
    public boolean doPayDetail(PayDetailed pd, RoomInstance room, Calculator calculator) {
        //结算细节对象无效并结算对象为null返回false
//        if (!pd.isValid() && pd.getFromUid() != null) {
//            return false;
//        }

        logger.debug("computeScore YXHuPlugin.doPayDetail: pd.isValid:" + pd.isValid() + ",pd.getFromUid():" + ArrayUtils.toString(pd.getFromUid()));

//		logger.debug("十三doPayDetail胡牌:");
//		// 把加码的帐都算到被抢杠人的头上
//		if(room.IsBankerQiang() && pd.bFromMa)//相关变量每局结束还原
//		{
//			logger.debug("jiama:把加码的帐都算到被抢杠人的头上");
//			List<Integer> id = new ArrayList<>();
//			id.add( room.getBankerQiangUid() );
//			pd.setFromUid(id);
//		}

        boolean flag = false;

        //判断是否抢杠，如果抢杠标记生效，则被抢杠的人包三家
        if (isQiangGangChargeAll(pd, room))
            flag = computeScoreYXChargeAll(pd, room, calculator);
        else
            flag = computeScoreYX(pd, room, calculator);

        return flag;
    }

    /**
     * 永修算分程序
     * （旧算法，已废弃）分数为：牌型分 + 附加分数 + 结算分 + 加码 + 本风分 + 杠分
     * 分数为：牌型分 + 附加分数 + 加码分 + 结算分 * 本风数量 + 杠分
     *
     * @param pd
     * @param room
     * @param calculator
     * @return
     */
    protected boolean computeScoreYX(PayDetailed pd, RoomInstance room, Calculator calculator) {
        //所有玩家
        ArrayList<IPlayer> allPlayer = room.getAllPlayer();
        //赢得玩家
        MJPlayer player = (MJPlayer) room.getPlayerById(pd.getToUid());
        //获得输的玩家
        ArrayList<IPlayer> fromPlayers = new ArrayList();
        for (int uid : pd.getFromUid())
            if (room.getPlayerById(uid) != null)
                fromPlayers.add(room.getPlayerById(uid));
        //如果输家为0，返回false
        if (fromPlayers.size() == 0)
            return false;

        //所有的基本分项算完后，在BattlePayStep.calculator中进行赢家与输家的分数计算

        //1:牌型分
        int addscore = computeSubTypeScore(pd);

        //2:附加分
        computeAttachScore(pd, player);

        //3:加码分
        computeJiamaScore(pd, player);

        //4:结算分*本风分
        computeJiesuanBenfengScore(player, allPlayer, fromPlayers, room, pd);

        //5:杠分，三家出杠分，一家赢
        computeGangScore(pd, allPlayer, room, calculator);

        //6:后续一些其他操作
        computeOther(pd, player, fromPlayers, room, calculator, addscore);

        logger.debug("computeScoreYX:addscore=" + addscore + ",attach:" + pd.getYxpd().attachScore + ",jiama:" + pd.getYxpd().jiamaScore
                + ",jiesuanBenfeng:" + pd.getYxpd().jiesuanBenfengScore + ",jiesuanBenfeng:" + pd.getYxpd().jiesuanBenfengDelScore
                + ",gang:" + pd.getYxpd().gangScore + ",gangDel:" + pd.getYxpd().gangDelScore);

        return false;
    }

    /**
     * 永修包三家计算代码，包三家时用该代码结算分数
     *
     * @param pd   支付细节对象
     * @param room 房间实例
     * @return
     */
    protected boolean computeScoreYXChargeAll(PayDetailed pd, RoomInstance room, Calculator calculator) {
        ArrayList<IPlayer> allPlayer = room.getAllPlayer();                                                                //所有玩家
        MJPlayer player = (MJPlayer) room.getPlayerById(pd.getToUid());
        ArrayList<IPlayer> fromPlayers = new ArrayList<>();
        for (int uid : pd.getFromUid())
            if (room.getPlayerById(uid) != null)
                fromPlayers.add(room.getPlayerById(uid));

        //如果输家为0，返回false
        if (fromPlayers.size() == 0) return false;

        //1:牌型分
        int addscore = computeSubTypeChargeAllScore(pd, 3);

        //2:附加分
        computeAttachChargeAllScore(pd, player, 3);

        //3:加码分
        computeJiamaChargeAllScore(pd, player, 3);

        //4:结算分*本风分
        computeJiesuanBenfengChargeAllScore(player, allPlayer, room, pd);

        //5:杠分，三家出杠分，一家赢
        computeGangScore(pd, allPlayer, room, calculator);

        //6:后续一些其他操作
        computeOtherChargeAll(pd, player, calculator, addscore);

        logger.debug("computeScoreYX:addscore=" + addscore + ",attach:" + pd.getYxpd().attachScore + ",jiama:" + pd.getYxpd().jiamaScore
                + ",jiesuanBenfeng:" + pd.getYxpd().jiesuanBenfengScore + ",jiesuanBenfeng:" + pd.getYxpd().jiesuanBenfengDelScore
                + ",gang:" + pd.getYxpd().gangScore + ",gangDel:" + pd.getYxpd().gangDelScore);

        return false;
    }

    /**
     * 永修双倍包三家计算代码，当同时输两个包三家时使用该函数。如 全求人、清一色同时包三家。
     *
     * @param pd   支付细节对象，需要在pd中重置输的玩家(fromPlayer)到清一色包三家的玩家上，两个包三家的钱全部由该玩家出
     * @param room 房间实例
     * @return
     */
    protected boolean computeScoreYXChargeAll2(PayDetailed pd, RoomInstance room, Calculator calculator) {
        ArrayList<IPlayer> allPlayer = room.getAllPlayer();                                                                //所有玩家
        MJPlayer player = (MJPlayer) room.getPlayerById(pd.getToUid());
        ArrayList<IPlayer> fromPlayers = new ArrayList<>();
        for (int uid : pd.getFromUid())
            if (room.getPlayerById(uid) != null)
                fromPlayers.add(room.getPlayerById(uid));

        //如果输家为0，返回false
        if (fromPlayers.size() == 0) return false;

        //1:牌型分
        int addscore = computeSubTypeChargeAllScore(pd, 6);

        //2:附加分
        computeAttachChargeAllScore(pd, player, 6);

        //3:加码分
        computeJiamaChargeAllScore(pd, player, 6);

        //4:结算分*本风分
        computeJiesuanBenfengChargeAllScore(player, allPlayer, room, pd);

        //5:杠分，三家出刚分，一家赢
        computeGangScore(pd, allPlayer, room, calculator);

        //6:后续一些其他操作
        computeOtherChargeAll(pd, player, calculator, addscore);

        logger.debug("computeScoreYX:addscore=" + addscore + ",attach:" + pd.getYxpd().attachScore + ",jiama:" + pd.getYxpd().jiamaScore
                + ",jiesuanBenfeng:" + pd.getYxpd().jiesuanBenfengScore + ",jiesuanBenfeng:" + pd.getYxpd().jiesuanBenfengDelScore
                + ",gang:" + pd.getYxpd().gangScore + ",gangDel:" + pd.getYxpd().gangDelScore);

        return false;
    }

    /**
     * 计算牌型分
     *
     * @param pd 支付细节对象
     */
    private int computeSubTypeScore(PayDetailed pd) {
        return computeSubTypeChargeAllScore(pd, 1);
    }

    /**
     * 计算牌型分
     *
     * @param pd   支付细节对象
     * @param mult 3倍就为包三家的分数,1倍为普通结算分数
     * @return
     */
    protected int computeSubTypeChargeAllScore(PayDetailed pd, int mult) {
        if (pd.getSubType() == YNMJGameType.HuAttachType.PingHu)
//            平胡不算平胡分了，只计算本风的基础分就行。此处的分不光被平胡分使用，还被其他特殊牌型分使用
            pd.getYxpd().subTypeScore = 0;
        else
            pd.getYxpd().subTypeScore = pd.getRate() * mult;
        return pd.getYxpd().subTypeScore;
    }

    /**
     * 计算牌型分
     *
     * @param pd        支付细节对象
     * @param winplayer 赢得玩家
     */
    private void computeAttachScore(PayDetailed pd, MJPlayer winplayer) {
        computeAttachChargeAllScore(pd, winplayer, 1);
    }

    /**
     * 计算牌型分
     *
     * @param pd        支付细节对象
     * @param winplayer 赢得玩家
     * @param mult      3倍就为包三家的分数,1倍为普通结算分数
     * @return
     */
    protected void computeAttachChargeAllScore(PayDetailed pd, MJPlayer winplayer, int mult) {
        logger.debug("\tYXHuPlugin.computeAttachChargeAllScore start:[winplayer.getHuAttachType().size():" + winplayer.getHuAttachType().size() + "]");
        int attachScore = 0;
        //当pd.getFromUids()的长度为3时表示当前和牌为自摸和，否则为点炮
        HashMap<Integer, Integer> hmHuAttach
                = pd.getFromUids().length > 1
                ? YNMJGameType.HuAttachType.getHuZiMoAttachScore()
                : YNMJGameType.HuAttachType.getHuDianPaoAttachScore();
        for (Integer hat : winplayer.getHuAttachType()) {
            //判断hat是否为null，打印日志
            if (hat == null) logger.debug("YXHuPlugin.computeAttachChargeAllScore:hat is null");
            else logger.debug("YXHuPlugin.computeAttachChargeAllScore:[hat:" + hat + "]");
            //判断hmHuAttach是否为null
            if (hmHuAttach == null) logger.debug("YXHuPlugin.computeAttachChargeAllScore:hmHuAttach is null");
            //判断winplayer是否为null
            if (winplayer == null) logger.debug("YXHuPlugin.computeAttachChargeAllScore:winplayer is null");

            //自摸不在此处记分,如果hmHuAttach.get(hat)为null，也不符合条件
            if (hat != null && hmHuAttach.get(hat) != null && hat != YNMJGameType.HuAttachType.ZiMo)
                attachScore += hmHuAttach.get(hat);
        }
        pd.getYxpd().attachScore = attachScore * mult;
        logger.debug("\tYXHuPlugin.computeAttachChargeAllScore end:[attachScore:" + attachScore + ",mult:" + mult + "]");
    }

    /**
     * 计算加码分
     *
     * @param pd
     * @param winplayer
     */
    private void computeJiamaScore(PayDetailed pd, MJPlayer winplayer) {
        computeJiamaChargeAllScore(pd, winplayer, 1);
    }

    /**
     * 计算加码分
     *
     * @param pd
     * @param winplayer
     * @param mult      3倍就为包三家的分数,1倍为普通结算分数
     */
    protected void computeJiamaChargeAllScore(PayDetailed pd, MJPlayer winplayer, int mult) {
        int jiamaScore = 0;
        int maType = (int) RoomManager.getRoomInstnaceByRoomid(winplayer.getRoomId()).getAttribute(RoomAttributeConstants.YB_MA_TYPE);
        if (maType == 0)
            jiamaScore = 0;
        else if (maType == 1) {
            jiamaScore = YNMJGameType.HuAttachType.OtherJieSuanPlus10;
            winplayer.getHuAttachType().add(YNMJGameType.HuAttachType.JieSuan10);
        } else if (maType == 2) {
            jiamaScore = YNMJGameType.HuAttachType.OtherJieSuanPlus20;
            winplayer.getHuAttachType().add(YNMJGameType.HuAttachType.JieSuan20);
        } else if (maType == 3) {
            jiamaScore = YNMJGameType.HuAttachType.OtherJieSuanPlus30;
            winplayer.getHuAttachType().add(YNMJGameType.HuAttachType.JieSuan30);
        }

        pd.getYxpd().jiamaScore = jiamaScore * mult;
    }

    /**
     * 结算分*本风分
     * 此处结算分为赢家与输家之间的1vs1结算，两人之间如果有一人为庄，结算10分，没有庄结算5分
     * 本风分每多一个结算分就*2一次,2本风:结算分*2*2,3本风:结算分*2*2*2,4本风:结算分*2^本风数量
     *
     * @param player      赢家
     * @param allPlayer   所有玩家
     * @param fromPlayers 输家
     * @param room        房间实例
     * @param pd          支付细节对象
     */
    protected void computeJiesuanBenfengScore(MJPlayer player, ArrayList<IPlayer> allPlayer, ArrayList<IPlayer> fromPlayers
            , RoomInstance room, PayDetailed pd) {

        int jiesuanBenfengScore = 0;                                                                                    //结算本风+的分
        Map<Integer, Integer> jiesuanBenfengDelScore = new HashMap<>();                                                 //结算本风-的分，输的人减
        //4.1:为赢的人增加 结算分*2^本风数量
        //4.1.1判断中发白与东南西北对应的本风刻子杠数量
        int bfc = collectKeZiGangFromFeng(player.getHandCards().getHandCards(), player.getHandCards().getOpencards(), player);
        //4.2:获得结算分
        //当前牌如果为字一色，且不符合3N+2牌型，即为乱风倒，乱风倒不记录结算与台风分
        if (isLuanFengDao(player.getHandCards().getHandCards(), player.getHandCards().getOpencards())) {
            jiesuanBenfengScore = 0;
            for (IPlayer p : allPlayer) jiesuanBenfengDelScore.put(p.getUid(), 0);
        } else {
            //循环所有玩家
            for (IPlayer p : allPlayer) {
                //如果当前玩家为输家,累计赢家分数，设置输家分数
                if (fromPlayers.contains(room.getPlayerById(p.getUid()))) {
                    //结算基础分，输家或赢家有一家为庄，结算分10分；输家或赢家都为闲，结算分5分
                    int jsbasic = (player.getUid() == room.getBankerUid() || p.getUid() == room.getBankerUid())
                            ? YNMJGameType.HuAttachType.OtherJieSuanZhuangJia : YNMJGameType.HuAttachType.OtherJieSuanXianJia;
                    //当前两玩家结算的分数
//                    int s = bfc==0?0:(int) Math.round(jsbasic * Math.pow(2, bfc));
                    int s = (int) Math.round(jsbasic * Math.pow(2, bfc));
                    //累计赢家分数
                    jiesuanBenfengScore += s;
                    //记录输家分数
                    jiesuanBenfengDelScore.put(p.getUid(), s);
                }
                //如果不包含当前输家(为闲家或赢家)，初始化改玩家的jiesuanBenfengDelScore的值为0
                else
                    jiesuanBenfengDelScore.put(p.getUid(), 0);
            }
        }

        //增加本风的显示项目
        YNMJGameType.HuAttachType.addHuTypeBenFeng(player, bfc);
        pd.getYxpd().jiesuanBenfengScore = jiesuanBenfengScore;
        pd.getYxpd().jiesuanBenfengDelScore = jiesuanBenfengDelScore;
    }

    /**
     * 结算分*本风分（包三家规则）
     *
     * @param player    赢家
     * @param allPlayer 所有玩家
     * @param room      房间对象
     * @param pd        支付细节对象
     */
    protected void computeJiesuanBenfengChargeAllScore(MJPlayer player, ArrayList<IPlayer> allPlayer
            , RoomInstance room, PayDetailed pd) {
        int jiesuanBenfengScore = 0;
        Map<Integer, Integer> jiesuanBenfengDelScore = new HashMap<>();
        //赢家本风数量
        int bfc = collectKeZiGangFromFeng(player.getHandCards().getHandCards(), player.getHandCards().getOpencards(), player);

        //当前牌如果为字一色，且不符合3N+2牌型，即为乱风倒，乱风倒不记录结算与本风分
        if (isLuanFengDao(player.getHandCards().getHandCards(), player.getHandCards().getOpencards())) {
            jiesuanBenfengScore = 0;
            for (IPlayer p : allPlayer) jiesuanBenfengDelScore.put(p.getUid(), 0);
        } else {
            //循环所有玩家
            for (IPlayer p : allPlayer) {
                //如果当前玩家不为赢家,计算结算分，但将分数计入包三家的玩家id
                if (p.getUid() != player.getUid()) {
                    //结算基础分，输家或赢家有一家为庄，结算分为10；都为闲，结算分5
                    int jsbasic = (player.getUid() == room.getBankerUid() || p.getUid() == room.getBankerUid())
                            ? YNMJGameType.HuAttachType.OtherJieSuanZhuangJia : YNMJGameType.HuAttachType.OtherJieSuanXianJia;
                    //当前两玩家结算分数
//                    int s = bfc==0?0:(int) Math.round(jsbasic * Math.pow(2, bfc));
                    int s = (int) Math.round(jsbasic * Math.pow(2, bfc));
                    //累计赢家分数
                    jiesuanBenfengScore += s;
                    //累计包三家玩家分数
                    if (jiesuanBenfengDelScore.get(pd.getFromUid()[0]) == null)
                        jiesuanBenfengDelScore.put(pd.getFromUid()[0], 0);
                    jiesuanBenfengDelScore.put(pd.getFromUid()[0], (jiesuanBenfengDelScore.get(pd.getFromUid()[0]) + s));
                } else
                    jiesuanBenfengDelScore.put(p.getUid(), 0);
            }
        }

        //增加本风的显示项目
        YNMJGameType.HuAttachType.addHuTypeBenFeng(player, bfc);
        pd.getYxpd().jiesuanBenfengScore = jiesuanBenfengScore;
        pd.getYxpd().jiesuanBenfengDelScore = jiesuanBenfengDelScore;
    }

    /**
     * 计算杠分
     *
     * @param pd         支付细节对象
     * @param allPlayer  所有玩家
     * @param room       房间对象
     * @param calculator 计算对象
     */
    protected void computeGangScore(PayDetailed pd, ArrayList<IPlayer> allPlayer, RoomInstance room, Calculator calculator) {
        Map<Integer, Integer> gangScore = new HashMap<>();                                                              //杠+的分
        Map<Integer, Integer> gangDelScore = new HashMap<>();                                                           //杠-的分，除了杠的人其他三家减
        StringBuilder sb = new StringBuilder("computeGangScore:[");
        for (IPlayer p : allPlayer) {
            int c = 0;
            //计算当前玩家有几个杠
            ArrayList<CardGroup> acg = p.getHandCards().getOpencards();
            for (CardGroup cg : acg) {
                if (cg.getCardsList().size() == 4) {
                    //暗杠非闭胡4分 闭胡10分
                    if (cg.getGType() == 11) c += YNMJGameType.HuAttachType.getAnGang(room);
                        //明杠或者补杠 非闭胡2分 闭胡5分
                    else if (cg.getGType() == 13 || cg.getGType() == 9)
                        c += YNMJGameType.HuAttachType.getMingGang(room);
                }

                sb.append(cg.toString()).append("-size:").append(cg.getCardsList().size()).append("\t");
            }

            //为有杠玩家增加杠分,杠分由3家出，所以乘以3
            gangScore.put(p.getUid(), c * 3);
            //其他玩家减少杠分
            for (IPlayer fp : allPlayer) {
                if (fp.getUid() != p.getUid()) {
                    //初始化当前玩家的减分为0
                    if (!gangDelScore.containsKey(fp.getUid())) gangDelScore.put(fp.getUid(), 0);
                    //为当前玩家减分
                    gangDelScore.put(fp.getUid(), (gangDelScore.get(fp.getUid()) + c));
                }
            }
        }
        sb.append("]");
        //为每个玩家增加杠分
        for (IPlayer p : allPlayer)
            calculator.getUserBattleBalances().get(p.getUid()).setGangPoint(gangScore.get(p.getUid()) - gangDelScore.get(p.getUid()));

        pd.getYxpd().gangScore = gangScore;
        pd.getYxpd().gangDelScore = gangDelScore;

        logger.debug("YXHuPlugin.computeGangScore:" + sb.toString());

    }

    /**
     * 后续的一些其他操作
     *
     * @param pd
     * @param player
     * @param fromPlayers
     * @param room
     * @param calculator
     * @param addscore
     */
    protected void computeOther(PayDetailed pd, MJPlayer player, ArrayList<IPlayer> fromPlayers, RoomInstance room
            , Calculator calculator, int addscore) {
        //赢家增加的分数
        //输家数量大于1为自摸，否则为点炮
        if (fromPlayers.size() > 1) {
            calculator.getBattleCensuss().get(pd.getToUid()).addWinSelf();                                              //统计数据里自摸加1
            if (room.getEngine().getCardPool().size() >= 4)
                calculator.addCardBalance(pd.getToUid(), 0, this.getGen().getZimoFlag(), addscore, pd);
        }
        //如果输家1，为点炮局
        else if (fromPlayers.size() == 1) {
            calculator.getBattleCensuss().get(pd.getToUid()).addWinOther();                                             //赢家统计数据中点炮赢加1
            calculator.getBattleCensuss().get(pd.getFromUid()[0]).addDiscardOther();                                    //输家统计数据中点炮加1
            calculator.addCardBalance(pd.getToUid(), pd.getFromUid()[0], this.getGen().getDianedFlag(), addscore, pd);  //赢家加分
            calculator.addCardBalance(pd.getFromUid()[0], pd.getToUid(), this.getGen().getDianFlag(), -addscore, pd);   //输家加负分
        }

        calculator.getUserBattleBalances().get(pd.getToUid()).setWinType(pd.getSubType());
        calculator.getUserBattleBalances().get(pd.getToUid()).setHu(true);

        calculator.addCardBalance(pd.getToUid(), this.getGen().getSubType(), pd.getCards(), addscore);

        //增加附加加分项目名称
        for (Integer hat : player.getHuAttachType()) {
            calculator.getUserBattleBalances().get(pd.getToUid()).getHutype().add(hat);
        }
    }

    /**
     * 点炮包三家的设置
     *
     * @param pd
     * @param player
     * @param calculator
     * @param addscore
     */
    protected void computeOtherChargeAll(PayDetailed pd, MJPlayer player
            , Calculator calculator, int addscore) {


        //包三家总为点炮，不为自摸
        //增加赢家的各项数据
        calculator.getBattleCensuss().get(pd.getToUid()).addWinOther();                                             //赢家统计数据中点炮赢加1
        calculator.getBattleCensuss().get(pd.getFromUid()[0]).addDiscardOther();                                    //输家统计数据中点炮加1
        calculator.addCardBalance(pd.getToUid(), pd.getFromUid()[0], this.getGen().getDianedFlag(), addscore, pd);  //赢家加分
        calculator.addCardBalance(pd.getFromUid()[0], pd.getToUid(), this.getGen().getDianFlag(), -addscore, pd);   //输家加负分


        calculator.getUserBattleBalances().get(pd.getToUid()).setWinType(pd.getSubType());
        calculator.getUserBattleBalances().get(pd.getToUid()).setHu(true);

        calculator.addCardBalance(pd.getToUid(), this.getGen().getSubType(), pd.getCards(), addscore);

        //增加附加加分项目名称
        for (Integer hat : player.getHuAttachType()) {
            calculator.getUserBattleBalances().get(pd.getToUid()).getHutype().add(hat);
        }
    }

    /**
     * 旧版本算分程序
     *
     * @param pd
     * @param room
     * @param calculator
     * @return
     */
    private boolean computeScoreOld(PayDetailed pd, RoomInstance room, Calculator calculator) {
        //-------旧版本算分程序-------

        //结算基础值
        int rate = pd.getRate();
        //获得输的玩家
        ArrayList fromPlayers = new ArrayList();
        for (int uid : pd.getFromUid()) {
            IPlayer player = room.getPlayerById(uid);
            if (player == null)
                continue;
            fromPlayers.add(player);
        }
        int payNum = fromPlayers.size();
        //如果输家数量为0，返回false
        if (payNum == 0)
            return false;


        // 赢家增加分数
        int addscore = payNum * rate;

        // 输家每家减少分数
        for (int uid : pd.getFromUid()) {
            IPlayer delP = room.getPlayerById(uid);
            if (pd.getFromUid().length > 1) {
                calculator.addCardBalance(delP.getUid(), pd.getToUid(), 0, -rate, pd);
            }
        }

        //如果输家数量大于1自摸
        if (payNum > 1) {
            calculator.getBattleCensuss().get(pd.getToUid()).addWinSelf(); // 自摸
            if (room.getEngine().getCardPool().size() >= 4) {
                calculator.addCardBalance(pd.getToUid(), 0, this.getGen().getZimoFlag(), addscore, pd);
            }
        }
        //如果输家为1点炮
        else if (pd.getFromUids().length == 1) {
            calculator.getBattleCensuss().get(pd.getToUid()).addWinOther();                                            // 为赢家增加1次接炮次数
            calculator.getBattleCensuss().get(pd.getFromUid()[0]).addDiscardOther();                                    // 为输家增加1次点炮次数
            calculator.addCardBalance(pd.getToUid(), pd.getFromUid()[0], this.getGen().getDianedFlag(), addscore, pd);
            calculator.addCardBalance(pd.getFromUid()[0], pd.getToUid(), this.getGen().getDianFlag(), -pd.getRate(), pd);
        }

        calculator.getUserBattleBalances().get(pd.getToUid()).setWinType(pd.getSubType());
        calculator.getUserBattleBalances().get(pd.getToUid()).setHu(true);
        //暂时为兼容原来的显示
        calculator.addCardBalance(pd.getToUid(), this.getGen().getSubType(), pd.getCards(), addscore);
//		calculator.addCardBalance(pd.getToUid(), 0, this.getGen().getZimoFlag(), addscore, pd);
        //-------旧版本算分程序-------

        return false;
    }

    //--------------一些判断函数-------------------

    /**
     * 判断是否为清一色包三家，如果返回输家id大于0，为包三家，输家id等于0不包三家
     * @param player    赢家player对象
     * @param pd        支付细节
     * @param isZiMo    是否自摸
     * @return
     */
    protected int isQingYiSeChargeAll(MJPlayer player, PayDetailed pd,boolean isZiMo) {
        //获得开门牌
        ArrayList<CardGroup> groupList = player.getHandCards().getOpencards();
        int fromPlayerId = 0;

        //如果不为清一色，直接返回0
        if (!this.oneCorlor(player.getHandCards().getHandCards(), player.getHandCards().getOpencards())) return fromPlayerId;

        //如果为清一色获得包三家的玩家id
        fromPlayerId = getFromPlayerId(groupList, pd, fromPlayerId,isZiMo);
        return fromPlayerId;
    }

    /**
     * 判断是否为字一色包三家
     * @param player
     * @param pd
     * @param isZiMo
     * @return
     */
    protected int isZiYiSeChargeAll(MJPlayer player,PayDetailed pd,boolean isZiMo) {
        ArrayList<MJCard> handCards = player.getHandCards().getHandCards();
        ArrayList<CardGroup> groupList = player.getHandCards().getOpencards();
        int fromPlayerId = 0;
        //如果不是字一色直接返回0
        if (!this.isZiOneColor(handCards, groupList)) return fromPlayerId;

        fromPlayerId = getFromPlayerId(groupList, pd, fromPlayerId,isZiMo);
        return fromPlayerId;
    }

    /**
     ** 清一色包三家规则:
     * 1: 赢家至少3个开门,且有同一玩家至少供3个开门 		自摸:供了3个开门的玩家包三家	点炮:供了3个开门的玩家包三家
     * 2: 赢家4个开门,且没有同一玩家至少供3个开门			自摸:供第2个开门的玩家包三家	点炮:供了第4个开门的玩家包三家
     * 3: 赢家3个开门,且没有同一玩家至少供3个开门			自摸:与普通自摸一样结算			点炮:由点炮的玩家包三家
     * 4: 赢家3个以下开门								自摸:与普通自摸一样结算			点炮:与普通点炮一样结算
     * @param groupList
     * @param pd
     * @param fromPlayerId  输家id
     * @return
     */
    private int getFromPlayerId(ArrayList<CardGroup> groupList, PayDetailed pd, int fromPlayerId,boolean isZiMo) {
        ArrayList<CardGroup> noCealedKongGroup = new ArrayList<>();
        //0:收集供吃碰玩家点开门的次数，排除暗杠的开门次数
        Map<Integer, Integer> targetId = new HashMap<>();
        for (CardGroup cg : groupList) {
            if (cg.getGType() == YNMJGameType.PlayType.CealedKong )
                continue;                                           //如果当前开门牌为暗杠不计算当前牌组
            if (!targetId.containsKey(cg.targetId)) {
                targetId.put(cg.targetId, 1);
            }
            else {
                targetId.put(cg.targetId, (targetId.get(cg.targetId) + 1));
            }
            noCealedKongGroup.add(cg);
        }

        //1:赢家至少3个开门,且有同一玩家至少供3个开门 		自摸:供了3个开门的玩家包三家	点炮:供了3个开门的玩家包三家
        if (noCealedKongGroup.size() >= 3) {
            //判断每个点开门的玩家点了几次，如果有3次以上的玩家则胡了包三家
            for(Integer tid:targetId.keySet()){
                //判断是否有一人点了3个开门
                if(targetId.get(tid)>=3) {
                    fromPlayerId = tid;
                    return fromPlayerId;
                }
            }
        }

        //2:赢家4个开门,且没有同一玩家至少供3个开门			自摸:供第2个开门的玩家包三家	点炮:供了第4个开门的玩家包三家
        if (noCealedKongGroup.size() == 4) {
            fromPlayerId = groupList.get(3).targetId;
            return fromPlayerId;
        }

        //3:赢家3个开门,且没有同一玩家至少供3个开门			自摸:与普通自摸一样结算			点炮:由点炮的玩家包三家
        if (noCealedKongGroup.size() == 3 && !isZiMo) {
            return pd.getFromUid()[0];
        }

        //4:赢家3个以下开门								自摸:与普通自摸一样结算			点炮:与普通点炮一样结算
        return 0;

//        if(groupList.size()>=3){
//            //收集其他玩家点开门的次数
//            Map<Integer,Integer> targetId = new HashMap<>();
//            for(CardGroup cg:groupList) {
//                if(cg.getGType()==YNMJGameType.PlayType.CealedKong) continue;                                           //如果当前开门牌为暗杠不计算当前牌组
//                if(!targetId.containsKey(cg.targetId))
//                    targetId.put(cg.targetId,1);
//                else
//                    targetId.put(cg.targetId,(targetId.get(cg.targetId)+1));
//            }
//            //有效的开门次数
//            int openCount = 0;
//            //判断每个点开门的玩家点了几次，如果有3次以上的玩家则胡了包三家
//            for(Integer tid:targetId.keySet()){
//                //判断是否有一人点了3个开门
//                if(targetId.get(tid)>=3) {
//                    fromPlayerId = tid;
//                    return fromPlayerId;
//                }else
//                    openCount += targetId.get(tid);
//            }
//            //如果没有连续供3次以上的玩家，但胡的人已经开3个以上的门，谁点炮谁包三家
//            if(openCount>=3) return pd.getFromUid()[0];
//        }
//        return fromPlayerId;
    }

    /**
     * 抢杠包三家标记
     * @param pd    支付细节
     * @param room  房间对象
     * @return      返回当前是否为抢杠包三家状态
     */
    protected boolean isQiangGangChargeAll(PayDetailed pd, RoomInstance room) {
        //所有玩家
        ArrayList<IPlayer> allPlayer = room.getAllPlayer();

        //判断是否有玩家有被抢杠的状态,如果有被抢杠状态的玩家，
        // 返回包三家状态，包三家积分
        for (IPlayer ip : allPlayer) {
            MJPlayer mjp = (MJPlayer) ip;
            if(mjp.isQiangGangFlag())
                return true;

//            if(mjp.isQiangGangFlag()){
//                pd.getFromUid()[0] = mjp.getUid();
//            }
        }
        return false;
    }

    //--------------一些判断函数-------------------

}
