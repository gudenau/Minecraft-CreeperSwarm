package net.gudenau.minecraft.creeperswarm.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CreeperEntityModel.class)
public abstract class CreeperEntityModelMixin<T extends Entity> extends SinglePartEntityModel<T> {
    @Shadow @Final private ModelPart root;
    @Shadow @Final private ModelPart head;
    @Shadow @Final private ModelPart leftHindLeg;
    @Shadow @Final private ModelPart rightHindLeg;
    @Shadow @Final private ModelPart leftFrontLeg;
    @Shadow @Final private ModelPart rightFrontLeg;
    
    @Unique private List<ModelPart> gud_headParts;
    @Unique private List<ModelPart> gud_bodyParts;
    
    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void init(ModelPart root, CallbackInfo ci) {
        gud_headParts = ImmutableList.of(head);
        gud_bodyParts = ImmutableList.of(
            this.root,
            leftHindLeg,
            rightHindLeg,
            leftFrontLeg,
            rightFrontLeg
        );
    }
    
    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        if (child) {
            final float childHeadYOffset = 16;
            final float invertedChildBodyScale = 2;
            final float childHeadZOffset = 0;
            final float childBodyYOffset = 24;
            
            matrices.push();
            float scale = 1.5f / invertedChildBodyScale;
            matrices.scale(scale, scale, scale);
            matrices.translate(0.0, childHeadYOffset / 16.0f, childHeadZOffset / 16.0f);
            gud_headParts.forEach(headPart -> headPart.render(matrices, vertices, light, overlay, red, green, blue, alpha));
            matrices.pop();
            
            matrices.push();
            scale = 1.0f / invertedChildBodyScale;
            matrices.scale(scale, scale, scale);
            matrices.translate(0.0, childBodyYOffset / 16.0f, 0.0);
            gud_bodyParts.forEach(bodyPart -> bodyPart.render(matrices, vertices, light, overlay, red, green, blue, alpha));
            matrices.pop();
        } else {
            super.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        }
    }
}
