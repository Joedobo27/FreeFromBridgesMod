package com.joedobo27.ffbm;

import com.wurmonline.server.behaviours.Actions;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.ServerStartedListener;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.logging.Logger;

import static com.joedobo27.libs.action.ActionMaster.setActionEntryMaxRangeReflect;

public class FreeFromBridgesMod implements WurmServerMod, PreInitable, ServerStartedListener {

    static final Logger logger = Logger.getLogger(FreeFromBridgesMod.class.getName());

    @Override
    public void preInit() {
        ModActions.init();
    }

    @Override
    public void onServerStarted() {
        BridgeBehaviours bridgeBehaviours = new BridgeBehaviours();
        ModActions.registerAction(bridgeBehaviours);

        MakeBridgeTileAction makeBridgeTileAction = new MakeBridgeTileAction(Actions.BUILD_PLAN_BRIDGE,
                Actions.actionEntrys[Actions.BUILD_PLAN_BRIDGE]);
        ModActions.registerAction(makeBridgeTileAction);
        setActionEntryMaxRangeReflect(Actions.actionEntrys[Actions.BUILD_PLAN_BRIDGE], 8, logger);
        FreeFormBridgesOptions.setMakeBridgeTileAction(makeBridgeTileAction);
    }
}
