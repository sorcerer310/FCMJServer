package com.rafo.chess.engine.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import com.rafo.chess.resources.DataContainer;
import com.rafo.chess.template.impl.PluginTemplateGen;

@SuppressWarnings({"rawtypes","unchecked"})
public class OptPluginFactory {

	/***
	 * 实例化插件
	 * 
	 * @param pluginConf
	 * @return
	 */
	public static IOptPlugin createOptPlugin(int pluginId) {
		IOptPlugin plugin = null;
		PluginTemplateGen gen = (PluginTemplateGen) DataContainer.getInstance()
				.getDataByNameAndId("pluginTemplateGen",pluginId);
//		PluginTemplateGen gen = (PluginTemplateGen) DataContainer.getInstance()
//				.getMapDataByName("pluginTemplateGen").get("" + pluginId);
		try {
			Class clazz = Class.forName(PluginConstants.PLUGIN_CLASS_PATH + "."
					+ gen.getPluginClass());
			plugin = (IOptPlugin) clazz.newInstance();
			plugin.setGen(gen);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return plugin;
	}
	
	public static IOptPlugin createPluginListBySubType(int subType,int roomTempId){
		ArrayList<PluginTemplateGen> genlist = (ArrayList<PluginTemplateGen>) DataContainer
				.getInstance().getListDataByName("pluginTemplateGen");
		for (PluginTemplateGen gen : genlist) {
			if(gen.getRoomSettingTempId()!=roomTempId)
				continue;
			if(gen.getSubType()==subType){
				IOptPlugin plugin = createOptPlugin(gen.getTempId());
				return plugin;
			}
		}
		return null;
	}

	/**
	 * 通过插件类型创建一个插件列表
	 * @param actonType
	 * @param roomTempId
	 * @return
	 */
	public static ArrayList<IOptPlugin> createPluginListByActionType(int actonType,int roomTempId){
		ArrayList<IOptPlugin> list = new ArrayList<IOptPlugin>();
		ArrayList<PluginTemplateGen> genlist = (ArrayList<PluginTemplateGen>) DataContainer
				.getInstance().getListDataByName("pluginTemplateGen");
		for (PluginTemplateGen gen : genlist) {
			if(gen.getRoomSettingTempId()!=roomTempId)
				continue;
			String[] actions = gen.getActionType().split(",");
			for (String str : actions) {
				if (Integer.parseInt(str) == actonType) {
					IOptPlugin plugin = createOptPlugin(gen.getTempId());
					list.add(plugin);
					break;
				}
			}
		}
		return list;
	}

	public static void main(String[] arge) {
		String str = "28,33,16,35,25,26,15,16,11,37,16,12,37,16";
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (String s : str.split(",")) {
			list.add(Integer.parseInt(s));
		}
		Collections.sort(list, new Comparator<Integer>() {

			@Override
			public int compare(Integer arg0, Integer arg1) {
				return arg0 - arg1;
			}
		});
		for (Integer i : list) {
			System.out.print(i + ",");
		}

	}
}
