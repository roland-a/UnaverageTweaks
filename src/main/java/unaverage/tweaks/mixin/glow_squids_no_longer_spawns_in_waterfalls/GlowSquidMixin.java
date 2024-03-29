package unaverage.tweaks.mixin.glow_squids_no_longer_spawns_in_waterfalls;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlowSquidEntity.class)
public class GlowSquidMixin {
    @Inject(
        method = "canSpawn",
        at = @At("RETURN"),
        cancellable = true
    )
    private static void requireMoreWaterBlocksToSpawn(EntityType<? extends LivingEntity> type, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(
            cir.getReturnValue() && world.getBlockState(pos.up()).isOf(Blocks.WATER) && world.getBlockState(pos.down()).isOf(Blocks.WATER)
        );
    }
}
