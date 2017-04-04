package com.rafo.hall.core;

import com.bbzhu.cache.CacheTimer;
import com.bbzhu.database.DatabaseConn;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.gm.GMInteface;
import com.rafo.chess.gm.service.EmailService;
import com.rafo.chess.gm.service.MarqueeService;
import com.rafo.chess.gm.service.NoticeService;
import com.rafo.hall.handlers.*;
import com.rafo.hall.task.GMTask;
import com.rafo.hall.utils.CmdsUtils;
import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;
import net.sf.json.JSONObject;

import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2016/9/18.
 */
public class HallExtension extends SFSExtension {

	private EmailService emailService;
	private MarqueeService marqueeService;
	private NoticeService noticeService;

	public void init() {
		emailService = new EmailService(this);
		noticeService = new NoticeService(this);
		marqueeService = new MarqueeService(this);
		RedisManager.getInstance().init(this.getConfigProperties());
		Properties props = this.getConfigProperties();
		DatabaseConn.getInstance().addDataSource(0,
				props.getProperty("local.jdbc.driver"),
				props.getProperty("local.jdbc.url"),
				props.getProperty("local.jdbc.user"),
				props.getProperty("local.jdbc.password"));
		DatabaseConn.getInstance().addDataSource(1,
				props.getProperty("local.jdbc.driver"),
				"jdbc:mysql://120.27.129.46:3306/gm?characterEncoding=utf-8&amp;autoReconnect=true",
				props.getProperty("local.jdbc.user"),
				props.getProperty("local.jdbc.password"));
		// HttpClentUtils.getHttpClentUtils().setGmUrl(props.getProperty("gmUrl"));
		new Timer().schedule(new CacheTimer(), 0, 30000);
		// ServletServer.startHttpServer(this);
		addRequestHandler(CmdsUtils.CMD_GetSer, GetserHandler.class);
		addEventHandler(SFSEventType.USER_LOGIN, PreLoginHandler.class);
		addEventHandler(SFSEventType.USER_JOIN_ZONE, LoginHandler.class);
		addEventHandler(SFSEventType.USER_DISCONNECT, LogoutHandler.class);
		addEventHandler(SFSEventType.USER_LOGOUT, LogoutHandler.class);
		addRequestHandler(CmdsUtils.CMD_ROUNDRECORD, RoundRecordHandler.class);
		addRequestHandler(CmdsUtils.CMD_CONTACT, ContactHandler.class);
		addRequestHandler(CmdsUtils.CMD_ROOMRECORD, RoomRecordHandler.class);
		addRequestHandler(CmdsUtils.CMD_WCVISITEMAIL, VisitEmailHandler.class);
		addRequestHandler(CmdsUtils.CMD_GM, GMHandler.class);
		addRequestHandler(CmdsUtils.CMD_GetSerHTTPLIST,
				GetserHttpListHandler.class);
		addRequestHandler(CmdsUtils.CMD_GetSerLIST, GetserListHandler.class);
		addRequestHandler(CmdsUtils.CMD_ActiveRequestLIST,
				ActiveListHandler.class);
		addRequestHandler(CmdsUtils.CMD_INVITE, InviteHandler.class);
		addRequestHandler(CmdsUtils.CMD_CARDNUM, RoomCardHandler.class);

		SmartFoxServer sfs = SmartFoxServer.getInstance();
		GMTask task = new GMTask(this);
		sfs.getTaskScheduler()
				.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
	}

	public EmailService getEmailService() {
		return emailService;
	}

	public MarqueeService getMarqueeService() {
		return marqueeService;
	}

	public NoticeService getNoticeService() {
		return noticeService;
	}

	/**
	 * 接受http请求的数据
	 * 
	 * @author yangtao
	 * @dateTime 2016年10月18日 上午11:25:20
	 * @version 1.0 (non-JSDoc)
	 * @see com.smartfoxserver.v2.extensions.BaseSFSExtension#handleInternalMessage(String,
	 *      Object)
	 */
	@Override
	public Object handleInternalMessage(String cmdName, Object params) {
		Object result = null;
		JSONObject paramsJosn = JSONObject.fromObject(params);
		trace("paramsJosn-----" + paramsJosn + "---this" + this.getName());
		if (cmdName.equals("gm")) {
			String action = paramsJosn.getString("action");
			JSONObject paramsObject = (JSONObject) paramsJosn.get("params");
			SFSObject paramsSFS = (SFSObject) SFSObject
					.newFromJsonData(paramsObject.toString());
			// 之后新加action,就要创建一个类,类的名字是"GM_"+action,必须实现GMCommand
			result = GMInteface.execCommand(action, paramsSFS, this);
			if (result.toString().equals("0"))
				result = 1;
			else {
				result = 0;
			}
		}
		return result;
	}
}
