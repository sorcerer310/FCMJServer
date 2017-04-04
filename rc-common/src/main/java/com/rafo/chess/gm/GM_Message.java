package com.rafo.chess.gm;

import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.ExtensionLogLevel;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class GM_Message extends GMCommand {

	@Override
	public boolean exec(SFSObject params, SFSExtension sFSExtension) {
		sFSExtension.trace(ExtensionLogLevel.WARN, "GMCommand   params " + params.toJson());
		// TODO Auto-generated method stub
//		int type = params.getInt("type");
//		String title = params.getUtfString("title");
//		String concent = params.getUtfString("concent");
//		long sendTime = params.getLong("sendTime");
		return true;
	}
}
