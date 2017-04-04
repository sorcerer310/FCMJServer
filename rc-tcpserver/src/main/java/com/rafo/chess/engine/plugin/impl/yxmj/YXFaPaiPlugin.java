package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.MahjongEngine;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.DealerDealAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;
import com.rafo.chess.engine.plugin.OptPluginFactory;
import com.rafo.chess.engine.plugin.impl.FapaiPlugin;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.template.impl.PluginTemplateGen;
import com.rafo.chess.template.impl.RoomSettingTemplateGen;

import java.util.*;

/***
 * 永修发牌
 * @author Administrator
 *
 */
public class YXFaPaiPlugin extends FapaiPlugin {
    boolean cheat = false;                                                                                                //是否作弊，调试用

    PluginTemplateGen gen = null;

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void doOperation(DealerDealAction action) {

        RoomInstance room = action.getRoomInstance();
        MahjongEngine engine = (MahjongEngine) room.getEngine();
        RoomSettingTemplateGen roomGen = room.getRstempateGen();
        ArrayList<IPlayer> players = room.getAllPlayer();

        Map<Integer, Integer[]> userCards = new HashMap<>();

/*		Integer[] dCards = {11, 15, 14, 14, 18, 36, 26, 19, 39, 21, 46, 25, 47 };
        Integer[] bCards = {21, 25, 21, 25, 25, 25, 27, 28, 22, 45, 46, 47, 47 };
		Integer[] cCards = {21, 15, 16, 15, 31, 31, 31, 29, 29, 45, 45, 45, 29 };
		Integer[] banker = {14, 14, 16, 16, 17, 17, 41, 41, 42, 43, 44, 41, 41 };*/

        //杠后
        Integer[] dCards = {11, 12, 13, 23, 24, 25, 35, 36, 37, 11, 27, 27, 27};
        Integer[] bCards = {26, 26, 23, 37, 38, 38, 39, 39, 11, 11, 16, 16, 17};
        Integer[] cCards = {14, 15, 15, 26, 31, 31, 31, 31, 29, 21, 22, 23, 12};
        Integer[] banker = {11, 12, 14, 16, 26, 19, 18, 18, 18, 18, 14, 14, 14};

        List<Integer[]> otherCards = new ArrayList<>();
        otherCards.add(bCards);
        otherCards.add(cCards);
        otherCards.add(dCards);
        int index = 0;

        ArrayList<MJCard> cardPool = null;
        //如果作弊获得作弊牌库
        if (cheat)
            cardPool = cheatMJCard(engine.getCardPool());


        for (IPlayer player : players) {
            MJPlayer p = (MJPlayer) player;
            if (p.getUid() == room.getBankerUid()) {
                userCards.put(p.getUid(), banker);
            } else {
                userCards.put(p.getUid(), otherCards.get(index));
                index++;
            }
            int count = roomGen.getInitHandCardCount();

            //如果不作弊，从这里获得所有牌的牌池
            if (!cheat) cardPool = engine.getCardPool();

            if (cardPool.size() < count) {
                return;
            }
            if (player == null)
                return;
            ArrayList cards = new ArrayList<MJCard>();
            for (int i = 0; i < count; i++) {
                MJCard card = cardPool.remove(0);
                card.setUid(player.getUid());
                cards.add(card);
            }
            player.getHandCards().addHandCards(cards);
        }

        //reCreateHandCards(room, userCards, room.getAllPlayer());

//        if(cheat){
//            putMoCard(44,cardPool);
//            putMoCard(44,cardPool);
//            putMoCard(44,cardPool);
//            putMoCard(44,cardPool);
//            putMoCard(44,cardPool);
//            putMoCard(44,cardPool);
//            putMoCard(44,cardPool);
//            putMoCard(44,cardPool);
//        }

        MJPlayer player = (MJPlayer) room.getPlayerById(room.getBankerUid());
        room.setFocusIndex(player.getIndex());
        ActionManager.moCheck(player);
        try {
            room.getEngine().getMediator().doAutoRunAction();
        } catch (ActionRuntimeException e) {
            e.printStackTrace();
        }

        this.createCanExecuteAction(action);

    }

    @Override
    public boolean doPayDetail(PayDetailed pd, RoomInstance room, Calculator calculator) {
        return false;
    }

    public static void reCreateHandCards(RoomInstance room, Map<Integer, Integer[]> userCards, ArrayList<IPlayer> players) {
        for (IPlayer player : players) {
            ArrayList<MJCard> handCards = player.getHandCards().getHandCards();
            ArrayList<MJCard> cardPool = room.getEngine().getCardPool();
            for (MJCard card : handCards) {
                cardPool.add(card);
            }
        }
        ArrayList<MJCard> cardPool = room.getEngine().getCardPool();
        for (IPlayer player : players) {
            ArrayList<MJCard> handCards = player.getHandCards().getHandCards();
            Integer[] newCards = userCards.get(player.getUid());
            int i = 0;
            for (int card : newCards) {
                Iterator<MJCard> cs = cardPool.iterator();
                while (cs.hasNext()) {
                    MJCard c = cs.next();
                    if (c.getCardNum() == card) {
                        handCards.set(i, c);
                        cs.remove();
                        i++;
                        break;
                    }
                }
            }

        }
    }

    @Override
    public boolean checkExecute(Object... objects) {
        return true;
    }

    @Override
    public void createCanExecuteAction(IEPlayerAction action) { //定缺
        RoomInstance room = action.getRoomInstance();
        //定缺未使用
        if ((int) room.getAttribute(RoomAttributeConstants.ROOM_QUEYIMEN) != 1) {
            return;
        }

        List<IOptPlugin> pluginList = OptPluginFactory.createPluginListByActionType(
                IEMajongAction.ROOM_MATCH_QUE, room.getRstempateGen().getTempId());

        if (pluginList.size() != 1) {
            return;
        }

        //取消玩家操作
        int step = room.getEngine().getMediator().getCurrentStep();
        List<IEPlayerAction> nextCandoAction = room.getEngine().getMediator().getCanExecuteActionByStep(step);
        if (nextCandoAction != null) {
            nextCandoAction.clear();
        }

        IPlayer[] players = room.getPlayerArr();
        //添加开始定缺操作
        for (IPlayer playerTemp : players) {
            IPluginCheckCanExecuteAction plugin = (IPluginCheckCanExecuteAction) pluginList.get(0);
            plugin.checkExecute(playerTemp, action);
        }
    }


    //-------------------测试部分用的代码-------------------
//    public static ArrayList<Integer> allcp = new ArrayList<>();
//
//    static {
//        //初始化所有牌11-19是万，21-29是条，31-39是饼，41-47是风
//
//        for (int i = 11; i <= 19; i++)
//            for (int c = 0; c < 4; c++)
//                allcp.add(i);
//
//        for (int i = 21; i <= 29; i++)
//            for (int c = 0; c < 4; c++)
//                allcp.add(i);
//
//        for (int i = 31; i <= 39; i++)
//            for (int c = 0; c < 4; c++)
//                allcp.add(i);
//
//        for (int i = 41; i <= 47; i++)
//            for (int c = 0; c < 4; c++)
//                allcp.add(i);
//    }

    /**
     * 测试用函数，发牌时对牌池进行一定规律的排序
     *
     * @return 返回排好序的牌池
     */
    private ArrayList<MJCard> cheatMJCard(ArrayList<MJCard> allCardPool) {
        //测试，按规划好四家的牌分配
//        Integer[] aCards = {11, 13, 14, 15, 16, 17, 18, 19, 18, 18, 18, 19, 19};
//        Integer[] bCards = {31, 14, 17, 22, 25, 12, 41, 42, 43, 41, 46, 46, 46};
//        Integer[] cCards = {46, 12, 31, 31, 25, 25, 25, 29, 29, 29, 32, 32, 32};
//        Integer[] dCards = {41, 41, 41, 45, 42, 42, 42, 45, 43, 43, 43, 44, 44};

        //七星点灯
//        Integer[] aCards = {11, 13, 14, 15, 16, 17, 18, 19, 18, 18, 18, 19, 19};
//        Integer[] bCards = {41, 42, 43, 45, 46, 47, 21, 24, 27, 31, 34, 37, 16};
//        Integer[] cCards = {12, 12, 31, 31, 25, 25, 25, 29, 29, 29, 32, 32, 32};
//        Integer[] dCards = {41, 42, 43, 45, 46, 47, 11, 14, 17, 21, 24, 27, 33};

        //碰后杠测试
//        Integer[] aCards = {21, 22, 29, 29, 27, 27, 28, 28, 28, 25, 26, 47, 42};
//        Integer[] bCards = {21, 21, 22, 22, 21, 22, 24, 24, 24, 25, 42, 44, 44};
//        Integer[] cCards = {25, 21, 41, 41, 46, 46, 45, 45, 47, 29, 27, 28, 24};
//        Integer[] dCards = {23, 29, 27, 41, 41, 46, 46, 45, 45, 47, 47, 25, 22};

//        抢杠胡
//        Integer[] aCards = {11, 13, 14, 15, 16, 17, 18, 19, 18, 18, 18, 19, 19};
//        Integer[] bCards = {11, 12, 17, 22, 25, 12, 41, 42, 43, 46, 46, 46, 46};
//        Integer[] cCards = {12, 12, 12, 31, 25, 25, 25, 29, 29, 29, 32, 32, 32};
//        Integer[] dCards = {41, 41, 41, 45, 42, 42, 42, 45, 43, 43, 43, 44, 44};

        //抢杠小胡
//        Integer[] aCards = {11, 13, 14, 15, 16, 17, 18, 19, 18, 18, 18, 19, 19};
//        Integer[] bCards = {11, 12, 17, 22, 25, 12, 41, 42, 43, 46, 46, 46, 46};
//        Integer[] cCards = {12, 12, 12, 31, 25, 25, 25, 29, 29, 29, 32, 32, 32};
//        Integer[] dCards = {41, 41, 41, 45, 42, 42, 42, 45, 43, 43, 43, 44, 44};

        //清一色包三家
//        Integer[] aCards = {11, 12, 13, 14, 15, 16, 17, 18, 29, 29, 32, 32, 32};
//        Integer[] bCards = {11, 12, 13, 14, 15, 16, 17, 19, 16, 15, 46, 46, 46};
//        Integer[] cCards = {11, 13, 14, 15, 16, 17, 18, 19, 18, 18, 18, 19, 19};
//        Integer[] dCards = {41, 41, 41, 45, 42, 42, 42, 45, 43, 43, 43, 44, 44};

        //普通全求人包三家
//        Integer[] aCards = {11, 12, 13, 14, 16, 16, 18, 18, 29, 29, 32, 32, 32};
//        Integer[] bCards = {11, 12, 13, 14, 15, 16, 17, 18, 19, 29, 32, 46, 46};
//        Integer[] cCards = {11, 13, 14, 15, 16, 17, 18, 19, 18, 18, 18, 19, 19};
//        Integer[] dCards = {41, 41, 41, 45, 42, 42, 42, 45, 43, 43, 43, 44, 44};

        //一炮多响 清一色包三家
//        Integer[] aCards = {21, 22, 29, 29, 27, 27, 28, 28, 28, 25, 26, 47, 42};
//        Integer[] bCards = {21, 21, 22, 22, 26, 27, 24, 24, 24, 25, 42, 44, 44};
//        Integer[] cCards = {25, 21, 41, 41, 46, 46, 45, 45, 47, 29, 27, 28, 24};
//        Integer[] dCards = {23, 29, 27, 41, 41, 46, 46, 45, 45, 47, 47, 25, 22};

        //清一色对对胡包三家
//        Integer[] aCards = {23, 29, 27, 41, 41, 46, 46, 45, 45, 47, 47, 25, 22};
//        Integer[] bCards = {21, 21, 22, 22, 25, 27, 24, 24, 24, 25, 27, 24, 44};
//        Integer[] cCards = {25, 21, 41, 41, 46, 46, 45, 45, 47, 29, 27, 28, 24};
//        Integer[] dCards = {21, 22, 29, 29, 27, 27, 28, 28, 28, 25, 25, 28, 42};

        //字一色、全求人 包三家测试
//        Integer[] aCards = {21, 22, 29, 29, 27, 27, 28, 28, 28, 25, 26, 47, 42};
//        Integer[] bCards = {21, 21, 22, 22, 21, 22, 24, 24, 24, 25, 42, 44, 44};
//        Integer[] cCards = {25, 21, 41, 41, 46, 46, 45, 45, 47, 29, 27, 28, 24};
//        Integer[] dCards = {43, 43, 43, 43, 41, 46, 46, 45, 45, 47, 47, 42, 42};

        //平胡、门清、自摸测试
//        Integer[] aCards = {12, 14, 26, 27, 27, 28, 28, 29, 36, 36, 37, 38, 39};
//        Integer[] bCards = {12, 14, 22, 23, 23, 24, 24, 25, 36, 36, 37, 38, 39};
//        Integer[] cCards = {12, 14, 41, 41, 46, 46, 46, 47, 47, 47, 45, 45, 45};
//        Integer[] dCards = {12, 14, 16, 17, 17, 18, 18, 19, 26, 26, 27, 28, 29};

        //暗杠夹胡,测试暗杠是否能胡门清
//        Integer[] aCards = {12, 14, 26, 28, 28, 28, 28, 29, 36, 36, 37, 38, 39};
//        Integer[] bCards = {12, 14, 22, 24, 24, 24, 24, 25, 36, 36, 37, 38, 39};
//        Integer[] cCards = {12, 14, 47, 46, 46, 46, 46, 47, 47, 47, 45, 45, 45};
//        Integer[] dCards = {12, 14, 16, 18, 18, 18, 18, 19, 26, 26, 27, 28, 29};

        //闭胡 门清自摸小胡 卡住问题
//        Integer[] aCards = {11, 12, 13, 22, 43, 24, 36, 37, 38, 39, 39, 43, 43};
//        Integer[] bCards = {11, 12, 13, 14, 37, 16, 31, 32, 33, 33, 33, 35, 36};
//        Integer[] cCards = {15, 16, 16, 17, 17, 18, 24, 24, 33, 34, 35, 36, 38};
//        Integer[] dCards = {12, 14, 16, 18, 18, 18, 18, 19, 26, 26, 27, 28, 29};

        //门前清自摸 本风分数测试
//        Integer[] aCards = {14, 21, 13, 24, 18, 19, 23, 16, 16, 16, 12, 21, 22};
//        Integer[] bCards = {14, 21, 13, 24, 18, 19, 23, 11, 11, 11, 12, 21, 22};
//        Integer[] cCards = {14, 21, 13, 24, 18, 19, 23, 16, 16, 16, 12, 21, 22};
//        Integer[] dCards = {14, 21, 13, 24, 18, 19, 23, 16, 16, 16, 12, 21, 22};

        //自摸清一色包三家 分数测试
        Integer[] aCards = {34, 36, 37, 34, 38, 38, 37, 32, 31, 31, 32, 21, 22};
        Integer[] bCards = {34, 36, 37, 34, 38, 38, 37, 32, 31, 31, 32, 21, 22};
        Integer[] cCards = {14, 16, 17, 14, 18, 18, 17, 12, 11, 11, 12, 21, 22};
        Integer[] dCards = {14, 16, 17, 14, 18, 18, 17, 12, 11, 11, 12, 21, 22};

        ArrayList<MJCard> cheats = new ArrayList<MJCard>();
        for (Integer c : aCards)
            putMJCard(cheats, c, allCardPool);

        for (Integer c : bCards)
            putMJCard(cheats, c, allCardPool);

        for (Integer c : cCards)
            putMJCard(cheats, c, allCardPool);

        for (Integer c : dCards)
            putMJCard(cheats, c, allCardPool);


        //牌池中最后一张牌，杠后获得的第一张牌
//        MJCard mjc = new MJCard();
//        mjc.setCardNum(44);
//        mjc.setStatus(0);
//        mjc.setUid(0);
//        allCardPool.add(allCardPool.size() - 1, mjc);
//
        return cheats;
    }

    /**
     * 放牌到集合中，并总从牌库中移除该牌
     *
     * @param amjc   要增加的作弊的牌
     * @param cNum   当前牌的Num
     * @param allmjc 所有牌的牌池
     */
    private void putMJCard(ArrayList<MJCard> amjc, Integer cNum, ArrayList<MJCard> allmjc) {
        MJCard mjc = new MJCard();
        mjc.setCardNum(cNum);
        mjc.setStatus(0);
        mjc.setUid(0);
        amjc.add(mjc);

        //从总牌库中移除
        for (MJCard mj : allmjc) {
            if (mj.getCardNum() == cNum) {
                allmjc.remove(mj);
                break;
            }
        }
    }

    /**
     * 向牌池中放入作弊牌
     *
     * @param cNum   作弊牌值
     * @param allmjc 所有的牌池
     */
    private void putMoCard(Integer cNum, ArrayList<MJCard> allmjc) {
        MJCard mjc = new MJCard();
        mjc.setCardNum(cNum);
        mjc.setStatus(0);
        mjc.setUid(0);

        //从总牌库中移除一张该牌，再将其加入牌池最后一张的位置，用来杠牌后抓
        for (MJCard mj : allmjc) {
            if (mj.getCardNum() == cNum) {
                allmjc.remove(mj);
                allmjc.add(0, mjc);
                break;
            }
        }
    }

    /**
     * 将玩家的顺序按index调整。
     *
     * @param players
     * @return
     */
    private ArrayList<IPlayer> cheatPlayersSort(ArrayList<IPlayer> players) {
        Collections.sort(players, new Comparator<IPlayer>() {
            @Override
            public int compare(IPlayer o1, IPlayer o2) {
                return o1.getIndex() - o2.getIndex();
            }
        });
        return players;
    }
}
