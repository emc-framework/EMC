package me.deftware.mixin.mixins;

import static org.spongepowered.asm.lib.Opcodes.GETFIELD;

import me.deftware.client.framework.utils.ChatProcessor;
import me.deftware.client.framework.wrappers.IResourceLocation;
import me.deftware.mixin.imp.IMixinEntityRenderer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.deftware.client.framework.event.events.EventHurtcam;
import me.deftware.client.framework.event.events.EventRender2D;
import me.deftware.client.framework.event.events.EventRender3D;
import me.deftware.client.framework.event.events.EventWeather;
import me.deftware.client.framework.maps.SettingsMap;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer  implements IMixinEntityRenderer {

	// TODO: Override RayTraceResult, line 462, FIELD: flag

	@Shadow
	private boolean renderHand;

	private float partialTicks = 0;

	@Shadow
	public abstract void loadShader(ResourceLocation p_loadShader_1_);

	@Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
	private void hurtCameraEffect(float partialTicks, CallbackInfo ci) {
		EventHurtcam event = new EventHurtcam();
		event.broadcast();
		if (event.isCanceled()) {
			ci.cancel();
		}
	}

	@Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "net/minecraft/client/gui/GuiIngame.renderGameOverlay(F)V"))
	private void onRender2D(CallbackInfo cb) {
		ChatProcessor.sendMessages();
		new EventRender2D(0f).broadcast();
	}

	@Inject(method = "addRainParticles", at = @At("HEAD"), cancellable = true)
	private void addRainParticles(CallbackInfo ci) {
		EventWeather event = new EventWeather(EventWeather.WeatherType.Rain);
		event.broadcast();
		if (event.isCanceled()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderRainSnow", at = @At("HEAD"), cancellable = true)
	private void renderRainSnow(float partialTicks, CallbackInfo ci) {
		EventWeather event = new EventWeather(EventWeather.WeatherType.Rain);
		event.broadcast();
		if (event.isCanceled()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/GlStateManager.enableAlpha()V"))
	private void renderWorld(CallbackInfo ci) {
		if (!((boolean) SettingsMap.getValue(SettingsMap.MapKeys.RENDER, "WORLD_DEPTH", true))) {
			GlStateManager.disableDepth();
		}
	}

	@Inject(method = "updateCameraAndRender(FJ)V", at = @At("HEAD"))
	private void updateCameraAndRender(float partialTicks, long finishTimeNano, CallbackInfo ci) {
		this.partialTicks = partialTicks;
	}

	@Redirect(method = "updateCameraAndRender(FJ)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;renderHand:Z", opcode = GETFIELD))
	private boolean updateCameraAndRender_renderHand(EntityRenderer self) {
		new EventRender3D(partialTicks).broadcast();
		return renderHand;
	}

	@Override
	public void loadCustomShader(IResourceLocation location) {
		loadShader(location);
	}

}
