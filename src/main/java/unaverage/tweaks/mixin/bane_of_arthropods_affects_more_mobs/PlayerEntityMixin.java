package unaverage.tweaks.mixin.bane_of_arthropods_affects_more_mobs;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static unaverage.tweaks.helper.BaneOfAnthropodsAffectMoreMobsKt.isAffectedByBaneOfArthropods;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Unique
    private Entity targetParam = null;

    /**
     * I'm not sure how to grab the local parameter in a redirect
     * A duct-tape fix would be to use an inject mixin to grab it, then store it in a variable
     */
    @Inject(
        method = "attack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/enchantment/EnchantmentHelper;getAttackDamage(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityGroup;)F",
            shift = At.Shift.BEFORE
        )
    )
    private void grabTargetParam(Entity target, CallbackInfo ci){
        this.targetParam = target;
    }

    /**
     * Applies extra damage to mobs that are configured to be effected by the bane of arthropods
     * I'm not sure how to directly modify the damage local variable in {@link PlayerEntity#attack(Entity)}
     * So instead, I redirected a function that modifies that local variable to indirectly modify that variable
     */
    @Redirect(
        method = "attack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/enchantment/EnchantmentHelper;getAttackDamage(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityGroup;)F"
        )
    )
    private float doExtraDamageToExtraMobs(ItemStack stack, EntityGroup group){
        var originalResult = EnchantmentHelper.getAttackDamage(stack, group);

        if (!isAffectedByBaneOfArthropods(this.targetParam.getType())) return originalResult;

        var level = EnchantmentHelper.getEquipmentLevel(Enchantments.BANE_OF_ARTHROPODS, (PlayerEntity)(Object)this);
        if (level == 0) return originalResult;

        return originalResult + level * 2.5f;
    }
}
