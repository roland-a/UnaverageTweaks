package unaverage.tweaks.mixin;

import net.minecraft.enchantment.ThornsEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import unaverage.tweaks.GlobalConfig;

@Mixin(ThornsEnchantment.class)
public class ThornsEnchantmentMixin {

    /**
     * Disables the default vanilla behavior of damaging armor if thorn inflict damage in enemies
     */
    @ModifyConstant(
        method = "onUserDamaged",
        constant = @Constant(intValue = 2)
    )
    private int cancelArmorWearDown(int constant){
        return GlobalConfig.thorns_no_longer_wears_down_armor ? 0 : constant;
    }
}
