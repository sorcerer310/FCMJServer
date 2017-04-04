package com.rafo.hall.handlers;

import com.bbzhu.system.HttpCenter;
import com.rafo.hall.common.GlobalConstants;
import com.rafo.hall.utils.CmdsUtils;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.extensions.SFSExtension;
import org.apache.commons.lang.StringUtils;

/**
 * Created by Administrator on 2016/9/22.
 */
public class ContactHandler extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User user, ISFSObject params) {
        ISFSObject respObj = new SFSObject();
        respObj.putInt("result", GlobalConstants.CONTACT_SUCCESS);
         // 通过读取GM配置的数据
//		HttpClentUtils.getHttpClentUtils().getHttpRechargeData(CmdsUtils.CMD_CONTACT, user, sfs, params);
        String value = getParentExtension().getConfigProperties().getProperty("gmt.httpurl");
        if (StringUtils.isEmpty(value)) {
            value = "http://192.168.10.163";
        }
        String httpurl = value + "/agentContact.lc";
        respObj.putUtfString("content", HttpCenter.getInstance().httpGet(httpurl + "?uid=" + user.getName(),"utf-8"));
        SFSExtension sfs = getParentExtension();
        sfs.send(CmdsUtils.CMD_CONTACT , respObj , user);
    }
}
