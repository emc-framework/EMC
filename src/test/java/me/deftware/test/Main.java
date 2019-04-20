package me.deftware.test;

import me.deftware.client.framework.event.EventBus;
import me.deftware.client.framework.event.EventHandler;
import me.deftware.client.framework.event.events.EventRender2D;
import me.deftware.client.framework.event.events.EventUpdate;
import me.deftware.client.framework.fonts.ColoredBitmapFont;
import me.deftware.client.framework.fonts.EMCFont;
import me.deftware.client.framework.fonts.FontManager;
import me.deftware.client.framework.main.EMCMod;

import java.awt.*;

public class Main extends EMCMod {

    private EMCFont customFont;

    /*
        Initialization
     */

    @Override
    public void initialize() {
        System.out.println("TestMod initialized");
        customFont = FontManager.getFont("Arial", 18, EMCFont.Modifiers.ANTIALIASED, ColoredBitmapFont.class);
        customFont.setShadowSize(2);
        customFont.initialize(Color.white, "");
        EventBus.registerClass(this.getClass(), this);
    }

    @Override
    public void postInit() {
        System.out.println("TestMod post initialized");
    }

    /*
        Events
     */

    @EventHandler(eventType = EventUpdate.class)
    public void onUpdate(EventUpdate event) {
        System.out.println("Event test");
    }

    @EventHandler(eventType = EventRender2D.class)
    public void onRender2D(EventRender2D event) {
        customFont.drawStringWithShadow(2,2, "TestMod");
    }

}
