package org.yogpstop.qp;

import net.minecraft.src.Block;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BlockInfMJSrc extends BlockContainer {

	public BlockInfMJSrc(int par1) {
		super(par1, Material.iron);
		setHardness(1.5F);
		setResistance(10F);
		setStepSound(soundStoneFootstep);
		setCreativeTab(CreativeTabs.tabRedstone);
		setBlockName("InfMJSrc");
		this.blockIndexInTexture = Block.portal.blockIndexInTexture;
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileInfMJSrc();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int par6, float par7, float par8, float par9) {
		if (!world.isRemote) return true;
		ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdInfMJSrc, world, x, y, z);
		return true;
	}
}
