package com.parzivail.pswg.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.parzivail.pswg.Client;
import com.parzivail.pswg.Resources;
import com.parzivail.pswg.client.item.render.LightsaberItemRenderer;
import com.parzivail.pswg.item.lightsaber.LightsaberItem;
import com.parzivail.pswg.item.lightsaber.LightsaberTag;
import com.parzivail.pswg.screen.LightsaberForgeScreenHandler;
import com.parzivail.util.client.ColorUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Quaternion;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class LightsaberForgeScreen extends HandledScreen<LightsaberForgeScreenHandler> implements ScreenHandlerListener
{
	private static final Identifier TEXTURE = Resources.identifier("textures/gui/container/lightsaber_forge.png");

	private final List<SliderWidget> sliders = new ArrayList<>();

	private int r;
	private int g;
	private int b;

	private ItemStack lightsaber = ItemStack.EMPTY;

	public LightsaberForgeScreen(LightsaberForgeScreenHandler handler, PlayerInventory inventory, Text title)
	{
		super(handler, inventory, title);
		backgroundWidth = 256;
		backgroundHeight = 241;
	}

	protected void init()
	{
		super.init();

		this.playerInventoryTitleX = 48;
		this.playerInventoryTitleY = this.backgroundHeight - 94;
		this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
		this.handler.addListener(this);

		if (lightsaber.getItem() instanceof LightsaberItem)
		{
			LightsaberTag lt = new LightsaberTag(lightsaber.getOrCreateTag());

			int color = lt.bladeColor;
			r = (color & 0xFF0000) >> 16;
			g = (color & 0xFF00) >> 8;
			b = (color & 0xFF);
		}

		sliders.clear();
		sliders.add(new SliderWidget(x + 41, y + 61, 100, 20, new LiteralText("R: 0"), r / 255f)
		{
			@Override
			protected void updateMessage()
			{
				this.setMessage(new LiteralText("R: " + (int)Math.round(value * 255)));
			}

			@Override
			protected void applyValue()
			{
				r = (int)Math.round(value * 255);
				commitChanges();
			}
		});

		sliders.add(new SliderWidget(x + 41, y + 83, 100, 20, new LiteralText("G: 0"), g / 255f)
		{
			@Override
			protected void updateMessage()
			{
				this.setMessage(new LiteralText("G: " + (int)Math.round(value * 255)));
			}

			@Override
			protected void applyValue()
			{
				g = (int)Math.round(value * 255);
				commitChanges();
			}
		});

		sliders.add(new SliderWidget(x + 41, y + 105, 100, 20, new LiteralText("B: 0"), 0)
		{
			@Override
			protected void updateMessage()
			{
				this.setMessage(new LiteralText("B: " + (int)Math.round(value * 255)));
			}

			@Override
			protected void applyValue()
			{
				b = (int)Math.round(value * 255);
				commitChanges();
			}
		});

		for (SliderWidget s : sliders)
		{
			this.addButton(s);
		}
	}

	public void removed()
	{
		super.removed();
		this.handler.removeListener(this);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
	{
		// functional programming hell yeah
		return sliders.stream().anyMatch(
				widget -> widget.isMouseOver(mouseX, mouseY) &&
				          widget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
		) || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	private void commitChanges()
	{
		LightsaberTag.mutate(lightsaber, (lt) ->
		{
			lt.bladeColor = ColorUtil.packRgb(r, g, b);
		});
	}

	@Override
	public void onHandlerRegistered(ScreenHandler handler, DefaultedList<ItemStack> stacks)
	{
		this.onSlotUpdate(handler, 0, handler.getSlot(0).getStack());
	}

	@Override
	public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack)
	{
		switch (slotId)
		{
			case 0:
			{
				lightsaber = stack.copy();
				break;
			}
		}
	}

	@Override
	public void onPropertyUpdate(ScreenHandler handler, int property, int value)
	{
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		this.drawMouseoverTooltip(matrices, mouseX, mouseY);
	}

	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY)
	{
		this.renderBackground(matrices);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.client.getTextureManager().bindTexture(TEXTURE);
		int i = (this.width - this.backgroundWidth) / 2;
		int j = (this.height - this.backgroundHeight) / 2;
		this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);

		final int stencilX = 9;
		final int stencilY = 17;
		final int stencilWidth = 238;
		final int stencilHeight = 39;

		final int hiltLength = 70;

		matrices.push();
		matrices.translate(x + stencilX + hiltLength, y + stencilY + stencilHeight / 2f, 100);

		matrices.multiply(new Quaternion(0, 0, 90, true));
		matrices.multiply(new Quaternion(0, 135, 0, true));
		matrices.scale(100, -100, 100);

		VertexConsumerProvider.Immediate immediate = Client.minecraft.getBufferBuilders().getEntityVertexConsumers();

		if (lightsaber.getItem() instanceof LightsaberItem)
		{
			LightsaberItemRenderer.INSTANCE.renderDirect(lightsaber, ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND, matrices, immediate, 0xf000f0, 0xFFFFFF);
			immediate.draw();
		}
		matrices.pop();

		matrices.push();

		int x0 = x + 7;
		int x1 = x + 7 + 30;
		int y0 = y + 88;
		int y1 = y + 88 + 30;

		int u0 = 0;
		int u1 = 10;
		int v0 = 0;
		int v1 = 10;

		int z = 0;

		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(matrices.peek().getModel(), (float)x0, (float)y1, (float)z).color(r, g, b, 255).texture(u0, v1).next();
		bufferBuilder.vertex(matrices.peek().getModel(), (float)x1, (float)y1, (float)z).color(r, g, b, 255).texture(u1, v1).next();
		bufferBuilder.vertex(matrices.peek().getModel(), (float)x1, (float)y0, (float)z).color(r, g, b, 255).texture(u1, v0).next();
		bufferBuilder.vertex(matrices.peek().getModel(), (float)x0, (float)y0, (float)z).color(r, g, b, 255).texture(u0, v0).next();
		bufferBuilder.end();
		RenderSystem.disableTexture();
		BufferRenderer.draw(bufferBuilder);
		RenderSystem.enableTexture();

		matrices.pop();
	}

	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY)
	{
		this.textRenderer.draw(matrices, this.title, (float)this.titleX, (float)this.titleY, 4210752);
		this.textRenderer.draw(matrices, this.playerInventory.getDisplayName(), (float)this.playerInventoryTitleX, (float)this.playerInventoryTitleY, 4210752);
	}
}
