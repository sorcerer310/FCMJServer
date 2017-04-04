package com.rafo.hall.handlers;

import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.gm.GMUtils;
import com.rafo.hall.common.GlobalConstants;
import com.rafo.hall.utils.CmdsUtils;
import com.smartfoxserver.bitswarm.sessions.ISession;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSErrorCode;
import com.smartfoxserver.v2.exceptions.SFSErrorData;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.exceptions.SFSLoginException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/18.
 */
public class PreLoginHandler extends BaseServerEventHandler {

    private final Logger logger = LoggerFactory.getLogger("login");

    @Override
    public void handleServerEvent(ISFSEvent isfsEvent) throws SFSException {
        long begin = System.currentTimeMillis();
        String uid = (String)isfsEvent.getParameter(SFSEventParam.LOGIN_NAME);
		if (GMUtils.getGMUtils().isGMUser(uid)) {
			return;
		}

        if(!RedisManager.getInstance().exists("uid."+uid)){
            SFSErrorData data = new SFSErrorData(SFSErrorCode.LOGIN_BAD_USERNAME);
            data.addParameter(uid);
            throw new SFSLoginException("band username uid: " + uid, data);
        }

        String password = (String)isfsEvent.getParameter(SFSEventParam.LOGIN_PASSWORD);
        ISession session = (ISession) isfsEvent.getParameter(SFSEventParam.SESSION);

        ISFSObject customObj = (SFSObject)isfsEvent.getParameter(SFSEventParam.LOGIN_IN_DATA);
        int channel = 0;
        int version = 0;
        if(customObj == null || customObj.size() == 0){

        }else {
            channel = customObj.getInt("channel");
            version = customObj.getInt("version");

            Map<String,String> data = new HashMap<>();
            data.put("channel", Integer.toString(channel));
            data.put("version", Integer.toString(version));
            data.put("ip", session.getAddress());

            RedisManager.getInstance().hMSet("uid." + uid , data);
        }
        //此账号是否封号状态
        String forbidTimeS = RedisManager.getInstance().hGet("uid." + uid, "forbidTime");
        if (forbidTimeS != null && !forbidTimeS.equals("")){
            long forbidTime = Long.valueOf(forbidTimeS);
            if (forbidTime > System.currentTimeMillis()){
                SFSErrorData data = new SFSErrorData(SFSErrorCode.LOGIN_BANNED_USER);
                data.addParameter(uid);//parameter不能为空，否则客户端无法收到LOGIN_BANNED_USER事件uid
                logger.debug("prelogin\t" + uid+ "\tbanned");
                throw new SFSLoginException("Banned uid: " + uid, data);
            }
        }

        String value = RedisManager.getInstance().hGet("uid." + uid , "status");
        User oldUser = getParentExtension().getParentZone().getUserManager().getUserByName(uid);
        if(oldUser != null && value != null){
            getApi().logout(oldUser);
            SFSObject obj = new SFSObject();
            obj.putInt("result", GlobalConstants.LOGOUT_KICKOFF);
            obj.putUtfString("msg","your account login in other place");
            getParentExtension().send(CmdsUtils.SFS_EVENT_ACCOUNT_LOGOUT,obj,oldUser);
            logger.debug("prelogin\t" + uid+ "\tkickoff");
        }
        RedisManager.getInstance().hSet("uid." + uid , "status" , "1");

        String token = RedisManager.getInstance().hGet("uid." + uid , "token");
        boolean tokenOK = false;
        if (token != null && !token.equals("")) {
            String expireStr = RedisManager.getInstance().hGet("uid." + uid , "expire");
            if (expireStr != null && !expireStr.equals("")){
                Long expire = Long.valueOf(expireStr);
                if (expire > System.currentTimeMillis()) {
                    if (getApi().checkSecurePassword(session, token, password)){
                        session.setProperty("uid",uid);
                        tokenOK = true;
                    }
                }
            }
        }

        if (!tokenOK) {
            SFSErrorData data = new SFSErrorData(SFSErrorCode.LOGIN_BAD_PASSWORD);
            data.addParameter(uid); //parameter不能为空，否则客户端无法收到LOGIN_ERROR事件uid
            logger.debug("prelogin\t" + uid+ "\tbad_password");
            throw new SFSLoginException("Token error uid: "  + uid, data);
        }
        logger.debug("prelogined\t" + uid+ "\t" + session.getId() + "\t" + session.getAddress()
                + "\t" + channel + "\t" + version + "\t" + (System.currentTimeMillis() - begin));

    }
}
