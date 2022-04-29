package net.gudenau.minecraft.creeperswarm.mixin;

import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRules.BooleanRule.class)
public interface GameRules$BooleanRuleAccessor {
    @Invoker static GameRules.Type<GameRules.BooleanRule> invokeCreate(boolean defaultValue) { throw new AssertionError(); }
}
