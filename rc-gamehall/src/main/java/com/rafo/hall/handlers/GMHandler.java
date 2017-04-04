package com.rafo.hall.handlers;

import java.util.List;

import com.rafo.chess.gm.GMInteface;
import com.rafo.hall.core.HallExtension;
import com.rafo.hall.utils.CmdsUtils;
import com.rafo.hall.vo.Marquee;
import com.rafo.hall.vo.WCMarqueeSYN;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.extensions.ExtensionLogLevel;
import com.smartfoxserver.v2.extensions.SFSExtension;

/**
 * 所有接受GM的处理
 * 
 * @author yangtao
 * @dateTime 2016年10月9日 下午3:59:09
 * @version 1.0
 */
public class GMHandler extends BaseClientRequestHandler {

	@Override
	public void handleClientRequest(User user, ISFSObject data) {
		String action = data.getUtfString("action");
		SFSObject params = (SFSObject) data.getSFSObject("params");
		trace(ExtensionLogLevel.WARN, "params " + params.toJson());
		// 之后新加action,就要创建一个类,类的名字是"GM_"+action,必须实现GMCommand
		int state = -1;
		if(action.equals("Marquee"))
		{
			state=MarQuee(params,getParentExtension());
		}
		else state = GMInteface.execCommand(action, params, getParentExtension());
		SFSObject resp = new SFSObject();
		resp.putInt("result", state);
		getParentExtension().send(CmdsUtils.CMD_GM, resp, user);
	}
	
	public int MarQuee(SFSObject params, SFSExtension sFSExtension)
	{
		sFSExtension.trace(ExtensionLogLevel.WARN, "GMCommand   params " + params.toJson());
        HallExtension hallExt = (HallExtension) sFSExtension;
        String content = params.getUtfString("content");
        int roll_times = params.getInt("count");
        String color = params.getUtfString("color");
        long endTime = params.getLong("endTime");//结束时间
        Marquee marquee = new Marquee(content, color, endTime, roll_times);
        WCMarqueeSYN proto = new WCMarqueeSYN();
        proto.setContent(content);
        proto.setColor(color);
        proto.setRollTimes(roll_times);
        //保存
        hallExt.getMarqueeService().saveMarquee(marquee);
        //大厅内在线玩家，牌局中玩家在房间内发送
        List<User> userList = (List<User>) hallExt.getParentZone().getUserList();
        hallExt.send(CmdsUtils.CMD_MARQUEESYN, proto.toObject(), userList);
        return 1;
	}
}
