package me.deftware.client.framework.wrappers;

import net.minecraft.client.Minecraft;

public class IGameSettings {

    public static void setLimitFramerate(int framerate) {
        Minecraft.getMinecraft().gameSettings.limitFramerate = framerate;
    }

    public static int getLimitFramerate() {
        return Minecraft.getMinecraft().gameSettings.limitFramerate;
    }

}
