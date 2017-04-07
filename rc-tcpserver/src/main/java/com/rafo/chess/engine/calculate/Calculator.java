package com.rafo.chess.engine.calculate;

import java.util.*;

import com.rafo.chess.engine.game.YBMJGameType;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;
import com.rafo.chess.engine.plugin.impl.HuPlugin;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import com.rafo.chess.model.BattlePayStep;
import com.rafo.chess.model.battle.BattleBalance;
import com.rafo.chess.model.battle.BattleCensus;
import com.rafo.chess.model.battle.BattleScore;
import com.rafo.chess.model.battle.CardBalance;
import com.rafo.chess.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.room.RoomInstance;

/***
 * 结算器
 * @author Administrator
 */
public class Calculator {
    protected Logger logger = LoggerFactory.getLogger("cacl");
    private RoomInstance room;
    private Set<Integer> winPlayers = new HashSet<>();                                                                    //胡牌的玩家列表，用于结算一炮多响等
    private Map<Integer, BattleBalance> userBattleBalances = new HashMap<>();                                            //战斗结果，目前主要用于牌局恢复
    private Map<Integer, BattleCensus> battleCensuss = new HashMap<>();                                                //战局统计(需要累积)最后总结算统计表
    /**
     * 结算列表
     */
    private ArrayList<PayDetailed> payDetailList = new ArrayList<PayDetailed>();

    //每个玩家在每一步的得分明细
    private Map<Integer, Map<Integer, BattleScore>> battleInScores = new HashMap<>();

    //每个玩家的失分汇总
    private Map<Integer, Map<Integer, Integer>> battleLostScores = new HashMap<>();

    //每一步的算分规则详情
    private Map<Integer, BattlePayStep> payStepMap = new TreeMap<>();

    //标记是否已经在calculateFinalScores中的BattlePayStep.calculator中计算过一次杠分
    //如果不使用该标记，一炮多响时会计算多次杠分
    private boolean computedGang = false;

    public boolean isComputedGang() {
        return computedGang;
    }

    public void setComputedGang(boolean computedGang) {
        this.computedGang = computedGang;
    }

    public Calculator(RoomInstance room) {
        this.room = room;
    }

    public void clean() {
        userBattleBalances = new HashMap<>();
        payDetailList.clear();
        winPlayers.clear();
        battleInScores.clear();
        battleLostScores.clear();
        payStepMap.clear();
        computedGang = false;
    }

    public ArrayList<PayDetailed> getPayDetailList() {
        return payDetailList;
    }

    public void addPayDetailed(PayDetailed ratePay) {
        payDetailList.add(ratePay);
    }

    public Map<Integer, BattleBalance> getUserBattleBalances() {
        return userBattleBalances;
    }

    public Map<Integer, BattleCensus> getBattleCensuss() {
        return battleCensuss;
    }

    /**
     * 计算分数，在向客户端发送信息时，调用该函数
     */
    public void calculatePay() {
        logger.debug("room [" + room.getRoomId() + "] is calculating ");

        try {
            //初始化数据
            userBattleBalances.clear();
            //对每个玩家进行结算
            for (IPlayer player : room.getPlayerArr()) {
                BattleBalance battleBalance = new BattleBalance();
                battleBalance.setPlayerId(player.getUid());
//				MJPlayer p = (MJPlayer)player;
                //设置当前玩家是否扣听，暂时未用
//				if(p.isTing()) {
//					battleBalance.setTing(YBMJGameType.PlayType.ReadyHand);
//				}else if(p.isKouTing()){
//					battleBalance.setTing(YBMJGameType.PlayType.KouTing);
//				}

                ArrayList<MJCard> handCards = player.getHandCards().getHandCards();
                List<Integer> cards = new ArrayList<Integer>();
                for (MJCard card : handCards) {
                    cards.add(card.getCardNum());
                }
                //BattleBalance数据处理1:为当前玩家的结算结果设置牌
                battleBalance.setCards(cards);

                userBattleBalances.put(player.getUid(), battleBalance);
                if (battleCensuss.get(player.getUid()) == null) {
                    BattleCensus battleCensus = new BattleCensus();
                    battleCensus.setPlayerId(player.getUid());
                    battleCensuss.put(player.getUid(), battleCensus);
                }
            }

            logger.debug("Calculator.calculatePay:loop payDetailList start:[payDetailList.size:"+payDetailList.size()+"]");
            //payDetailList可以容纳多个payDetail对象，每个payDetail对象都应该是一个结算项，不知道是不是这么处理的
            for (PayDetailed pd : payDetailList) {
                if (!pd.isAttached()) { //非叠加的番数（杠后），加到cardbalance里面去
                    pd.getPlugin().doPayDetail(pd, room, this);
                }else{
                    logger.debug("Calculator.calculatePay:[pd.isAttached:"+pd.isAttached()+"]");
                }
                logger.debug("Calculator.calculatePay:convertToPayStep(pd) start");
                convertToPayStep(pd);
                logger.debug("Calculator.calculatePay:convertToPayStep(pd) end");
            }
            logger.debug("Calculator.calculatePay:loop payDetailList end");

            //设置所有开门的牌
            logger.debug("Calculator.calculatePay: setCardBalance start");
            setCardBalance();
            logger.debug("Calculator.calculatePay: setCardBalance end");
            //构造统计界面分数数据结构
            logger.debug("Calculator.calculatePay: calculateFinalScores start");
            calculateFinalScores();
            logger.debug("Calculator.calculatePay: calculateFinalScores end");

            //更新玩家分数
            StringBuilder sb = new StringBuilder();
            //BattleBalance数据 到此处已经增加了cards与balbances、WinPoint数据
            for (BattleBalance balance : userBattleBalances.values()) {
                int winPoint = balance.getWinPoint();
                IPlayer player = room.getPlayerById(balance.getPlayerId());
                player.setScore(player.getScore() + winPoint);
                battleCensuss.get(balance.getPlayerId()).addPoint(winPoint);
                balance.setWinPoint(winPoint);
                sb.append(",").append(player.getUid()).append(":").append(player.getScore());
            }
            logger.debug("totalscore room:" + room.getRoomId() + ",round:" + room.getCurrRounds() + sb.toString());

            /**
             * 目前暂时将得分和失分都显示到界面上
             * 主界面显示胡牌和杠牌总得分，胡牌杠牌的明目为基础番的明目battleScore.type, BattleScore.score
             * 点击胡或杠，则显示明细（BattleScore.detail），明细包括补充番的明目
             */

            //所有的得分明细
            //BattleBalance数据 在此处增加的Score数据
            for (Map.Entry<Integer, Map<Integer, BattleScore>> battleInScore : battleInScores.entrySet()) {
                for (BattleScore battleScore : battleInScore.getValue().values()) {
                    //这里的battleScore是需要的和法和得分
                    userBattleBalances.get(battleInScore.getKey()).addBattleScore(battleScore);
                }
            }

            //失分的,临时加的，可能还会调整
            //结算界面上每个人的明细下显示每个风位的失分，即东北西南 失分
            int[] lostScoreType = Constants.lostScoreType;


            for (Map.Entry<Integer, Map<Integer, Integer>> userBattleLostScore : battleLostScores.entrySet()) {
                int lostScoreUid = userBattleLostScore.getKey();
                for (Map.Entry<Integer, Integer> lostScore : userBattleLostScore.getValue().entrySet()) {
                    int toUid = lostScore.getKey();

                    BattleScore battleScore = new BattleScore();
                    int index = room.getPlayerById(toUid).getIndex();
                    battleScore.setType(lostScoreType[index]);
                    battleScore.setScore(lostScore.getValue());
                    userBattleBalances.get(lostScoreUid).addBattleScore(battleScore);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("Calculator.calculatePay:"+e.toString());
            logger.error("Calculator.calculatePay:",e);
        }
    }

    public void addCardBalance(int uid, int fromuid, int type, int score, PayDetailed pd) {
        if (pd.getFromUid().length == 1 || score > 0) {
            addCardBalance(uid, type, pd.getCards(), score);
        }
    }


    /**
     * @param uid
     * @param type  产生分数的明目
     * @param score 分数
     */
    public void addCardBalance(int uid, int type, List<Integer> cards, int score) {
        if (type <= 0) {
            return;
        }
        //生成一个结果对象
        BattleBalance battleBalance = userBattleBalances.get(uid);
        if (battleBalance == null) {
            battleBalance = new BattleBalance();
            userBattleBalances.put(uid, battleBalance);
        }

        //牌结算
        CardBalance cardBalance = null;
        if (cards.size() > 1) {
            cardBalance = new CardBalance(type, cards, score);
        } else {
            int card = cards.size() == 0 ? 0 : cards.get(0);
            cardBalance = new CardBalance(type, card, score);
        }

        battleBalance.getBalances().add(cardBalance);
    }

    /**
     * 为所有玩家设置明牌（开门的牌）
     */
    private void setCardBalance() {
        List<MJPlayer> players = room.getAllPlayer();
        for (MJPlayer player : players) {
            BattleBalance battleBalance = userBattleBalances.get(player.getUid());
            if (battleBalance == null) {
                battleBalance = new BattleBalance();
                userBattleBalances.put(player.getUid(), battleBalance);
            }

            // 设置明牌(开门牌)
            if (player.getHandCards().getOpencards().size() > 0) {
                List<CardGroup> cardGroups = player.getHandCards().getOpencards();
                for (CardGroup cg : cardGroups) {
                    CardBalance cardBlance = new CardBalance();
                    cardBlance.setType(cg.getGType());
                    cardBlance.setCard(cg.getCardsList().get(0).getCardNum());

                    if (cg.getSubType() > 0) {
                        cardBlance.getSubtype().add(cg.getSubType());
                    }
                    cardBlance.setTargetId(cg.targetId);
                    battleBalance.addBalances(cardBlance);

                    ArrayList<Integer> list = new ArrayList<Integer>();

                    for (MJCard c : cg.getCardsList()) {
                        list.add(c.getCardNum());
                    }
                    cardBlance.setCards(list);
                }
            }
        }
    }


    public Set<Integer> getWinPlayers() {
        return winPlayers;
    }

    public void setWinPlayers(Set<Integer> winPlayers) {
        this.winPlayers = winPlayers;
    }

    public void addWinPlayer(Integer playerId) {
        this.winPlayers.add(playerId);
    }


    public void convertToPayStep(PayDetailed pd) {
        logger.debug("room:" + room.getRoomId() + ",round:" + room.getCurrRounds() + "," + pd.toString());
        //如果结算细节无效或结算基础分数为0，直接返回
        if (!pd.isValid() || pd.getRate() == 0) {
            return;
        }
        //获得动作类型字符串
        String[] actionTypes = pd.getPlugin().getGen().getActionType().split(",");
        try {
            for (String at : actionTypes) {
                int actionType = Integer.parseInt(at);
                //如果动作类型不为和或者杠，直接返回
                if (actionType != IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU
                        && actionType != IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG) {
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        //增加一个结算步骤进去
        //原处理方法
        payStepMap.put(pd.getStep(), makeOneBattlePayStep(pd));

        //当前处理方法
//		payStepMap.put(pd.getStep(),makeBattlePayStep(pd));
        //获得结算步骤,如果结算步骤对象不存在，生成一个新的结算步骤对象并加入payStepMap
        BattlePayStep battlePayStep = payStepMap.get(pd.getStep());
        if (battlePayStep == null) {
            battlePayStep = new BattlePayStep();
            payStepMap.put(pd.getStep(), battlePayStep);
        }
        //从支付细节对象中获得插件
        IOptPlugin plugin = pd.getPlugin();
        //如果插件为可检查执行动作插件 并且 不为附件插件？？附件属性什么意思？
		if(plugin instanceof IPluginCheckCanExecuteAction && !pd.isAttached()){
//        if (plugin instanceof IPluginCheckCanExecuteAction) {
            battlePayStep.setType(plugin.getGen().getSubType());                                                        //设置类型
            battlePayStep.setToUid(pd.getToUid());                                                                        //设置目的玩家
            battlePayStep.setStep(pd.getStep());                                                                        //设置步骤
//			battlePayStep.addMultipleScoreDetail(pd.getFromUids(), pd.getSubType(), pd.getRate(), pd.isCanMerge());		//
        } else {
            if (pd.getPayType() == PayDetailed.PayType.Multiple) {
                battlePayStep.addMultipleScoreDetail(pd.getFromUids(), pd.getSubType(), pd.getRate(), pd.isCanMerge());
            } else {
                battlePayStep.addAddScoreDetail(pd.getFromUids(), pd.getSubType(), pd.getRate());
            }
        }
    }

    /**
     * 生成一个结算步骤,一个结算步骤代表一个玩家和的操作。
     * 生成多个BattlePayStep就有多个和法
     *
     * @return 返回结算步骤对象
     */
    private BattlePayStep makeOneBattlePayStep(PayDetailed pd) {
        BattlePayStep battlePayStep = payStepMap.get(pd.getStep());
        if (battlePayStep == null)
            battlePayStep = new BattlePayStep();

        //从支付细节对象中获得插件
        IOptPlugin plugin = pd.getPlugin();
        //如果插件为可检查执行动作插件 并且 不为附件插件？？附件属性什么意思？
        if (plugin instanceof IPluginCheckCanExecuteAction && !pd.isAttached()) {
//        if (plugin instanceof IPluginCheckCanExecuteAction ) {
            battlePayStep.setType(plugin.getGen().getSubType());                                                        //设置类型
            battlePayStep.setToUid(pd.getToUid());                                                                        //设置目的玩家
            battlePayStep.setStep(pd.getStep());                                                                        //设置步骤

            //在此向battlePayStep对象增加要结算的数据
            battlePayStep.addYXScoreDetail(pd.getFromUid(), pd.getToUid(), pd.getYxpd().subTypeScore, pd.getYxpd().attachScore
                    , pd.getYxpd().jiamaScore, pd.getYxpd().jiesuanBenfengScore, pd.getYxpd().jiesuanBenfengDelScore
                    , pd.getYxpd().gangScore, pd.getYxpd().gangDelScore, pd);
        } else {
            if (pd.getPayType() == PayDetailed.PayType.Multiple) {
                battlePayStep.addMultipleScoreDetail(pd.getFromUids(), pd.getSubType(), pd.getRate(), pd.isCanMerge());
            } else {
                battlePayStep.addAddScoreDetail(pd.getFromUids(), pd.getSubType(), pd.getRate());
            }
        }

        return battlePayStep;
    }

    /**
     * 根据参数直接生成一个BattlePayStep对象
     *
     * @return
     */
    private BattlePayStep makeBattlePayStep(int type, int toUid, int step, int[] fromUids, int subType, int rate, boolean isCanMerge) {
        BattlePayStep bps = new BattlePayStep();
        bps.setType(type);
        bps.setToUid(toUid);
        bps.setStep(step);
        bps.addMultipleScoreDetail(fromUids, subType, rate, isCanMerge);
        return bps;
    }

    /**
     * 构造结算界面的分数
     */
    private void calculateFinalScores() {
        int bankerId = room.getBankerUid();
        logger.debug("finalscore room:" + room.getRoomId() + ",round:" + room.getCurrRounds() + ",red:" + 0 + ",bankerId:" + bankerId);


        //统计界面
        for (BattlePayStep bps : payStepMap.values()) {
            bps.calculate(room, this);
            logger.debug("finalscore room:" + room.getRoomId() + ",round:" + room.getCurrRounds() + ",red:" + 0 + ",bankerId:" + bankerId + "," + bps.log());
            bps.toBattleScore(room);

            //玩家的得分汇总
            userBattleBalances.get(bps.getToUid()).addPoint(bps.getGainTotal());
            Map<Integer, BattleScore> userBattleScore = battleInScores.get(bps.getToUid());
            if (userBattleScore == null) {
                userBattleScore = new HashMap<>();
                //此处增加要结算的所有玩家成绩，userBattleScore中包含的是一个玩家的成绩
                battleInScores.put(bps.getToUid(), userBattleScore);
            }
            //此处向userBattleScore中增加成绩数据
            userBattleScore.put(bps.getStep(), bps.getBattleScore());

            //玩家的失分汇总
            int fromUid = bps.getToUid();
            for (Map.Entry<Integer, Integer> userLostScore : bps.getLostTotal().entrySet()) {
                int lostUid = userLostScore.getKey();

                // 加码
                //if(battlePayStep.getToUid() == lostUid)
                //	continue;

                int score = -userLostScore.getValue();

                Map<Integer, Integer> lostScore = battleLostScores.get(lostUid);
                if (lostScore == null) {
                    lostScore = new HashMap<>();
                    battleLostScores.put(lostUid, lostScore);
                }

//				lostScore.put(lostUid,score);
                if (lostScore.get(fromUid) == null) {
                    lostScore.put(fromUid, score);
                } else {
                    lostScore.put(fromUid, lostScore.get(fromUid) + score);
                }

                userBattleBalances.get(lostUid).addPoint(score);
            }

        }
    }

    /**
     * 构造结算界面的分数
     */
    private void calculateFinalScores_old() {
        //获得房间基础分，这个是错的，要修改
        int redRate = room.getAttribute(RoomAttributeConstants.ROOM_BASE_RATE) == null ? 1
                : (int) room.getAttribute(RoomAttributeConstants.ROOM_BASE_RATE);
        //获得庄家id
        int bankerId = room.getBankerUid();

        logger.debug("finalscore room:" + room.getRoomId() + ",round:" + room.getCurrRounds() + ",red:" + redRate + ",bankerId:" + bankerId);
        /**
         * 结算界面
         * 得分明目 得分
         *   --得分方位 得分
         *
         * 失分方位 失分
         */
        //payStepMap,包含最后要和的和法和值
        for (BattlePayStep battlePayStep : payStepMap.values()) {
            battlePayStep.calculate(bankerId, redRate);

            logger.debug("finalscore room:" + room.getRoomId() + ",round:" + room.getCurrRounds() + ",red:" + redRate + ",bankerId:" + bankerId + "," + battlePayStep.log());

            battlePayStep.toBattleScore(room);

            //玩家的得分汇总
            userBattleBalances.get(battlePayStep.getToUid()).addPoint(battlePayStep.getGainTotal());
            Map<Integer, BattleScore> userBattleScore = battleInScores.get(battlePayStep.getToUid());
            if (userBattleScore == null) {
                userBattleScore = new HashMap<>();
                //此处增加要结算的所有玩家成绩，userBattleScore中包含的是一个玩家的成绩
                battleInScores.put(battlePayStep.getToUid(), userBattleScore);
            }

            //此处向userBattleScore中增加成绩数据
            userBattleScore.put(battlePayStep.getStep(), battlePayStep.getBattleScore());

            //玩家的失分汇总
            int fromUid = battlePayStep.getToUid();
            for (Map.Entry<Integer, Integer> userLostScore : battlePayStep.getLostTotal().entrySet()) {
                int lostUid = userLostScore.getKey();

                // 加码
                //if(battlePayStep.getToUid() == lostUid)
                //	continue;

                int score = -userLostScore.getValue();

                Map<Integer, Integer> lostScore = battleLostScores.get(lostUid);
                if (lostScore == null) {
                    lostScore = new HashMap<>();
                    battleLostScores.put(lostUid, lostScore);
                }

                if (lostScore.get(fromUid) == null) {
                    lostScore.put(fromUid, score);
                } else {
                    lostScore.put(fromUid, lostScore.get(fromUid) + score);
                }

                userBattleBalances.get(lostUid).addPoint(score);
            }

        }

    }
}
