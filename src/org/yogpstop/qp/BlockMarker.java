/*
 * Copyright (C) 2012,2013 yogpstop
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the
 * GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.yogpstop.qp;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import static buildcraft.BuildCraftCore.markerModel;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.Material;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.Packet3Chat;
import net.minecraft.src.TileEntity;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.StatCollector;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockMarker extends BlockContainer {
	private static final ForgeDirection[] m2fd = new ForgeDirection[] { ForgeDirection.DOWN, ForgeDirection.EAST, ForgeDirection.WEST, ForgeDirection.SOUTH,
			ForgeDirection.NORTH, ForgeDirection.UP };
	private static final byte[] s2m = new byte[] { 0, 5, 4, 3, 2, 1 };

	public BlockMarker(int i) {
		super(i, Material.circuits);
		setLightValue(0.5F);
		setCreativeTab(CreativeTabs.tabRedstone);
		setBlockName("MarkerPlus");
		this.blockIndexInTexture = 16;
	}

	@Override
	public int getRenderType() {
		return markerModel;
	}

	private static AxisAlignedBB getBoundingBox(int meta) {
		double w = 0.15;
		double h = 0.65;

		ForgeDirection dir = m2fd[meta];
		switch (dir) {
		case DOWN:
			return AxisAlignedBB.getBoundingBox(0.5F - w, 1F - h, 0.5F - w, 0.5F + w, 1F, 0.5F + w);
		case UP:
			return AxisAlignedBB.getBoundingBox(0.5F - w, 0F, 0.5F - w, 0.5F + w, h, 0.5F + w);
		case SOUTH:
			return AxisAlignedBB.getBoundingBox(0.5F - w, 0.5F - w, 0F, 0.5F + w, 0.5F + w, h);
		case NORTH:
			return AxisAlignedBB.getBoundingBox(0.5F - w, 0.5F - w, 1 - h, 0.5F + w, 0.5F + w, 1);
		case EAST:
			return AxisAlignedBB.getBoundingBox(0F, 0.5F - w, 0.5F - w, h, 0.5F + w, 0.5F + w);
		default:
			return AxisAlignedBB.getBoundingBox(1 - h, 0.5F - w, 0.5F - w, 1F, 0.5F + w, 0.5F + w);
		}
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		AxisAlignedBB bb = getBoundingBox(meta);
		setBlockBounds((float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k) {
		return null;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
		ForgeDirection dir = ForgeDirection.getOrientation(side);
		return world.isBlockSolidOnSide(x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ, dir.getOpposite());
	}

	@Override
	public int func_85104_a(World world, int x, int y, int z, int side, float par6, float par7, float par8, int meta) {
		return s2m[side];
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);
		dropTorchIfCantStay(world, x, y, z);
	}

	private void dropTorchIfCantStay(World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		if (!canPlaceBlockOnSide(world, x, y, z, m2fd[meta].ordinal())) {
			dropBlockAsItem(world, x, y, z, this.blockID, 0);
			world.setBlock(x, y, z, 0);
		}
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TileMarker();
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockId) {
		((TileMarker) world.getBlockTileEntity(x, y, z)).G_updateSignal();
		dropTorchIfCantStay(world, x, y, z);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int par6, float par7, float par8, float par9) {
		if (!world.isRemote) {
			Item equipped = ep.getCurrentEquippedItem() != null ? ep.getCurrentEquippedItem().getItem() : null;
			if (equipped instanceof ItemTool && ep.getCurrentEquippedItem().getItemDamage() == 0) {
				TileMarker.Link l = ((TileMarker) world.getBlockTileEntity(x, y, z)).link;
				if (l == null) return true;
				PacketDispatcher.sendPacketToPlayer(new Packet3Chat(StatCollector.translateToLocal("chat.markerarea")), (Player) ep);
				PacketDispatcher.sendPacketToPlayer(new Packet3Chat(String.format("x:%d y:%d z:%d - x:%d y:%d z:%d", l.xn, l.yn, l.zn, l.xx, l.yx, l.zx)),
						(Player) ep);
				return true;
			}
			((TileMarker) world.getBlockTileEntity(x, y, z)).S_tryConnection();
		}
		return true;
	}

	@Override
	public void func_85105_g(World world, int x, int y, int z, int meta) {
		((TileMarker) world.getBlockTileEntity(x, y, z)).requestTicket();
		super.func_85105_g(world, x, y, z, meta);
	}

	@Override
	public String getTextureFile() {
		return "/mods/yogpstop_qp/textures/textures.png";
	}
}
