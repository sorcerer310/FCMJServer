package com.rafo.hall.handlers;

import com.bbzhu.cache.Cache;
import com.rafo.hall.common.GlobalConstants;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.hall.model.cache.*;
import com.rafo.hall.utils.CmdsUtils;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.extensions.SFSExtension;
import com.www.model.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Created by Administrator on 2016/9/18.
 */
public class GetserHandler extends BaseClientRequestHandler {

    private final Logger logger = LoggerFactory.getLogger("login");

    @Override
    public void handleClientRequest(User user, ISFSObject params) {
        String roomid = params.getUtfString("roomid");
        Integer count = params.getInt("count");
        Integer type = params.getInt("type");
        Integer maType = params.getInt("maType");
        Server server = null;
        if (roomid == null || roomid.equals("")) {
            server = getServerByPreCreate();
        } else {
            server = getServerIdByRoomId(roomid);
            params.putUtfString("roomid",roomid);
        }

        params.putInt("result",0);
        if (server != null) {
            params.putUtfString("ip",server.getIp());
            params.putInt("port",server.getPort());
        } else {
            params.putInt("result", GlobalConstants.ROOM_ENTER_FAILED_NUMBER_ERROR);  //
        }

        if (count != null && type != null ) {
            params.putInt("count",count);
            params.putInt("type",type);
            params.putInt("maType",maType);
        }


        SFSExtension sfs = getParentExtension();
        sfs.send(CmdsUtils.CMD_GetSer , params , user);

        logger.info("getser\t" + (user!=null?user.getName():"") + "\t" + (server != null? server.getIp():"error") + "\t" + roomid);
    }

    //FIXME
    private Server getServerByPreCreate() {
        int max = (int) Cache.getInstance().get(CacheKey.ServerCount);
        int cur = (int) Cache.getInstance().get(CacheKey.curSerIndex);
        cur++;
        if (cur >= max) {
            cur = 0;
        }
        Cache.getInstance().set(CacheKey.curSerIndex,cur);
        return (Server) Cache.getInstance().get(CacheKey.ServerConfigIndex + "." + cur);
    }

    private Server getServerByPreCreateNum() {
        Set<String> keys = RedisManager.getInstance().keys("server.*");
        Integer min = Integer.MAX_VALUE;
        String serverid = "";
        Integer v = 0;
        for (String key : keys) {
            String value = RedisManager.getInstance().hGet(key , "num");
            trace("server:key:" + key + ";num:" + value);
            v = Integer.valueOf(value);
            if (min > v) {
                min = v;
                serverid = RedisManager.getInstance().hGet(key , "serverid");
            }
        }
        Object o = null;
        if (!serverid.equals("")) {
            o = Cache.getInstance().get(CacheKey.Server+ "." + serverid);
        }
        if (o != null)
        {
            return(Server)o;
        } else {
            return null;
        }
    }

    //FIXME
    private Server getServerIdByRoomId(String roomId) {
        int rid = Integer.valueOf(roomId);
        String serverid = RedisManager.getInstance().hGet("roomid." + rid , "serId");
        Object o = Cache.getInstance().get(CacheKey.ServerConfig + "." + serverid);
        if (o != null)
        {
            return(Server)o;
        } else {
            return null;
        }
    }
}
