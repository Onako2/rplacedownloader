package rs.onako2.placedownloader.mixin;

import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static rs.onako2.placedownloader.Manager.mayExecute;

@Mixin(value = SchematicPlacementManager.class, remap = false)
public class SchematicPlacementManagerMixin {
    @Inject(method = "processQueuedChunks()V", at = @At("HEAD"), cancellable = true)
    public void processQueuedChunks(CallbackInfo ci) {
        if (!mayExecute) {
            ci.cancel();
        }
    }
}
