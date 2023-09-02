package unaverage.tweaks.mixin.endermen_can_teleport_unreachable_players_closer;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndermanEntity.class)
public abstract class EndermanMixin extends HostileEntity {
    protected EndermanMixin(EntityType<? extends HostileEntity> entityType, World world) {super(entityType, world);}


    @Unique
    private int coolDown = 0;

    @Inject(
        method = "tickMovement",
        at = @At(
            value = "HEAD"
        )
    )
    void tryTeleportUnreachablePlayers(CallbackInfo ci){
        if (coolDown > 0){
            coolDown -= 1;
            return;
        }

        if (!(this.getTarget() instanceof PlayerEntity player)){
            return;
        }

        var dist = this.getPos().distanceTo(player.getPos());

        if (dist > 3.5){
            return;
        }
        if (dist < 1.5){
            return;
        }

        var blockOverHead = this.getWorld().getBlockState(
            player.getBlockPos().up().up()
        );

        if (!blockOverHead.isSolid()) return;

        player.teleport(this.getPos().x, this.getPos().y, this.getPos().z);

        this.coolDown = 3*20;
    }
}
