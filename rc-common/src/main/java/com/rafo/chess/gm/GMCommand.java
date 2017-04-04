package com.rafo.chess.gm;

import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;

public abstract class GMCommand {

	public abstract boolean exec(SFSObject params, SFSExtension sFSExtension);
}
