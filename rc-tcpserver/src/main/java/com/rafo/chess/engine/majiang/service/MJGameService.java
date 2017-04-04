package com.rafo.chess.engine.majiang.service;

import com.rafo.chess.common.model.record.PlayerPointInfoPROTO;
import com.rafo.chess.common.service.record.RecordService;
import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.majiang.action.GangAction;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.plugin.OptPluginFactory;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import com.rafo.chess.model.battle.*;
import com.rafo.chess.common.GlobalConstants;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.game.YBMJGameType;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.*;
import com.rafo.chess.engine.majiang.action.BaseMajongPlayerAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.room.RoomHelper;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.exception.PersistException;
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
public class MJGameService {

    private MahjongEngine majiang;
    private GameExtension gameExtension;
    private RoomInstance room;
    private final Logger logger = LoggerFactory.getLogger("play");

    public MJGameService(GameExtension roomExt) {
        this.gameExtension = roomExt;
    }


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

    //准备
    public synchronized void ready(int playerId) throws ActionRuntimeException {
        setPlayerStatus(playerId, false);
        sendBattleStatus(playerId); //发送准备
        if (room.getRoomStatus() == RoomInstance.RoomState.gameing.ordinal()) {
            BattleStep step = new BattleStep(playerId, room.getCurrentTurnPlayerId(), YBMJGameType.PlayType.Deal);
            sendBattleData(step, playerId, false);
        } else if (room.getRoomStatus() == RoomInstance.RoomState.seating.ordinal()) {//离线重连,动作定义为发牌,发送牌局切片
            sendBattleSeatMessage(playerId); //恢复首局定座位消息
        } else if (isAllPlayerReady()) { //定庄
            majiang.dingzhuang();

            setBattleStatus();
            sendBattleStatus(playerId); //battle

            sendBattleSeatMessage(0); //发送座位信息
        }
        logger.debug("ready\troom:" + room.getRoomId() + ",uid:" + playerId);
    }

    //定完庄，坐好了，发牌
    public synchronized void seated(int playerId) throws ActionRuntimeException {
        if (room.getRoomStatus() != RoomInstance.RoomState.seating.ordinal()) {
            throw new RuntimeException("invalid seated request " + playerId);
        }
        room.getPlayerById(playerId).setSeated(true);

        if (isAllPlayerSeat()) {
            //发牌
            //开始游戏
            majiang.startGame();

            //海底测试
   /*         int len = room.getEngine().getCardPool().size();
            for (int i = 20; i < len; i++) {
                room.getEngine().getCardPool().remove(room.getEngine().getCardPool().size() - 1);
            }
            *//*MJCard c= new MJCard();
            for(int i=5;i<15;i++) {
                c.setCardNum(46);
                room.getEngine().getCardPool().add(c);
            }*/
            //发送数据
            BattleStep step = new BattleStep(room.getBankerUid(), room.getBankerUid(), YBMJGameType.PlayType.Deal);
            sendBattleData(step, 0, false);
        }
        logger.debug("seated\troom:" + room.getRoomId() + ",uid:" + playerId);
    }


    public synchronized void play(int playerId, int playType, int card) throws ActionRuntimeException {
        this.play(playerId, playType, card, "");
    }

    //打牌
    public synchronized void play(int playerId, int playType, int card, String tobeCards) throws ActionRuntimeException {
        long begin = System.currentTimeMillis();

        if (this.gameExtension != null) {
            this.gameExtension.trace("==== play card " + playerId + " " + playType + " " + card);
        }

        //打牌step
        BattleStep step = new BattleStep(playerId, room.getCurrentTurnPlayerId(), playType);
        step.addCard(card);

        if (tobeCards.length() > 0) {
            if (playType == YBMJGameType.PlayType.XuanFengGang) {
                step.getCard().clear();
            }

            String[] cards = tobeCards.split(",");
            for (String c : cards) {
                step.addCard(Integer.parseInt(c));
            }
        }

        int[] actionType = YBMJGameType.getActionTypeByPlayType(playType);
        int currentStep = majiang.getMediator().getCurrentStep();
        majiang.executeAction(actionType[0], card, playerId, actionType[1], tobeCards);

        if (playType == YBMJGameType.PlayType.Kong) {
            //找出碰的UID
            Integer fromUid = getPengUid(card, playerId);
            if (fromUid != null) {
                step.setTargetId(fromUid);
            }
        }

        if (playType == YBMJGameType.PlayType.CealedKong
                || playType == YBMJGameType.PlayType.XuanFengGang
                || playType == YBMJGameType.PlayType.SanJianKe) {
            GangAction action = getGangAction(playerId, currentStep);
            if (action != null && action.isGanghou()) {
                step.getSubtype().add(YBMJGameType.PlayType.GangHouAnGang);
            }
        } else {
            MJPlayer player = (MJPlayer) room.getPlayerById(step.getOwnerId());
            step.setSubtype(getTingSubType(step.getPlayType(), player));
        }

        if (room.getRoomStatus() == RoomInstance.RoomState.calculated.getValue()) { //判断游戏是否结束
            calculateResult(step, playerId, currentStep); //算分，取结果
        } else {
            sendBattleData(step, 0, false);
        }

        logger.debug("room:" + room.getRoomId() + ";round:" + room.getCurrRounds() + ";uid:" + playerId + ";type:" + playType
                + ";card:" + card + ";tobe:" + tobeCards + ";ts:" + (System.currentTimeMillis() - begin));
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

    // 发送玩家状态
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

    //判断是否所有用户都准备好了
    private boolean isAllPlayerSeat() {
        if (!room.isFull()) {
            return false;
        }
        ArrayList<MJPlayer> players = room.getAllPlayer();
        for (MJPlayer player : players) {
            if (!player.isSeated()) {
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


        if (step.getPlayType() != YBMJGameType.PlayType.Pass) { //玩家过牌不用通知任何人
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
            if (playType == YBMJGameType.PlayType.CanChi
                    || playType == YBMJGameType.PlayType.CanCealedKong
                    || playType == YBMJGameType.PlayType.CanSanJianKe) {
                step = stepContainPlayType(steps, playType, action.getPlayerUid());
            }

            if (step != null) {
                addStepCard(step.getCard(), playType, action.getCard(), action.getToBeCards());
            } else {
                step = new BattleStep();
                step.setPlayType(playType);
                step.setOwnerId(action.getPlayerUid());
                step.setTargetId(action.getFromUid());
                if (!action.isCanPass()) {
                    step.setAuto(true);
                }

                if (playType == YBMJGameType.PlayType.CanKong) { //补杠的时候
                    Integer uid = getPengUid(action.getCard(), action.getPlayerUid());
                    if (uid != null) {
                        step.setTargetId(uid);
                    }
                }

                if (playType == YBMJGameType.PlayType.CanXuanFengGang ||
                        playType == YBMJGameType.PlayType.CanCealedKong ||
                        playType == YBMJGameType.PlayType.CanSanJianKe) {
                    GangAction gangAction = (GangAction) action;
                    if (gangAction.isGanghou()) {
                        step.getSubtype().add(YBMJGameType.PlayType.GangHouAnGang);
                    }
                }

                addStepCard(step.getCard(), playType, action.getCard(), action.getToBeCards());

                //摸牌或者空的摸牌动作，不通知其他人
                if (playType != YBMJGameType.PlayType.Draw || action.getCard() == 0) {
                    step.setIgnoreOther(true);
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
                if ((player.getUid() != step.getOwnerId() && step.isIgnoreOther())
                        || (messageTargetPlayerId > 0 && player.getUid() != messageTargetPlayerId)) {
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
                    battleData.setRed((Integer) room.getAttribute(RoomAttributeConstants.ROOM_RED_DICE));
                } else {
                    battleData = res.getBattleData();
                }

                BattleStep stepBuilder = step.clone();

                //如果是摸牌，不能让其他用户知道他摸了什么牌，设置为0; 特别的发牌时的庄家的摸牌动作设置为-1
                if (stepBuilder.getPlayType() == YBMJGameType.PlayType.Draw && stepBuilder.getOwnerId() != player.getUid()) {
                    stepBuilder.getCard().clear();
                    if (steps.get(0).getPlayType() != YBMJGameType.PlayType.Deal) {
                        stepBuilder.addCard(-1);
                    } else {
                        stepBuilder.addCard(0);
                    }
                }

                if (step.getPlayType() == YBMJGameType.PlayType.Deal) { //发牌，首次或者离线重连
                    stepBuilder.setOwnerId(player.getUid());
                    for (MJPlayer p : players) {
                        BattleDealCard dealBuild = new BattleDealCard();
                        dealBuild.setPlayerId(p.getUid());
                        if (player.getUid() == p.getUid()) {
                            // 设置手上的牌
                            if (p.getHandCards() != null) {
                                int deleteCard = 0; //摸牌已经移动到手牌里面去了，需要把最后一次的摸牌提出来
                                for (BattleStep bs : steps) {
                                    if (bs.getPlayType() == YBMJGameType.PlayType.Draw
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
                        if (p.isKouTing() || p.isTing()) { //如果是听牌，需要添加听牌的balance
                            balance = new BattleBalance();
                            balance.setPlayerId(p.getUid());
                            CardBalance cardBlance = new CardBalance();
                            cardBlance.setType(p.isKouTing() ? YBMJGameType.PlayType.KouTing : YBMJGameType.PlayType.ReadyHand);
                            List<Integer> subType = getTingSubType(cardBlance.getType(), p);
                            cardBlance.setSubtype(subType);
                            balance.addBalances(cardBlance);
                        }

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
                                if (cg.getGType() == YBMJGameType.PlayType.Chi ||
                                        cg.getGType() == YBMJGameType.PlayType.XuanFengGang) {
                                    for (MJCard card : cg.getCardsList()) {
                                        cardBlance.getCards().add(card.getCardNum());
                                    }
                                } else {
                                    cardBlance.setCard(cg.getCardsList().get(0).getCardNum());
                                }
                                if (cg.getSubType() > 0) {
                                    cardBlance.getSubtype().add(cg.getSubType());
                                }
                                cardBlance.setTargetId(cg.targetId);
                                balance.addBalances(cardBlance);
                            }
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

    //牌局结束，走算分流程
    public void calculateResult(BattleStep lastStep, int playerId, int currentStep) {
        List<BattleStep> steps = new ArrayList<>();
        LinkedList<BaseMajongPlayerAction> actions = room.getCanExecuteActionList();

        //海底
        BattleStep winStep = null;
        if (actions != null && actions.size() > 0) {
            BaseMajongPlayerAction liujuAction = getActionByType(actions, IEMajongAction.ROOM_MATCH_LIUJU);
            if (liujuAction != null) {
                winStep = new BattleStep();
                steps.add(lastStep);
                BattleStep haidiStep = new BattleStep();
                haidiStep.setOwnerId(liujuAction.getPlayerUid());
                haidiStep.setPlayType(YBMJGameType.PlayType.HaiDi);
                String[] cards = liujuAction.getToBeCards().split(",");
                for (String card : cards) {
                    haidiStep.addCard(Integer.parseInt(card));
                }
                steps.add(haidiStep);

                winStep.setPlayType(YBMJGameType.PlayType.He);
                winStep.setOwnerId(room.getBankerUid());
            }
        }

        if (winStep == null) {
            winStep = new BattleStep();
            winStep.setPlayType(YBMJGameType.PlayType.Hu);
            winStep.setOwnerId(playerId);
        }

        winStep.setTargetId(0);

        steps.add(winStep);

        Map<Integer, BWBattleStepRES> results = new HashMap<>();

        //算分
        majiang.getCalculator().calculatePay();

        BattleData battleData = new BattleData();
        battleData.getBattleCensuss().addAll(majiang.getCalculator().getBattleCensuss().values());
        battleData.getBattleBalances().addAll(majiang.getCalculator().getUserBattleBalances().values());
        convertBattleDataToOldClient(battleData);
        battleData.setRed((Integer) room.getAttribute(RoomAttributeConstants.ROOM_RED_DICE));
        battleData.setBankerId(room.getBankerUid());
        battleData.setBattleTime(room.getCurrRounds());
        battleData.setBattleCount(room.getTotalRound());
        battleData.setOwnerId(room.getOwnerId());
        for (BattleStep step : steps) {
            step.setRemainCardCount(majiang.getCardPool().size());
            battleData.addBattleSteps(step);
        }

        ArrayList<MJPlayer> players = room.getAllPlayer();

        for (MJPlayer player : players) {
            BWBattleStepRES res = new BWBattleStepRES();
            res.setResult(GlobalConstants.BW_Battle_Step_SUCCESS);
            res.setAccountId(String.valueOf(player.getUid()));
            res.setBattleData(battleData);

            results.put(player.getUid(), res);
        }

        //缓存记录战绩
        saveRoundRecord(battleData);

        for (Map.Entry<Integer, BWBattleStepRES> palayerResult : results.entrySet()) {
            if (gameExtension != null) {
                gameExtension.trace(palayerResult.getValue().toSFSObject().toJson());
                logger.debug("room:" + room.getRoomId() + ",GameService: " + palayerResult.getValue().toSFSObject().toJson());
                CmdsUtils.sendMessage(gameExtension, CmdsUtils.CMD_BATTLE_STEP, palayerResult.getValue().toSFSObject(), String.valueOf(palayerResult.getKey()));
            }
        }

        //运营要求暂时不扣钻石，李志刚
        /*if(room.getCurrRounds() == 1 && gameExtension != null){// gameExtension != null for test
            try {
                RoomHelper.subCard(gameExtension.getParentRoom(), room.getCurrRounds(), (SFSExtension)gameExtension);
            } catch (SFSVariableException e) {
                e.printStackTrace();
            } catch (PersistException e) {
                e.printStackTrace();
            }
        }*/

        //重置玩家状态
        for (MJPlayer p : players) {
            p.setPlayerState(IPlayer.PlayState.Idle);
        }

        // 完成所有牌局，解散房间
        if (room.getTotalRound() <= room.getCurrRounds()) {
            try {
                RoomHelper.destroyRoom(gameExtension);
                logger.debug("cleanroom:" + room.getRoomId() + ",round:" + room.getCurrRounds());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void convertBattleDataToOldClient(BattleData battleData) {
        List<BattleBalance> balances = battleData.getBattleBalances();
        for (BattleBalance balance : balances) {
            balance.setWinType(YBMJGameType.getWinType(balance.getWinType()));
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

        if (playType == YBMJGameType.PlayType.CanKouTing || playType == YBMJGameType.PlayType.CanReadyHand) {
            String[] toCards = toBeCards.split(",");
            for (String c : toCards) {
                cards.add(Integer.parseInt(c));
            }
        } else if (playType == YBMJGameType.PlayType.CanChi) { //吃的消息格式 eg: 3 12 24 45 第一位是别人打的牌，剩下为候选的吃的组合
            if (cards.size() == 0) {
                cards.add(card);
            }
            String[] cs = toBeCards.split(",");
            for (String c : cs) {
                cards.add(Integer.parseInt(c));
            }
        } else if (playType == YBMJGameType.PlayType.CanXuanFengGang) {
            //不发了
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


    /**
     * 听牌明细，用于界面展示用户听了什么牌
     * 长度为3
     * 第一位表示牌型[一色/飘/七对/豪七等]
     * 第二位表示时候是手把一
     * 第三位表示听牌类型[扣听/听]
     *
     * @param player
     * @return
     */
    private List<Integer> getTingSubType(int playType, MJPlayer player) {
        List<Integer> tingSubType = new ArrayList<>();
        if (playType != YBMJGameType.PlayType.ReadyHand
                && playType != YBMJGameType.PlayType.KouTing) {
            return tingSubType;
        }

        IEPlayerAction tingAction = getTingAction(player.getUid());
        IOptPlugin huPlugin = OptPluginFactory.createOptPlugin(player.isJiaozui());
        if (huPlugin != null) {
            tingSubType.add(huPlugin.getGen().getSubType());
            tingSubType.add(player.getHandCards().getHandCards().size() <= 2 ? YBMJGameType.PlayType.ShouBaYi : 0);
            tingSubType.add(tingAction.getSubType());
        }
        return tingSubType;
    }


    private Integer getPengUid(int card, int uid) {
        ArrayList<IEPlayerAction> doneActions = room.getEngine().getMediator().getDoneActionList();
        for (IEPlayerAction doneAction : doneActions) {
            if (doneAction.getPlayerUid() == uid
                    && doneAction.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_PENG
                    && doneAction.getCard() == card) {
                return doneAction.getFromUid();
            }
        }
        return null;
    }

    private IEPlayerAction getTingAction(int uid) {
        ArrayList<IEPlayerAction> doneActions = room.getEngine().getMediator().getDoneActionList();
        for (IEPlayerAction doneAction : doneActions) {
            if (doneAction.getPlayerUid() == uid
                    && doneAction.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING) {
                return doneAction;
            }
        }
        return null;
    }

    private GangAction getGangAction(int uid, int step) {
        IEPlayerAction doneAction = room.getEngine().getMediator().getDoneActionByStep(step);
        if (doneAction.getPlayerUid() == uid && doneAction.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG) {
            return (GangAction) doneAction;
        }
        return null;
    }
}
