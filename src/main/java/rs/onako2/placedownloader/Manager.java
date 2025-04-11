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

import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.text.Text;
import rs.onako2.placedownloader.compat.litematica.SchematicUtils;
import rs.onako2.placedownloader.json.SchematicEntry;
import rs.onako2.placedownloader.json.SchematicJson;
import rs.onako2.placedownloader.privacy.PrivacyUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static rs.onako2.placedownloader.PlaceDownloaderClient.gson;
import static rs.onako2.placedownloader.PlaceDownloaderClient.servers;

public class Manager {
    public static final MinecraftClient mc = MinecraftClient.getInstance();
    public static boolean mayExecute = true;
    private static boolean hasDownloaded;

    public static void forceRefresh() {
        PlayerEntity player = mc.player;

        if (player == null) {
            return;
        }

        if (mc.player.clientWorld.getScoreboard().getScoreHolderObjectives(ScoreHolder.fromName("Fortschritt")).isEmpty()) {
            return;
        }

        hasDownloaded = false;

        CompletableFuture.runAsync(() -> {
            mayExecute = false;
            servers.forEach(serverEntry -> {
                if (Objects.equals(serverEntry.url, "https://example.local/example.json")) return;
                URL urlServer;
                try {
                    urlServer = new URI(serverEntry.url).toURL();

                    HttpURLConnection conServer = download(player, urlServer);

                    BufferedReader in = new BufferedReader(new InputStreamReader(conServer.getInputStream()));
                    String inputLine;
                    StringBuffer content = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();

                    String json = content.toString();


                    SchematicJson schematicJson = gson.fromJson(json, SchematicJson.class);
                    SchematicEntry[] schematics = schematicJson.schematics;
                    Arrays.stream(schematics).toList().forEach(schematicEntry -> {

                        boolean mayLoad = false;

                        // check if file size matches
                        File file = new File(PlaceDownloaderClient.PATH + schematicEntry.name + "." + schematicEntry.type);
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            player.sendMessage(Text.translatable("placedownloader.schematics.creating.error", schematicEntry.name, e.getMessage()), false);
                        }

                        if (file.length() != schematicEntry.size) {
                            if (!hasDownloaded) {
                                player.sendMessage(Text.translatable("placedownloader.schematics.downloading"), true);
                                hasDownloaded = true;
                            }

                            PlaceDownloaderClient.LOGGER.info((Text.translatable("placedownloader.schematics.downloading.schematic", schematicEntry.name).getString()));
                            try {
                                URL url = new URI(schematicEntry.url).toURL();
                                HttpURLConnection con = download(player, url);

                                Files.write(file.toPath(), con.getInputStream().readAllBytes());

                                mayLoad = true;
                                PlaceDownloaderClient.LOGGER.info((Text.translatable("placedownloader.schematics.downloaded.schematic", schematicEntry.name).getString()));
                            } catch (IOException | URISyntaxException e) {
                                e.printStackTrace();
                                player.sendMessage(Text.translatable("placedownloader.schematics.error", schematicEntry.name, e.getMessage()), false);
                            }
                        }

                        List<Integer> toBeRemoved = new ArrayList<>();

                        List<SchematicPlacement> schematicPlacements = new java.util.ArrayList<>(List.copyOf(SchematicUtils.getPlacements()));
                        if (mayLoad) {
                            schematicPlacements.forEach(placement -> {
                                if (placement != null && placement.getName().equals(schematicEntry.name)) {
                                    SchematicUtils.removePlacement(placement);
                                    toBeRemoved.add(schematicPlacements.indexOf(placement));
                                }
                            });
                        }

                        toBeRemoved.forEach(index -> {
                            int indexToRemove = index;
                            schematicPlacements.remove(indexToRemove);
                        });

                        if (schematicEntry.autoload && (!SchematicUtils.placementExists(schematicEntry.name, schematicPlacements) || mayLoad)) {
                            LitematicaSchematic schematic = SchematicUtils.loadSchematic(new File(PlaceDownloaderClient.PATH).getAbsoluteFile(), schematicEntry.name + "." + schematicEntry.type);
                            SchematicUtils.placeSchematic(schematic, schematicEntry.x, schematicEntry.y, schematicEntry.z, schematicEntry.name);
                        }
                    });
                } catch (IOException | URISyntaxException e) {
                    if (!Objects.equals(serverEntry.url, "https://example.local/example.json")) {
                        e.printStackTrace();
                        player.sendMessage(Text.translatable("placedownloader.schematics.error", serverEntry.name, e.getMessage()), false);
                    }
                }

            });
            mayExecute = true;
            if (hasDownloaded) {
                player.sendMessage(Text.translatable("placedownloader.schematics.downloaded"), true);
            }
        }).exceptionally(ex -> {
            mayExecute = true;
            player.sendMessage(Text.literal("Failed to download: " + ex.getMessage()), false);
            ex.printStackTrace();
            return null;
        });
    }

    public static void refresh() {
        PlayerEntity player = mc.player;

        if (!mayExecute) {
            player.sendMessage(Text.translatable("placedownloader.schematics.busy"), false);
        } else {
            forceRefresh();
        }
    }

    private static HttpURLConnection download(PlayerEntity player, URL urlServer) throws IOException {
        HttpURLConnection conServer = (HttpURLConnection) urlServer.openConnection();
        conServer.setRequestMethod("GET");
        conServer.setRequestProperty("User-Agent", PrivacyUtils.getUserAgent(player.getNameForScoreboard(), player.getBlockX(), player.getBlockZ()));
        conServer.setConnectTimeout(5000);
        conServer.setReadTimeout(5000);
        conServer.setInstanceFollowRedirects(true);
        return conServer;
    }
}
