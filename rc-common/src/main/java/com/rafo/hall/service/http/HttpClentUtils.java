package com.rafo.hall.service.http;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class HttpClentUtils {

	private static CloseableHttpAsyncClient httpclient = null;

	String gmUrl = "";

	private static HttpClentUtils sfsClientInstance;

	public static synchronized HttpClentUtils getHttpClentUtils() {
		if (sfsClientInstance == null) {
			sfsClientInstance = new HttpClentUtils();
			getHttpClient();
			httpclient.start();
		}
		return sfsClientInstance;
	}

	public void setGmUrl(String gmUrl) {
		this.gmUrl = gmUrl;
	}

	/**
	 * 从GM得到充值提示数据
	 * 
	 * @author yangtao
	 * @dateTime 2016年10月12日 下午1:41:45
	 * @version 1.0
	 * @param cmd 发给客户端的指令
	 * @param user 用户id
	 * @param sfs
	 */
	public void getHttpRechargeData(String cmd, User user, SFSExtension sfs, ISFSObject iSFSObject) {
		// 2 代表贵阳 4 代表沈阳
		Integer type = iSFSObject.getInt("type");
		if (type == null)
			type = 2;
		String serverid = iSFSObject.getUtfString("serverid");
		if (serverid == null) {
			InetAddress addr;
			try {
				addr = InetAddress.getLocalHost();
				serverid = addr.getHostAddress().toString();// 获得本机IP
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String buildurl = String.format("%1$s?type=%2$s&serverid=%3$s", new Object[] { gmUrl, type, serverid });
		HttpGet request = new HttpGet(buildurl);
		httpclient.execute(request, new SendMsgHttpHandler(cmd, user, sfs));
	}

	public void getHttpTestData(String cmd, User user, SFSExtension sfs) {
		HttpGet request = new HttpGet(gmUrl);
		httpclient.execute(request, new SendMsgHttpHandler(cmd, user, sfs));
	}

	public static CloseableHttpAsyncClient getHttpClient() {
		if (httpclient == null) {
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(60000).build();
			int maxConnTotal = 2000;
			int maxConnPerRoute = 2000;
			httpclient = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig).setMaxConnTotal(maxConnTotal).setMaxConnPerRoute(maxConnPerRoute).build();
		}
		return httpclient;
	}
}
