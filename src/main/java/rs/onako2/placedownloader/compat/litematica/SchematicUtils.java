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

package rs.onako2.placedownloader.compat.litematica;

import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import fi.dy.masa.litematica.util.FileType;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SchematicUtils {
    public static SchematicPlacementManager placementManager = new SchematicPlacementManager();

    public static LitematicaSchematic loadSchematic(File directory, String filename) {
        FileType fileType = FileType.fromName(filename);
        return LitematicaSchematic.createFromFile(directory, filename, fileType);
    }

    public static SchematicPlacement placeSchematic(LitematicaSchematic schematic, int x, int y, int z, String name) {
        SchematicPlacement placement = SchematicPlacement.createFor(schematic, new BlockPos(x, y, z), name, true, true);
        placementManager.addSchematicPlacement(placement, true);
        return placement;
    }

    public static List<SchematicPlacement> getPlacements() {
        return placementManager.getAllSchematicsPlacements();
    }

    public static void removePlacement(SchematicPlacement placement) {
        placement.setEnabled(false);
        placementManager.removeSchematicPlacement(placement, true);
    }

    public static boolean placementExists(String name, List<SchematicPlacement> placements) {
        AtomicBoolean exists = new AtomicBoolean(false);
        placements.forEach(placement -> {
            if (placement.getName().equals(name)) {
                exists.set(true);
            }
        });
        return exists.get();
    }
}
