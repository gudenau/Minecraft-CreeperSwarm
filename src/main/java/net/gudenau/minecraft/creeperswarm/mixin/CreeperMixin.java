package net.gudenau.minecraft.creeperswarm.mixin;

import net.gudenau.minecraft.creeperswarm.CreeperSwarm;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("WrongEntityDataParameterClass")
@Mixin(CreeperEntity.class)
public abstract class CreeperMixin extends HostileEntity {
	@Shadow private int explosionRadius;
	@Shadow protected abstract void initDataTracker();
	@Unique private static TrackedData<Boolean> gud_BABY;
	@Unique private static TrackedData<Optional<UUID>> gud_PARENT;
	
	@SuppressWarnings("ConstantConditions")
	private CreeperMixin() {
		super(null, null);
	}
	
	// Overridden methods
	
	@Override
	public boolean isBaby() {
		return dataTracker.get(gud_BABY);
	}
	
	@Override
	public void setBaby(boolean value) {
		dataTracker.set(gud_BABY, value);
		if (world != null && !world.isClient) {
			EntityAttributeInstance movementSpeed = getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
			EntityAttributeModifier bonus = ZombieEntityAccessor.getBABY_SPEED_BONUS();
			movementSpeed.removeModifier(bonus);
			if (value && world.getGameRules().getBoolean(CreeperSwarm.GameRules.SPEED_BOOST)) {
				movementSpeed.addTemporaryModifier(bonus);
			}
		}
	}
	
	@Override
	public void onTrackedDataSet(TrackedData<?> data) {
		if (gud_BABY.equals(data)) {
			calculateDimensions();
		}
		super.onTrackedDataSet(data);
	}
	
	@Override
	public boolean damage(DamageSource source, float amount) {
		if(isBaby() && !world.getGameRules().getBoolean(CreeperSwarm.GameRules.SIBLING_RIVALRY)){
			Entity attacker = source.getAttacker();
			if(attacker instanceof CreeperEntity){
				CreeperEntity creeper = (CreeperEntity)attacker;
				Optional<UUID> otherParent = creeper.getDataTracker().get(gud_PARENT);
				Optional<UUID> thisParent = dataTracker.get(gud_PARENT);
				if(otherParent.isPresent() && otherParent.equals(thisParent)){
					return false;
				}
			}
		}
		
		return super.damage(source, amount);
	}
	
	// Injections
	
	@Inject(
		method = "<clinit>",
		at = @At("TAIL")
	)
	private static void clinit(CallbackInfo ci) {
		gud_BABY = DataTracker.registerData(CreeperEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
		gud_PARENT = DataTracker.registerData(CreeperEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
	}
	
	@Inject(
		method = "initDataTracker",
		at = @At("TAIL")
	)
	private void initDataTracker(CallbackInfo ci) {
		dataTracker.startTracking(gud_BABY, false);
		dataTracker.startTracking(gud_PARENT, Optional.empty());
	}
	
	@Inject(
		method = "writeCustomDataToNbt",
		at = @At("TAIL")
	)
	public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
		if(isBaby()) {
			nbt.putBoolean("IsBaby", true);
			dataTracker.get(gud_PARENT).ifPresent((uuid)->nbt.putUuid("Parent", uuid));
		}
	}
	
	@Inject(
		method = "readCustomDataFromNbt",
		at = @At("TAIL")
	)
	private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
		boolean isBaby = nbt.getBoolean("IsBaby");
		setBaby(isBaby);
		if(isBaby){
			dataTracker.set(gud_PARENT, Optional.ofNullable(nbt.getUuid("Parent")));
		}else{
			dataTracker.set(gud_PARENT, Optional.empty());
		}
	}
	
	@Inject(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/mob/CreeperEntity;discard()V"
		),
		method = "explode",
		locals = LocalCapture.CAPTURE_FAILEXCEPTION
	)
	private void explode(CallbackInfo info, Explosion.DestructionType destructionType, float damageScale) {
		if(isBaby()){
			return;
		}
		
		GameRules gameRules = world.getGameRules();
		
		int count = gameRules.getInt(CreeperSwarm.GameRules.BABY_COUNT);
		if(count == -1){
			count = MathHelper.ceil(getHealth());
		}
		if(world.getDifficulty() == Difficulty.HARD){
			count *= gameRules.getInt(CreeperSwarm.GameRules.HARD_MULTIPLIER);
		}
		float powerScale = 100F / gameRules.getInt(CreeperSwarm.GameRules.EXPLOSION_SCALE);
		int explosionRadius = Math.max(MathHelper.ceil(this.explosionRadius * powerScale), 1);
		
		NbtCompound savedCreeper = new NbtCompound();
		writeNbt(savedCreeper);
		savedCreeper.remove("UUID");
		savedCreeper.putByte("ExplosionRadius", (byte) explosionRadius);
		
		for(int i = 0; i < count; i++){
			CreeperEntity creeper = new CreeperEntity(EntityType.CREEPER, world);
			creeper.readNbt(savedCreeper);
			
			DataTracker tracker = creeper.getDataTracker();
			
			creeper.setBaby(true);
			tracker.set(gud_PARENT, Optional.of(getUuid()));
			
			// Make them all "burst" out of the parent
			float yaw = (creeper.getYaw() + 360F * (i / (float)count)) % 360;
			float pitch = creeper.getPitch();
			creeper.setYaw(yaw);
			creeper.setPitch(pitch);
			Vec3d boost = Vec3d.fromPolar(pitch, yaw).multiply(0.25);
			creeper.addVelocity(boost.x, boost.y + 0.25, boost.z);
			world.spawnEntity(creeper);
		}
	}
}
