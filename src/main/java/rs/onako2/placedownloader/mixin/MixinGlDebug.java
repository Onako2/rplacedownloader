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

import net.minecraft.client.gl.GlDebug;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(GlDebug.class)
public class MixinGlDebug {
    @Unique
    private static final String GL_VENDOR = GL11.glGetString(GL11.GL_VENDOR);
    
    @Inject(method = "info", at = @At("HEAD"), cancellable = true)
    private static void info(int source, int type, int id, int severity, int messageLength, long message, long l, CallbackInfo ci) {
        // Debug Logging has to be off because users trying to fix some issue should have the option to read this
        if (Objects.equals(GL_VENDOR, "Intel") && id == 1282) {
            ci.cancel();
        }
    }
}
