package unaverage.tweaks.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import unaverage.tweaks.GlobalConfig;


@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {super(type, world);}

    @Redirect(
        method = "takeShieldHit",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;knockback(Lnet/minecraft/entity/LivingEntity;)V"
        )
    )
    private void disableShieldNoKnockback(LivingEntity instance, LivingEntity target){
        if (!GlobalConfig.Miscellaneous.shields_no_longer_prevent_knockback){
            instance.takeKnockback(0.5, target.getX() - instance.getX(), target.getZ() - instance.getZ());
            return;
        }

        this.velocityModified = true;
    }
}