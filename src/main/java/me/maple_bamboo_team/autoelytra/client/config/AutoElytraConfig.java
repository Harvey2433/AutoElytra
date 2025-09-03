package me.maple_bamboo_team.autoelytra.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AutoElytraConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("autoelytra.json");

    private boolean autoExitEnabled = false;
    private int maxDurabilityThreshold = 30; // 最大耐久临界值

    public boolean isAutoExitEnabled() {
        return autoExitEnabled;
    }

    public void setAutoExitEnabled(boolean autoExitEnabled) {
        this.autoExitEnabled = autoExitEnabled;
        save();
    }

    public int getMaxDurabilityThreshold() {
        return maxDurabilityThreshold;
    }

    public void setMaxDurabilityThreshold(int maxDurabilityThreshold) {
        this.maxDurabilityThreshold = maxDurabilityThreshold;
        save();
    }

    public static AutoElytraConfig load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                return GSON.fromJson(json, AutoElytraConfig.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new AutoElytraConfig();
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}