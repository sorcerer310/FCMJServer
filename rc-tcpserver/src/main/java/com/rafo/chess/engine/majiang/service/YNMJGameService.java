package com.rafo.chess.engine.majiang.service;

import com.rafo.chess.common.GlobalConstants;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.common.model.record.PlayerPointInfoPROTO;
import com.rafo.chess.common.service.record.RecordService;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.game.YNMJGameType;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.MahjongEngine;
import com.rafo.chess.engine.majiang.action.BaseMajongPlayerAction;
import com.rafo.chess.engine.majiang.action.GangAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.plugin.OptPluginFactory;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import com.rafo.chess.engine.room.RoomHelper;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.exception.PersistException;
import com.rafo.chess.model.battle.*;
import com.rafo.chess.utils.CmdsUtils;
import com.smartfoxserver.v2.exceptions.SFSVariableException;
import com.smartfoxserver.v2.extensions.SFSExtension;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.rafo.chess.engine.majiang.action.IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN;
import static com.rafo.chess.engine.majiang.action.IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT;

/**
 * Created by Administrator on 2016/10/12.
 */
public class YNMJGameService {

    private MahjongEngine majiang;
    private GameExtension gameExtension;
    private RoomInstance room;
    private final Logger logger = LoggerFactory.getLogger("play");

    Map<Integer, Map<Integer, BWBattleStepRES>> roomResultMap = new HashMap<>();

    public YNMJGameService(GameExtension roomExt) {
        this.gameExtension = roomExt;
    }

    /**
     * 玩家离线
     *
     * @param playerId
     */
    public void playerOffline(int playerId) {
        try {
            setPlayerStatus(playerId, true);
            sendBattleStatus(playerId);
        } catch (Exception e) {
            e.printStackTrace();
            sendFailedStatus(playerId);
        }
        logger.debug("disconnct\troom:" + room.getRoomId() + ",uid:" + playerId);
    }

    /**
     * 准备
     *
     * @param playerId
     * @throws ActionRuntimeException
     */
    public synchronized void ready(int playerId) throws ActionRuntimeException {
        setPlayerStatus(playerId, false);
        sendBattleStatus(playerId); //发送准备
        if (room.getRoomStatus() == RoomInstance.RoomState.gameing.ordinal()) {
            BattleStep step = new BattleStep(playerId, room.getCurrentTurnPlayerId(), YNMJGameType.PlayType.Deal);
            sendBattleData(step, playerId, false);
        } else if (isAllPlayerReady()) { //定庄
            majiang.dingzhuang();

            setBattleStatus();
            sendBattleStatus(playerId); //battle

            //sendBattleSeatMessage(0); //发送座位信息

            majiang.startGame();

            BattleStep step = new BattleStep(room.getBankerUid(), room.getBankerUid(), YNMJGameType.PlayType.Deal);
            sendBattleData(step, 0, false);
        }
        logger.debug("ready\troom:" + room.getRoomId() + ",uid:" + playerId);
    }

    /**
     * 定庄结束，开始牌局
     *
     * @param playerId
     * @throws ActionRuntimeException
     */
    public synchronized void start(int playerId) throws ActionRuntimeException {
        setPlayerStatus(playerId, false);
        sendBattleStatus(playerId); //发送准备
        if (room.getRoomStatus() == RoomInstance.RoomState.gameing.ordinal()) {
            BattleStep step = new BattleStep(playerId, room.getCurrentTurnPlayerId(), YNMJGameType.PlayType.Deal);
            sendBattleData(step, playerId, false);
        } else if (isAllPlayerReady()) { //定庄
            majiang.startGame();

            BattleStep step = new BattleStep(room.getBankerUid(), room.getBankerUid(), YNMJGameType.PlayType.Deal);
            sendBattleData(step, 0, false);
        }
        logger.debug("ready\troom:" + room.getRoomId() + ",uid:" + playerId);
    }

    /**
     * 玩家定庄信息，如果是首局，还包括换座位的信息
     *
     * @param playerId
     */
    public void sendBattleSeatMessage(int playerId) {
        BattleSeat seats = new BattleSeat();
        if (room.getCurrRounds() == 1) {
            Map<Integer, Integer[]> seatDice = (Map<Integer, Integer[]>) room.getAttribute(RoomAttributeConstants.ROOM_SEAT_DICE);
            seats.setPlayerDice(seatDice);
        }
        seats.setBankerId(room.getBankerUid());
        IPlayer[] players = room.getPlayerArr();
        int[] playerSeqUid = new int[players.length];
        for (int i = 0; i < players.length; i++) {
            playerSeqUid[i] = players[i].getUid();
        }
        seats.setPlayerIds(playerSeqUid);
        seats.setTheDice((int[]) room.getAttribute(RoomAttributeConstants.ROOM_ROOL_THE_DICE));
        seats.setRed((Integer) room.getAttribute(RoomAttributeConstants.ROOM_RED_DICE));
        if (gameExtension != null) {
            if (playerId > 0) {
                CmdsUtils.sendMessage(gameExtension, CmdsUtils.CMD_BANKER_SET_START, seats.toSFSObject(), String.valueOf(playerId));
            } else {
                gameExtension.send(CmdsUtils.CMD_BANKER_SET_START, seats.toSFSObject(), this.gameExtension.getParentRoom().getUserList());
            }
        }
        logger.debug("seat:" + seats.toSFSObject());
    }

    /**
     * 每收到一次客户端请求执行一次打牌操作
     *
     * @param playerId
     * @param playType
     * @param card
     * @param tobeCards
     * @throws ActionRuntimeException
     */
    public synchronized void play(int playerId, int playType, int card, String tobeCards) throws ActionRuntimeException {
        long begin = System.currentTimeMillis();

        if (this.gameExtension != null) {
            this.gameExtension.trace("==== play card " + playerId + " " + playType + " " + card);
        }

        //打牌step
        BattleStep step = new BattleStep(playerId, room.getCurrentTurnPlayerId(), playType);
        step.addCard(card);

        if (tobeCards.length() > 0) {
            String[] cards = tobeCards.split(",");
            for (String c : cards) {
                step.addCard(Integer.parseInt(c));
            }
        }
        //根据客户端传过来的playType,获得动作类型actionType
        int[] actionType = YNMJGameType.getActionTypeByPlayType(playType);
        int currentStep = majiang.getMediator().getCurrentStep();
        //引擎执行该动作
        majiang.executeAction(actionType[0], card, playerId, actionType[1], tobeCards);

        //执行完动作后做各种判断
        //判断游戏是否进入RoomState.calculated状态，如果进入该状态结束游戏结算
        if (room.getRoomStatus() == RoomInstance.RoomState.calculated.getValue()) { //判断游戏是否结束
            calculateResult(step, playerId, currentStep, false); //算分，取结果
        }
        //由于原来客户端会弹出加码窗口，现在没有了加码步骤，所以此处代码没什么用，不会执行了。
        else if (room.getRoomStatus() == RoomInstance.RoomState.jiama.getValue()) {
            logger.debug("room:" + room.getRoomId() + "jiamastate11111");
            calMa();
            calculateResult(step, playerId, currentStep, true); //算分，取结果
        }
        //如果未进入结算状态，就发送数据到客户端
        else {
            sendBattleData(step, 0, false);
        }

        logger.debug("room:" + room.getRoomId() + ";round:" + room.getCurrRounds() + ";uid:" + playerId + ";type:" + playType
                + ";card:" + card + ";tobe:" + tobeCards + ";ts:" + (System.currentTimeMillis() - begin));
    }

    // 加码数据发送
    public void calMa() {
        // 把加码/翻码数据整理发送
        BattleMa ma = new BattleMa();
        ma.setMaCards(room.getEngine().getMaCards());
        ma.setMaResult(room.getEngine().getMaResult());
        ma.setMaScores(room.getEngine().getMaScores());
        ma.setMaType((int) room.getAttribute(RoomAttributeConstants.YB_MA_TYPE));

        gameExtension.send(CmdsUtils.SFS_EVENT_MA_START, ma.toSFSObject(), this.gameExtension.getParentRoom().getUserList());

        logger.debug("calMa:" + ma.toSFSObject().toJson());
        //logger.debug("calMa-Score:" + room.getEngine().getMaScores(). );
    }

    // 结束码相关显示，给玩家结算界面
    public synchronized void closeMa(int playerId) {
        // 找到玩家，给他结算界面
        //if(room.getRoomStatus() == RoomInstance.RoomState.jiama.getValue())
        {
            Map<Integer, BWBattleStepRES> results = roomResultMap.get(room.getRoomId());

            for (Map.Entry<Integer, BWBattleStepRES> palayerResult : results.entrySet()) {
                if (gameExtension != null) {
                    gameExtension.trace(palayerResult.getValue().toSFSObject().toJson());
                    logger.debug("room:" + room.getRoomId() + ",GameService:closeMa " + palayerResult.getValue().toSFSObject().toJson());
                    if (palayerResult.getKey() == playerId)
                        CmdsUtils.sendMessage(gameExtension, CmdsUtils.CMD_BATTLE_STEP, palayerResult.getValue().toSFSObject(), String.valueOf(palayerResult.getKey()));
                }
            }
        }

        //所有人都点击了再清除
        //最后一局
        if (room.getTotalRound() <= room.getCurrRounds()) {
            int nCount = 0;
            ArrayList<MJPlayer> players = room.getAllPlayer();
            for (MJPlayer player : players) {
                if (playerId == player.getUid())
                    player.SetLookMa();
                if (player.IsLookMa())
                    ++nCount;
            }


            //发完消息再清除
            if (nCount >= 4) {
                //if (room.getTotalRound() <= room.getCurrRounds()) {
                try {
                    RoomHelper.destroyRoom(gameExtension);
                    logger.debug("cleanroom:" + room.getRoomId() + ",round:" + room.getCurrRounds());
                } catch (Exception e) {
                    e.printStackTrace();
                    //}
                }
            }
        }
    }

    /**
     * 发送玩家状态
     *
     * @param playerId
     */
    public void sendBattleStatus(int playerId) {
        BWBattleStartRES res = new BWBattleStartRES();
        ArrayList<MJPlayer> players = room.getAllPlayer();

        for (MJPlayer player : players) {
            if (playerId == player.getUid())
                res.setPlayerId(playerId);

            BattlePlayerStatus statusBuilder = new BattlePlayerStatus();
            statusBuilder.setPlayerId(player.getUid());
            statusBuilder.setStatus(player.getPlayState().ordinal());
            statusBuilder.setPoints(player.getScore());
            statusBuilder.setOffline(player.isOffline());
            res.addPlayerStatus(statusBuilder);
        }

        res.setCurrentBattleCount(room.getCurrRounds());
        res.setAccountId(String.valueOf(playerId));
        if (this.gameExtension != null) {
            this.gameExtension.send(CmdsUtils.CMD_BATTLE_READY, res.toSFSObject(), this.gameExtension.getParentRoom().getUserList());
        }
    }

    public void sendFailedStatus(int playerId) {
        BWBattleStepRES res = new BWBattleStepRES();
        res.setResult(GlobalConstants.BW_Battle_Step_InValid_Operator);
        res.setAccountId(String.valueOf(playerId));
        if (gameExtension != null) {
            CmdsUtils.sendMessage(gameExtension, CmdsUtils.CMD_BATTLE_STEP, res.toSFSObject(), String.valueOf(playerId));
        }
    }

    //判断是否所有用户都准备好了
    private boolean isAllPlayerReady() {
        if (!room.isFull()) {
            return false;
        }
        ArrayList<MJPlayer> players = room.getAllPlayer();
        for (MJPlayer player : players) {
            if (player.isOffline() || player.getPlayState() != IPlayer.PlayState.Ready) {
                return false;
            }
        }
        return true;
    }

    public void setBattleStatus() {
        ArrayList<MJPlayer> players = room.getAllPlayer();
        for (MJPlayer player : players) {
            player.setPlayerState(IPlayer.PlayState.Battle);
        }
    }

    /**
     * 发送打牌信息
     *
     * @param step
     * @param messageTargetPlayerId
     */
    public void sendBattleData(BattleStep step, int messageTargetPlayerId, boolean deal) {
        //将可做的操作封装成step
        List<BattleStep> steps = new ArrayList<>();

        step.setRemainCardCount(this.majiang.getCardPool().size());
        LinkedList<BaseMajongPlayerAction> actions = room.getCanExecuteActionListByPriority();

        if (actions.size() == 0) {
            throw new RuntimeException("invalid action");
        }

        if (step.getPlayType() != YNMJGameType.PlayType.Pass) { //玩家过牌不用通知任何人
            steps.add(step);
        }

        boolean hasDraw = false;
        for (BaseMajongPlayerAction action : actions) {
            if (action.getActionType() == PLAYER_ACTION_TYPE_CARD_GETIN) {
                hasDraw = true;
                if (deal) { //发牌的时候不用通知摸牌
                    continue;
                }
            } else if (action.getActionType() == PLAYER_ACTION_TYPE_CARD_PUTOUT && hasDraw && !deal) { //摸牌不用发打牌的动作
                continue;
            }

            int playType = action.getCanDoType();

            step = null;
            //多个吃或者暗杠，合并成一个step, 只把牌添加进去
            if (playType == YNMJGameType.PlayType.CanCealedKong) {
                step = stepContainPlayType(steps, playType, action.getPlayerUid());
            }

            if (step != null) {
                addStepCard(step.getCard(), playType, action.getCard(), action.getToBeCards());
            } else {
                step = new BattleStep();
                step.setPlayType(playType);
                step.setOwnerId(action.getPlayerUid());
                step.setTargetId(action.getFromUid());

                addStepCard(step.getCard(), playType, action.getCard(), action.getToBeCards());

                //摸牌或者空的摸牌动作，不通知其他人
                if (playType != YNMJGameType.PlayType.Draw || action.getCard() == 0) {
                    step.setIgnoreOther(true);
                }

                //断线重连的时候，该谁打牌了还是要通知自己（DaAction）
                if (messageTargetPlayerId > 0 && action.getActionType() == PLAYER_ACTION_TYPE_CARD_PUTOUT) {
                    step.setIgnoreOther(false);
                }

                steps.add(step);
            }
        }

        sendBattleStep(steps, messageTargetPlayerId);
    }

    public BattleStep stepContainPlayType(List<BattleStep> steps, int playType, int playerId) {
        for (BattleStep step : steps) {
            if (step.getPlayType() == playType && step.getOwnerId() == playerId) {
                return step;
            }
        }
        return null;
    }


    public void setPlayerStatus(int playerId, boolean offline) {
        MJPlayer player = (MJPlayer) room.getPlayerById(playerId);
        if (player == null) {
            return;
        }

        player.setOffline(offline);

        if (!offline && player.getPlayState() != IPlayer.PlayState.Battle) {
            player.setPlayerState(IPlayer.PlayState.Ready);
        }
    }


    /**
     * 组装并发送打牌消息
     *
     * @param steps
     * @param messageTargetPlayerId
     */
    public void sendBattleStep(List<BattleStep> steps, int messageTargetPlayerId) {

        Map<Integer, BWBattleStepRES> results = new HashMap<>();
        ArrayList<MJPlayer> players = room.getAllPlayer();

        for (BattleStep step : steps) {
            step.setRemainCardCount(this.majiang.getCardPool().size());
            for (MJPlayer player : players) {
                //忽略其他用户
                if (player.getUid() != step.getOwnerId() && step.isIgnoreOther()) {
                    continue;
                }

                if (messageTargetPlayerId > 0 && player.getUid() != messageTargetPlayerId) {
                    continue;
                }

                BWBattleStepRES res = results.get(player.getUid());
                BattleData battleData = null;
                if (res == null) {
                    res = new BWBattleStepRES();
                    results.put(player.getUid(), res);

                    res.setResult(GlobalConstants.BW_Battle_Step_SUCCESS);
                    res.setAccountId(String.valueOf(player.getUid()));
                    battleData = new BattleData();
                    res.setBattleData(battleData);

                    battleData.setBankerId(room.getBankerUid());
                    battleData.setBattleTime(room.getCurrRounds());
                    battleData.setBattleCount(room.getTotalRound());
                    //battleData.setWinner(room.getLastWinner());
                } else {
                    battleData = res.getBattleData();
                }

                BattleStep stepBuilder = step.clone();

                //如果是摸牌，不能让其他用户知道他摸了什么牌，设置为0; 特别的发牌时的庄家的摸牌动作设置为-1
                if (stepBuilder.getPlayType() == YNMJGameType.PlayType.Draw && stepBuilder.getOwnerId() != player.getUid()) {
                    stepBuilder.getCard().clear();
                    if (steps.get(0).getPlayType() != YNMJGameType.PlayType.Deal) {
                        stepBuilder.addCard(-1);
                    } else {
                        stepBuilder.addCard(0);
                    }
                }

                if (step.getPlayType() == YNMJGameType.PlayType.Deal) { //发牌，首次或者离线重连
                    stepBuilder.setOwnerId(player.getUid());
                    for (MJPlayer p : players) {
                        BattleDealCard dealBuild = new BattleDealCard();
                        dealBuild.setPlayerId(p.getUid());

                        if (player.getUid() == p.getUid()) {
                            // 设置手上的牌
                            if (p.getHandCards() != null) {
                                int deleteCard = 0; //摸牌已经移动到手牌里面去了，需要把最后一次的摸牌提出来
                                for (BattleStep bs : steps) {
                                    if (bs.getPlayType() == YNMJGameType.PlayType.Draw
                                            && bs.getOwnerId() == player.getUid()
                                            && bs.getCard().size() > 0 && bs.getCard().get(0) > 0) {
                                        deleteCard = bs.getCard().get(0);
                                    }
                                }

                                ArrayList<MJCard> handCards = p.getHandCards().getHandCards();
                                for (MJCard c : handCards) {
                                    if (c.getCardNum() != deleteCard) {
                                        dealBuild.addCards(c.getCardNum());
                                    } else {
                                        deleteCard = 0; //排除一张之后恢复，防止删多张牌
                                    }
                                }
                            }
                        }

                        // 设置打出去的牌
                        if (p.getHandCards().getHandCards() != null) {
                            for (MJCard c : majiang.getOutCardPool()) {
                                if (c.getUid() == p.getUid()) {
                                    dealBuild.addDisposeCards(c.getCardNum());
                                }
                            }
                        }

                        battleData.addBattleDealCards(dealBuild);

                        BattleBalance balance = null;

                        // 设置明牌
                        if (p.getHandCards().getOpencards() != null && p.getHandCards().getOpencards().size() > 0) {
                            if (balance == null) {
                                balance = new BattleBalance();
                                balance.setPlayerId(p.getUid());
                            }
                            ArrayList<CardGroup> cardGroups = p.getHandCards().getOpencards();
                            for (CardGroup cg : cardGroups) {
                                CardBalance cardBlance = new CardBalance();
                                cardBlance.setType(cg.getGType());
                                cardBlance.setCard(cg.getCardsList().get(0).getCardNum());

                                //断线重连复牌
                                ArrayList<Integer> list = new ArrayList<>();
                                for (MJCard c : cg.getCardsList()) list.add(c.getCardNum());
                                cardBlance.setCards(list);

                                if (cg.getSubType() > 0) {
                                    cardBlance.getSubtype().add(cg.getSubType());
                                }
                                cardBlance.setTargetId(cg.targetId);
                                balance.addBalances(cardBlance);
                            }
                        }

                        Map<Integer, Integer> map = (HashMap<Integer, Integer>) room.getAttribute(RoomAttributeConstants.YN_GAME_QUE);
                        if (map != null && map.containsKey(p.getUid())) {
                            if (balance == null) {
                                balance = new BattleBalance();
                                balance.setPlayerId(p.getUid());
                            }
                            CardBalance cardBlance = new CardBalance();
                            if (map.size() == room.getPlayerArr().length) {
                                cardBlance.setType(YNMJGameType.PlayType.LackFinish);
                                cardBlance.setCard(map.get(p.getUid()));
                                ArrayList<Integer> clist = new ArrayList<>();
                                for (int i = 0; i < room.getPlayerArr().length; i++) {
                                    IPlayer tPlayer = room.getPlayerArr()[i];
                                    clist.add(i, map.get(tPlayer.getUid()));
                                }
                                cardBlance.setCards(clist);
                            } else {
                                cardBlance.setType(YNMJGameType.PlayType.Lack);
                                cardBlance.setCard(map.get(p.getUid()));
                                balance.addBalances(cardBlance);
                            }
                            balance.addBalances(cardBlance);
                        }

                        if (balance != null) {
                            battleData.addBattleBalances(balance);
                        }
                    }
                }

                battleData.addBattleSteps(stepBuilder);
            }
        }

        if (messageTargetPlayerId > 0 && results.containsKey(messageTargetPlayerId)) {
            if (gameExtension != null) { //for 单元测试
                logger.debug("GameService " + messageTargetPlayerId + " : " + results.get(messageTargetPlayerId).toSFSObject().toJson());
                CmdsUtils.sendMessage(gameExtension, CmdsUtils.CMD_BATTLE_STEP, results.get(messageTargetPlayerId).toSFSObject(), String.valueOf(messageTargetPlayerId));
            }
        } else {
            for (Map.Entry<Integer, BWBattleStepRES> playerResult : results.entrySet()) {
                if (gameExtension != null) {
                    gameExtension.trace(playerResult.getKey() + " length : " + playerResult.getValue().toSFSObject().toBinary().length);
                    CmdsUtils.sendMessage(gameExtension, CmdsUtils.CMD_BATTLE_STEP, playerResult.getValue().toSFSObject(), String.valueOf(playerResult.getKey()));
                }
            }
        }

    }

    /**
     * 牌局结束，计算分数
     *
     * @param lastStep
     * @param playerId
     * @param currentStep
     * @param bMa
     */
    public void calculateResult(BattleStep lastStep, int playerId, int currentStep, boolean bMa) {
        List<BattleStep> steps = new ArrayList<>();
        LinkedList<BaseMajongPlayerAction> actions = room.getCanExecuteActionList();

        //海底
        BattleStep winStep = null;
        if (actions != null && actions.size() > 0) {
            BaseMajongPlayerAction liujuAction = getActionByType(actions, IEMajongAction.ROOM_MATCH_LIUJU);
            if (liujuAction != null) {
                winStep = new BattleStep();
                steps.add(lastStep);

                winStep.setPlayType(YNMJGameType.PlayType.He);
                winStep.setOwnerId(room.getBankerUid());
            }
        }
        //为winStep增加和操作
        if (winStep == null) {
            winStep = new BattleStep();
            winStep.setPlayType(YNMJGameType.PlayType.Hu);
            winStep.setOwnerId(playerId);
        }

        winStep.setTargetId(0);

        steps.add(winStep);

        //results中存放的是要发送的所有数据
        Map<Integer, BWBattleStepRES> results = new HashMap<>();

        //算分
        majiang.getCalculator().calculatePay();
        //battle中包含最终要发送的成绩数据
        BattleData battleData = new BattleData();
        battleData.getBattleCensuss().addAll(majiang.getCalculator().getBattleCensuss().values());                      //记录了赢家id
        battleData.getBattleBalances().addAll(majiang.getCalculator().getUserBattleBalances().values());                //到此处已经增加了scores对象数据
        //convertBattleDataToOldClient(battleData);
        battleData.setBankerId(room.getBankerUid());
        battleData.setBattleTime(room.getCurrRounds());
        battleData.setBattleCount(room.getTotalRound());
        battleData.setOwnerId(room.getOwnerId());
        //battleData.setWinner(room.getLastWinner());
        //steps中包含同时存在的多种和法
        for (BattleStep step : steps) {
            step.setRemainCardCount(majiang.getCardPool().size());
            battleData.addBattleSteps(step);
        }

        ArrayList<MJPlayer> players = room.getAllPlayer();

        for (MJPlayer player : players) {
            //res对象中存放的是每个用户的详细数据
            BWBattleStepRES res = new BWBattleStepRES();
            res.setResult(GlobalConstants.BW_Battle_Step_SUCCESS);
            res.setAccountId(String.valueOf(player.getUid()));
            res.setBattleData(battleData);

            results.put(player.getUid(), res);
        }

        //缓存记录战绩
        saveRoundRecord(battleData);

        if (!bMa) {
            for (Map.Entry<Integer, BWBattleStepRES> playerResult : results.entrySet()) {
                if (gameExtension != null) {
//                    playerResult.getValue().getBattleData().getBattleBalances().
                    gameExtension.trace(playerResult.getValue().toSFSObject().toJson());
                    logger.debug("room:" + room.getRoomId() + ",GameService: " + playerResult.getValue().toSFSObject().toJson());
                    CmdsUtils.sendMessage(gameExtension, CmdsUtils.CMD_BATTLE_STEP, playerResult.getValue().toSFSObject(), String.valueOf(playerResult.getKey()));
                }
            }

            if (room.getCurrRounds() == 1 && gameExtension != null) {// gameExtension != null for test
                try {
                    RoomHelper.subCard(gameExtension.getParentRoom(), room.getCurrRounds(), (SFSExtension) gameExtension);
                } catch (SFSVariableException e) {
                    e.printStackTrace();
                    logger.error("YNMJGameService.calculateResult:",e);
                } catch (PersistException e) {
                    e.printStackTrace();
                    logger.error("YNMJGameService.calculateResult:",e);
                }
            }


        } else {
            //把计算结果缓存起来，等到玩家要看结果时再发出去
            //此处保存了所有缓存计算结果
            roomResultMap.put(room.getRoomId(), results);

            for (Map.Entry<Integer, BWBattleStepRES> palayerResult : results.entrySet()) {
                logger.debug("缓存结算结果:" + palayerResult.getValue().toSFSObject().toJson());
            }
        }

        //重置玩家状态
        for (MJPlayer p : players) {
            p.setPlayerState(IPlayer.PlayState.Idle);
        }

        // 完成所有牌局，解散房间
//        if ( !bMa )		//加码的时候其他时间销毁
//        {
        if (room.getTotalRound() <= room.getCurrRounds()) {
            //如果有玩家胡牌，并且胡牌的玩家中不包括庄，或者当前流局了，才销毁房间
            if (room.getLastWinner().size() > 0 && !room.getLastWinner().contains(room.getBankerUid())
                    || room.getLastWinner().size() == 0) {
                try {
                    RoomHelper.destroyRoom(gameExtension);
                    logger.debug("cleanroom:" + room.getRoomId() + ",round:" + room.getCurrRounds());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
//        }
    }

    public void convertBattleDataToOldClient(BattleData battleData) {
        List<BattleBalance> balances = battleData.getBattleBalances();
        for (BattleBalance balance : balances) {
            balance.setWinType(YNMJGameType.getWinType(balance.getWinType()));
        }

        //sort
        Collections.sort(battleData.getBattleBalances(), new Comparator<BattleBalance>() {
            @Override
            public int compare(BattleBalance o1, BattleBalance o2) {
                Integer o1Index = room.getPlayerById(o1.getPlayerId()).getIndex();
                Integer o2Index = room.getPlayerById(o2.getPlayerId()).getIndex();
                return o1Index.compareTo(o2Index);
            }
        });

        Collections.sort(battleData.getBattleCensuss(), new Comparator<BattleCensus>() {
            @Override
            public int compare(BattleCensus o1, BattleCensus o2) {
                Integer o1Index = room.getPlayerById(o1.getPlayerId()).getIndex();
                Integer o2Index = room.getPlayerById(o2.getPlayerId()).getIndex();
                return o1Index.compareTo(o2Index);
            }
        });
    }


    /**
     * card
     */
    private void addStepCard(List<Integer> cards, int playType, int card, String toBeCards) {
        if (card <= 0 && StringUtils.isBlank(toBeCards)) {
            return;
        }

        if (playType == YNMJGameType.PlayType.Ting) {
            String[] toCards = toBeCards.split(",");
            for (String c : toCards) {
                cards.add(Integer.parseInt(c));
            }
        } else if (playType == YNMJGameType.PlayType.CanChi) { //吃的消息格式 eg: 3 12 24 45 第一位是别人打的牌，剩下为候选的吃的组合
            if (cards.size() == 0) {
                cards.add(card);
            }
            String[] cs = toBeCards.split(",");
            for (String c : cs) {
                cards.add(Integer.parseInt(c));
            }
        } else {
            cards.add(card);
        }
    }

    public RoomInstance getRoom() {
        return room;
    }

    public void setRoom(RoomInstance room) {
        this.room = room;
        this.majiang = (MahjongEngine) room.getEngine();
    }


    private void saveRoundRecord(BattleData battleData) {
        List<MJPlayer> players = room.getAllPlayer();
        List<PlayerPointInfoPROTO> infos = new ArrayList<>();
        for (MJPlayer player : players) {
            PlayerPointInfoPROTO ppi = new PlayerPointInfoPROTO();
            ppi.setPlayerID(player.getUid());
            ppi.setHead(player.getHead());
            ppi.setNickName(player.getNickName());
            ppi.setChair(player.getIndex());
            ppi.setPoint(player.getScore());

            infos.add(ppi);
        }

        RecordService.saveRoundData(room.getRoomId(), (int) room.getCreateTime(), (int) (System.currentTimeMillis() / 1000), room.getRecordId(), infos, battleData.toSFSObject());
    }

    private BaseMajongPlayerAction getActionByType(LinkedList<BaseMajongPlayerAction> actions, int type) {
        for (BaseMajongPlayerAction action : actions) {
            if (type == action.getActionType()) {
                return action;
            }
        }
        return null;
    }

}
