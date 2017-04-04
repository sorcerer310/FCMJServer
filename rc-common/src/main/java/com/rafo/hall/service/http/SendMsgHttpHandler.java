package com.rafo.hall.service.http;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class SendMsgHttpHandler extends HttpCallBackHandler {

	private final User user;

	private final SFSExtension sfs;

	private final String cmd;

	public SendMsgHttpHandler(String cmd, User user, SFSExtension sfs) {
		this.user = user;
		this.sfs = sfs;
		this.cmd = cmd;
	}

	protected boolean process(JSONObject json) {
		if (cmd.equals(CmdsUtils.CMD_CONTACT)) {
			/*
			 * "time":
			 * [{"content":"内容库3","endTime":"10","id":3,"startTime":"9"},
			 * {"content":"内容库4","endTime":"11","id":4,"startTime":"10"}
			 * ],
			 * "common":
			 * {"content":"内容库1","endTime":"","id":1,"startTime":""}
			 **/
			System.out.println("json:" + json);
			ISFSObject respObj = new SFSObject();
			JSONObject commonJSONObject = (JSONObject) json.get("common");
			ISFSObject commonSfsObject = new SFSObject();
			respObj.putSFSObject("common", commonSfsObject);
			commonSfsObject.putUtfString("content", (String) commonJSONObject.get("content"));
			JSONArray timeArray = json.getJSONArray("time");
			ISFSArray timeSFSArrayList = new SFSArray();
			for (int i = 0; i < timeArray.size(); i++) {
				ISFSObject dataSfsObject = new SFSObject();
				JSONObject dataJSONObject = timeArray.getJSONObject(i);
				dataSfsObject.putUtfString("content", (String) dataJSONObject.get("content"));
				dataSfsObject.putInt("startTime", (Integer) dataJSONObject.get("startTime"));
				dataSfsObject.putInt("endTime", (Integer) dataJSONObject.get("endTime"));
				timeSFSArrayList.addSFSObject(dataSfsObject);
			}
			respObj.putSFSArray("time", timeSFSArrayList);
			// 数据发送
			sfs.send(cmd, respObj, user);
		}
		return true;
	}
}
