package com.littlesheep.tprandom;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
public class Main extends JavaPlugin {
    private FileConfiguration langConfig;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadLangFile();
        loadLangFile();
        getLogger().info("==========================================");
        getLogger().info(getDescription().getName());
        getLogger().info("Version/版本: " + getDescription().getVersion());
        getLogger().info("Author/作者: " + String.join(", ", getDescription().getAuthors()));
        getLogger().info("QQ Group/QQ群: 690216634");
        getLogger().info("Github: https://github.com/znc15/tprandom");
        getLogger().info("Tprandom 已启用！");
        getLogger().info("❛‿˂̵✧");
        getLogger().info("==========================================");
        this.saveDefaultConfig();
        // 设置命令执行器
        this.getCommand("tpr").setExecutor(new RandomTeleportCommand(this));
    }
    public void reloadPluginConfig() {
        reloadConfig(); // 重新加载config.yml
        loadLangFile(); // 重新加载语言文件
    }

    private void loadLangFile() {
        String lang = getConfig().getString("language", "en");
        File langFile = new File(getDataFolder() + "/lang", "messages_" + lang + ".yml");
        if (!langFile.exists()) {
            saveResource("lang/messages_" + lang + ".yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getLangMessage(String path) {
        return langConfig.getString(path, "Missing language key/语言文件设置失败: " + path);
    }

    @Override
    public void onDisable() {
        getLogger().info("==========================================");
        getLogger().info("Goodbye! 插件已关闭。");
        getLogger().info("==========================================");
    }
}
