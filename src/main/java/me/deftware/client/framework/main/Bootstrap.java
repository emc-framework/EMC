package me.deftware.client.framework.main;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import me.deftware.client.framework.FrameworkConstants;
import me.deftware.client.framework.apis.marketplace.MarketplaceAPI;
import me.deftware.client.framework.command.CommandRegister;
import me.deftware.client.framework.command.commands.*;
import me.deftware.client.framework.maps.SettingsMap;
import me.deftware.client.framework.utils.OSUtils;
import me.deftware.client.framework.utils.Settings;
import me.deftware.client.framework.wrappers.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.realms.RealmsSharedConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * This class is responsible for bootstrapping (initialization) process of EMC freamwork
 * it handles loading all of the mods, connecting with event listeners and checking
 * for available updates
 */
public class Bootstrap {

    public static Logger logger = LogManager.getLogger();
    public static ArrayList<JsonObject> modsInfo = new ArrayList<>();
    public static ArrayList<String> internalModClassNames = new ArrayList<>();
    public static boolean isRunning = true;
    public static Settings EMCSettings;
    public static String JSON_JARNAME_NOTE = "DYNAMIC_jarname";
    private static URLClassLoader modClassLoader;
    private static ConcurrentHashMap<String, EMCMod> mods = new ConcurrentHashMap<>();

    public static void init() {
        try {
            Bootstrap.logger.info("Loading EMC...");

            File emc_configs = new File(OSUtils.getMCDir() + "libraries" + File.separator + "EMC" + File.separator + IMinecraft.getMinecraftVersion() + File.separator + "configs" + File.separator);
            if (!emc_configs.exists()) {
                emc_configs.mkdirs();
            }

            // EMC mods are stored in .minecraft/libraries/EMC
            File emc_root = new File(OSUtils.getMCDir() + "libraries" + File.separator + "EMC" + File.separator + RealmsSharedConstants.VERSION_STRING + File.separator);
            if (!emc_root.exists()) {
                emc_root.mkdir();
            }

            // Load all EMC mods
            Arrays.stream(emc_root.listFiles()).forEach((file) -> {
                if (!file.isDirectory() && file.getName().endsWith(".jar")) {
                    try {
                        if (new File(file.getAbsolutePath() + ".delete").exists()) {
                            Bootstrap.logger.info("Deleting mod %s...", file.getName());
                            new File(file.getAbsolutePath() + ".delete").delete();
                        } else {
                            // Update check
                            File udpateJar = new File(emc_root.getAbsolutePath() + File.separator
                                    + file.getName().substring(0, file.getName().length() - ".jar".length())
                                    + "_update.jar");
                            if (udpateJar.exists()) {
                                file.delete();
                                udpateJar.renameTo(file);
                            }
                            // Load the mod
                            Bootstrap.loadMod(file);
                        }
                    } catch (Exception ex) {
                        Bootstrap.logger.warn("Failed to load EMC mod: " + file.getName());
                        ex.printStackTrace();
                    }
                }
            });

            EMCSettings = new Settings();
            EMCSettings.initialize(null);

            // Register default EMC commands
            registerFrameworkCommands();

            SettingsMap.update(SettingsMap.MapKeys.EMC_SETTINGS, "COMMAND_TRIGGER", EMCSettings.getString("commandtrigger", "."));

            // Initialize the EMC marketplace API
            MarketplaceAPI.init((status) -> Bootstrap.mods.forEach((name, mod) -> mod.onMarketplaceAuth(status)));
        } catch (Exception ex) {
            Bootstrap.logger.warn("Failed to load EMC", ex);
        }
    }

    /**
     * Registers framework commands
     */
    static void registerFrameworkCommands() {
        CommandRegister.registerCommand(new CommandMods());
        CommandRegister.registerCommand(new CommandUnload());
        CommandRegister.registerCommand(new CommandVersion());
        CommandRegister.registerCommand(new CommandHelp());
        CommandRegister.registerCommand(new CommandOAuth());
        CommandRegister.registerCommand(new CommandTrigger());
        CommandRegister.registerCommand(new CommandReload());
    }

    /**
     * Loads an EMC mod
     *
     * @param clientJar
     * @throws Exception
     */
    public static void loadMod(File clientJar) throws Exception {
        JarFile jarFile = new JarFile(clientJar);
        Bootstrap.modClassLoader = URLClassLoader.newInstance(
                new URL[]{new URL("jar", "", "file:" + clientJar.getAbsolutePath() + "!/")},
                Bootstrap.class.getClassLoader());

        // Read client.json
        BufferedReader buffer = new BufferedReader(
                new InputStreamReader(Bootstrap.modClassLoader.getResourceAsStream("client.json")));
        JsonObject jsonObject = new Gson().fromJson(buffer.lines().collect(Collectors.joining("\n")), JsonObject.class);

        //Add the name of Jar file to the corresponding jsonObject
        jsonObject.addProperty(JSON_JARNAME_NOTE, clientJar.getName());

        Bootstrap.modsInfo.add(jsonObject);

        //Inform about the mod being loaded
        Bootstrap.logger.info("Loading mod: " + jsonObject.get("name").getAsString()
                + " [ver. " + jsonObject.get("version").getAsString() + "] " +
                "by " + jsonObject.get("author").getAsString());

        // Version check
        if (jsonObject.get("minversion").getAsDouble() > FrameworkConstants.VERSION) {
            Minecraft.getInstance().displayGuiScreen(new GuiUpdateLoader(jsonObject));
            jarFile.close();
            return;
        }

        // Load classes
        Bootstrap.mods.put(jsonObject.get("name").getAsString(),
                (EMCMod) Bootstrap.modClassLoader.loadClass(jsonObject.get("main").getAsString()).newInstance());
        Enumeration<?> e = jarFile.entries();
        for (JarEntry je = (JarEntry) e.nextElement(); e.hasMoreElements(); je = (JarEntry) e.nextElement()) {
            if (je.isDirectory() || !je.getName().endsWith(".class")) {
                continue;
            }
            String className = je.getName().replace(".class", "").replace('/', '.');
            Bootstrap.logger.info("Loaded class " + Bootstrap.modClassLoader.loadClass(className).getName());
        }

        jarFile.close();
        Bootstrap.mods.get(jsonObject.get("name").getAsString()).init(jsonObject);
        Bootstrap.logger.info("Loaded mod");
        Bootstrap.mods.get(jsonObject.get("name").getAsString()).postInit();
    }

    /**
     * Call a function in another EMC mod from your mod, using this you can call functions across EMC mods
     *
     * @param mod    The name of the mod you want to talk with
     * @param method The method name you want to call
     * @param caller The name of your mod
     */
    public static void callMethod(String mod, String method, String caller) {
        if (Bootstrap.mods.containsKey(mod)) {
            Bootstrap.mods.get(mod).callMethod(method, caller);
        } else {
            Bootstrap.logger.error(String.format("EMC mod %s tried to call method %s in mod %s", caller, method, mod));
        }
    }

    /**
     * Returns a list of all loaded EMC mods
     */
    public static ConcurrentHashMap<String, EMCMod> getMods() {
        return Bootstrap.mods;
    }

    /**
     * Unloads all loaded EMC mods
     */
    public static void ejectMods() {
        Bootstrap.mods.forEach((key, mod) -> mod.onUnload());
        Bootstrap.mods.clear();

        //Remove all the commands the mods might have created
        RootCommandNode<?> root = CommandRegister.getDispatcher().getRoot();
        clearChildren(root);

        //Clear the command dispatcher
        CommandRegister.clearDispatcher();

        //Reinitialize the framework default commands
        registerFrameworkCommands();
    }

    /**
     * Loops around command nodes until all of them are removed
     * Clears the unnecessary data from the memory
     *
     * @param commandNode
     */
    private static void clearChildren(CommandNode<?> commandNode) {
        for (CommandNode<?> child : commandNode.getChildren()) {
            clearChildren(child);
        }
        commandNode.getChildren().clear();
    }

}
