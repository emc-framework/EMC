package me.deftware.client.framework.command.commands;

import me.deftware.client.framework.command.CommandBuilder;
import me.deftware.client.framework.command.EMCModCommand;
import me.deftware.client.framework.event.EventBus;
import me.deftware.client.framework.main.Bootstrap;
import me.deftware.client.framework.utils.ChatProcessor;
import net.minecraft.client.MinecraftClient;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class CommandUnload extends EMCModCommand {

    @Override
    public CommandBuilder getCommandBuilder() {
        return new CommandBuilder().set(literal("unload")
                /* Disable until a way to unload mod specific events/commands is made
                .then(
                        argument("modname", new EMCModArgument())
                                .executes(c -> {
                                    CommandResult r = new CommandResult(c);
                                    EMCMod mod = (EMCMod) r.getCustom("modname", EMCMod.class);
                                    Bootstrap.getMods().get(mod.modInfo.get("name").getAsString()).onUnload();
                                    Bootstrap.getMods().remove(mod.modInfo.get("name").getAsString());
                                    ChatProcessor.printFrameworkMessage("Unloaded " + mod.modInfo.get("name").getAsString());
                                    return 1;
                                })
                )*/
                .executes(c -> {
                    // Remove event listeners
                    EventBus.clearEvents();
                    // Unload all mods
                    Bootstrap.ejectMods();
                    // Reset gamma
                    MinecraftClient.getInstance().options.gamma = 0.5F;
                    ChatProcessor.printFrameworkMessage("Unloaded all EMC mods, Minecraft is now running as vanilla");
                    return 1;
                }));
    }

}
