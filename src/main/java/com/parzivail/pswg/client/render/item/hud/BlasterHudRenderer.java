package com.parzivail.pswg.client.render.item.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.parzivail.pswg.Resources;
import com.parzivail.pswg.item.blaster.BlasterItem;
import com.parzivail.pswg.item.blaster.data.BlasterTag;
import com.parzivail.util.client.render.ICustomHudRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class BlasterHudRenderer extends DrawableHelper implements ICustomHudRenderer
{
	public static final BlasterHudRenderer INSTANCE = new BlasterHudRenderer();

	private static final Identifier HUD_ELEMENTS_TEXTURE = Resources.id("textures/gui/blasters.png");

	@Override
	public boolean render(PlayerEntity player, Hand hand, ItemStack stack, MatrixStack matrices)
	{
		var client = MinecraftClient.getInstance();
		var scaledWidth = client.getWindow().getScaledWidth();
		var scaledHeight = client.getWindow().getScaledHeight();

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, HUD_ELEMENTS_TEXTURE);

		var i = scaledWidth / 2;
		var j = scaledHeight / 2;
		final var cooldownWidth = 61;

		var b = (BlasterItem)stack.getItem();
		var bt = new BlasterTag(stack.getOrCreateTag());
		var bd = BlasterItem.getBlasterDescriptor(player.world, stack);

		var profile = bd.cooling;
		final var crosshairIdx = 0;

		/*
		 * Cooldown
		 */

		if (bt.isOverheatCooling() || bt.heat > 0)
		{
			var cooldownBarX = (scaledWidth - cooldownWidth) / 2;

			// translucent background
			this.drawTexture(matrices, cooldownBarX, j + 30, 0, 0, cooldownWidth, 3);

			final float maxHeat = bd.heat.capacity;

			if (bt.isOverheatCooling())
			{
				var cooldownTimer = (bt.overheatTimer - client.getTickDelta()) / maxHeat;

				cooldownTimer = MathHelper.clamp(cooldownTimer, 0, 0.98f);

				// red cooldown background
				this.drawTexture(matrices, cooldownBarX, j + 30, 0, 16, cooldownWidth, 3);

				if (bt.canBypassOverheat)
				{
					var primaryBypassStartX = (int)((profile.primaryBypassTime - profile.primaryBypassTolerance) * cooldownWidth);
					var primaryBypassWidth = (int)(2 * profile.primaryBypassTolerance * cooldownWidth);
					var secondaryBypassStartX = (int)((profile.secondaryBypassTime - profile.secondaryBypassTolerance) * cooldownWidth);
					var secondaryBypassWidth = (int)(2 * profile.secondaryBypassTolerance * cooldownWidth);

					// blue primary bypass
					this.drawTexture(matrices, cooldownBarX + primaryBypassStartX, j + 30, primaryBypassStartX, 8, primaryBypassWidth, 3);
					// yellow secondary bypass
					this.drawTexture(matrices, cooldownBarX + secondaryBypassStartX, j + 30, secondaryBypassStartX, 12, secondaryBypassWidth, 3);
				}

				// cursor
				this.drawTexture(matrices, (int)(cooldownBarX + cooldownTimer * cooldownWidth - 1), j + 28, 0, 24, 3, 7);
			}
			else
			{
				float deltaHeat = 0;
				if (bt.passiveCooldownTimer == 0)
					deltaHeat = client.getTickDelta();

				var heatPercentage = (bt.heat - deltaHeat) / maxHeat;
				this.drawTexture(matrices, cooldownBarX, j + 30, 0, 4, (int)(cooldownWidth * heatPercentage), 3);
			}

			// endcaps
			this.drawTexture(matrices, cooldownBarX, j + 30, 0, 20, cooldownWidth, 3);
		}

		/*
		 * Crosshair
		 */

		if (bt.isAimingDownSights)
		{
			RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE_MINUS_DST_COLOR, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
			this.drawTexture(matrices, (scaledWidth - 15) / 2, (scaledHeight - 15) / 2, 62 + 16 * crosshairIdx, 0, 15, 15);
			RenderSystem.defaultBlendFunc();
		}

		RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);

		return bt.isAimingDownSights;
	}
}
