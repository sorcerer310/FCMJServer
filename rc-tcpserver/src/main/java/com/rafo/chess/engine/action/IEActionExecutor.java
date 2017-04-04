package com.rafo.chess.engine.action;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.room.RoomInstance;

/***
 * 行为处理逻辑接口
 * @author Administrator
 *
 */
public interface IEActionExecutor {
	public int getActionType();
	public RoomInstance getRoomInstance();
	/***
	 * 执行行为逻辑
	 */
	public void doAction() throws ActionRuntimeException;
}
