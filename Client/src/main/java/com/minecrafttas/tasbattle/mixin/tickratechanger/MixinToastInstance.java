package com.minecrafttas.tasbattle.mixin.tickratechanger;

import com.minecrafttas.tasbattle.TASBattle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * This mixin slows down the toast to the tickrate
 * @author Pancake
 */
@Mixin(targets = "net/minecraft/client/gui/components/toasts/ToastComponent$ToastInstance")
public class MixinToastInstance {

	/**
	 * Slows down the toaster timer
	 * @param animationTimer Original value
	 * @return Manipulated value
	 */
	@ModifyVariable(method = "Lnet/minecraft/client/gui/components/toasts/ToastComponent$ToastInstance;render(ILcom/mojang/blaze3d/vertex/PoseStack;)Z", at = @At(value = "STORE"), ordinal = 0, index = 4)
	public long modifyAnimationTime(long animationTimer) {
		return TASBattle.instance.getTickrateChanger().getMilliseconds();
	}

}
