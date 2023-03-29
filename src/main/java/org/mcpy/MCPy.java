package org.mcpy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class MCPy extends JavaPlugin {

    private static final Logger log = Logger.getLogger("Minecraft");

    @Override
    public void onEnable() {
        // Create the PyScripts directory if it doesn't exist
        File pyScriptsDir = new File(getDataFolder(), "PyScripts");
        if (!pyScriptsDir.exists()) {
            pyScriptsDir.mkdirs();
        }
        log.info("MCPy plugin enabled.");
    }

    @Override
    public void onDisable() {
        log.info("MCPy plugin disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("MCPy")) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("load")) {
                    loadPyScript();
                    return true;
                } else if (args[0].equalsIgnoreCase("reload")) {
                    reloadPlugin();
                    return true;
                }
            }
        }
        return false;
    }

    private void loadPyScript() {
        // Load the first .py file found in the PyScripts directory
        File pyScriptsDir = new File(getDataFolder(), "PyScripts");
        File[] pyScriptFiles = pyScriptsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".py"));
        if (pyScriptFiles != null && pyScriptFiles.length > 0) {
            File pyScriptFile = pyScriptFiles[0];
            log.info("Loading Python script: " + pyScriptFile.getName());
            try {
                // Execute the Python script using the command line
                Process process = Runtime.getRuntime().exec("python " + pyScriptFile.getAbsolutePath());
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                }
                reader.close();
            } catch (Exception e) {
                log.warning("Failed to load Python script: " + e.getMessage());
            }
        } else {
            log.warning("No Python script found in PyScripts directory.");
        }
    }

    private void reloadPlugin() {
        log.info("Reloading MCPy plugin...");
        // Disable the plugin
        getServer().getPluginManager().disablePlugin(this);
        // Enable the plugin
        getServer().getPluginManager().enablePlugin(this);
        log.info("MCPy plugin reloaded.");
    }
}