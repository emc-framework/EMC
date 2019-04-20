package me.deftware.client.framework.maps;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Used to store and modify Minecraft settings, this is used instead of Events because it is faster
 */
public class SettingsMap {

	private static boolean overrideMode = false;
	private static final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Object>> map = new ConcurrentHashMap<>();

	/**
	 * Sets a value, creates the value if it does not exist yet
	 *
	 * @param mapKey The key of the map
	 * @param key The key of the current map
	 * @param value The value of the map
	 */
	public static void update(int mapKey, String key, Object value) {
		SettingsMap.map.putIfAbsent(mapKey, new ConcurrentHashMap<>());
		SettingsMap.map.get(mapKey).put(key, value);
	}

	/**
	 * Returns a value from the map
	 *
	 * @param mapKey The map you want to access
	 * @param key The key in the map you want
	 * @param _default Default value to return if the map does not exist
	 * @return Object
	 */
	public static Object getValue(int mapKey, String key, Object _default) {
		return SettingsMap.map.containsKey(mapKey) ? SettingsMap.map.get(mapKey).containsKey(key) ? SettingsMap.map.get(mapKey).get(key) : _default : _default;
	}

	public static boolean isOverrideMode() {
		return SettingsMap.overrideMode;
	}

	/**
	 * Used to toggle override mode, with override mod on EMC will use the setting defined for a map key no matter what
	 *
	 * @param state
	 */
	public static void setOverrideMode(boolean state) {
		SettingsMap.overrideMode = state;
	}

	/**
	 * Pre-defined keys for common settings
	 */
	public static class MapKeys {

		/**
		 * lightValue = 0 - 10
		 * render = true | false
		 * translucent = true | false
		 * liquid_aabb_solid = true | false
		 * custom_cactus_aabb = true | false
		 * render_barrier_blocks = true | false
		 */
		public static final int BLOCKS = 1;

		/**
		 * RAINBOW_ITEM_GLINT = true | false
		 * WORLD_DEPTH = true | false
		 * FLIP_USERNAMES = String,String...
		 * CROSSHAIR = true | false
		 */
		public static final int RENDER = 2;

		/**
		 * DEADMAU_EARS
		 */
		public static final int MISC = 3;

	}

}
