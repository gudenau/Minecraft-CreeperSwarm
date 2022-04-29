package net.gudenau.minecraft.creeperswarm.mixin;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.mob.ZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ZombieEntity.class)
public interface ZombieEntityAccessor {
    @Accessor static EntityAttributeModifier getBABY_SPEED_BONUS() { throw new AssertionError(); }
}
