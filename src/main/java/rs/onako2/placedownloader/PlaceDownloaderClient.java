/**
 * Copyright (C) 2025  Nebojša Majić (Onako2)
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see
 * <https://www.gnu.org/licenses/>.
 */

package rs.onako2.placedownloader;

import com.google.gson.Gson;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.onako2.placedownloader.json.SettingsJson;
import rs.onako2.placedownloader.json.SettingsServerEntry;
import rs.onako2.placedownloader.privacy.PrivacyScreen;
import rs.onako2.placedownloader.privacy.PrivacyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static rs.onako2.placedownloader.Manager.mc;

public class PlaceDownloaderClient implements ClientModInitializer {
    
    public static final String MOD_ID = "placedownloader";
    public static final String PATH = "placedownloader/";
    public static final File SETTINGS_FILE = new File(PATH + "settings.json");
    public static final Gson gson = new Gson();
    public static int timer;
    public static boolean setScreen = false;
    public static List<SettingsServerEntry> servers;
    public static final Logger LOGGER = LoggerFactory.getLogger("placedownloader");
    
    @Override
    public void onInitializeClient() {
        
        File base = new File(PATH);
        
        base.mkdirs();
        
        try {
            if (SETTINGS_FILE.createNewFile()) {
                
                SettingsServerEntry[] servers = new SettingsServerEntry[]{
                        new SettingsServerEntry("https://example.local/example.json", "Example Server")
                };
                
                SettingsJson defaultSettings = new SettingsJson(servers, 0, -1);
                
                Files.writeString(SETTINGS_FILE.toPath(), gson.toJson(defaultSettings, SettingsJson.class), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        try {
            PrivacyUtils.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("schematics").executes(context -> {
                        Manager.refresh();
                        return 0;
                    }));
            dispatcher.register(
                    ClientCommandManager.literal("placeprivacy").executes(context -> {
                        setScreen = true;
                        return 0;
                    }));
        });
        
        ClientTickEvents.END_WORLD_TICK.register(client -> {
            timer++;
            if (timer >= 6000) {
                timer = 0;
                Manager.refresh();
            }
            if (setScreen) {
                setScreen = false;
                mc.setScreen(new PrivacyScreen(mc.currentScreen));
            }
        });
    }
}
