package me.deftware.client.framework.wrappers;

import me.deftware.client.framework.utils.ChatProcessor;
import me.deftware.mixin.imp.IMixinGuiChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;

public class IChat {

	public static void sendChatMessage(String message) {
		Minecraft.getMinecraft().player.sendChatMessage(message);
	}

	public static void sendClientMessage(String message) {
		ChatProcessor.printClientMessage(message);
	}

	public static void sendClientMessage(String message, String prefix) {
		ChatProcessor.printClientMessage(prefix + " " + message, false);
	}

	public static void clearMessages() {
		Minecraft.getMinecraft().ingameGUI.getChatGUI().clearChatMessages(true);
	}

	public static String getCurrentChatText() {
		if (Minecraft.getMinecraft().currentScreen instanceof GuiChat) {
			return ((IMixinGuiChat) Minecraft.getMinecraft().currentScreen).getCurrentText();
		}
		return "";
	}

}
