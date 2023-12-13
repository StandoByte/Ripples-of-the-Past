package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.crash.CrashReport;

@Mixin(CrashReport.class)
public class TheMostUnnecessaryMixin {
    
    @ModifyVariable(method = "getErrorComment", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private static String[] jojoAddErrorComment(String[] errorComments) {
        String[] comments = new String[errorComments.length + 1];
        System.arraycopy(errorComments, 0, comments, 0, errorComments.length);
        comments[errorComments.length] = "This must be the work of an enemy Stand!";
        return comments;
    }

}
