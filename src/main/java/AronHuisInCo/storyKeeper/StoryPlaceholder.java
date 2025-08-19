/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.clip.placeholderapi.expansion.PlaceholderExpansion
 *  org.bukkit.OfflinePlayer
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package AronHuisInCo.storyKeeper;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class StoryPlaceholder
extends PlaceholderExpansion {
    private final StoryKeeper plugin;

    public StoryPlaceholder(StoryKeeper plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public String getIdentifier() {
        return "StoryKeep";
    }

    @NotNull
    public String getAuthor() {
        return "AronHuisInCo";
    }

    @NotNull
    public String getVersion() {
        return "4.2";
    }

    public boolean canRegister() {
        return true;
    }

    public boolean persist() {
        return true;
    }

    @Nullable
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        Object[] args = params.split("_");
        this.plugin.getLogger().warning("params: " + params);
        this.plugin.getLogger().warning("args " + Arrays.toString(args));
        if (args.length == 2 && ((String)args[0]).equalsIgnoreCase("msnget")) {
            return this.plugin.missionGet(player.getName(), (String)args[1]);
        }
        if (args.length == 3 && ((String)args[0]).equalsIgnoreCase("msnset")) {
            this.plugin.missionSet(player.getName(), (String)args[1], args[2].equals("true"));
        }
        if (((String)args[0]).equalsIgnoreCase("openToken")) {
            return this.plugin.getConfig().getString("openToken");
        }
        return "";
    }
}

