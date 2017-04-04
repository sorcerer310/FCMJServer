package com.rafo.chess.gm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class GMInteface {

	private final static Logger logger = LoggerFactory.getLogger("Extensions");

	public static int execCommand(String action, SFSObject params, SFSExtension sFSExtension) {
		int succ = -1;
		try {
			GMCommand obj = (GMCommand) Class.forName(GMInteface.class.getPackage().getName() + ".GM_" + action).newInstance();
			succ = obj.exec(params, sFSExtension) ? 0 : -1;
			if (succ == 0) {
				logger.info("exc GM success");
			} else {
				logger.info("xc GM fail");
			}
		} catch (Exception ex) {
 			ex.printStackTrace();
		}
		return succ;
	}
}
