package com.rafo.chess.gm;

import com.rafo.chess.exception.PersistException;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.hall.service.LoginService;
import com.rafo.hall.utils.TimeUtils;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.ExtensionLogLevel;
import com.smartfoxserver.v2.extensions.SFSExtension;

/**
 * Created by YL.
 * Date: 16-10-17
 */
public class GM_UnForbid extends GMCommand
{

    @Override
    public boolean exec(SFSObject params, SFSExtension sFSExtension)
    {
        sFSExtension.trace(ExtensionLogLevel.WARN, "GMCommand UnForbid  params " + params.toJson());
        int playerId = params.getInt("playerId");
        try
        {
            //保存禁止时间
            RedisManager.getInstance().hMSet("uid." + playerId, "forbidTime", String.valueOf(System.currentTimeMillis()));
            LoginService.updateForbidTime(playerId, TimeUtils.millisecondToDate(System.currentTimeMillis()));
        } catch (PersistException e)
        {
            e.printStackTrace();
        }
        return true;
    }
}
