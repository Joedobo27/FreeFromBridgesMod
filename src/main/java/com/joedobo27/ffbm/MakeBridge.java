package com.joedobo27.ffbm;

import com.joedobo27.libs.CardinalDirection;
import com.joedobo27.libs.TileUtilities;
import com.joedobo27.libs.action.ActionMaster;
import com.wurmonline.math.TilePos;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.structures.*;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.BridgeConstants;
import org.jetbrains.annotations.Nullable;


import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.WeakHashMap;
import java.util.stream.IntStream;

class MakeBridge extends ActionMaster {

    private final String bridgeQuestionAnswer;
    private final boolean onSurface;
    private final int heightOffset;
    private final int cardinalDirection;
    static WeakHashMap<Action, MakeBridge> actionDataWeakHashMap = new WeakHashMap<>();
    static private final byte BRIDGE_STRUCTURE_TYPE = 1;

    MakeBridge(Action action, Creature performer, @Nullable Item activeTool, @Nullable Integer usedSkill, int minSkill, int maxSkill,
               int longestTime, int shortestTime, String bridgeQuestionAnswer, boolean onSurface, int heightOffset, byte cardinalDirection){
        super(action, performer, activeTool, usedSkill, minSkill, maxSkill, longestTime, shortestTime);
       this.bridgeQuestionAnswer = bridgeQuestionAnswer;
       this.onSurface = onSurface;
       this.heightOffset = heightOffset;
       this.cardinalDirection = cardinalDirection;
    }

    static boolean hashMapHasInstance(Action action) {
        return actionDataWeakHashMap.containsKey(action);
    }

    boolean hasAFailureCondition() {

        TilePos performerOccupiedTile = TileUtilities.getPerformerOccupiedTile(this.performer);
        TilePos targetTile = CardinalDirection.offsetByOneCardinal(this.cardinalDirection, performerOccupiedTile);
        VolaTile volaTile = Zones.getOrCreateTile(targetTile, this.onSurface);
        boolean doesTileHaveBuild = volaTile.getStructure() != null && (volaTile.getStructure().isTypeHouse() ||
                volaTile.getBridgeParts() != null);
        if (doesTileHaveBuild) {
            this.performer.getCommunicator().sendNormalServerMessage("You can't plan a bridge on a tile occupied by another" +
                    " structure.");
            return true;
        }

        TilePos[] fenceCheckTiles = {targetTile, targetTile, targetTile.East(), targetTile.South()};
        Tiles.TileBorderDirection[] tileBorderDirections = {Tiles.TileBorderDirection.DIR_HORIZ, Tiles.TileBorderDirection.DIR_DOWN,
                Tiles.TileBorderDirection.DIR_DOWN, Tiles.TileBorderDirection.DIR_HORIZ};
        boolean doesTileHaveFences = IntStream.range(0, fenceCheckTiles.length)
                .anyMatch(value -> {
                    VolaTile volaTile1 = Zones.getOrCreateTile(fenceCheckTiles[value], performer.isOnSurface());
                    Fence[] fences = volaTile1.getFencesForDir(tileBorderDirections[value]);
                    return fences != null && fences.length > 0;
                });
        if (doesTileHaveFences) {
            this.performer.getCommunicator().sendNormalServerMessage("You can't plan a bridge on a tile occupied by a fence.");
            return true;
        }

        TilePos[] newBridgeCheckTiles = {targetTile.North(), targetTile.NorthEast(), targetTile.East(), targetTile.SouthEast(), targetTile.South(),
                targetTile.SouthWest(), targetTile.West(), targetTile.NorthWest()};
        VolaTile performerVolaTile = Zones.getOrCreateTile(performerOccupiedTile, performer.isOnSurface());
        Structure occupiedStructure = performerVolaTile.getStructure();
        boolean tooCloseToOtherBridge1 = (occupiedStructure == null ||
                occupiedStructure.getStructureType() != BRIDGE_STRUCTURE_TYPE) &&
                IntStream.range(0, newBridgeCheckTiles.length)
                        .anyMatch(value -> {
                            VolaTile volaTile1 = Zones.getOrCreateTile(newBridgeCheckTiles[value], performer.isOnSurface());
                            Structure structure = volaTile1.getStructure();
                            return structure != null && structure.getStructureType() == BRIDGE_STRUCTURE_TYPE;
                        });
        if (tooCloseToOtherBridge1){
            this.performer.getCommunicator().sendNormalServerMessage("Another bridge's proximity is too close.");
            return true;
        }

        boolean tooCloseToOtherBridge2 = (occupiedStructure != null &&
                occupiedStructure.getStructureType() == BRIDGE_STRUCTURE_TYPE) &&
                IntStream.range(0, newBridgeCheckTiles.length)
                        .anyMatch(value -> {
                            VolaTile volaTile1 = Zones.getOrCreateTile(newBridgeCheckTiles[value], performer.isOnSurface());
                            Structure structure = volaTile1.getStructure();
                            Long foundId = null;
                            if (structure != null && structure.getStructureType() == BRIDGE_STRUCTURE_TYPE){
                                foundId = structure.getWurmId();
                            }
                            return foundId != null && occupiedStructure.getWurmId() != foundId;
                        });
        if (tooCloseToOtherBridge2){
            this.performer.getCommunicator().sendNormalServerMessage("Another bridge's proximity is too close.");
            return true;
        }

        return false;
    }

    static boolean isBridgeMakeTool(Item activeItem) {
        if (activeItem == null)
            return false;
        boolean toReturn = false;
        switch (activeItem.getTemplateId()) {
            case ItemList.hammerMetal:
                toReturn = true;
                break;
            case ItemList.hammerWood:
                toReturn = true;
                break;
            case ItemList.trowel:
                toReturn= true;
                break;
        }
        if (activeItem.isWand())
            toReturn = true;
        return toReturn;
    }

    static boolean isTargetingOccupiedTile(Creature performer, int tileX, int tileY) {
        TilePos performerTile = TileUtilities.getPerformerOccupiedTile(performer);
        TilePos targetTile = TilePos.fromXY(tileX, tileY);
        return performerTile.equals(targetTile);
    }

    void doConstruction() {
        TilePos performerOccupiedTile = TileUtilities.getPerformerOccupiedTile(this.performer);
        VolaTile performerVolaTile = Zones.getOrCreateTile(performerOccupiedTile, this.performer.isOnSurface());
        TilePos createTile = CardinalDirection.offsetByOneCardinal(this.cardinalDirection, performerOccupiedTile);
        VolaTile createVolaTile = Zones.getOrCreateTile(createTile, this.performer.isOnSurface());

        Object[] objects = this.getJsonData();
        BridgeConstants.BridgeType bridgeType = (BridgeConstants.BridgeType) objects[0];
        float qualityLevel = (float) objects[1];
        long attachedStructureId = getAttachedStructureId();
        BridgeConstants.BridgeMaterial bridgeMaterial = (BridgeConstants.BridgeMaterial) objects[2];
        byte direction = (byte) objects[3]; // 0 to 7 value, not sure what each does.
        byte slope = (byte) objects[4];
        int northExitHeight = (int) objects[5]; // for this and the other exit values -1 means no exit.
        int eastExitHeight = (int) objects[6];
        int southExitHeight = (int) objects[7];
        int westExitHeight = (int) objects[8];
        byte roadType = 0;
        int layer = this.performer.getLayer(); // 0 is surface.

        Structure structure = null;
        if (performerVolaTile.getStructure() == null || performerVolaTile.getStructure().getStructureType() != BRIDGE_STRUCTURE_TYPE) {
            structure = Structures.createStructure(BRIDGE_STRUCTURE_TYPE, this.performer.getName() +
                    "'s planned bridge", WurmId.getNextPlanId(), createTile.x, createTile.y, this.performer.isOnSurface());
            this.performer.getStatus().setBuildingId(structure.getWurmId());
        }
        else if (performerVolaTile.getStructure().getStructureType() == BRIDGE_STRUCTURE_TYPE) {
            structure = performerVolaTile.getStructure();
            try {
                Structure.expandStructureToTile(structure, createVolaTile);
            }catch (NoSuchZoneException e){
                return;
            }
        }
        if (structure == null) {
            return;
        }
        createVolaTile.addBridge(structure);

        BridgePart bridgePart = new DbBridgePart(bridgeType, createTile.x, createTile.y,
                this.heightOffset, qualityLevel, attachedStructureId, bridgeMaterial,
                direction, slope, northExitHeight, eastExitHeight, southExitHeight, westExitHeight, roadType, layer);
        try {
            createVolaTile.addBridgePart(bridgePart);
        } catch (Exception e){
            return;
        }
        try {
            structure.makeFinal(this.performer, this.performer.getName() + "'s ");
        } catch (NoSuchZoneException | IOException e) {
            return;
        }
    }

    private Object[] getJsonData() {
        BridgeConstants.BridgeType bridgeType; // need to be converted to WU BridgeType object. and # match enum Ids
        float qualityLevel;
        BridgeConstants.BridgeMaterial bridgeMaterial; // needs to be converted to WU BridgeMaterial object. and # match enum Ids
        byte direction; // 0 to 7 value, not sure what each does.
        byte slope; // unsure about ranges or its use.
        int northExitHeight;
        int eastExitHeight;
        int southExitHeight;
        int westExitHeight;

        Object[] toReturn = new Object[9];
        String keyName;
        JsonParserFactory jpf = Json.createParserFactory(null);
        JsonParser jsonParser = jpf.createParser(new StringReader(this.bridgeQuestionAnswer));
        while (jsonParser.hasNext()) {
            JsonParser.Event event = jsonParser.next();
            if (event == JsonParser.Event.START_OBJECT || event == JsonParser.Event.END_OBJECT)
                continue;
            if (event != JsonParser.Event.KEY_NAME)
                break;
            keyName = jsonParser.getString();
            switch (keyName) {
                case "bridgeType":
                    event = jsonParser.next();
                    if (event != JsonParser.Event.VALUE_NUMBER)
                        break;
                    byte type = (byte) jsonParser.getInt();
                    bridgeType = BridgeConstants.BridgeType.fromByte(type);
                    toReturn[0] = bridgeType;
                    break;
                case "qualityLevel":
                    event = jsonParser.next();
                    if (event != JsonParser.Event.VALUE_NUMBER)
                        break;
                    qualityLevel = (float)jsonParser.getLong();
                    toReturn[1] = qualityLevel;
                    break;
                case "bridgeMaterial":
                    event = jsonParser.next();
                    if (event != JsonParser.Event.VALUE_NUMBER)
                        break;
                    byte material = (byte) jsonParser.getInt();
                    bridgeMaterial = BridgeConstants.BridgeMaterial.fromByte(material);
                    toReturn[2] = bridgeMaterial;
                    break;
                case "direction":
                    event = jsonParser.next();
                    if (event != JsonParser.Event.VALUE_NUMBER)
                        break;
                    direction = (byte) jsonParser.getInt();
                    toReturn[3] = direction;
                    break;
                case "slope":
                    event = jsonParser.next();
                    if (event != JsonParser.Event.VALUE_NUMBER)
                        break;
                    slope = (byte) jsonParser.getInt();
                    toReturn[4] = slope;
                    break;
                case "northExitHeight":
                    event = jsonParser.next();
                    if (event != JsonParser.Event.VALUE_NUMBER)
                        break;
                    northExitHeight = jsonParser.getInt();
                    toReturn[5] = northExitHeight;
                    break;
                case "eastExitHeight":
                    event = jsonParser.next();
                    if (event != JsonParser.Event.VALUE_NUMBER)
                        break;
                    eastExitHeight = jsonParser.getInt();
                    toReturn[6] = eastExitHeight;
                    break;
                case "southExitHeight":
                    event = jsonParser.next();
                    if (event != JsonParser.Event.VALUE_NUMBER)
                        break;
                    southExitHeight = jsonParser.getInt();
                    toReturn[7] = southExitHeight;
                    break;
                case "westExitHeight":
                    event = jsonParser.next();
                    if (event != JsonParser.Event.VALUE_NUMBER)
                        break;
                    westExitHeight = jsonParser.getInt();
                    toReturn[8] = westExitHeight;
                    break;
            }
        }
        return toReturn;
    }

    private long getAttachedStructureId() {
        try{return this.performer.getStructure().getWurmId();}catch (NoSuchStructureException e) {return -10L;}
    }
}
