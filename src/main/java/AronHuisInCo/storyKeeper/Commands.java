/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.jetbrains.annotations.NotNull
 */
package AronHuisInCo.storyKeeper;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Commands
implements CommandExecutor {
    private final StoryKeeper plugin;

    public Commands(StoryKeeper plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equals("storyReload")) {
            this.plugin.reloadConfig();
            commandSender.sendRichMessage("Перезагружается \ud83d\udea7");
        }
        if (command.getName().equals("msnget") && args.length > 1) {
            commandSender.sendMessage(this.plugin.missionGet(args[0], args[1]));
        }
        this.plugin.getLogger().warning("msn: " + Arrays.toString(args));
        if (command.getName().equals("msnset") && args.length > 2) {
            this.plugin.missionSet(args[0], args[1], args[2].equals("true"));
        }
        if (!(commandSender instanceof Player player)) {
            return true;
        }
        if (command.getName().equals("openMission") && args.length > 0) {
            this.plugin.getLogger().warning("gettedKey: " + args[0] + " truekey " + this.plugin.openMissionToken);
            if (args[0].equals(this.plugin.openMissionToken)) {
                this.plugin.openMission(player, args[1]);
            } else {
                commandSender.sendRichMessage("Нет :)");
            }
        }
        if (command.getName().equals("openMissions")) {
            this.plugin.openMissionsBook(player);
        }
        return true;
    }
}

