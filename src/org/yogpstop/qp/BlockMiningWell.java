package org.yogpstop.qp;

import java.util.ArrayList;

import buildcraft.api.tools.IToolWrench;
import static buildcraft.core.CreativeTabBuildCraft.tabBuildCraft;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockMiningWell extends BlockContainer {

	public BlockMiningWell(int par1) {
		super(par1, Material.ground);
		setHardness(1.5F);
		setResistance(10F);
		setCreativeTab(tabBuildCraft);
		setStepSound(soundStoneFootstep);
		setBlockName("MiningWellPlus");
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileMiningWell();
	}

	@Override
	public String getTextureFile() {
		return "/mods/yogpstop_qp/textures/textures.png";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBlockTexture(IBlockAccess ba, int x, int y, int z, int side) {
		TileEntity tile = ba.getBlockTileEntity(x, y, z);
		if (tile instanceof TileMiningWell && side == 1 && ((TileMiningWell) tile).isActive()) return 52;
		return super.getBlockTexture(ba, x, y, z, side);
	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int i, int j) {
		if (j == 0 && i == 3) return 49;
		if (i == 1) return 51;
		else if (i == 0) return 50;
		else if (i == j) return 49;
		else if (j >= 0 && j < 6 && ForgeDirection.values()[j].getOpposite().ordinal() == i) return 50;
		else return 48;
	}

	@Override
	public void onBlockPlacedBy(World w, int x, int y, int z, EntityLiving el) {
		super.onBlockPlacedBy(w, x, y, z, el);
		ForgeDirection orientation = get2dOrientation(el.posX, el.posZ, x, z);
		w.setBlockMetadata(x, y, z, orientation.getOpposite().ordinal());
		((TileMiningWell) w.getBlockTileEntity(x, y, z)).G_init(el.getHeldItem().getEnchantmentTagList());
	}

	private static ForgeDirection get2dOrientation(double x1, double z1, double x2, double z2) {
		double Dx = x1 - x2;
		double Dz = z1 - z2;
		double angle = Math.atan2(Dz, Dx) / Math.PI * 180 + 180;

		if (angle < 45 || angle > 315) return ForgeDirection.EAST;
		else if (angle < 135) return ForgeDirection.SOUTH;
		else if (angle < 225) return ForgeDirection.WEST;
		else return ForgeDirection.NORTH;
	}

	private final ArrayList<ItemStack> drop = new ArrayList<ItemStack>();

	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int meta) {
		this.drop.clear();
		TileMiningWell tmw = (TileMiningWell) world.getBlockTileEntity(x, y, z);
		if (world.isRemote || tmw == null) return;
		int count = quantityDropped(meta, 0, world.rand);
		int id1 = idDropped(meta, world.rand, 0);
		if (id1 > 0) {
			for (int i = 0; i < count; i++) {
				ItemStack is = new ItemStack(id1, 1, damageDropped(meta));
				tmw.S_setEnchantment(is);
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
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int side, float par7, float par8, float par9) {
		Item equipped = ep.getCurrentEquippedItem() != null ? ep.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(ep, x, y, z)) {
			((TileMiningWell) world.getBlockTileEntity(x, y, z)).G_reinit();
			((IToolWrench) equipped).wrenchUsed(ep, x, y, z);
			return true;
		}
		if (equipped instanceof ItemTool && ep.getCurrentEquippedItem().getItemDamage() == 0) {
			if (world.isRemote) return true;
			PacketDispatcher.sendPacketToPlayer(new Packet3Chat(StatCollector.translateToLocal("chat.plusenchant")), (Player) ep);
			for (String s : ((TileMiningWell) world.getBlockTileEntity(x, y, z)).C_getEnchantments())
				PacketDispatcher.sendPacketToPlayer(new Packet3Chat(s), (Player) ep);
			return true;
		}
		return false;
	}
}
