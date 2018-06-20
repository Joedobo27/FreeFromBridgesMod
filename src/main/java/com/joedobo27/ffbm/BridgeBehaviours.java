package com.joedobo27.ffbm;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;

import java.util.Collections;
import java.util.List;

public class BridgeBehaviours implements ModAction, BehaviourProvider {

    BridgeBehaviours(){}

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item activeItem, int tileX, int tileY, boolean onSurface,
                                              int encodedTile) {
        if (!MakeBridge.isBridgeMakeTool(activeItem) || !MakeBridge.isTargetingOccupiedTile(performer, tileX, tileY))
            return BehaviourProvider.super.getBehavioursFor(performer, activeItem, tileX, tileY, onSurface, encodedTile);
        return Collections.singletonList(FreeFormBridgesOptions.makeBridgeTileAction.actionEntry);
    }

    /**
     * Plan a bridge tile.
     *
     * @param performer Player that did something.
     * @param activeItem Item player had active when that something was done.
     * @param tileX Targeted tile location X.
     * @param tileY Targeted tile location Y.
     * @param onSurface Is player on surface? Or is {@code performer.getLayer() == 0}
     * @param borderDirection Constant from this field type's enum.
     * @param border Has targeted tile border.
     * @param heightOffset Height of the NW tile corner associated with the border/corner.
     * @return
     */
    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item activeItem, int tileX, int tileY, boolean onSurface,
                                              Tiles.TileBorderDirection borderDirection, boolean border, int heightOffset) {
        if (!MakeBridge.isBridgeMakeTool(activeItem) || !border)
            return BehaviourProvider.super.getBehavioursFor(performer, activeItem, tileX, tileY, onSurface, borderDirection,
                    border, heightOffset);
        return Collections.singletonList(FreeFormBridgesOptions.makeBridgeTileAction.actionEntry);
    }
}
