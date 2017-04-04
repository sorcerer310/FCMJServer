package com.rafo.chess.gm;

import com.rafo.chess.exception.PersistException;
import com.rafo.hall.common.GlobalConstants;
import com.rafo.hall.core.HallExtension;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.hall.service.LoginService;
import com.rafo.hall.utils.CmdsUtils;
import com.rafo.hall.utils.TimeUtils;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.ExtensionLogLevel;
import com.smartfoxserver.v2.extensions.SFSExtension;

/**
 * Created by YL.
 * Date: 16-10-21
 */
public class GM_Forbid extends GMCommand
{

    @Override
    public boolean exec(SFSObject params, SFSExtension sFSExtension)
    {
        sFSExtension.trace(ExtensionLogLevel.WARN, "GMCommand  Forbid params " + params.toJson());
        HallExtension hallExt = (HallExtension) sFSExtension;
        try
        {
            int playerId = params.getInt("playerId");
            long forbidTime = params.getLong("forbidTime");
            //保存禁止时间
            RedisManager.getInstance().hMSet("uid." + playerId, "forbidTime", String.valueOf(forbidTime));
            LoginService.updateForbidTime(playerId, TimeUtils.millisecondToDate(forbidTime));
            //如果当前该玩家在线，要将其踢下线
            User user = hallExt.getApi().getUserByName(String.valueOf(playerId));
            if (user != null)
            {
                hallExt.getApi().logout(user);
                SFSObject obj = new SFSObject();
                obj.putInt("result", GlobalConstants.LOGOUT_FORBID);
                obj.putUtfString("msg","Your account has been banned!");
                hallExt.send(CmdsUtils.SFS_EVENT_ACCOUNT_LOGOUT,obj,user);
            }
        } catch (PersistException e)
        {
            e.printStackTrace();
        }
        return true;
    }
}
