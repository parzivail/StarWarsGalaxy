package com.parzivail.pswg.mixin;

import com.parzivail.pswg.client.camera.MutableCameraEntity;
import com.parzivail.pswg.entity.ship.ShipEntity;
import com.parzivail.util.client.render.ICustomHudRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InGameHud.class)
public class InGameHudMixin
{
	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;blendFuncSeparate(Lcom/mojang/blaze3d/platform/GlStateManager$SrcFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DstFactor;Lcom/mojang/blaze3d/platform/GlStateManager$SrcFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DstFactor;)V"), cancellable = true)
	public void renderCrossHair(MatrixStack matrices, CallbackInfo ci)
	{
		assert this.client.player != null;

		var mainHandStack = this.client.player.getInventory().getMainHandStack();
		var customHUDRenderer = ICustomHudRenderer.CUSTOM_HUD_RENDERERS.get(mainHandStack.getItem());
		if (customHUDRenderer != null)
		{
			if (customHUDRenderer.render(this.client.player, Hand.MAIN_HAND, mainHandStack, matrices))
				ci.cancel();
		}
	}

	@Inject(method = "Lnet/minecraft/client/gui/hud/InGameHud;getCameraPlayer()Lnet/minecraft/entity/player/PlayerEntity;", at = @At("HEAD"), cancellable = true)
	void getCameraPlayer(CallbackInfoReturnable<PlayerEntity> cir)
	{
		var camEntity = this.client.getCameraEntity();
		if (camEntity instanceof ShipEntity || camEntity instanceof MutableCameraEntity)
			cir.setReturnValue(this.client.player);
	}
}
