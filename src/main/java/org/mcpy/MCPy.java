package org.mcpy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.InteractiveInterpreter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MCPy extends JavaPlugin {
    private static final String SCRIPTS_FOLDER = "scripts";
    private static final String SCRIPTS_FOLDER_PATH = "plugins/MCPy/" + SCRIPTS_FOLDER;
    private final Map<String, InteractiveInterpreter> interpreters = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("MCPy plugin enabled.");
        File scriptsFolder = new File(SCRIPTS_FOLDER_PATH);
        if (!scriptsFolder.exists()) {
            scriptsFolder.mkdirs();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("MCPy plugin disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("mcpy")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Usage: /mcpy <load|reload>");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "load":
                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "Usage: /mcpy load <script_name>");
                        return true;
                    }

                    String scriptName = args[1];
                    if (interpreters.containsKey(scriptName)) {
                        interpreters.remove(scriptName);
                    }

                    try {
                        InteractiveInterpreter interpreter = createInterpreter();
                        String scriptPath = SCRIPTS_FOLDER_PATH + "/" + scriptName;
                        String scriptCode = Files.readString(Path.of(scriptPath));
                        interpreter.exec(scriptCode);
                        interpreters.put(scriptName, interpreter);
                        sender.sendMessage(ChatColor.GREEN + "Script " + scriptName + " loaded successfully.");
                    } catch (IOException e) {
                        sender.sendMessage(ChatColor.RED + "Error loading script: " + e.getMessage());
                    }
                    break;

                case "reload":
                    Bukkit.getPluginManager().disablePlugin(this);
                    Bukkit.getPluginManager().enablePlugin(this);
                    sender.sendMessage(ChatColor.GREEN + "MCPy plugin reloaded successfully.");
                    break;

                default:
                    sender.sendMessage(ChatColor.RED + "Usage: /mcpy <load|reload>");
                    break;
            }

            return true;
        }

        return false;
    }

    public void runScript(String scriptName, Player player, String[] args) {
        if (interpreters.containsKey(scriptName)) {
            InteractiveInterpreter interpreter = interpreters.get(scriptName);
            PySystemState state = interpreter.getSystemState();
            state.argv.clear();
            state.argv.append(new PyString(player.getName()));
            for (String arg : args) {
                state.argv.append(new PyString(arg));
            }

            try {
                interpreter.execfile(SCRIPTS_FOLDER_PATH + "/" + scriptName);
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Error running script: " + e.getMessage());
            }
        } else {
            player.sendMessage(ChatColor.RED + "Script not loaded.");
        }
    }

    private InteractiveInterpreter createInterpreter() {
        PySystemState state = new PySystemState();
        state.path.append(new PyString(SCRIPTS_FOLDER_PATH));
        return new InteractiveInterpreter(null, state);
    }
}