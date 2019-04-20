package me.deftware.mixin.mixins;

import me.deftware.client.framework.event.events.EventKnockback;
import me.deftware.client.framework.event.events.EventTotemAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.world.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

	@Shadow
	private Minecraft gameController;

	@Inject(method = "handleEntityStatus", at = @At("HEAD"), cancellable = true)
	public void handleEntityStatus(SPacketEntityStatus packetIn, CallbackInfo ci) {
		if (packetIn.getOpCode() == 35) {
			EventTotemAnimation event = new EventTotemAnimation().send();
			if (event.isCanceled()) {
				ci.cancel();
			}
		}
	}

	/**
	 * @Author Deftware
	 * @reason
	 */
	@Overwrite
	public void handleExplosion(SPacketExplosion packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, gameController);
		Explosion explosion = new Explosion(gameController.world, (Entity) null, packetIn.getX(), packetIn.getY(),
				packetIn.getZ(), packetIn.getStrength(), packetIn.getAffectedBlockPositions());
		explosion.doExplosionB(true);
		EventKnockback event = new EventKnockback(packetIn.getMotionX(), packetIn.getMotionY(), packetIn.getMotionZ()).send();
		if (event.isCanceled()) {
			return;
		}
		gameController.player.motionX += packetIn.getMotionX();
		gameController.player.motionY += packetIn.getMotionY();
		gameController.player.motionZ += packetIn.getMotionZ();
	}
}
