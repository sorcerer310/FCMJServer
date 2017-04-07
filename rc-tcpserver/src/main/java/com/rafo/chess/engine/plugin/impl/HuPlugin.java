package com.rafo.chess.engine.plugin.impl;

import java.util.*;

import com.rafo.chess.engine.game.YNMJGameType;
import com.rafo.chess.engine.majiang.NoPass;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.gameModel.IECardModel;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.engine.room.RoomManager;

/***
 * 抓到指定牌触发执行
 *
 * @author Administrator
 */
public abstract class HuPlugin extends AbstractPlayerPlugin<HuAction>
        implements IPluginCheckCanExecuteAction<HuAction> {
    private final Logger logger = LoggerFactory.getLogger("cacl");
    private boolean huCheckOperation = false;

    public boolean isHuCheckOperation() {
        return huCheckOperation;
    }

    public void setHuCheckOperation(boolean huCheckOperation) {
        this.huCheckOperation = huCheckOperation;
    }

    private boolean payIsColor = false;                                                                                 //统计分数用的清一色属性
    private boolean payIsOpen = false;                                                                                  //统计用的是否开门属性

    @Override
    public void createCanExecuteAction(HuAction action) {
    }

    /***
     * 3个参数分别为room，handlist，openlist
     */
    public boolean checkExecute(Object... objects) {
        MJPlayer player = (MJPlayer) objects[0];
        ArrayList<MJCard> handCards = (ArrayList<MJCard>) objects[1];
        ArrayList<CardGroup> groupList = (ArrayList<CardGroup>) objects[2];
        int lastCard = handCards.get(handCards.size() - 1).getCardNum();

        //同一圈不能和第二张牌
        if (player.getPassNohu().noPass && player.getPassNohu().cardNum == lastCard)
            return false;


        return checkHu(player, handCards, groupList);
    }


    @SuppressWarnings("unchecked")
    @Override
    public void doOperation(HuAction action) throws ActionRuntimeException {
        PayDetailed pay = this.payment(action);

        List<Integer> fromUids = new ArrayList<>();
        Set<Integer> winPlayers = action.getRoomInstance().getEngine().getCalculator().getWinPlayers();
        for (int uid : pay.getFromUid()) { //流局可能会有多个人胡牌，多人之间，相互不结算
            if (!winPlayers.contains(uid)) {
                fromUids.add(uid);
            }
        }
        pay.setFromUid(fromUids);
        pay.setPayType(PayDetailed.PayType.Multiple);

        RoomInstance room = action.getRoomInstance();
        MJPlayer player = (MJPlayer) room.getPlayerById(action.getPlayerUid());
        // 胜利
        room.addLastWinner(player.getUid());
        room.getEngine().getCalculator().addWinPlayer(player.getUid());


        if (action.getPlayerUid() != action.getFromUid()) {
            if (room.getEngine().getCalculator().getWinPlayers().size() == 0) {
                LinkedList<MJCard> pool = action.getRoomInstance().getEngine().getOutCardPool();
                MJCard card = pool.removeLast();
                player.getHandCards().getHandCards().add(card);
            } else {
                MJCard card = new MJCard();
                card.setCardNum(action.getCard());
                player.getHandCards().getHandCards().add(card);
            }
        }


        // 永修麻将无叫嘴
        //player.setJiaozui(gen.getTempId());
        // 计算其他玩家是否叫嘴
        //this.checkJiaozuiAllPlayer(action);

        // 开始加番摸牌流程
//		if(isMaRoom(room))
//		{
//			logger.debug("jiamaStart" );
//			startMa(action);
//		}
    }

    public boolean analysis(HuAction action) {
        return action.getSubType() == gen.getSubType();
    }

    public abstract boolean checkHu(MJPlayer player, ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList);

    /**
     * 增加可以执行的动作,此处在ActionManager里执行即可，没有必要拿到这里来处理
     *
     * @param action 动作对象
     * @param player 玩家对象
     */
    public void addCanExecuteHuAction(IEPlayerAction action, MJPlayer player) {
        HuAction huAct = new HuAction(action.getRoomInstance());
        huAct.setCard(action.getCard());
        huAct.setPlayerUid(player.getUid());
        huAct.setFromUid(action.getPlayerUid());
        huAct.setSubType(gen.getSubType());
        huAct.setCanDoType(gen.getCanDoType());
        huAct.setPluginId(gen.getTempId());

        RoomManager.getRoomInstnaceByRoomid(player.getRoomId()).addCanExecuteAction(huAct);
    }

//	public void addCanExecuteHuAction(Hash)

    /***
     * 混一色
     * @param handCards    手牌
     * @param groupList    开门牌
     * @return
     */
    public boolean mixedOneColour(ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList) {
        ArrayList<MJCard> allCards = new ArrayList<MJCard>();
        allCards.addAll(handCards);
        for (CardGroup group : groupList) {
            ArrayList<MJCard> list = group.getCardsList();
            allCards.addAll(list);
        }
        HashSet<Integer> set = new HashSet<Integer>();
        for (IECardModel c : allCards) {
            if (c.getCardNum() > 40)
                continue;
            set.add(c.getCardNum() / 10);
            if (set.size() > 1) {
                return false;
            }
        }
        return true;
    }

    /***
     * 清一色
     * @param handCards    手牌
     * @param groupList    开门牌
     * @return
     */
    public boolean oneCorlor(ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList) {
        int temp = 0;
        for (IECardModel c : handCards) {
            if (temp == 0) {
                temp = c.getCardNum() / 10;
                continue;
            }
            if (c.getCardNum() / 10 != temp) {
                this.payIsColor = false;
                return this.payIsColor;
            }

        }
        for (CardGroup cg : groupList) {
            ArrayList<MJCard> cardsList = cg.getCardsList();
            if (temp != cardsList.get(0).getCardNum() / 10) {
                this.payIsColor = false;
                return this.payIsColor;
            }
        }
        this.payIsColor = true;
        return this.payIsColor;
    }

    /**
     * 字一色
     *
     * @param handCards
     * @param groupList
     * @return
     */
    public boolean isZiOneColor(ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList) {
        //开门牌不能万 饼 条
        for (CardGroup cg : groupList) {
            for (MJCard mjc : cg.getCardsList()) {
                if (mjc.getCardNum() < 40)
                    return false;
            }
        }

        int[] hCard = this.list2intArray(handCards);
        //如果手牌发现除了字以外的牌不算字一色不能和
        for (int c : hCard)
            if (c < 40)
                return false;

        return true;
    }

    /**
     * 全求人
     *
     * @param handCards
     * @param groupList
     * @param player
     * @return
     */
    public boolean isQuanQiuRen(ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList, MJPlayer player) {
        //1:如果手牌不为2张，返回false
        if (handCards.size() != 2)
            return false;

        //2:如果手里有暗杠，不算全求人
        for (CardGroup cg : groupList)
            if (cg.getGType() == YNMJGameType.PlayType.CealedKong)
                return false;

        //3:判断和牌是否为别人打出
        ArrayList<IEPlayerAction> lAction = RoomManager.getRoomInstnaceByRoomid(player.getRoomId()).getEngine().getMediator().getDoneActionList();
        IEPlayerAction iepa = lAction.get(lAction.size() - 1);
        //别人打出的牌与handCards的牌不一致不和
        if (iepa.getCard() != handCards.get(handCards.size() - 1).getCardNum()
                //动作执行者为自己不和
                || iepa.getPlayerUid() == player.getUid()
                //打出的牌与自己手里的牌不一致不和
                || iepa.getCard() != handCards.get(0).getCardNum())
            return false;
        return true;
    }

    /**
     * 判断是否为对对和
     *
     * @param handCards 手牌
     * @param groupList 开门牌
     * @return 返回判断是否为对对和
     */
    public boolean isDuiDuiHu(ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList) {
        int[] handcardsTemp = this.list2intArray(handCards);
        // 手牌没有顺子
        HashMap<Integer, Integer> map = this.arrayHandsCardCount(handcardsTemp);
        int duiCount = 0;
        for (int count : map.values()) {
            if (count == 1) return false;
            if (count == 2) duiCount++;
            if (count == 4) return false;
        }
        if (duiCount != 1)
            return false;
        //开门的牌不为碰返回false
        for (CardGroup cg : groupList) {
            ArrayList<MJCard> amjc = cg.getCardsList();
            //判断如果开门牌不为碰牌，返回false
            if (amjc.get(0).getCardNum() != amjc.get(1).getCardNum())
                return false;
        }

        return true;
    }

    /**
     * 判断是否为自摸
     *
     * @param handCards 手牌
     * @param player    玩家对象
     * @return 返回判断是否为自摸
     */
    public boolean isZiMo(ArrayList<MJCard> handCards, MJPlayer player) {
        MJCard lastcard = handCards.get(handCards.size() - 1);
        if (lastcard.getUid() == player.getUid()) return true;
        return false;
    }

    /**
     * 永修规则，必须自摸算门清，点炮不算门清
     * 门清只能自摸、和夹心或单钓
     *
     * @return
     */
    public boolean isMenQianQing(ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList, MJPlayer player) {
        //如果不是自摸，不算门前清
        if (!isZiMo(handCards, player)) return false;
        if (player.isOpen()) return false;
        if (!isHu(this.list2intArray(handCards)) || !this.isjia) return false;
        return true;
    }

    /**
     * 判断牌型是否为一条龙
     * @param handCards 手牌
     * @param groupList 开门的牌
     * @return          返回牌型是否符合一条龙
     */
    public boolean isYiTiaoLong(ArrayList<MJCard> handCards,ArrayList<CardGroup> groupList){
        //1:判断开门的牌如果有顺子必须为清一色，否则返回失败
        for (CardGroup cg : groupList) {
            ArrayList<MJCard> amjc = cg.getCardsList();
            if (amjc.get(0).getCardNum() != amjc.get(1).getCardNum() && !this.oneCorlor(handCards, groupList))
                return false;
        }

        //2:判断是否包含一条龙的牌
        ArrayList<MJCard> lLong = null;                                                                                 //保存一条龙的集合
        HashMap<MJCard.MJCardType, ArrayList<MJCard>> hmCard = splitHandCards(handCards);
        Iterator<MJCard.MJCardType> itCardKey = hmCard.keySet().iterator();
        while (itCardKey.hasNext()) {
            MJCard.MJCardType mjct = itCardKey.next();
            if (hmCard.get(mjct).size() >= 9) {
                lLong = hmCard.remove(mjct);
                break;
            }
        }
        if (lLong == null) return false;

        //3:判断龙牌集合中是否有一条龙,从龙牌集合中依次一处从1到9的牌，如果缺哪一张牌，说明不成为一条龙
        ArrayList<Integer> save_long = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            boolean removeflag = false;
            Iterator<MJCard> itmjc = lLong.iterator();
            while (itmjc.hasNext()) {
                MJCard mjc = itmjc.next();
                if (mjc.getCardNum() % 10 == i) {
                    lLong.remove(mjc);
                    save_long.add(mjc.getCardNum());
                    removeflag = true;
                    break;
                }
            }
            if (!removeflag) return false;
        }

        //4:判断胡牌是否为2、3、5、7、8，如果是这几张牌，那么可以判断当前胡夹
        int lastCard = handCards.get(handCards.size() - 1).getCardNum();
        boolean ytljia = false;                                                                                         //表示当前一条龙胡的是否为夹
        if (lastCard % 10 == 2 || lastCard % 10 == 3 || lastCard % 10 == 5 || lastCard % 10 == 7 || lastCard % 10 == 8)
            ytljia = true;

        //5:判断龙牌集合剩下的牌型如果为3N+2，则为和牌，和是否为夹，此处还可以补充判断是否为边或单钓
        int[] remainCards = new int[0];                                                                                 //除了龙的剩余牌
        Iterator<ArrayList<MJCard>> itRemainCard = hmCard.values().iterator();
        while (itRemainCard.hasNext()) {
            ArrayList<MJCard> almjc = itRemainCard.next();
            for (MJCard mjc : almjc)
                remainCards = ArrayUtils.add(remainCards, mjc.getCardNum());
        }

        for (MJCard mjc : lLong)
            remainCards = ArrayUtils.add(remainCards, mjc.getCardNum());
        boolean huflag = isHu(remainCards,lastCard);
        if (!huflag)
            return false;
        else if (huflag && this.isjia)
            ytljia = true;

        //6:判断是【青龙在手】还是【青龙点睛】,如果是【青龙在手】必须胡夹或者单吊
        //【青龙在手】的特点是最后一张牌不为一条龙中的牌

        //如果最后一张牌在龙牌容器中，需要将该牌从龙牌容器中去掉,这样剩下的才是手牌中的龙牌组合
        if (save_long.contains(lastCard)) save_long.remove(new Integer(lastCard));
        //如果龙牌9张在手，并且不是夹不让胡（龙在手，非夹不让胡）
        if (save_long.size() == 9)
            if(!ytljia)
                return false;

        return true;
    }

    /**
     * 判断三张牌是否间隔为2以上的牌
     * 至少为[1、4、7]，[2、5、8]，[3、6、9]
     *
     * @return
     */
    public boolean isIntervalCard(List<MJCard> cards) {
        int[] cardValue = new int[cards.size()];
        for (int i = 0; i < cardValue.length; i++)
            cardValue[i] = cards.get(i).getCardNum();

        int[] sortCardValue = this.arraySort(cardValue);
        //获得牌面值
        for (int i = 0; i < sortCardValue.length; i++)
            sortCardValue[i] = sortCardValue[i] % 10;
        //2张牌判断,牌2面值比牌1面值至少大2
        if (sortCardValue.length == 2
                && (sortCardValue[0] + 2 < (sortCardValue[1])))
            return true;
            //3张牌判断，牌2面值比牌1面值大2，且牌3面值比牌2面值大2
        else if (sortCardValue.length == 3
                && (sortCardValue[0] + 2 < (sortCardValue[1]) && sortCardValue[1] + 2 < (sortCardValue[2])))
            return true;
        else if (sortCardValue.length == 1)
            return true;
        else
            return false;
    }

    /**
     * 所有手牌计数,记录每种手牌的数量
     *
     * @param cardsTemp
     * @return
     */
    public HashMap<Integer, Integer> arrayHandsCardCount(int[] cardsTemp) {
        // 计数
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int i = 0; i < cardsTemp.length; i++) {
            if (!map.containsKey(cardsTemp[i])) {
                map.put(cardsTemp[i], 0);
            }
            int count = map.get(cardsTemp[i]) + 1;
            map.put(cardsTemp[i], count);
        }
        return map;
    }

    /**
     * 移除对应个数的指定牌
     *
     * @param cards      要操作的牌的集合
     * @param ocard      要移除的目标牌
     * @param countLimit 要移除的数量
     * @return 返回移除目标牌后剩下牌的集合，失败返回null
     */
    public int[] arrayRemove(int[] cards, int ocard, int countLimit) {
        int count = 0;
        int[] reCards = new int[cards.length - countLimit];
        int index = 0;
        for (int i = 0; i < cards.length; i++) {
            //当前牌等于要移除的目标牌 且 计数count小于移除数量上线，count++
            if (cards[i] == ocard && count < countLimit) {
                count++;
                continue;
            }
            if (index < reCards.length)
                reCards[index++] = cards[i];
            else {
                return null;
            }
        }
        if (count != countLimit)
            return null;
        return reCards;
    }


    public boolean mid = false;
    public boolean side = false;
    public boolean single = false;
    public int singlecard = -1;
    public int lastcard = -1;                                               //手牌最后一张，要和的牌
    public boolean isjia = false;                                           //是否为特殊和法，全部成为夹和

    /**
     * 判断是否为和牌,默认胡牌为数组的最后一张牌
     * @param cardsTemp
     * @return
     */
    public boolean isHu(int[] cardsTemp) {
        return isHu(cardsTemp,cardsTemp[cardsTemp.length-1]);
    }

    /**
     * 判定当前牌型是否符合3N+2可以胡的牌型，同时制定最后胡的一张牌的值
     * @param cardsTemp 要判定的牌数组
     * @param lastcard  最后要胡的一张牌
     * @return          返回是否胡
     */
    public boolean isHu(int[] cardsTemp,int lastcard) {
        boolean res = false;
        if (cardsTemp == null || cardsTemp.length == 0) {
            return res;
        }
        if ((cardsTemp.length - 2) % 3 != 0) {
            // 胡牌时张数 = 3N+2
            return res;
        }

        boolean ishu = false;                                               //是否可以和

//        lastcard = cardsTemp[cardsTemp.length - 1];                        //最后一张
        HashMap<Integer, Integer> map = arrayHandsCardCount(cardsTemp);
        for (Integer cNum : map.keySet()) {
            mid = false;
            side = false;
            single = true;
            if (map.get(cNum) > 1) {
                //除所有的对子
                int[] rescards = arrayRemove(cardsTemp, cNum, 2);
                singlecard = cNum;
                //判断剩下的牌是否全为顺子或刻子
                res = isSentence(rescards);
                if (res) {
                    ishu = true;
                    if (mid || side || (single && cNum == lastcard))
                        isjia = true;
                }
            }
        }

        return ishu;
    }

    /**
     * 一句牌（顺子）
     * 是否全部为顺子或刻子
     *
     * @param handCards 手牌集合
     * @return
     */
    public boolean isSentence(int[] handCards) {
        if (handCards != null && handCards.length > 0) {
            handCards = arraySort(handCards);
            while (handCards.length > 0) {
                //先移除当前牌的刻子
                int[] temp = arrayRemove(handCards, handCards[0], 3);
                //如果移除为null表示移除碰失败，继续移除顺子
                if (temp == null) {
                    int cardTemp = handCards[0];
                    //如果当前牌出现了字牌直接返回失败
                    if (cardTemp > 40)
                        return false;
                    //移除相邻的顺子三张牌，如果移除失败表示出现了不为顺子的牌返回失败
                    for (int i = 0; i < 3; i++) {
                        temp = arrayRemove(handCards, cardTemp + i, 1);
                        if (temp == null) return false;

                        //增加对加、边的判断，但不影响原来的和牌判断
                        if (i == 1 && cardTemp + i == lastcard)
                            mid = true;
                        handCards = temp;
                        if (cardTemp % 10 == 1)
                            if (lastcard == cardTemp + 2)
                                side = true;
                        if ((cardTemp + 2) % 10 == 9)
                            if (lastcard == cardTemp)
                                side = true;
                    }

                    //增加单调判断，不影响原来和平判断
                    if (cardTemp % 10 > 1 && cardTemp % 10 < 7) {
                        if (lastcard == cardTemp - 1 || lastcard == cardTemp + 3
                                || singlecard != lastcard)
                            single = false;
                    }
                }
                handCards = temp;
            }
        }
        return true;
    }

    /**
     * 判断牌型是否为刻子
     *
     * @param handCards
     * @return
     */
    public boolean isKeZi(int[] handCards) {
        if (handCards.length != 3)
            return false;
        if (handCards[0] != handCards[1] || handCards[1] != handCards[2])
            return false;
        return true;
    }

    /**
     * 判断手里的字一色是否为乱风倒
     *
     * @param hmjc       手牌
     * @param cardGroups 开门牌
     * @return
     */
    public boolean isLuanFengDao(ArrayList<MJCard> hmjc, ArrayList<CardGroup> cardGroups) {
        //开门牌不能万 饼 条
        for (CardGroup cg : cardGroups) {
            for (MJCard mjc : cg.getCardsList()) {
                if (mjc.getCardNum() < 40)
                    return false;
            }
        }

        //手牌不能为万饼条
        for (MJCard c : hmjc)
            if (c.getCardNum() < 40)
                return false;

        //字一色和牌符合3*N+2,则不是乱风倒
        if (isHu(this.list2intArray(hmjc)))
            return false;

        return true;
    }

    /**
     * 收集手牌与开门牌的刻子
     *
     * @param hmjc 手牌
     * @param omjc 开门牌
     * @param p    玩家对象，取玩家位置 0-3表示东、南、西、北
     * @return 返回刻子或杠的数量
     */
    public int collectKeZiGangFromFeng(ArrayList<MJCard> hmjc, ArrayList<CardGroup> omjc, IPlayer p) {

        logger.debug("Huplugin.collectKeZiGangFromFeng:{player:" + p.getUid() + ",playerIndex:" + p.getIndex()
                + ",handCards:" + hmjc.toString() + ",cardGroups:" + omjc.toString());

        //如果是字一色，牌型不为N*3+2,不计算本风数量
        if (!this.isHu(this.list2intArray(hmjc)))
            return 0;

        //用来处理永修东家跟庄走的问题
        RoomInstance room = RoomManager.getRoomInstnaceByRoomid(p.getRoomId());
        int offset = room.getPlayerById(room.getBankerUid()).getIndex();
        int idx = (p.getIndex() - offset + 4) % 4;

        int c = 0;
        //判断手牌
        HashMap<Integer, Integer> hm = this.arrayHandsCardCount(this.list2intArray(hmjc));
        Iterator<Integer> it = hm.keySet().iterator();
        while (it.hasNext()) {
            Integer ci = it.next();
            if (hm.get(ci) >= 3) {
                //判断中发白
                if ((ci == 45 || ci == 46 || ci == 47))
                    c++;
                    //判断本风位对应的东南西北
                else if ((idx + 41) == ci)
                    c++;
            }
        }
        //判断开门牌
        for (CardGroup cg : omjc) {
            ArrayList<MJCard> mjc = cg.getCardsList();
            //如果为碰或杠
            if (mjc.size() >= 3) {
                //如果为中发白
                if (mjc.get(0).getCardNum() == 45 || mjc.get(0).getCardNum() == 46 || mjc.get(0).getCardNum() == 47)
                    c++;
                    //如果为东南西北
                else if ((idx + 41) == mjc.get(0).getCardNum())
                    c++;
            }
        }
        return c;
    }

    /**
     * 获得指定类型牌集合
     *
     * @param list
     * @return
     */
    public ArrayList<MJCard> getSameTypeCards(ArrayList<MJCard> list, MJCard.MJCardType mjct) {
        ArrayList<MJCard> ziCards = new ArrayList<>();
        switch (mjct) {
            case WAN:
                for (int i = 0; i < list.size(); i++) {
                    MJCard mjc = list.get(i);
                    if (mjc.getCardNum() > 10 && mjc.getCardNum() < 20)
                        ziCards.add(mjc);
                }
                break;
            case TIAO:
                for (int i = 0; i < list.size(); i++) {
                    MJCard mjc = list.get(i);
                    if (mjc.getCardNum() > 20 && mjc.getCardNum() < 30)
                        ziCards.add(mjc);
                }
                break;
            case TONG:
                for (int i = 0; i < list.size(); i++) {
                    MJCard mjc = list.get(i);
                    if (mjc.getCardNum() > 30 && mjc.getCardNum() < 40)
                        ziCards.add(mjc);
                }
                break;
            case ZI:
                for (int i = 0; i < list.size(); i++) {
                    MJCard mjc = list.get(i);
                    if (mjc.getCardNum() > 40)
                        ziCards.add(mjc);
                }
                break;
        }
        return ziCards;
    }

    /**
     * 拆分手牌，按牌类型拆分
     *
     * @param hcards 手牌
     * @return 返回以key分类的各组牌
     */
    public HashMap<MJCard.MJCardType, ArrayList<MJCard>> splitHandCards(ArrayList<MJCard> hcards) {
        HashMap<MJCard.MJCardType, ArrayList<MJCard>> hm = new HashMap<>();
        hm.put(MJCard.MJCardType.WAN, getSameTypeCards(hcards, MJCard.MJCardType.WAN));
        hm.put(MJCard.MJCardType.TIAO, getSameTypeCards(hcards, MJCard.MJCardType.TIAO));
        hm.put(MJCard.MJCardType.TONG, getSameTypeCards(hcards, MJCard.MJCardType.TONG));
        hm.put(MJCard.MJCardType.ZI, getSameTypeCards(hcards, MJCard.MJCardType.ZI));
        return hm;
    }

    /**
     * 判断集合中是否重复的牌
     *
     * @param cards 牌集合
     * @return 返回是否有重复牌
     */
    public boolean isRepeat(ArrayList<MJCard> cards) {
        Set<Integer> sCards = new HashSet<>();
        for (MJCard mjc : cards)
            sCards.add(mjc.getCardNum());

        return sCards.size() == cards.size() ? false : true;
    }


    /**
     * ArrayList牌集合容器转为数组形式
     *
     * @param handCards
     * @return
     */
    public int[] list2intArray(ArrayList<MJCard> handCards) {
        int size = handCards.size();
        int[] cardsTemp = new int[size];

        for (int i = 0; i < handCards.size(); i++) {
            cardsTemp[i] = handCards.get(i).getCardNum();
        }

        return cardsTemp;
    }

    // 排序
    public int[] arraySort(int[] cards) {
        if (cards != null) {
            for (int i = 0; i < cards.length; i++) {
                Integer tmp = cards[i];
                for (int j = i + 1; j < cards.length; j++) {
                    if (tmp.intValue() > cards[j]) {
                        tmp = cards[j];
                        cards[j] = cards[i];
                        cards[i] = tmp;
                    }
                }
            }
        }
        return cards;
    }

    // 排序
    public ArrayList<MJCard> ArraySort(ArrayList<MJCard> cards) {
        if (cards != null) {
            for (int i = 0; i < cards.size(); i++) {
                MJCard tmp = cards.get(i);
                for (int j = i + 1; j < cards.size(); j++) {
                    if (tmp.getCardNum() > cards.get(j).getCardNum()) {
                        tmp = cards.get(j);
                        cards.set(j, cards.get(i));
                        cards.set(i, tmp);
                    }
                }
            }
        }

        return cards;
    }

    public void checkJiaozuiAllPlayer(HuAction action) {
        // 计算其他玩家是否叫嘴
        ArrayList<IPlayer> others = action.getRoomInstance().getAllPlayer();
        for (IPlayer other : others) {
            MJPlayer p = (MJPlayer) other;
            if (action.getRoomInstance().getLastWinner().contains(other.getUid()))
                continue;
            if (p.isJiaozui() < 0) {
                ActionManager.jiaozuiCheck(p);
            }
        }
    }

    // 加码相关
    public boolean isMaRoom(RoomInstance room) {
        return ((Integer) room.getAttribute(RoomAttributeConstants.YB_MA_TYPE) > 0 &&
                (Integer) room.getAttribute(RoomAttributeConstants.YB_MA_TYPE) < 5);
    }

    public boolean isFanMaRoom(RoomInstance room) {
        return (Integer) room.getAttribute(RoomAttributeConstants.YB_MA_TYPE) == 1;
    }

    public int getJiaMaCount(RoomInstance room) {
        if ((Integer) room.getAttribute(RoomAttributeConstants.YB_MA_TYPE) == 1)
            return 1;
        else if ((Integer) room.getAttribute(RoomAttributeConstants.YB_MA_TYPE) == 2)
            return 4;
        else if ((Integer) room.getAttribute(RoomAttributeConstants.YB_MA_TYPE) == 3)
            return 8;
        else if ((Integer) room.getAttribute(RoomAttributeConstants.YB_MA_TYPE) == 4)
            return 16;

        return 0;
    }

    // 加码
    public void startMa(HuAction act) {
        try {
            // 从牌库里抽牌
            ArrayList<MJCard> pool = act.getRoomInstance().getEngine().getCardPool();
            ArrayList<Integer> cards = act.getRoomInstance().getEngine().getMaCards();
            cards.clear();                //发多次的判定，王萌要求添加的
            int nCount = isFanMaRoom(act.getRoomInstance()) ? 1 : getJiaMaCount(act.getRoomInstance());
            nCount = Math.min(nCount, pool.size());
            for (int i = 0; i < nCount; ++i) {
                MJCard card = pool.get(i);
                if (card != null) {
                    cards.add(card.getCardNum());
                }
            }

            // 把四家匹配结果放到room的结果队列里
            if (isFanMaRoom(act.getRoomInstance())) {
//				checkMa(act, cards, false);
            } else {
            }
//				checkMa(act, cards, true);
        } catch (Exception e) {
            // TODO: handle exception
            logger.debug("加码错误1:" + e.toString());
            act.getRoomInstance().setRoomStatus(RoomInstance.RoomState.jiama.getValue());
        }
    }

    // 加码翻码处理
    public void checkMa(HuAction act, ArrayList<Integer> cards, boolean bJiaMa) {
        // 找出自己符合的牌型
        IPlayer player = act.getRoomInstance().getPlayerById(act.getPlayerUid());
        // 自己与庄家的关系(庄、下家、对家、上家)
        MJPlayer banker = (MJPlayer) act.getRoomInstance().getPlayerById(act.getRoomInstance().getBankerUid());
        int value = player.getIndex() - banker.getIndex();
        int nIdxInArr = -1;
        ArrayList<Integer> checkCards = new ArrayList<Integer>();
        if (Math.abs(value) == 2) {
            // 对家
            nIdxInArr = 2;

            if (bJiaMa) {
                checkCards.add(46);
                checkCards.add(43);
                checkCards.add(13);
                checkCards.add(17);
                checkCards.add(23);
                checkCards.add(27);
                checkCards.add(33);
                checkCards.add(37);
            } else {
                checkCards.add(cards.get(0));
            }
        } else if (value == 0) {
            // 自己是庄家
            nIdxInArr = 0;

            if (bJiaMa) {
                checkCards.add(41);
                checkCards.add(11);
                checkCards.add(15);
                checkCards.add(19);
                checkCards.add(21);
                checkCards.add(25);
                checkCards.add(29);
                checkCards.add(31);
                checkCards.add(35);
                checkCards.add(39);
            } else {
                checkCards.add(cards.get(0));
            }
        } else if (value == 1 || value == -3) {
            // 下家
            nIdxInArr = 1;

            if (bJiaMa) {
                checkCards.add(45);
                checkCards.add(42);
                checkCards.add(12);
                checkCards.add(16);
                checkCards.add(22);
                checkCards.add(26);
                checkCards.add(32);
                checkCards.add(36);
            } else {
                checkCards.add(cards.get(0));
            }
        } else if (value == -1 || value == 3) {
            nIdxInArr = 3;

            if (bJiaMa) {
                checkCards.add(47);
                checkCards.add(44);
                checkCards.add(14);
                checkCards.add(18);
                checkCards.add(24);
                checkCards.add(28);
                checkCards.add(34);
                checkCards.add(38);
            } else {
                checkCards.add(cards.get(0));
            }
        } else {
            logger.debug("加码计算出错!" + "player idx:" + player.getIndex() + ";banker idx:" + banker.getIndex() + ", nIdxInArr:" + nIdxInArr);
        }

        ArrayList<ArrayList<Integer>> list = act.getRoomInstance().getEngine().getMaResult();
        if (list.size() != 4) {
            list.clear();
            for (int i = 0; i < 4; ++i) {
                ArrayList<Integer> playerCardResult = new ArrayList<Integer>();
                list.add(playerCardResult);
            }
        }

        // 匹配
        ArrayList<Integer> matchCardsArrayList = getMatchCard(checkCards, cards);
        list.set(nIdxInArr, matchCardsArrayList);

        logger.debug("加码流程2: 第" + nIdxInArr + "家" + "匹配了" + matchCardsArrayList.size() + "张牌");

        int nScores = 0;
        if (matchCardsArrayList.size() != 0) {
            logger.debug("加码支付---------------");
            PayDetailed pay = this.payment(act);
//			int[] fromIds = new int[1];
//			fromIds[0] = player.getUid();
//			pay.setFromUid(fromIds);

            if (bJiaMa) {
                nScores = matchCardsArrayList.size();
            } else {
                if (matchCardsArrayList.get(0) > 40)
                    nScores = 5;
                else
                    nScores = matchCardsArrayList.get(0) % 10;
            }

            pay.setRate(nScores);

            pay.setPayType(PayDetailed.PayType.Multiple);

            pay.bFromMa = true;

            pay.setCanMerge(false);
        }

        // 四家翻码得分
        ArrayList<Integer> listScores = act.getRoomInstance().getEngine().getMaScores();
        listScores.clear();
        for (int i = 0; i < 4; ++i)
            listScores.add(0);

        listScores.set(nIdxInArr, nScores);
    }

    public ArrayList<Integer> getMatchCard(ArrayList<Integer> checkCards, ArrayList<Integer> maCards) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < maCards.size(); ++i) {
            for (int j = 0; j < checkCards.size(); ++j) {
                if (maCards.get(i) == checkCards.get(j).intValue()) {
                    result.add(maCards.get(i));
                }
            }
        }

        return result;
    }

    public boolean isPayIsColor() {
        return payIsColor;
    }

    public void setPayIsColor(boolean payIsColor) {
        this.payIsColor = payIsColor;
    }

    public boolean isPayIsOpen() {
        return payIsOpen;
    }

    public void setPayIsOpen(boolean payIsOpen) {
        this.payIsOpen = payIsOpen;
    }
}
