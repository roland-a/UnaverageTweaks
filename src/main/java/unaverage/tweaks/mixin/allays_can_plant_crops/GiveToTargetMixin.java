package unaverage.tweaks.mixin.allays_can_plant_crops;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.GiveInventoryToLookTargetTask;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

import static unaverage.tweaks.helper.AllaysCanPlantCropsKt.getHeldItemAsCropBlock;
import static unaverage.tweaks.helper.AllaysCanPlantCropsKt.getNearestFarmPos;

@Mixin(GiveInventoryToLookTargetTask.class)
public class GiveToTargetMixin<E extends LivingEntity> extends MultiTickTask<E> {
    @Final @Shadow
    private float speed;

    @Unique
    BlockPos targetFarmland = null;

    public GiveToTargetMixin(Map<MemoryModuleType<?>, MemoryModuleState> requiredMemoryState) {super(requiredMemoryState);}

    @Inject(
        method = "shouldRun",
        at = @At("HEAD"),
        cancellable = true
    )
    void injectShouldRun(ServerWorld world, E entity, CallbackInfoReturnable<Boolean> cir){
        if (!(entity instanceof AllayEntity allay)) return;

        var cropBlock = getHeldItemAsCropBlock(allay);
        if (cropBlock == null) return;

        var nearestFarmland = getNearestFarmPos(allay.getBlockPos(), cropBlock, allay.getWorld());
        if (nearestFarmland == null) return;

        targetFarmland = nearestFarmland;
        cir.setReturnValue(true);
    }

    @Inject(
        method = "run",
        at = @At("HEAD"),
        cancellable = true
    )
    void injectRun(ServerWorld world, E entity, long time, CallbackInfo ci){
        if (targetFarmland == null) return;

        LookTargetUtil.walkTowards(entity, targetFarmland, this.speed, 0);
        ci.cancel();
    }

    @Inject(
        method = "shouldKeepRunning",
        at = @At("HEAD"),
        cancellable = true
    )
    void injectShouldKeepRunning(ServerWorld world, E entity, long time, CallbackInfoReturnable<Boolean> cir){
        if (!(entity instanceof AllayEntity allay)) return;
        if (targetFarmland == null) return;

        var crop = getHeldItemAsCropBlock(allay);
        if (crop == null){
            cir.setReturnValue(false);
            return;
        }

        if (!crop.canPlaceAt(world, targetFarmland) || !world.getBlockState(targetFarmland).isAir()) {
            cir.setReturnValue(false);
            return;
        }

        var distSq = targetFarmland.getSquaredDistance(allay.getPos());
        if (distSq > 10*10){
            cir.setReturnValue(false);
            return;
        }

        cir.setReturnValue(true);
    }

    @Inject(
        method = "keepRunning",
        at = @At("HEAD"),
        cancellable = true
    )
    void injectKeepRunning(ServerWorld world, E entity, long time, CallbackInfo ci){
        if (!(entity instanceof AllayEntity allay)) return;
        if (targetFarmland == null) return;

        var crop = getHeldItemAsCropBlock(allay);
        if (crop == null) return;

        var distSq = targetFarmland.getSquaredDistance(allay.getPos());
        if (distSq < .5*.5){
            world.setBlockState(targetFarmland, crop, 3);
            allay.playSound(crop.getSoundGroup().getPlaceSound(), 1.0f, 1.0f);
            world.emitGameEvent(GameEvent.BLOCK_PLACE, targetFarmland, GameEvent.Emitter.of(allay, crop));

            allay.getInventory().removeStack(0, 1);
            allay.getBrain().remember(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, 60);
        }
        ci.cancel();
    }
}
