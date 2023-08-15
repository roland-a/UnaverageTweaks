package unaverage.tweaks.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import unaverage.tweaks.GlobalConfig;

import java.util.Map;

import static unaverage.tweaks.HelperKt.capEnchantmentMap;
import static unaverage.tweaks.HelperKt.getCapacity;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {
    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Inject(
        method = "canTakeOutput",
        at = @At("HEAD"),
        cancellable = true
    )
    void removeXpRequirement(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> cir){
        if (!GlobalConfig.XP.anvil_no_longer_requires_xp) return;

        cir.setReturnValue(true);
    }

    /**
     * Redirects the {@link EnchantmentHelper#set(Map, ItemStack)} that would have used the default capping behavior, and instead use a different capping behavior
     * The default capping behavior would not have prioritized the enchantment in the sacrifice item
     * The new capping behavior will prioritize the enchantments that are from the sacrificed item in the anvil
     */
    @Redirect(
        method = "updateResult",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/enchantment/EnchantmentHelper;set(Ljava/util/Map;Lnet/minecraft/item/ItemStack;)V"
        )
    )
    private void anvilUsesDifferentCappingBehavior(Map<Enchantment, Integer> enchantments, ItemStack stack){
        var inputFromSecondSlot = this.input.getStack(1);

        //applies the new capping behavior
        capEnchantmentMap(
            enchantments,
            getCapacity(stack.getItem()),
            //prioritize the enchantment if its from the sacrifice item
            e -> EnchantmentHelper.get(inputFromSecondSlot).containsKey(e)
        );

        //EnchantmentHelper#set will still perform the default capping behavior, but it doesnt matter because it is already capped by the previous capping behavior
        EnchantmentHelper.set(enchantments, stack);
    }
}
