package net.gudenau.minecraft.creeperswarm.mixin;

import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRules.IntRule.class)
public interface GameRules$IntRuleAccessor {
    @Invoker static GameRules.Type<GameRules.IntRule> invokeCreate(int defaultValue) { throw new AssertionError(); }
}
