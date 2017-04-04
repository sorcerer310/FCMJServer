package com.rafo.chess.engine.majiang.action;

import java.util.*;

import com.rafo.chess.engine.plugin.impl.yxmj.YXHuPingHuPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.gameModel.factory.GameModelFactory;
import com.rafo.chess.engine.gameModel.factory.GameModelFactory.CardType;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;
import com.rafo.chess.engine.plugin.OptPluginFactory;
import com.rafo.chess.engine.plugin.impl.HuPlugin;
import com.rafo.chess.engine.plugin.impl.TingPlugin;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.engine.room.RoomManager;
import com.rafo.chess.resources.DataContainer;
import com.rafo.chess.template.impl.PluginTemplateGen;
import com.rafo.chess.template.impl.RoomSettingTemplateGen;

@SuppressWarnings({ "unchecked", "unused", "rawtypes" })
public class ActionManager {
	private static Logger logger = LoggerFactory.getLogger(ActionManager.class);

	/**
	 * 检测是否和
	 * @param pTemp		玩家对象
	 * @param act		动作
	 * @return
	 */
	public static boolean huCheck(IPlayer pTemp, IEPlayerAction act) {

		if(!isValidCard(pTemp, act)){
			return false;
		}
		//用户对象
		MJPlayer player = (MJPlayer) pTemp;
		//当前用户的手牌
		ArrayList<MJCard> handlistTemp = new ArrayList<MJCard>();
		handlistTemp.addAll(pTemp.getHandCards().getHandCards());

		//如果触发动作的牌不是自摸，创建一个麻将对象加入到手牌中
		MJCard card = null;
		if (act.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN){
			card = (MJCard) GameModelFactory.createCard(act.getCard(), CardType.CARD_MAJIANG.getFlag());
			handlistTemp.add(card);
			card.setUid(act.getPlayerUid());
		}
		//清空房间和牌插件容器
		act.getRoomInstance().getHuPlugins().clear();

		//获得所有和的插件列表
		List<IOptPlugin> pluginList = getHuPlugin(act.getRoomInstance());
		for (IOptPlugin pluginTemp : pluginList) {
			//判断如果插件能执行检查代表能和的插件
			if ((pluginTemp instanceof IPluginCheckCanExecuteAction)) {
				HuPlugin plugin = (HuPlugin) pluginTemp;

				//如果玩家叫嘴、并且扣听了做以下处理，永修麻将没有用到这里。
//				if(player.isJiaozui()>0 && (player.isKouTing() || player.isTing())){
//					if(plugin.getGen().getTempId() != player.isJiaozui()){
//						continue;
//					}
//				}

				//插件设置和牌检查操作为true，不知道有什么用，似乎没有地方用到这个属性
				plugin.setHuCheckOperation(true);


				//如果当前插件检查和牌为true，插件增加和牌动作，返回true这里需要修改适应多重和牌插件
//				if (plugin.checkExecute(player,handlistTemp,pTemp.getHandCards().getOpencards())) {
//					plugin.addCanExecuteHuAction(act, player);
//					return true;
//				}

				//如果当前插件检查和牌为TRUE，将插件增加到和牌容器中，待结算处理
				if(plugin.checkExecute(player,handlistTemp,pTemp.getHandCards().getOpencards())){
					act.getRoomInstance().getHuPlugins().add(plugin);

					HuAction huAct = new HuAction(act.getRoomInstance());
					huAct.setCard(act.getCard());
					huAct.setPlayerUid(player.getUid());
					huAct.setFromUid(act.getPlayerUid());
					huAct.setSubType(plugin.getGen().getSubType());
					huAct.setCanDoType(plugin.getGen().getCanDoType());
					huAct.setPluginId(plugin.getGen().getTempId());

					act.getRoomInstance().addCanExecuteAction(huAct);
					return true;
				}
			}
		}
		//如果至少有一个牌型可以和，返回true
//		ArrayList<HuPlugin> hulist = act.getRoomInstance().getHuPlugins();
//		if(hulist.size()>0){
//			//如果可以和的插件大于1，去掉其中的平和插件
//			if(hulist.size()>1) {
//				Iterator<HuPlugin> it_hp = hulist.iterator();
//				while (it_hp.hasNext()) {
//					HuPlugin hp = it_hp.next();
//					if (hp instanceof YXHuPingHuPlugin) {
//						hulist.remove(hp);
//						break;
//					}
//				}
//			}
//			return true;
//		}
		return false;
	}

	// 是否能吃
	public static void chiCheck(IPlayer pTemp, DaAction act) {

		ArrayList<IOptPlugin> pluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.PLAYER_ACTION_TYPE_CARD_CHI, act.getRoomInstance().getRstempateGen().getTempId());
		for (IOptPlugin pluginTemp : pluginList) {
			if ((pluginTemp instanceof IPluginCheckCanExecuteAction)) {
				IPluginCheckCanExecuteAction plugin = (IPluginCheckCanExecuteAction) pluginTemp;
				if (plugin.checkExecute(pTemp, act)) {

				}
			}
		}
	}

	/**
	 * 判断是否为碰
	 * @param pTemp
	 * @param act
	 */
	public static void pengCheck(IPlayer pTemp, IEPlayerAction act) {
		//如果当前动作类型不为打牌，返回
		if (act.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT)
			return;
		//判断当前是否缺牌？？？
		if(!isValidCard(pTemp, act)){
			return;
		}

		MJPlayer player = (MJPlayer) pTemp;

		ArrayList<IOptPlugin> pluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.PLAYER_ACTION_TYPE_CARD_PENG, act.getRoomInstance().getRstempateGen().getTempId());

		for (IOptPlugin pluginTemp : pluginList) {
			if ((pluginTemp instanceof IPluginCheckCanExecuteAction)) {
				IPluginCheckCanExecuteAction plugin = (IPluginCheckCanExecuteAction) pluginTemp;
				if (plugin.checkExecute(pTemp, act)) {

				}
			}
		}

	}

	// 是否是杠
	public static void gangCheck(IPlayer pTemp, IEPlayerAction act) {
		if(!isValidCard(pTemp, act)){
			return;
		}

		ArrayList<IOptPlugin> pluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG, act.getRoomInstance().getRstempateGen().getTempId());
		for (IOptPlugin pluginTemp : pluginList) {
			if ((pluginTemp instanceof IPluginCheckCanExecuteAction)) {
				IPluginCheckCanExecuteAction plugin = (IPluginCheckCanExecuteAction) pluginTemp;
				if (!plugin.checkExecute(pTemp, act))
					continue;
			}
		}
	}

	public static void tingCheck(IPlayer player, IEPlayerAction act) {
		MJPlayer mjP = (MJPlayer) player;
		if (mjP.isKouTing() || mjP.isTing())
			return;

		LinkedList<BaseMajongPlayerAction> canDoActions = act.getRoomInstance().getCanExecuteActionList();
		for(BaseMajongPlayerAction actTemp : canDoActions){
			if(actTemp.getActionType()==IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING){
				return;
			}
		}

		TingPlugin tingplugin = null;
		ArrayList<IOptPlugin> pluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING, act.getRoomInstance().getRstempateGen().getTempId());
		for (IOptPlugin pluginTemp : pluginList) {
			if ((pluginTemp instanceof IPluginCheckCanExecuteAction)) {
				tingplugin = (TingPlugin) pluginTemp;
				if (!tingplugin.checkExecute( mjP)){
					tingplugin = null;
					continue;
				}else{
					break;
				}
			}
		}
		if(tingplugin == null){
			return;
		}

		HashSet<Integer> cardSet = new HashSet<Integer>();
		ArrayList<MJCard> list = player.getHandCards().getHandCards();
		ArrayList<MJCard> listTemp = new ArrayList<MJCard>();

		RoomSettingTemplateGen gen = act.getRoomInstance().getRstempateGen();
		String cardNumPool = gen.getCardNumPool();
		String[] cardNumArr = cardNumPool.split(",");

		Set<Integer> comparedCards = new HashSet<>();
		Map<Integer,Integer> cardCount = getCardsCount(player.getHandCards().getHandCards(), player.getHandCards().getOpencards());
		Map<Integer, Integer> cardHuType = new HashMap<>();

		List<IOptPlugin> huPluginList = getHuPlugin(act.getRoomInstance());
		for (MJCard card : list) {
			Integer count = cardCount.get(card.getCardNum());
			if(count != null && count == 4){ //如果手里已经有4张牌了，不能再加了
				continue;
			}
			listTemp.clear();
			listTemp.addAll(list);
			listTemp.remove(card);// 移除一张牌

			if (comparedCards.contains(card.getCardNum()))
				continue;

			comparedCards.add(card.getCardNum());
			// 所有牌替换测试是否胡牌
			for (String cardNum : cardNumArr) {
				ArrayList<MJCard> listTemp2 = new ArrayList<MJCard>();
				listTemp2.addAll(listTemp);
				int num = Integer.parseInt(cardNum);
				if (num == card.getCardNum()) {
					continue;
				}
				MJCard c = (MJCard) GameModelFactory.createCard(num, CardType.CARD_MAJIANG.getFlag());
				listTemp2.add(c);

				for (IOptPlugin pluginTemp : huPluginList) {
					HuPlugin plugin = (HuPlugin) pluginTemp;
					plugin.setHuCheckOperation(false);
					if (plugin.checkExecute(mjP,listTemp2, mjP.getHandCards().getOpencards())) {
						cardSet.add(card.getCardNum());
						cardHuType.put(card.getCardNum(), plugin.getGen().getTempId());
						break;
					}
				}
			}
		}
		if (cardSet.size() > 0) {
			StringBuffer sb = new StringBuffer();
			int index = 0;
			for (Integer c : cardSet) {
				index++;
				sb.append(c);
				if (index < cardSet.size())
					sb.append(",");
			}
			TingAction ting = new TingAction(act.getRoomInstance());
			ting.setPlayerUid(player.getUid());
			ting.setFromUid(player.getUid());
			ting.setToBeCards(sb.toString());
			ting.setSubType(tingplugin.getGen().getSubType());
			ting.setCanDoType(tingplugin.getGen().getCanDoType());
			ting.setCardHuType(cardHuType);
			act.getRoomInstance().addCanExecuteAction(ting);
		}
	}

	public static void moCheck(MJPlayer player) {
		RoomInstance roomIns = RoomManager.getRoomInstnaceByRoomid(player.getRoomId());
		ArrayList<IOptPlugin> pluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN, roomIns.getRstempateGen().getTempId());
		for (IOptPlugin pluginTemp : pluginList) {
			if ((pluginTemp instanceof IPluginCheckCanExecuteAction)) {
				((IPluginCheckCanExecuteAction) pluginTemp).checkExecute(player);
			}
		}
	}

	public static void daCheck(MJPlayer player) {
		RoomInstance roomIns = RoomManager.getRoomInstnaceByRoomid(player.getRoomId());
		ArrayList<IOptPlugin> pluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT, roomIns.getRstempateGen().getTempId());
		for (IOptPlugin pluginTemp : pluginList) {
			if ((pluginTemp instanceof IPluginCheckCanExecuteAction)) {
				((IPluginCheckCanExecuteAction) pluginTemp).checkExecute(player);
			}
		}
	}

	/***
	 * 检测叫嘴，将最高胡牌牌行记录到player身上
	 *
	 * @param player
	 * @return
	 */
	public static boolean jiaozuiCheck(MJPlayer player) {
		return jiaozuiCheck(player, null, null);
	}

	public static boolean jiaozuiCheck(MJPlayer player, List<MJCard> cards, ArrayList<CardGroup> openCards){
		RoomInstance roomIns = RoomManager.getRoomInstnaceByRoomid(player.getRoomId());

		ArrayList<MJCard> listTemp = new ArrayList<MJCard>();
		if(cards == null ) {
			ArrayList<MJCard> list = player.getHandCards().getHandCards();
			listTemp.addAll(list);
		}else{
			for(MJCard c : cards){
				listTemp.add(c);
			}
		}

		RoomSettingTemplateGen gen = RoomManager.getRoomInstnaceByRoomid(player.getRoomId()).getRstempateGen();
		String cardNumPool = gen.getCardNumPool();
		String[] cardNumArr = cardNumPool.split(",");

		if(openCards == null){
			openCards = player.getHandCards().getOpencards();
		}

		RoomInstance room = RoomManager.getRoomInstnaceByRoomid(player.getRoomId());
		List<IOptPlugin> pluginList = getHuPlugin(room);

		int lastJiaoZui = player.isJiaozui();
		boolean withTing = ((player.isKouTing() || player.isTing()) && lastJiaoZui > 0)? true : false;

		//重置叫嘴状态
		player.setJiaozui(-1);
		// 所有牌替换测试是否胡牌
		for (String cardNum : cardNumArr) {
			int num = Integer.parseInt(cardNum);
			MJCard c = (MJCard) GameModelFactory.createCard(num, CardType.CARD_MAJIANG.getFlag());
			ArrayList<MJCard> handListTemp = new ArrayList<MJCard>();
			handListTemp.addAll(listTemp);
			handListTemp.add(c);

			for (IOptPlugin pluginTemp : pluginList) {
				if (!(pluginTemp instanceof IPluginCheckCanExecuteAction))
					continue;

				//如果已经听牌或者扣听，只比较相同的胡牌类型
				HuPlugin plugin = (HuPlugin) pluginTemp;
				if(withTing && plugin.getGen().getTempId() != lastJiaoZui){
					continue;
				}

				plugin.setHuCheckOperation(false);
				if (plugin.checkExecute(player,handListTemp, openCards)) {
					int rate1 = Integer.parseInt(plugin.getGen().getEffectStr().split(",")[1]);
					PluginTemplateGen playGen = (PluginTemplateGen) DataContainer.getInstance()
							.getDataByNameAndId("pluginTemplateGen",player.isJiaozui());

					if(withTing) {
						player.setJiaozui(plugin.getGen().getTempId());
						break;
					}

					if (playGen == null) {
						player.setJiaozui(plugin.getGen().getTempId());
						continue;
					}

					int rate2 = Integer.parseInt(playGen.getEffectStr().split(",")[1]);
					if (rate1 > rate2) {
						player.setJiaozui(plugin.getGen().getTempId());
					}
				}
			}

			if (player.isJiaozui() < 0) {
				player.setJiaozui(0);
			}

			if(withTing && player.isJiaozui() == lastJiaoZui){
				break;
			}
		}
		return player.isJiaozui() > 0;
	}

	public static boolean tingWithoutGangCheck(IPlayer player, IEPlayerAction act, CardGroup gangCard){

		TingPlugin tingplugin = null;
		ArrayList<IOptPlugin> pluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING, act.getRoomInstance().getRstempateGen().getTempId());

		for (IOptPlugin pluginTemp : pluginList) {
			if ((pluginTemp instanceof IPluginCheckCanExecuteAction)) {
				tingplugin = (TingPlugin) pluginTemp;
				if (tingplugin.checkExecute(player)) {
					break;
				}else{
					tingplugin = null;
				}
			}
		}

		ArrayList<MJCard> listTemp = new ArrayList<MJCard>();
		listTemp.addAll(player.getHandCards().getHandCards());

		for(MJCard card : gangCard.getCardsList()){
			Iterator<MJCard> handCards = listTemp.iterator();
			while (handCards.hasNext()){
				MJCard handCard = handCards.next();
				if(card.getCardNum() == handCard.getCardNum()){
					handCards.remove();
					break;
				}
			}
		}

		ArrayList<CardGroup> mockOpenCards = new ArrayList<>();
		mockOpenCards.addAll(player.getHandCards().getOpencards());
		mockOpenCards.add(gangCard);

		if(jiaozuiCheck((MJPlayer) player, listTemp, mockOpenCards)){
			if(tingplugin != null) {
				TingAction ting = new TingAction(act.getRoomInstance());
				ting.setPlayerUid(player.getUid());
				ting.setFromUid(player.getUid());
				ting.setSubType(tingplugin.getGen().getSubType());
				ting.setCanDoType(tingplugin.getGen().getCanDoType());
				act.getRoomInstance().addCanExecuteAction(ting);
				return true;
			}
		}
		return false;
	}

	private static Map<Integer,Integer> getCardsCount(List<MJCard> handCards, List<CardGroup> openCards){
		Map<Integer,Integer> cardCount = new HashMap<>();
		for(MJCard card : handCards){
			int c = card.getCardNum();
			Integer count = cardCount.get(c);
			if(count == null){
				cardCount.put(c, 1);
			}else{
				cardCount.put(c, count+1);
			}
		}

		for(CardGroup cg : openCards){
			for(MJCard card : cg.getCardsList()){
				int c = card.getCardNum();
				Integer count = cardCount.get(c);
				if(count == null){
					cardCount.put(c, 1);
				}else{
					cardCount.put(c, count+1);
				}
			}
		}

		return cardCount;
	}

	/**
	 * 获得当前房间的和插件
	 * @param room	房间对象
	 * @return
	 */
	private static ArrayList<IOptPlugin> getHuPlugin(RoomInstance room){
		//根据房间对象模板id获得所有的和插件
		ArrayList<IOptPlugin> allHuPluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU, room.getRstempateGen().getTempId());
		//将所有实现了IPluginCheckCanExecuteAction接口的插件挑出来
		//所有能和的插件都实现了IPluginCheckCanExecuteAction接口
		ArrayList<IOptPlugin> huPluginList = new ArrayList<>();
		for(IOptPlugin plugin : allHuPluginList){
			if ((plugin instanceof IPluginCheckCanExecuteAction)) {
				huPluginList.add(plugin);
			}
		}
		//按照和的点数排序
		Collections.sort(huPluginList, new Comparator<IOptPlugin>() {
			@Override
			public int compare(IOptPlugin o1, IOptPlugin o2) {
				int rate1 = Integer.parseInt(o1.getGen().getEffectStr().split(",")[1]);
				int rate2 = Integer.parseInt(o2.getGen().getEffectStr().split(",")[1]);
				if(rate1 == rate2){
					return o2.getGen().getTempId() - o1.getGen().getTempId();
				}
				return rate2 - rate1;
			}
		});

		return huPluginList;
	}


	/**
	 * 判断当前牌是否是缺牌
	 * @param pTemp
	 * @param act
	 * @return
	 */
	public static boolean isValidCard(IPlayer pTemp, IEPlayerAction act){
		if((int)act.getRoomInstance().getAttribute(RoomAttributeConstants.ROOM_QUEYIMEN) != 1) {
			return true;
		}

		Map<Integer,Integer> map = (HashMap<Integer, Integer>) act.getRoomInstance().getAttribute(RoomAttributeConstants.YN_GAME_QUE);
		Integer card = map.get(pTemp.getUid());

		if(card != null && card/10 == act.getCard()/10)
			return false;
		return true;
	}


	/**
	 * 设置定缺牌的状态，返回是否包含有缺的牌
	 * @param playerId
	 * @param room
	 * @return
	 */
	public static boolean checkQueCardStatus(int playerId, RoomInstance room){
		if((int)room.getAttribute(RoomAttributeConstants.ROOM_QUEYIMEN) != 1) {
			return false;
		}
		MJPlayer player = (MJPlayer) room.getPlayerById(playerId);
		Map<Integer, Integer> map = (HashMap<Integer, Integer>) room.getAttribute(RoomAttributeConstants.YN_GAME_QUE);
		if(!map.containsKey(playerId)){
			return false;
		}

		int card = map.get(playerId);
		ArrayList<MJCard> list = player.getHandCards().getHandCards();

		boolean hasQueCard = false;
		for (MJCard c : list) {
			if (c.getCardNum() / 10 == card/10) {
				hasQueCard = true;
				break;
			}
		}

		for (MJCard c : list) {
			if (c.getCardNum() / 10 == card/10) {
				continue;
			}
			if (hasQueCard) {
				c.setStatus(1);
			}else{
				c.setStatus(0);
			}
		}

		return hasQueCard;
	}
}
