package com.rafo.chess.resources.define;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import com.rafo.chess.resources.config.ann.DataDefine;
import com.rafo.chess.resources.config.build.interfaces.IBuilder;
import com.rafo.chess.resources.config.check.inerfaces.IChecker;
import com.rafo.chess.resources.config.load.interfaces.ILoader;


public class DataConfigManager {
	// 日志
	private Logger log = Logger.getLogger(DataConfigManager.class);
	private static DataConfigManager dataConfigManager = null;
	private Map<String, HSSFSheet> xlsMap;
	public static String resourcePath = null;
	

	public static DataConfigManager getInstance() {
		if (dataConfigManager == null) {
			synchronized (DataConfigManager.class) {
				dataConfigManager = new DataConfigManager();
			}
		}
		return dataConfigManager;
	}
	
	public Map<String, HSSFSheet> getXlsMap() {
		return xlsMap;
	}

	public void setXlsMap(Map<String, HSSFSheet> xlsMap) {
		this.xlsMap = xlsMap;
	}

	private DataConfigManager() {
		this.xlsMap = new HashMap<String, HSSFSheet>();
	}

	@SuppressWarnings("unchecked")
	public void createDataConfigBean(Class clazz){
		if (!clazz.isAnnotationPresent(DataDefine.class)) {
			return;
		}
		DataDefine dataDefine = (DataDefine) clazz
				.getAnnotation(DataDefine.class);
		// 初始化配置bean
		DataConfigBean dataConfigBean = new DataConfigBean();
		dataConfigBean.setBeanClass(clazz);
		dataConfigBean.setBuildClass(dataDefine.buildClass());
		dataConfigBean.setCanUse(dataDefine.canUse());
		dataConfigBean.setCheckClass(dataDefine.checkClass());
		dataConfigBean.setConfigFileName(dataDefine
				.configFileName());
		dataConfigBean.setIdColunm(dataDefine.idColunm());
		dataConfigBean.setName(dataDefine.name());
		dataConfigBean.setSheetFileName(dataDefine.sheetFileName());
		// 保存配置
		DataConfigContainer.getInstance().addDataConfig(
				dataConfigBean);
	}
	
	

	/**
	 * 加载所有基础数据
	 */
	public void loadData() {
		Set<String> beanClassNames = DataConfigContainer.getInstance()
				.getAllDataConfigBean();
		for (String beanClassName : beanClassNames) {
			this.loadData(beanClassName);
		}
	}

	/**
	 * 加载单个数据，指定要加载bean Class
	 * 
	 * @param beanClassName
	 *            要加载bean class
	 */
	public void loadData(String beanClassName) {
		DataConfigBean dataConfigBean = DataConfigContainer.getInstance()
				.getDataConfigBean(beanClassName);
		if (dataConfigBean != null) {
			log.info(String
					.format("Load Data:File[%s] sheetName[%s] Bean[%s] Loader[%s] Builder[%s] Checker[%s]",
							dataConfigBean.getConfigFileName(), dataConfigBean
									.getSheetFileName(), dataConfigBean
									.getBeanClass().getName(), dataConfigBean
									.getLoaderClass().getName(), dataConfigBean
									.getBuildClass().getName(), dataConfigBean
									.getCheckClass().getName()));

			try {
				// 读取数据
				ILoader<?, ?> loader = dataConfigBean.getLoaderClass()
						.newInstance();
				 String path =resourcePath+dataConfigBean.getConfigFileName();
				String[][] dataOriginal = loader.loadConfig(path,
						dataConfigBean.getSheetFileName());

				// 校验数据
				IChecker checker = dataConfigBean.getCheckClass().newInstance();
				boolean back = checker.checkData(dataOriginal);
				if (!back) {
					throw new IllegalArgumentException(
							String.format(
									"Check Data Error:File[%s] sheetName[%s] Bean[%s] Loader[%s] Builder[%s] Checker[%s]",
									dataConfigBean.getConfigFileName(),
									dataConfigBean.getSheetFileName(),
									dataConfigBean.getBeanClass().getName(),
									dataConfigBean.getLoaderClass().getName(),
									dataConfigBean.getBuildClass().getName(),
									dataConfigBean.getCheckClass().getName()));
				}

				// 构建数据
				IBuilder builder = dataConfigBean.getBuildClass().newInstance();
				builder.initBuild(dataOriginal, dataConfigBean.getBeanClass());
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Load data failure! beanClassName=" + beanClassName,
						e);
				System.exit(0);
			}
		}
	}
}
