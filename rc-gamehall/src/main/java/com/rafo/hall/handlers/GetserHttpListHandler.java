package com.rafo.hall.handlers;

import com.bbzhu.cache.Cache;
import com.rafo.hall.model.cache.CacheKey;
import com.rafo.hall.utils.CmdsUtils;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.extensions.SFSExtension;
import com.www.model.Server;

/**
 * 获取所有服务器的http端口号
 * @author yangtao
 * @dateTime 2016年10月17日 上午9:35:42
 * @version 1.0
 */
public class GetserHttpListHandler extends BaseClientRequestHandler {

	@Override
	public void handleClientRequest(User user, ISFSObject params) {
		ISFSObject respObj = new SFSObject();
		ISFSArray serverList = getServerByPreCreate();
		respObj.putSFSArray("serverList", serverList);
		SFSExtension sfs = getParentExtension();
		sfs.send(CmdsUtils.CMD_GetSerHTTPLIST, respObj, user);
	}

	// FIXME
	/**
	 * 得到服务器列表
	 * 
	 * @author yangtao
	 * @dateTime 2016年10月10日 下午1:44:06
	 * @version 1.0
	 * @return
	 */
	private ISFSArray getServerByPreCreate() {
		ISFSArray sFSArray = new SFSArray();
		int max = (int) Cache.getInstance().get(CacheKey.ServerCount);
		for (int cur = 0; cur < max; cur++) {
			Server server = (Server) Cache.getInstance().get(CacheKey.ServerConfigIndex + "." + cur);
			if (server != null) {
				ISFSObject respObj = new SFSObject();
				respObj.putUtfString("ip", server.getIp());
				respObj.putInt("httpPort", server.getPort());
				sFSArray.addSFSObject(respObj);
			}
		}
		return sFSArray;
	}
}
