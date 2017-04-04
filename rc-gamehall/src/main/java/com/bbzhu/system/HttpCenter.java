package com.bbzhu.system;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class HttpCenter {
	private static HttpCenter obj = null;
	
	public static HttpCenter getInstance(){

			if(obj == null){
				obj = new HttpCenter();
			}

		return obj;
	}
	
	private HttpCenter() {
	}
	
	public String httpClient(String path, String encode, Map<String, String> paramMap) {
		StringBuffer sb = new StringBuffer();
		// 默认连接
		HttpClient httpclient = new DefaultHttpClient();
		
		httpclient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);//为防止因为网络不良好，而带来线程锁定，长时间阻塞。
		httpclient.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);//为防止因为网络不良好，而带来线程锁定，长时间阻塞。
		httpclient.getParams().setIntParameter(CoreConnectionPNames.SO_LINGER, 0);// 为了防止高并发短链接（托管服务重发服务）
		
		HttpPost httppost = null;
		try {
			httppost = new HttpPost(path);
			String param = this.convertParam(paramMap);
			if (param != null) {
				StringEntity reqEntity = new StringEntity(param, encode);
				httppost.setEntity(reqEntity);
				reqEntity.setContentType("application/x-www-form-urlencoded");
			}

			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntity = response.getEntity();
			BufferedReader reader = new BufferedReader(new InputStreamReader(resEntity.getContent(), encode));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			if(resEntity != null) {
				if(resEntity.isStreaming()) {
					InputStream instream = resEntity.getContent();
					if(instream != null) {
						instream.close();
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			sb.append(e.getMessage());
		} finally {
			try {
				httpclient.getConnectionManager().shutdown();
			} catch (Exception e) {
				sb.append(e.getMessage());
			}
		}

		return sb.toString();
	}
	
	public String httpGet(String path, String encode) {
		StringBuffer sb = new StringBuffer();
		// 默认连接
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = null;
		try {
			HttpGet get = new HttpGet(path);

			HttpResponse response = httpclient.execute(get);
			HttpEntity resEntity = response.getEntity();
			BufferedReader reader = new BufferedReader(new InputStreamReader(resEntity.getContent(), encode));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}

			if(resEntity != null) {
				if(resEntity.isStreaming()) {
					InputStream instream = resEntity.getContent();
					if(instream != null) {
						instream.close();
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			sb.append(e.getMessage());
		} finally {
			try {
				httpclient.getConnectionManager().shutdown();
			} catch (Exception e) {
				sb.append(e.getMessage());
			}
		}

		return sb.toString();
	}
	
	public String httpClient(String path, Map<String, String> paramMap){
		return httpClient(path,"utf-8",paramMap);
	}

	private String convertParam(Map<String, String> paramMap) {
		if (paramMap == null || paramMap.size() == 0) {
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		for (String s : paramMap.keySet()) {
			sb.append(s).append("=").append(paramMap.get(s)).append("&");
		}
		
		return sb.substring(0, sb.length() - 1);
	}
	
	public static void main(String args[]) {
		

		
	}

}
