package net.gudenau.minecraft.creeperswarm;

import net.fabricmc.api.ModInitializer;

import net.gudenau.minecraft.creeperswarm.mixin.GameRules$BooleanRuleAccessor;
import net.gudenau.minecraft.creeperswarm.mixin.GameRules$IntRuleAccessor;
import net.gudenau.minecraft.creeperswarm.mixin.GameRulesAccessor;
import net.minecraft.world.GameRules.*;

public final class CreeperSwarm implements ModInitializer {
    @Override
    public void onInitialize() {
        GameRules.init();
    }
    
    public static final class GameRules {
        public static Category category = EnumAdder.createEnum(Category.class, "CREEPER_SWARM", "gamerule.category.creeper_swarm");
        
        public static final Key<BooleanRule> SPEED_BOOST = create("creeperSwarmSpeedBoost", true);
        public static final Key<BooleanRule> SIBLING_RIVALRY = create("creeperSwarmSiblingRivalry", false);
        public static final Key<IntRule> BABY_COUNT = create("creeperSwarmBabyCount", -1);
        public static final Key<IntRule> HARD_MULTIPLIER = create("creeperSwarmHardMultiplier", 2);
        public static final Key<IntRule> EXPLOSION_SCALE = create("creeperSwarmExplosionScale", 25);
    
        private static Key<BooleanRule> create(String name, boolean defaultValue) {
            return GameRulesAccessor.invokeRegister(name, category, GameRules$BooleanRuleAccessor.invokeCreate(defaultValue));
        }
    
        private static Key<IntRule> create(String name, int defaultValue) {
            return GameRulesAccessor.invokeRegister(name, category, GameRules$IntRuleAccessor.invokeCreate(defaultValue));
        }
    
        public static void init() {}
    }
}
