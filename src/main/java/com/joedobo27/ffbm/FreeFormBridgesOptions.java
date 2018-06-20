package com.joedobo27.ffbm;

/**
 * Data structure like class. It should only have one INSTANCE.
 */
class FreeFormBridgesOptions {

    static MakeBridgeTileAction makeBridgeTileAction;

    FreeFormBridgesOptions(){
    }

    public static void setMakeBridgeTileAction(MakeBridgeTileAction _makeBridgeTileAction) {
        makeBridgeTileAction = _makeBridgeTileAction;
    }
}
