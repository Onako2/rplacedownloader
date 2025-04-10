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

package rs.onako2.placedownloader.privacy;

import rs.onako2.placedownloader.PlaceDownloaderClient;
import rs.onako2.placedownloader.json.SettingsJson;
import rs.onako2.placedownloader.json.SettingsServerEntry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

import static rs.onako2.placedownloader.PlaceDownloaderClient.SETTINGS_FILE;
import static rs.onako2.placedownloader.PlaceDownloaderClient.gson;

public class PrivacyUtils {

    private static final String BASE_AGENT = "PlaceDownloader (%s, x:%s, z:%s) by Onako2";
    public static int allowCoords = -1;

    public static String getUserAgent(String username, int x, int z) {
        if (allowCoords == 0) {
            return BASE_AGENT.formatted(username, x, z);
        } else {
            return BASE_AGENT.formatted(username, "<redacted>", "<redacted>");
        }
    }

    public static void agree() throws IOException {
        String settingsString = Files.readString(SETTINGS_FILE.toPath());
        SettingsJson settings = gson.fromJson(settingsString, SettingsJson.class);
        settings.privacy = 0;
        Files.writeString(SETTINGS_FILE.toPath(), gson.toJson(settings, SettingsJson.class), StandardCharsets.UTF_8);
        PrivacyUtils.load();
    }

    public static void disAgree() throws IOException {
        String settingsString = Files.readString(SETTINGS_FILE.toPath());
        SettingsJson settings = gson.fromJson(settingsString, SettingsJson.class);
        settings.privacy = 1;
        Files.writeString(SETTINGS_FILE.toPath(), gson.toJson(settings, SettingsJson.class), StandardCharsets.UTF_8);
        PrivacyUtils.load();
    }

    public static void load() throws IOException {
        String settingsString = Files.readString(SETTINGS_FILE.toPath());
        SettingsJson settings = gson.fromJson(settingsString, SettingsJson.class);
        PlaceDownloaderClient.servers = new ArrayList<>();
        PlaceDownloaderClient.servers.addAll(Arrays.stream(settings.servers).toList());
        PlaceDownloaderClient.servers.add(new SettingsServerEntry("https://nuc.de.majic.rs/rplace/testing.json", "Test-Server"));

        allowCoords = settings.privacy;
    }
}
