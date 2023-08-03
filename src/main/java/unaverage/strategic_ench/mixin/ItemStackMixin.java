package unaverage.strategic_ench.mixin;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

import static unaverage.strategic_ench.HelperKt.*;
import static unaverage.strategic_ench.config.GlobalConfigKt.configInitialized;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(
        method = "getTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;appendEnchantments(Ljava/util/List;Lnet/minecraft/nbt/NbtList;)V",
            shift = At.Shift.AFTER
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void injectExtraToolTip(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        if (!configInitialized) return;
        
        var self = (ItemStack)(Object)this;

        var enchantments = EnchantmentHelper.get(self);
        if (enchantments.isEmpty()) return;
        
        var weight = getWeight(enchantments);
        var cap = getCapacity(self.getItem());
        if (cap == null || cap < 0) return;

        var text = "Enchantment Capacity: %.1f/%s".formatted(weight, cap);

        list.add(
            MutableText.of(new LiteralTextContent(text))
        );
    }

    @Inject(
        method = "addEnchantment",
        at = @At(value = "RETURN")
    )
    private void injectCappingOnAddEnch(Enchantment enchantment, int level, CallbackInfo ci){
        if (!configInitialized) return;
        
        var self = (ItemStack)(Object)this;

        var enchantments = EnchantmentHelper.get(self);

        capEnchantmentMap(
            enchantments,
            getCapacity(self.getItem()),
            item->false
        );

        EnchantmentHelper.set(enchantments, self);
    }

    /**
     * Injects capping at {@link ItemStack#setNbt(NbtCompound)} of an item
     * Setting the nbt of an item from another is a way to transfer enchantments between those items
     * An example is done by smithing tables upgrading a diamond tool to its perspective netherite tool
     * The problem is that these different tools can have different capacities, and must be recapped
     * This is done by capping the enchantment of the tool after {@link ItemStack#setNbt(NbtCompound)} has finished
     */
    @Inject(
        method = "setNbt",
        at = @At(value = "RETURN")
    )
    private void injectCappingOnSetNBT(NbtCompound nbt, CallbackInfo ci){
        if (!configInitialized) return;
        
        var self = (ItemStack)(Object)this;
        
        var enchantments = EnchantmentHelper.get(self);

        //I'm not sure why this fixes the issue with EnchantedShulker mod
        //I'm also not sure why this doesn't work in the dev environment, only the real environment
        if (enchantments.isEmpty()) return;

        capEnchantmentMap(
            enchantments,
            getCapacity(self.getItem()),
            item->false
        );

        //This function is probably the root cause of the problem with EnchantedShulker
        EnchantmentHelper.set(enchantments, self);
    }
}
