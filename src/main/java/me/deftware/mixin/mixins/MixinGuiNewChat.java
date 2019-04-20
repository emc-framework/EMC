package me.deftware.mixin.mixins;

import me.deftware.client.framework.event.events.EventChatReceive;
import me.deftware.mixin.imp.IMixinGuiNewChat;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public abstract class MixinGuiNewChat implements IMixinGuiNewChat {

    EventChatReceive event;

    @Shadow
    protected abstract void addMessage(TextComponent textComponent_1, int int_1, int int_2, boolean boolean_1);

    @Override
    public void setTheChatLine(TextComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly) {
        addMessage(chatComponent, chatLineId, updateCounter, displayOnly);
    }

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/TextComponent;IIZ)V", at = @At("HEAD"))
    public TextComponent addMessage(TextComponent chatComponent) {
        event = new EventChatReceive(chatComponent);
        event.broadcast();
        return event.getItc();
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/TextComponent;IIZ)V", at = @At("HEAD"), cancellable = true)
    public void addMessage(TextComponent textComponent_1, int int_1, int int_2, boolean boolean_1, CallbackInfo ci) {
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

}
