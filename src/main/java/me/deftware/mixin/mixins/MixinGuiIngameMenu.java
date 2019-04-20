package me.deftware.mixin.mixins;

import me.deftware.client.framework.event.events.EventActionPerformed;
import me.deftware.client.framework.event.events.EventGuiScreenDraw;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngameMenu.class)
public class MixinGuiIngameMenu {

	@Inject(method = "actionPerformed", at = @At("HEAD"))
	private void actionPerformed(GuiButton button, CallbackInfo ci) {
		new EventActionPerformed((GuiScreen) (Object) this, button.id).send();
	}

	@Inject(method = "drawScreen", at = @At("RETURN"))
	private void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		new EventGuiScreenDraw((GuiScreen) (Object) this).send();
	}

}
