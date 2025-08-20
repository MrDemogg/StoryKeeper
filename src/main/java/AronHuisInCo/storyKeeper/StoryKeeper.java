/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.clip.placeholderapi.PlaceholderAPI
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent$Builder
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.minimessage.MiniMessage
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.entity.PlayerDeathEvent
 *  org.bukkit.event.player.PlayerAttemptPickupItemEvent
 *  org.bukkit.event.player.PlayerDropItemEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.event.player.PlayerRespawnEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.BookMeta
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataContainer
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 */
package AronHuisInCo.storyKeeper;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Consumer;

public final class StoryKeeper
extends JavaPlugin
implements Listener {
    private NamespacedKey storyBookKey;
    public String openMissionToken;
    private int MAX_LINES = 14;
    private String SPLIT_FORMAT = "";
    private PlayerDataConfig playerConfig;

    public void reload()
    {
        this.reloadConfig();
        this.playerConfig.reload();
        this.openMissionToken = this.getConfig().getString("openToken");
        this.MAX_LINES = this.getConfig().getInt("maxLines");
        this.SPLIT_FORMAT = this.getConfig().getString("splitFormat");
    }
    public void onEnable() {
        this.saveDefaultConfig();
        this.playerConfig = new PlayerDataConfig(this);
        this.storyBookKey = new NamespacedKey(this, "storyBook");
        reload();
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.getLogger().warning("PlaceholderAPI not found, disabling plugin");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getPluginManager().registerEvents(this, this);
        new StoryPlaceholder(this).register();
        Commands commandsExecutor = new Commands(this);
        Objects.requireNonNull(this.getCommand("storyReload")).setExecutor(commandsExecutor);
        Objects.requireNonNull(this.getCommand("openmission")).setExecutor(commandsExecutor);
        Objects.requireNonNull(this.getCommand("openmissions")).setExecutor(commandsExecutor);
        Objects.requireNonNull(this.getCommand("msnget")).setExecutor(commandsExecutor);
        Objects.requireNonNull(this.getCommand("msnset")).setExecutor(commandsExecutor);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        this.giveStoryBook(e.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        this.giveStoryBook(e.getPlayer());
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (this.isStoryBook(e.getItemDrop().getItemStack())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(PlayerAttemptPickupItemEvent e) {
        if (this.isStoryBook(e.getItem().getItemStack())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        e.getDrops().removeIf(this::isStoryBook);
    }

    private boolean isStoryBook(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(this.storyBookKey);
    }

    public void giveStoryBook(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (!this.isStoryBook(item)) continue;
            return;
        }
        ItemStack book = new ItemStack(Material.KNOWLEDGE_BOOK);
        ItemMeta meta = book.getItemMeta();
        meta.displayName(Component.text("Дневник", NamedTextColor.GREEN));
        meta.getPersistentDataContainer().set(this.storyBookKey, PersistentDataType.BYTE, (byte)1);
        book.setItemMeta(meta);
        player.getInventory().addItem(book);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.KNOWLEDGE_BOOK) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        PersistentDataContainer c = meta.getPersistentDataContainer();
        if (!c.has(this.storyBookKey, PersistentDataType.BYTE)) {
            return;
        }
        e.setCancelled(true);
        getLogger().warning("OPEN_MISSION_BOOK");
        this.openMissionsBook(e.getPlayer());
    }

    private void openBook(Player player, Consumer<BookMeta> consumer) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta)book.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.setTitle("Задания");
        meta.setAuthor("StoryKeeper");
        consumer.accept(meta);
        book.setItemMeta(meta);
        player.openBook(book);
    }

    public void openMissionsBook(Player player) {
        this.giveStoryBook(player);
        this.openBook(player, bookMeta -> {
            ConfigurationSection cats = this.getConfig().getConfigurationSection("missionCategories");
            ConfigurationSection ms = this.getConfig().getConfigurationSection("missions");
            if (cats == null || ms == null) return;

            LinkedHashMap<String, List<List<String>>> grouped = new LinkedHashMap<>();
            for (String catKey : cats.getKeys(false)) {
                grouped.put(catKey, new ArrayList<>());
            }
            grouped.put("Миссии", new ArrayList<>());

            // Заменяем получение данных игрока
            Set<String> missionKeys = playerConfig.getKeys("players." + player.getName());
            if (missionKeys.isEmpty()) return;

            for (String missionKey : missionKeys) {
                if (!ms.contains(missionKey)) continue;
                String cat = ms.getString(missionKey + ".category", "Миссии");
                if (!grouped.containsKey(cat)) grouped.put(cat, new ArrayList<>());

                List<String> titles = new ArrayList<>(ms.getStringList(missionKey + ".titles"));
                // Заменяем получение статуса миссии
                boolean done = playerConfig.getBoolean("players." + player.getName() + "." + missionKey, false);
                if (done) {
                    titles.replaceAll(s -> "<st>" + s + "</st>");
                }

                String cmd = "/openmission " + this.openMissionToken + " " + missionKey;
                List<String> wrapped = new ArrayList<>();
                for (String t : titles) {
                    String withClick = "<click:run_command:'" + cmd + "'>" + t + "</click>";
                    wrapped.add(PlaceholderAPI.setPlaceholders(player, withClick));
                }
                grouped.get(cat).add(wrapped);
            }

            for (Map.Entry<String, List<List<String>>> entry : grouped.entrySet()) {
                if (entry.getValue().isEmpty()) continue;
                List<String> header = this.getConfig().getStringList("missionCategories." + entry.getKey());
                if (header.isEmpty()) header = Collections.singletonList(entry.getKey());

                List<Component> pages = this.buildBookPages(header, entry.getValue(), Collections.emptyList(), true);
                if (!pages.isEmpty()) bookMeta.addPages(pages.toArray(new Component[0]));
            }
        });
    }

    public void openMission(Player player, String key) {
        this.giveStoryBook(player);
        this.openBook(player, bookMeta -> {
            ConfigurationSection ms = this.getConfig().getConfigurationSection("missions");
            if (ms == null || !ms.contains(key)) return;

            List<String> titles = new ArrayList<>(ms.getStringList(key + ".titles"));
            List<String> description = ms.getStringList(key + ".description");

            titles.replaceAll(s -> PlaceholderAPI.setPlaceholders(player, s));
            description.replaceAll(s -> PlaceholderAPI.setPlaceholders(player, s));

            List<String> footer = Arrays.asList(
                    "",
                    "<click:run_command:'/openmissions'>Назад</click>"
            );

            // Преобразуем описание в список блоков (один блок)
            List<List<String>> content = Collections.singletonList(description);
            List<Component> pages = this.buildBookPages(titles, content, footer, false);
            if (!pages.isEmpty()) bookMeta.addPages(pages.toArray(new Component[0]));
        });
    }

    public String missionGet(String playerName, String missionName) {
        String v = this.playerConfig.getString("players." + playerName + "." + missionName);
        return v == null ? "no" : v;
    }

    public void missionSet(String playerName, String missionName, boolean finished) {
        this.playerConfig.set("players." + playerName + "." + missionName, finished);
        this.saveConfig();
    }

    private List<Component> buildBookPages(List<String> header, List<List<String>> content, List<String> footer, boolean repeatHeader) {
        ArrayList<Component> pages = new ArrayList<>();
        MiniMessage mm = MiniMessage.miniMessage();
        int footerSize = footer != null ? footer.size() + 1 : 0; // +1 для пустой строки перед футером

        TextComponent.Builder currentPage = Component.text();
        int currentLineCount = 0;

        // Добавляем заголовок на первую страницу
        for (String headerLine : header) {
            currentPage.append(mm.deserialize(headerLine)).append(Component.newline());
            currentLineCount++;
        }
        if (!header.isEmpty()) {
            currentPage.append(Component.newline());
            currentLineCount++;
        }

        // Обрабатываем контент
        for (int i = 0; i < content.size(); i++) {
            List<String> block = content.get(i);
            boolean isLastBlock = (i == content.size() - 1);

            for (String line : block) {
                // Проверяем, нужно ли создать новую страницу
                int linesNeeded = 1;
                if (isLastBlock) {
                    linesNeeded += footerSize;
                }

                if ((currentLineCount + linesNeeded) > (MAX_LINES - 2)) {
                    pages.add(currentPage.build());
                    currentPage = Component.text();
                    currentLineCount = 0;

                    // Повторяем заголовок если нужно
                    if (repeatHeader && !header.isEmpty()) {
                        for (String headerLine : header) {
                            currentPage.append(mm.deserialize(headerLine)).append(Component.newline());
                            currentLineCount++;
                        }
                        currentPage.append(Component.newline());
                        currentLineCount++;
                    }
                }

                currentPage.append(mm.deserialize(line)).append(Component.newline());
                if (!SPLIT_FORMAT.isEmpty()) {
                    currentPage.append(mm.deserialize(SPLIT_FORMAT)).append(Component.newline());
                    currentLineCount++;
                }
                currentLineCount++;
            }
        }

        // Добавляем футер
        if (footer != null && !footer.isEmpty()) {
            if (currentLineCount + footerSize > MAX_LINES) {
                pages.add(currentPage.build());
                currentPage = Component.text();
            }

            currentPage.append(Component.newline());
            for (String footerLine : footer) {
                currentPage.append(mm.deserialize(footerLine)).append(Component.newline());
            }
        }

        pages.add(currentPage.build());
        return pages;
    }
}

