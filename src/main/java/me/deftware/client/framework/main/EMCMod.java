package me.deftware.client.framework.main;

import com.google.gson.JsonObject;
import me.deftware.client.framework.event.Event;
import me.deftware.client.framework.utils.Settings;

public abstract class EMCMod {

	private Settings settings;
	public JsonObject clientInfo;

	protected void init(JsonObject json) {
		clientInfo = json;
		settings = new Settings();
		settings.initialize(json);
		initialize();
	}

	/**
	 * Called before any events are sent to your mod, do your initialization here
	 */
	public abstract void initialize();

	/**
	 *
	 * @return EMCModInfo
	 */
	public abstract EMCModInfo getModInfo();

	/**
	 * The main function that EMC uses to send events to your mod
	 *
	 * @param event
	 */
	public abstract void onEvent(Event event);

	/**
	 * Called when EMC has tried to connect to the marketplace API, both successfully and unsuccessfully
	 *
	 * @param status Whether or not EMC has a successful connection with the EMC mod marketplace
	 */
	public void onMarketplaceAuth(boolean status) { }

	/**
	 * Unloads your mod from EMC
	 */
	protected void disable() {
		Bootstrap.getMods().remove(clientInfo.get("name").getAsString());
	}

	/**
	 * Returns your main EMC mod settings handler
	 *
	 * @return Settings
	 */
	public Settings getSettings() {
		return settings;
	}

	/**
	 * Stores all info about an EMC mod
	 */
	public static class EMCModInfo {

		private String clientName, clientVersion;

		public EMCModInfo(String clientName, String clientVersion) {
			this.clientName = clientName;
			this.clientVersion = clientVersion;
		}

		public String getClientName() {
			return clientName;
		}

		public String getClientVersion() {
			return clientVersion;
		}

	}

	/**
	 * Called when Minecraft is closed, use this method to save anything in your mod
	 */
	public void onUnload() { }

	/**
	 *	By implementing this function you can call functions in other EMC mods
	 *
	 * @param method The method the caller wants to call
	 * @param caller The EMC mod that is calling your function
	 */
	public void callMethod(String method, String caller) { }

}
