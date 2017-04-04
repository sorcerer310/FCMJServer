package com.rafo.chess.gm;

import com.rafo.hall.core.HallExtension;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.hall.utils.CmdsUtils;
import com.rafo.hall.utils.TimeUtils;
import com.rafo.hall.vo.RESEmailDataPROTO;
import com.rafo.hall.vo.WCHaveNewEmailSynRES;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.ExtensionLogLevel;
import com.smartfoxserver.v2.extensions.SFSExtension;

import java.util.Collection;
import java.util.Map;

/**
 * Created by YL.
 * Date: 16-10-13
 */
public class GM_GameEmail extends GMCommand
{

    @Override
    public boolean exec(SFSObject params, SFSExtension sFSExtension)
    {
        sFSExtension.trace(ExtensionLogLevel.WARN, "GMCommand   params " + params.toJson());
        HallExtension hallExt = (HallExtension) sFSExtension;
        String content = params.getUtfString("content");
        int type = params.getInt("type");
        String createTime = TimeUtils.millisecondToTime(System.currentTimeMillis());
        RESEmailDataPROTO proto = new RESEmailDataPROTO();
        proto.setEmailDate(createTime);
        proto.setEmailContent(content);
        //单个,多个玩家
        if (type == 1)
        {
            String playerIds = params.getUtfString("playerid");
            String[] playerIdArr = playerIds.split(",");
            for (String uid : playerIdArr)
            {
                Map<String, String> uInfo = RedisManager.getInstance().hMGetAll("uid." + uid);
                if (uInfo != null && uInfo.size() != 0)
                {
                    User user = hallExt.getApi().getUserByName(uid);
                    hallExt.getEmailService().addEmailToPlayer(Integer.parseInt(uid), content);
                    if (user != null)
                    {
                        WCHaveNewEmailSynRES res = new WCHaveNewEmailSynRES();
                        res.setNewNumber(1);//有新邮件,在线推送
                        hallExt.send(CmdsUtils.CMD_NEWEMAILSYN, res.toObject(), user);
                    }
                }
            }
        } else if (type == 0)//暂时不做全局玩家的存储处理
        {
            //获取当前zone内所有玩家(遍历发送，用户量多的情况....?)
            Collection<User> list = hallExt.getParentZone().getUserList();
            WCHaveNewEmailSynRES res = new WCHaveNewEmailSynRES();
            res.setNewNumber(1);
            for (User user : list)
            {
                hallExt.send(CmdsUtils.CMD_NEWEMAILSYN, res.toObject(), user);
            }
        }
        return true;
    }
}
