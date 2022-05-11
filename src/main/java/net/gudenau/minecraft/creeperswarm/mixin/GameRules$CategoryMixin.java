package net.gudenau.minecraft.creeperswarm.mixin;

import net.gudenau.minecraft.creeperswarm.EnumAdder;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(GameRules.Category.class)
public abstract class GameRules$CategoryMixin {
    @Shadow @Final @Mutable private static GameRules.Category[] field_24102;
    
    @Invoker("<init>") static GameRules.Category init(String enumName, int ordinal, String name){ throw new AssertionError(); }
    
    @Inject(
        method = "<clinit>",
        at = @At("TAIL")
    )
    private static void init(CallbackInfo ci) {
        EnumAdder.register(GameRules.Category.class, (valueName, arguments)->{
            int ordinal = field_24102.length;
            GameRules.Category[] values = Arrays.copyOf(field_24102, ordinal + 1);
            GameRules.Category category = init(valueName, ordinal, (String) arguments[0]);
            values[ordinal] = category;
            field_24102 = values;
            return category;
        });
    }
}
