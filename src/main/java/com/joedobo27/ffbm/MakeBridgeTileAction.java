package com.joedobo27.ffbm;


import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;

import java.util.WeakHashMap;

public class MakeBridgeTileAction implements ModAction, ActionPerformer {

    final ActionEntry actionEntry;
    final Short actionId;
    private static WeakHashMap<Action, BridgeQuestion> questionWeakHashMap = new WeakHashMap<>();

    MakeBridgeTileAction(Short actionId, ActionEntry actionEntry) {
        this.actionEntry = actionEntry;
        this.actionId = actionId;
    }

    @Override
    public short getActionId() {
        return this.actionId;
    }

    @Override
    public boolean action(Action action, Creature performer, Item activeItem, int tileX, int tileY, boolean onSurface,
                          int heightOffset, int tile, short actionId, float counter) {
        if (this.actionId != actionId || !MakeBridge.isBridgeMakeTool(activeItem)) {
            ActionPerformer.super.action(action, performer, activeItem, tileX, tileY, onSurface, heightOffset,
                    tile, actionId, counter);
            return false;
        }
        BridgeQuestion bridgeQuestion;
        if (questionWeakHashMap.get(action) == null){
            bridgeQuestion = new BridgeQuestion(performer, "Bridge Data",
                    "Supply Json bridge data.", 513, performer.getWurmId());
            questionWeakHashMap.put(action, bridgeQuestion);
        }
        else
            bridgeQuestion = questionWeakHashMap.get(action);
        if (!bridgeQuestion.isAnswered())
            return false;

        MakeBridge makeBridge;
        if (!MakeBridge.hashMapHasInstance(action)) {
            makeBridge = new MakeBridge(action, performer, null, null, 99, 99,
                    10, 10, bridgeQuestion.getAnswer(), onSurface, heightOffset,
                    performer.getStatus().getDir());
        }
        else
            makeBridge = MakeBridge.actionDataWeakHashMap.get(action);

        if (makeBridge.hasAFailureCondition())
            return true;
        makeBridge.doConstruction();
        return true;
    }

    @Override
    public boolean action(Action action, Creature performer, Item activeItem, int tileX, int tileY, boolean onSurface,
                          int heightOffset, Tiles.TileBorderDirection borderDirection, long borderId, short actionId,
                          float counter) {
        if (this.actionId != actionId || !MakeBridge.isBridgeMakeTool(activeItem)) {
            ActionPerformer.super.action(action, performer, activeItem, tileX, tileY, onSurface, heightOffset,
                    borderDirection, borderId, actionId, counter);
            return false;
        }

        BridgeQuestion bridgeQuestion;
        if (questionWeakHashMap.get(action) == null){
            bridgeQuestion = new BridgeQuestion(performer, "Bridge Data",
                    "Supply Json bridge data.", 513, performer.getWurmId());
            questionWeakHashMap.put(action, bridgeQuestion);
        }
        else
            bridgeQuestion = questionWeakHashMap.get(action);
        if (!bridgeQuestion.isAnswered())
            return false;

        MakeBridge makeBridge;
        if (!MakeBridge.hashMapHasInstance(action)) {
            makeBridge = new MakeBridge(action, performer, null, null, 99, 99,
                    10, 10, bridgeQuestion.getAnswer(), onSurface, heightOffset,
                    performer.getStatus().getDir());
        }
        else
            makeBridge = MakeBridge.actionDataWeakHashMap.get(action);

        if (makeBridge.hasAFailureCondition())
            return true;
        makeBridge.doConstruction();
        return true;
    }
}
