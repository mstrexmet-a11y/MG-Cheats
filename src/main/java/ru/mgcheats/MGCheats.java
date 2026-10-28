package ru.mgcheats;

import net.minecraftforge.fml.common.Mod;

@Mod(MGCheats.MOD_ID)
public class MGCheats {
    public static final String MOD_ID = "mgcheats";

    public MGCheats() {
        ClientFeatures.register();
    }
}
