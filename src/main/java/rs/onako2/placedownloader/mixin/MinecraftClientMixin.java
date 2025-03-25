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

package rs.onako2.placedownloader.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.ReconfiguringScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rs.onako2.placedownloader.PlaceDownloaderClient;
import rs.onako2.placedownloader.privacy.PrivacyScreen;
import rs.onako2.placedownloader.privacy.PrivacyUtils;

import static rs.onako2.placedownloader.compat.litematica.SchematicUtils.placementManager;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    @Nullable
    public Screen currentScreen;
    
    @Shadow
    public abstract void setScreen(@Nullable Screen screen);
    
    @Inject(at = @At("TAIL"), method = "setScreen")
    public void setScreen(@Nullable Screen screen, CallbackInfo ci) {
        if (screen instanceof TitleScreen && PrivacyUtils.allowCoords != 0 && PrivacyUtils.allowCoords != 1) {
            this.setScreen(new PrivacyScreen(currentScreen));
        }
        if (screen instanceof ReconfiguringScreen || screen instanceof DownloadingTerrainScreen) {
            // remove all schematics that might not be used anymore because we will readd them anyways
            placementManager.getAllSchematicsPlacements().removeAll(placementManager.getAllSchematicsPlacements());
            // schedule loading the schematics to 10s
            PlaceDownloaderClient.timer = 5800;
        }
    }
}
