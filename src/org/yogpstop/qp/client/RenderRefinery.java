package org.yogpstop.qp.client;

import java.util.HashMap;

import net.minecraft.src.Block;
import net.minecraft.src.GLAllocation;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Item;
import net.minecraft.src.ModelBase;
import net.minecraft.src.ModelRenderer;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityRenderer;
import net.minecraft.src.TileEntitySpecialRenderer;
import net.minecraft.src.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.liquids.LiquidStack;

import org.lwjgl.opengl.GL11;
import org.yogpstop.qp.TileRefinery;

import buildcraft.core.render.RenderEntityBlock;
import buildcraft.core.render.RenderEntityBlock.BlockInterface;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class RenderRefinery extends TileEntitySpecialRenderer implements ISimpleBlockRenderingHandler {
	public static final RenderRefinery INSTANCE = new RenderRefinery();
	private static final float pixel = (float) (1.0 / 16.0);
	private final ModelRenderer tank;
	private final ModelRenderer magnet[] = new ModelRenderer[4];
	private final ModelBase model = new ModelBase() {};

	private RenderRefinery() {
		this.tank = new ModelRenderer(this.model, 0, 0);
		this.tank.addBox(-4F, -8F, -4F, 8, 16, 8);
		this.tank.rotationPointX = 8;
		this.tank.rotationPointY = 8;
		this.tank.rotationPointZ = 8;

		for (int i = 0; i < 4; ++i) {
			this.magnet[i] = new ModelRenderer(this.model, 32, i * 8);
			this.magnet[i].addBox(0, -8F, -8F, 8, 4, 4);
			this.magnet[i].rotationPointX = 8;
			this.magnet[i].rotationPointY = 8;
			this.magnet[i].rotationPointZ = 8;
		}

		setTileEntityRenderer(TileEntityRenderer.instance);
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {
		render((TileRefinery) tileentity, x, y, z);
	}

	private void render(TileRefinery tile, double x, double y, double z) {
		LiquidStack liquid1 = null, liquid2 = null, liquidResult = null;

		float anim = 0;
		int angle = 0;
		ModelRenderer theMagnet = this.magnet[0];
		if (tile != null) {
			liquid1 = tile.src1;
			liquid2 = tile.src2;
			liquidResult = tile.res;

			anim = tile.getAnimationStage();

			angle = 0;
			switch (tile.worldObj.getBlockMetadata(tile.xCoord, tile.yCoord, tile.zCoord)) {
			case 2:
				angle = 90;
				break;
			case 3:
				angle = 270;
				break;
			case 4:
				angle = 180;
				break;
			case 5:
				angle = 0;
				break;
			}

			if (tile.animationSpeed <= 1) {
				theMagnet = this.magnet[0];
			} else if (tile.animationSpeed <= 2.5) {
				theMagnet = this.magnet[1];
			} else if (tile.animationSpeed <= 4.5) {
				theMagnet = this.magnet[2];
			} else {
				theMagnet = this.magnet[3];
			}
		}

		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_CULL_FACE);

		GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
		GL11.glScalef(0.99F, 0.99F, 0.99F);

		GL11.glRotatef(angle, 0, 1, 0);

		bindTextureByName("/mods/yogpstop_qp/textures/blocks/refinery.png");

		GL11.glPushMatrix();
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		GL11.glTranslatef(-4F * pixel, 0, -4F * pixel);
		this.tank.render(pixel);
		GL11.glTranslatef(4F * pixel, 0, 4F * pixel);

		GL11.glTranslatef(-4F * pixel, 0, 4F * pixel);
		this.tank.render(pixel);
		GL11.glTranslatef(4F * pixel, 0, -4F * pixel);

		GL11.glTranslatef(4F * pixel, 0, 0);
		this.tank.render(pixel);
		GL11.glTranslatef(-4F * pixel, 0, 0);
		GL11.glPopMatrix();

		float trans1, trans2;

		if (anim <= 100) {
			trans1 = 12F * pixel * anim / 100F;
			trans2 = 0;
		} else if (anim <= 200) {
			trans1 = 12F * pixel - (12F * pixel * (anim - 100F) / 100F);
			trans2 = 12F * pixel * (anim - 100F) / 100F;
		} else {
			trans1 = 12F * pixel * (anim - 200F) / 100F;
			trans2 = 12F * pixel - (12F * pixel * (anim - 200F) / 100F);
		}

		GL11.glPushMatrix();
		GL11.glScalef(0.99F, 0.99F, 0.99F);
		GL11.glTranslatef(-0.51F, trans1 - 0.5F, -0.5F);
		theMagnet.render(pixel);
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glScalef(0.99F, 0.99F, 0.99F);
		GL11.glTranslatef(-0.51F, trans2 - 0.5F, 12F * pixel - 0.5F);
		theMagnet.render(pixel);
		GL11.glPopMatrix();

		if (tile != null) {
			GL11.glTranslatef(-4F * factor, 0, -4F * factor);

			if (liquid1 != null && liquid1.amount > 0) {
				int[] list1 = getDisplayLists(liquid1.itemID, liquid1.itemMeta, tile.worldObj);
				if (list1 != null) {
					setTextureFor(liquid1.itemID);
					GL11.glCallList(list1[(int) (liquid1.amount / (float) tile.buf * (displayStages - 1))]);
				}
			}

			GL11.glTranslatef(4F * factor, 0, 4F * factor);
			GL11.glTranslatef(-4F * factor, 0, 4F * factor);

			if (liquid2 != null && liquid2.amount > 0) {
				int[] list2 = getDisplayLists(liquid2.itemID, liquid2.itemMeta, tile.worldObj);
				if (list2 != null) {
					setTextureFor(liquid2.itemID);
					GL11.glCallList(list2[(int) (liquid2.amount / (float) tile.buf * (displayStages - 1))]);
				}
			}

			GL11.glTranslatef(4F * factor, 0, -4F * factor);
			GL11.glTranslatef(4F * factor, 0, 0);

			if (liquidResult != null && liquidResult.amount > 0) {
				int[] list3 = getDisplayLists(liquidResult.itemID, liquidResult.itemMeta, tile.worldObj);
				if (list3 != null) {
					setTextureFor(liquidResult.itemID);
					GL11.glCallList(list3[(int) (liquidResult.amount / (float) tile.buf * (displayStages - 1))]);
				}
			}
			GL11.glTranslatef(-4F * factor, 0, 0);
		}

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
		if (block.getRenderType() != getRenderId()) return;
		render(null, -0.5, -0.5, -0.5);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		return true;
	}

	@Override
	public boolean shouldRender3DInInventory() {
		return true;
	}

	private final int renderID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public int getRenderId() {
		return this.renderID;
	}

	static final float factor = (float) (1.0 / 16.0);
	final static private int displayStages = 100;

	private HashMap<Integer, HashMap<Integer, int[]>> stage = new HashMap<Integer, HashMap<Integer, int[]>>();

	private int[] getDisplayLists(int liquidId, int damage, World world) {
		if (this.stage.containsKey(liquidId)) {
			HashMap<Integer, int[]> x = this.stage.get(liquidId);
			if (x.containsKey(damage)) return x.get(damage);
		} else {
			this.stage.put(liquidId, new HashMap<Integer, int[]>());
		}

		int[] d = new int[displayStages];
		this.stage.get(liquidId).put(damage, d);

		BlockInterface block = new BlockInterface();

		// Retrieve the texture depending on type of item.
		if (liquidId < Block.blocksList.length && Block.blocksList[liquidId] != null) {
			block.texture = Block.blocksList[liquidId].getBlockTextureFromSideAndMetadata(0, damage);
		} else if (Item.itemsList[liquidId] != null) {
			block.texture = Item.itemsList[liquidId].getIconFromDamage(damage);
		} else return null;

		for (int s = 0; s < displayStages; ++s) {
			d[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d[s], 4864 /* GL_COMPILE */);

			block.minX = 0.5 - 4F * factor + 0.01;
			block.minY = 0;
			block.minZ = 0.5 - 4F * factor + 0.01;

			block.maxX = 0.5 + 4F * factor - 0.01;
			block.maxY = (float) s / (float) displayStages;
			block.maxZ = 0.5 + 4F * factor - 0.01;

			RenderEntityBlock.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();
		}

		return d;
	}

	public static void setTextureFor(int liquidId) {
		if (liquidId < Block.blocksList.length && Block.blocksList[liquidId] != null) {
			ForgeHooksClient.bindTexture(Block.blocksList[liquidId].getTextureFile(), 0);
		} else {
			ForgeHooksClient.bindTexture(Item.itemsList[liquidId].getTextureFile(), 0);
		}
	}
}
