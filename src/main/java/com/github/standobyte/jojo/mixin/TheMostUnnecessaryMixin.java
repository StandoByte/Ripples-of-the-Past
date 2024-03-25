package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.crash.CrashReport;

@Mixin(CrashReport.class)
public abstract class TheMostUnnecessaryMixin {
    
    @Shadow
    public abstract String getExceptionMessage();
    
    @Redirect(method = "getFriendlyReport", at = @At(
            value = "INVOKE", 
            target = "Ljava/lang/StringBuilder;append(Ljava/lang/String;)Ljava/lang/StringBuilder;", 
            ordinal = 2))
    public StringBuilder jojoErrorComment(StringBuilder crashReport, String errorComment) {
        String exceptionMessage = getExceptionMessage();
        String[] lines = exceptionMessage.split("\n");
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.startsWith("\tat com.github.standobyte.") || 
                    line.startsWith("\tat net.minecraft.") && line.contains("$jojo")) {
                return crashReport.append("This must be the work of an enemy Stand!");
            }
        }
        return crashReport.append(errorComment);
    }

}
