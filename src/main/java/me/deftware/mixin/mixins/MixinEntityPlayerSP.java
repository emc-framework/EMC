package me.deftware.mixin.mixins;

import me.deftware.client.framework.command.CommandRegister;
import me.deftware.client.framework.event.events.*;
import me.deftware.client.framework.utils.ChatColor;
import me.deftware.client.framework.utils.ChatProcessor;
import me.deftware.client.framework.wrappers.IChat;
import me.deftware.mixin.imp.IMixinEntityPlayerSP;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.server.network.packet.ChatMessageC2SPacket;
import net.minecraft.server.network.packet.ClientCommandC2SPacket;
import net.minecraft.server.network.packet.PlayerMoveServerMessage;
import net.minecraft.util.math.BoundingBox;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinEntityPlayerSP extends MixinEntity implements IMixinEntityPlayerSP {

    @Shadow
    @Final
    public ClientPlayNetworkHandler networkHandler;

    @Shadow
    private boolean lastOnGround;

    @Shadow
    private boolean lastSprinting;

    @Shadow
    private boolean lastIsHoldingSneakKey;

    @Shadow
    private float lastYaw;

    @Shadow
    private float lastPitch;

    @Shadow
    private double lastX;

    @Shadow
    private double lastBaseY;

    @Shadow
    private double lastZ;

    @Shadow
    private int field_3923;

    @Shadow
    private float field_3922;

    @Shadow
    private boolean lastAutoJump = true;

    @Shadow
    protected abstract boolean isCamera();

    @Shadow
    public abstract boolean isUsingItem();

    @Redirect(method = "updateMovement", at = @At(value = "INVOKE", target = "net/minecraft/client/network/ClientPlayerEntity.isUsingItem()Z", ordinal = 0))
    private boolean itemUseSlowdownEvent(ClientPlayerEntity self) {
        EventSlowdown event = new EventSlowdown(EventSlowdown.SlowdownType.Item_Use);
        event.broadcast();
        if (event.isCanceled()) {
            return false;
        }
        return isUsingItem();
    }

    @Redirect(method = "updateMovement", at = @At(value = "INVOKE", target = "net/minecraft/entity/player/HungerManager.getFoodLevel()I"))
    private int hungerSlowdownEvent(HungerManager self) {
        EventSlowdown event = new EventSlowdown(EventSlowdown.SlowdownType.Hunger);
        event.broadcast();
        if (event.isCanceled()) {
            return 7;
        }
        return self.getFoodLevel();
    }

    /*
    @Redirect(method = "updateMovement", at = @At(value = "INVOKE", target = "net/minecraft/entity/LivingEntity.hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z", remap = false))
    private boolean blindlessSlowdownEvent(LivingEntity self) {
        EventSlowdown event = new EventSlowdown(EventSlowdown.SlowdownType.Blindness);
        event.broadcast();
        if (event.isCanceled()) {
            return false;
        }
        return self.hasStatusEffect(StatusEffects.BLINDNESS);
    } */

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tick(CallbackInfo ci) {
        EventUpdate event = new EventUpdate(x, y, z, yaw, pitch, onGround);
        event.broadcast();
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void sendChatMessage(String message, CallbackInfo ci) {
        String trigger = CommandRegister.getCommandTrigger();
        if (message.startsWith(trigger) && !trigger.equals("")) {
            try {
                if (message.startsWith(trigger + "say")) {
                    if (!message.contains(" ")) {
                        ChatProcessor.printClientMessage(
                                "Invalid syntax, please use: " + ChatColor.AQUA + trigger + "say <Message>");
                        return;
                    }
                    networkHandler.sendPacket(new ChatMessageC2SPacket(message.substring((trigger + "say ").length())));
                    ci.cancel();
                    return;
                }
                CommandRegister.getDispatcher().execute(message.substring(CommandRegister.getCommandTrigger().length()), MinecraftClient.getInstance().player.getCommandSource());
            } catch (Exception ex) {
                ex.printStackTrace();
                IChat.sendClientMessage(ex.getMessage());
            }
            ci.cancel();
        } else if (message.startsWith("#")) {
            message = message.startsWith("# ") ? message.substring(2) : message.substring(1);
            if (message.equals("")) {
                ChatProcessor.printClientMessage("Invalid syntax, please use: " + ChatColor.AQUA + "# <Message>");
                ci.cancel();
                return;
            }
            new EventIRCMessage(message).broadcast();
            ci.cancel();
            return;
        }
        EventChatSend event = new EventChatSend(message);
        event.broadcast();
        if (event.isCanceled()) {
            ci.cancel();
        } else if (!event.getMessage().equals(message)) {
            networkHandler.sendPacket(new ChatMessageC2SPacket(event.getMessage()));
            ci.cancel();
        }
    }


    @Override
    public void setHorseJumpPower(float height) {
        field_3922 = height;
    }

    /**
     * @Author Deftware
     * @reason
     */
    @Overwrite
    private void sendMovementPackets() {
        EventPlayerWalking event = new EventPlayerWalking(x, y, z, yaw, pitch, onGround);
        event.broadcast();
        if (event.isCanceled()) {
            return;
        }

        boolean boolean_1 = this.isSprinting();
        if (boolean_1 != this.lastSprinting) {
            if (boolean_1) {
                this.networkHandler.sendPacket(new ClientCommandC2SPacket((ClientPlayerEntity) (Object) this, ClientCommandC2SPacket.Mode.START_SPRINTING));
            } else {
                this.networkHandler.sendPacket(new ClientCommandC2SPacket((ClientPlayerEntity) (Object) this, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            }

            this.lastSprinting = boolean_1;
        }

        boolean boolean_2 = this.isSneaking();
        if (boolean_2 != this.lastIsHoldingSneakKey) {
            if (boolean_2) {
                this.networkHandler.sendPacket(new ClientCommandC2SPacket((ClientPlayerEntity) (Object) this, ClientCommandC2SPacket.Mode.START_SNEAKING));
            } else {
                this.networkHandler.sendPacket(new ClientCommandC2SPacket((ClientPlayerEntity) (Object) this, ClientCommandC2SPacket.Mode.STOP_SNEAKING));
            }

            this.lastIsHoldingSneakKey = boolean_2;
        }

        if (isCamera()) {
            BoundingBox axisalignedbb = getBoundingBox();
            double d0 = x - lastX;
            double d1 = event.getPosY() - lastBaseY;
            double d2 = z - lastZ;
            double d3 = event.getRotationYaw() - lastYaw;
            double d4 = event.getRotationPitch() - lastPitch;
            ++field_3923;
            boolean flag2 = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4D || field_3923 >= 20;
            boolean flag3 = d3 != 0.0D || d4 != 0.0D;

            if (hasVehicle()) {
                Vec3d vec3d_1 = ((ClientPlayerEntity) (Object) this).getVelocity();
                this.networkHandler.sendPacket(new PlayerMoveServerMessage.Both(vec3d_1.x, -999.0D, vec3d_1.z,
                        event.getRotationYaw(), event.getRotationPitch(), event.isOnGround()));
                flag2 = false;
            } else if ((flag2 && flag3)) {
                this.networkHandler.sendPacket(new PlayerMoveServerMessage.Both(x, event.getPosY(), z,
                        event.getRotationYaw(), event.getRotationPitch(), event.isOnGround()));
            } else if (flag2) {
                this.networkHandler.sendPacket(new PlayerMoveServerMessage.PositionOnly(x, event.getPosY(), z, event.isOnGround()));
            } else if (flag3) {
                this.networkHandler.sendPacket(new PlayerMoveServerMessage.LookOnly(event.getRotationYaw(), event.getRotationPitch(),
                        event.isOnGround()));
            } else if (lastOnGround != onGround) {
                this.networkHandler.sendPacket(new PlayerMoveServerMessage(event.isOnGround()));
            }

            if (flag2) {
                this.lastX = this.x;
                this.lastBaseY = axisalignedbb.minY;
                this.lastZ = this.z;
                this.field_3923 = 0;
            }

            if (flag3) {
                this.lastYaw = this.yaw;
                this.lastPitch = this.pitch;
            }

            this.lastOnGround = this.onGround;
            this.lastAutoJump = MinecraftClient.getInstance().options.autoJump;
        }

    }

}
