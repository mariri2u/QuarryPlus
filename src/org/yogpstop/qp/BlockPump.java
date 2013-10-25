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

import java.util.ArrayList;

import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.Material;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Packet3Chat;
import net.minecraft.src.TileEntity;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.World;

public class BlockPump extends BlockContainer {

	public BlockPump(int i) {
		super(i, Material.iron);
		setHardness(5F);
		setCreativeTab(CreativeTabs.tabRedstone);
		setBlockName("PumpPlus");
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TilePump();
	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int i, int j) {
		switch (i) {
		case 0:
			return 65;
		case 1:
			return 66;
		default:
			return 64;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBlockTexture(IBlockAccess ba, int x, int y, int z, int side) {
		TileEntity tile = ba.getBlockTileEntity(x, y, z);
		if (tile instanceof TilePump && side == 1) {
			if (((TilePump) tile).G_working()) return 68;
			if (((TilePump) tile).G_connected() != null) return 67;
		}
		return super.getBlockTexture(ba, x, y, z, side);
	}

	@Override
	public String getTextureFile() {
		return "/mods/yogpstop_qp/textures/textures.png";
	}

	private final ArrayList<ItemStack> drop = new ArrayList<ItemStack>();

	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int meta) {
		this.drop.clear();
		TilePump tile = (TilePump) world.getBlockTileEntity(x, y, z);
		if (world.isRemote || tile == null) return;
		int count = quantityDropped(meta, 0, world.rand);
		int id1 = idDropped(meta, world.rand, 0);
		if (id1 > 0) {
			for (int i = 0; i < count; i++) {
				ItemStack is = new ItemStack(id1, 1, damageDropped(meta));
				EnchantmentHelper.enchantmentToIS(tile, is);
				this.drop.add(is);
			}
		}
		super.breakBlock(world, x, y, z, id, meta);
	}

	@Override
	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune) {
		return this.drop;
	}

	@Override
	public void onBlockPlacedBy(World w, int x, int y, int z, EntityLiving el) {
		super.onBlockPlacedBy(w, x, y, z, el);
		EnchantmentHelper.init((IEnchantableTile) w.getBlockTileEntity(x, y, z), el.getHeldItem().getEnchantmentTagList());
	}

	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, int bid) {
		((TilePump) w.getBlockTileEntity(x, y, z)).G_reinit();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int side, float par7, float par8, float par9) {
		Item equipped = ep.getCurrentEquippedItem() != null ? ep.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(ep, x, y, z)) {
			((IToolWrench) equipped).wrenchUsed(ep, x, y, z);
			if (world.isRemote) return true;
			((TilePump) world.getBlockTileEntity(x, y, z)).S_changeRange(ep);
			return true;
		}
		if (equipped instanceof ItemTool) {
			if (ep.getCurrentEquippedItem().getItemDamage() == 0) {
				if (world.isRemote) return true;
				for (String s : ((TilePump) world.getBlockTileEntity(x, y, z)).C_getNames())
					PacketDispatcher.sendPacketToPlayer(new Packet3Chat(s), (Player) ep);
				for (String s : EnchantmentHelper.getEnchantmentsChat((IEnchantableTile) world.getBlockTileEntity(x, y, z)))
					PacketDispatcher.sendPacketToPlayer(new Packet3Chat(s), (Player) ep);
				return true;
			}
			if (ep.getCurrentEquippedItem().getItemDamage() == 2) {
				if (!world.isRemote) ((TilePump) world.getBlockTileEntity(x, y, z)).S_OpenGUI(side, ep);
				return true;
			}
		}
		return false;
	}
}
