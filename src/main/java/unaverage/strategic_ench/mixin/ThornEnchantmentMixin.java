package unaverage.strategic_ench.mixin;

import net.minecraft.enchantment.ThornsEnchantment;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import static unaverage.strategic_ench.config.GlobalConfigKt.configInitialized;
import static unaverage.strategic_ench.config.GlobalConfigKt.thornsWearDownArmor;

@Mixin(ThornsEnchantment.class)
public class ThornEnchantmentMixin {

    /**
     * Disables the default vanilla behavior of damaging armor if thorn inflict damage in enemies
     */
    @ModifyConstant(
        method = "onUserDamaged",
        constant = @Constant(intValue = 2)
    )
    private <T extends LivingEntity> int doNoDamage(int constant){
        if (!configInitialized) return constant;

        return thornsWearDownArmor() ? 0 : constant;
    }
}
