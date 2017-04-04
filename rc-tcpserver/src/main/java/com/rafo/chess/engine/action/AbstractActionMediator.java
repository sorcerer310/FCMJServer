package com.rafo.chess.engine.action;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.rafo.chess.engine.majiang.action.PassAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rafo.chess.engine.EngineLogInfoConstants;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.room.RoomInstance;

/***
 * 玩家行为管理的中介
 * 
 * @author Administrator
 */
public abstract class AbstractActionMediator implements IEActionExecutor {
	protected Logger logger = LoggerFactory.getLogger("play");
	// 行为对象的列表
	public ArrayList<IEPlayerAction> doneActionList = new ArrayList<IEPlayerAction>();

	protected HashMap<Integer, Class<? extends IEPlayerAction>> actionMapper = new HashMap<Integer, Class<? extends IEPlayerAction>>();

	protected RoomInstance roomIns;

	AtomicInteger step = new AtomicInteger(0);

	/** 玩家可操作队列 */
	protected HashMap<Integer, ArrayList<IEPlayerAction>> canExecuteActionMap = new HashMap<Integer, ArrayList<IEPlayerAction>>();

	public int getCurrentStep() {
		return step.get();
	}

	public int getNextStep() {
		int stepInteger = step.getAndAdd(1);
		logger.debug("step增加:" + stepInteger);
		return stepInteger;
	}

	public void addCanExecuteAction(IEPlayerAction action) {
		ArrayList<IEPlayerAction> list = canExecuteActionMap.get(step.get());
		if (list == null) {
			list = new ArrayList<IEPlayerAction>();
			canExecuteActionMap.put(step.get(), list);
		}
		
		MJPlayer player = (MJPlayer)action.getRoomInstance().getPlayerById(action.getPlayerUid());
		logger.debug("添加一个可执行操作1:" + step.get() + ", action:" + EngineLogInfoConstants.actionName.get(action.getActionType()) + ", player:" + player.getNickName() + 
				", subaction:" + action.getSubType());
		
		list.add(action);
	}

	public void addCanExecuteActionByStep(int step, IEPlayerAction action) {
		ArrayList<IEPlayerAction> list = canExecuteActionMap.get(step);
		if (list == null) {
			list = new ArrayList<IEPlayerAction>();
			canExecuteActionMap.put(step, list);
		}
		
		MJPlayer player = (MJPlayer)action.getRoomInstance().getPlayerById(action.getPlayerUid());
		logger.debug("添加一个可执行操作2:" + step + ", action:" + EngineLogInfoConstants.actionName.get(action.getActionType()) + ", player:" + player.getNickName() + 
				", subaction:" + action.getSubType());
		
		list.add(action);
	}

	public ArrayList<IEPlayerAction> getCanExecuteActionByStep(int step) {
		ArrayList<IEPlayerAction> list = canExecuteActionMap.get(step);
		return list;
	}

	public AbstractActionMediator(RoomInstance roomIns) {
		this.roomIns = roomIns;
		registerAction();
	}

	public ArrayList<IEPlayerAction> getDoneActionList() {
		return doneActionList;
	}

	public IEPlayerAction getDoneActionByStep(int step) {
		if (step < 0)
			return null;
		if (doneActionList.size() > step)
			return doneActionList.get(step);

		return null;
	}

	public HashMap<Integer, Class<? extends IEPlayerAction>> getActionMapper() {
		return actionMapper;
	}

	@Override
	public RoomInstance getRoomInstance() {
		return roomIns;
	}

	/****
	 * 不同引擎注册不同的行为
	 */
	public abstract void registerAction();

	public static String printHandsCard(IPlayer player) {
		List<MJCard> hands = player.getHandCards().getHandCards();

		List<MJCard> tempHandCards = new ArrayList<>();
		tempHandCards.addAll(hands);

		Collections.sort(tempHandCards, new Comparator<MJCard>() {
			@Override
			public int compare(MJCard o1, MJCard o2) {
				return o1.getCardNum() - o2.getCardNum();
			}
		});

		StringBuffer sb = new StringBuffer();
		sb.append("handcards[");
		for (MJCard c : tempHandCards) {
			sb.append(c.getCardNum() + ",");
		}
		sb.append("]");
		return sb.toString();
	}

	/***
	 * 过牌操作
	 *
	 * @throws IllegalAccessException
	 * @throws Exception
	 */
	public boolean executePass(int card, int playerUid) throws ActionRuntimeException {
		ArrayList<IEPlayerAction> list = canExecuteActionMap.get(step.get());
		for (IEPlayerAction acttemp : list) {

			if(!acttemp.isCanPass()){
				throw new ActionRuntimeException("can not pass", acttemp.getSubType(), playerUid);
			}

			if (acttemp.getPlayerUid() != playerUid)
				continue;

			if (acttemp.getStatus() == 0) {
				PassAction guo = new PassAction(this.roomIns);
				guo.setPlayerUid(playerUid);
				guo.setFromUid(acttemp.getFromUid());
				guo.setStatus(1);
				guo.setCard(acttemp.getCard());
				guo.setStep(step.getAndAdd(1));
				doneActionList.add(guo);
				guo.doAction();
				return true;
			}
		}

		return false;
	}

	/***
	 * 生成行为对象,执行行为逻辑,添加到行为队列
	 * 
	 * @throws IllegalAccessException
	 * @throws Exception
	 */
	public void executeAction(int actionType, int card, int playerUid, int subType, String toBeCards)
			throws ActionRuntimeException {
		boolean isDoneAction = false;
		//判断各种动作类型
		if (actionType == IEMajongAction.PLAYER_ACTION_TYPE_CARD_GUO) { // 过牌不单独处理
			executePass(card, playerUid);
			isDoneAction = true;
		} else {
			//获得所有可执行动作
			ArrayList<IEPlayerAction> list = canExecuteActionMap.get(step.get());
			logger.debug("请求可执行列表:" + step + ", size:" + list.size());
			//挨个执行
			for (IEPlayerAction action : list) {
				//动作如果自检未通过，继续执行下一个动作
				if (!action.checkMySelf(actionType, card, playerUid, subType, toBeCards))
					continue;
				//设置动作执行完标识为true
				isDoneAction = true;
				//获得玩家在房间中的位置
				int index = roomIns.getPlayerById(action.getPlayerUid()).getIndex();
				//设置执行动作玩家的位置为焦点
				roomIns.setFocusIndex(index);
				//设置步骤加1，状态为1，1为动作已执行
				action.setStep(step.getAndAdd(1));
				action.setStatus(1);
				//将该动作加入到已执行完队列中
				doneActionList.add(action);
				//执行动作的doAction()方法，做该动作的所有操作
				action.doAction();
				//获得玩家信息用于写日志
				IPlayer player = roomIns.getPlayerById(action.getPlayerUid());
				logger.debug("room:"+roomIns.getRoomId()+";round:"+roomIns.getCurrRounds()+";"+"[step=" + step.get() + ";atype="
						+ EngineLogInfoConstants.actionName.get(action.getActionType()) + ";uid="
						+ action.getPlayerUid() + ";from=" + action.getFromUid() + ";stype="
						+ action.getSubType() + ";card=" + action.getCard() +"," + printHandsCard(player));

				break;
			}
		}
		//如果完成动作标识为false，抛出异常
		if (!isDoneAction) {
			throw new ActionRuntimeException(
					"action is invalid ...[ actionType=" + actionType + ",playerUid=" + playerUid + "]", actionType,
					playerUid);
		}
		//做可以自动运行的动作
		this.doAutoRunAction();
	}

	public void doAutoRunAction() throws ActionRuntimeException {
		// 如果有后台执行的操作，自动执行
		ArrayList<IEPlayerAction> list2 = canExecuteActionMap.get(step.get());
		if (list2 != null) {
			for (IEPlayerAction action2 : list2) {
				if (action2.isAutoRun() && action2.getStatus() == 0) {
					executeAction(action2.getActionType(), action2.getCard(), action2.getPlayerUid(),
							action2.getSubType(), action2.getToBeCards());
				}
			}
		}
	}

	@Override
	public void doAction() {
	}
}
