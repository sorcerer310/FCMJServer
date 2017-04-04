package com.rafo.chess.gm;

/**
 * gm工具类
 * 
 * @author yangtao
 * @dateTime 2016年10月12日 下午7:35:58
 * @version 1.0
 */
public class GMUtils {

	private static GMUtils gMUtils;

	public static synchronized GMUtils getGMUtils() {
		if (gMUtils == null) {
			gMUtils = new GMUtils();
		}
		return gMUtils;
	}

	/**
	 * 判断是否是gm用户
	 * 
	 * @author yangtao
	 * @dateTime 2016年10月12日 上午9:50:11
	 * @version 1.0
	 * @param userName
	 * @return
	 */
	public boolean isGMUser(String userName) {
		if (userName != null && userName.startsWith("MJGM")) {
			return true;
		}
		return false;
	}
}
