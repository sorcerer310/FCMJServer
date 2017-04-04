package com.rafo.hall.service.http;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;

public class Test {

	public static void main(String[] args) {
		ISFSObject iSFSObject = new SFSObject();
		iSFSObject.putInt("type", 2);
		HttpClentUtils.getHttpClentUtils().setGmUrl("http://192.168.1.23:18080/games-admin/housecardinfo.lc");
		 HttpClentUtils.getHttpClentUtils().getHttpRechargeData(CmdsUtils.CMD_CONTACT, null, null, iSFSObject);
		//HttpClentUtils.getHttpClentUtils().getHttpTestData(null, null, null);
	}
}
