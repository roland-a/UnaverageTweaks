package unaverage.tweaks.mixin.tools_have_custom_repair_rate;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static unaverage.tweaks.helper.ToolsHaveCustomRepairRateKt.getIngotsToFullyRepair;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {
    @Shadow private int repairItemUsage;

    @Shadow @Final private Property levelCost;

    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Inject(
        method = "updateResult",
        at = @At("TAIL")
    )
    void useNewRepair(CallbackInfo ci){
        var input1 = this.input.getStack(0);
        var input2 = this.input.getStack(1);
        var result = this.output.getStack(0);

        var ingotsToFullyRepair = getIngotsToFullyRepair(input1.getItem());
        if (ingotsToFullyRepair == null) return;

        if (!input1.getItem().canRepair(input1, input2)) return;
        if (input1.getItem() == input2.getItem()) return;

        //Undo the vanilla repair
        result.setDamage(input1.getDamage());

        //Repairs the item our own way
        for (int i = 0; i < input2.getCount(); i++){
            if (result.getDamage() == 0) break;

            var repairPerIngots = result.getMaxDamage() / ingotsToFullyRepair;

            result.setDamage(result.getDamage() - repairPerIngots);

            this.repairItemUsage = i+1;
            this.levelCost.set(i+1);
        }
    }
}
