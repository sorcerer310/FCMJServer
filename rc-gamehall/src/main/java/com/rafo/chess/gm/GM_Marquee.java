package com.rafo.chess.gm;

import com.rafo.hall.core.HallExtension;
import com.rafo.hall.utils.CmdsUtils;
import com.rafo.hall.vo.Marquee;
import com.rafo.hall.vo.WCMarqueeSYN;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.ExtensionLogLevel;
import com.smartfoxserver.v2.extensions.SFSExtension;

import java.util.List;

/**
 * Created by YL.
 * Date: 16-10-17
 */
public class GM_Marquee extends GMCommand
{

    @Override
    public boolean exec(SFSObject params, SFSExtension sFSExtension)
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
        return true;
    }
}
