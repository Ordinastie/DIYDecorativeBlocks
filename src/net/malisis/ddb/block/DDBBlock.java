/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.ddb.block;

import net.malisis.core.util.EntityUtils;
import net.malisis.ddb.BlockDescriptor;
import net.malisis.ddb.BlockPack;
import net.malisis.ddb.BlockType;
import net.malisis.ddb.DDB;
import net.malisis.ddb.DDBIcon;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Ordinastie
 * 
 */
public class DDBBlock extends Block
{
	protected BlockPack pack;
	protected BlockDescriptor descriptor;
	protected DDBIcon[] icons;

	//	private IIcon testIcon;

	public DDBBlock(BlockPack pack, BlockDescriptor descriptor)
	{
		super(descriptor.getMaterial());
		this.pack = pack;
		this.descriptor = descriptor;
		this.opaque = descriptor.opaque || descriptor.translucent;

		setBlockName(pack.getName() + "_" + descriptor.name);
		setHardness(descriptor.hardness);
		setStepSound(descriptor.getSoundType());

		setCreativeTab(DDB.tab);
	}

	public String getName()
	{
		return getUnlocalizedName().substring(5);
	}

	public BlockType getBlockType()
	{
		return descriptor.type;
	}

	public Class getItemClass()
	{
		return ItemBlock.class;
	}

	@Override
	public void registerBlockIcons(IIconRegister register)
	{
		icons = new DDBIcon[6];
		boolean registerBlockIcon = false;

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			String textureName = descriptor.getTexture(dir);
			if (textureName != null)
			{
				icons[dir.ordinal()] = (DDBIcon) new DDBIcon(getName() + "_" + dir.toString(), pack, textureName)
						.register((TextureMap) register);
			}
			else
				registerBlockIcon = true;
		}

		if (registerBlockIcon)
			blockIcon = new DDBIcon(getName(), pack, descriptor.getTexture()).register((TextureMap) register);
	}

	@Override
	public IIcon getIcon(int side, int metadata)
	{
		IIcon icon = null;
		if (descriptor.type == BlockType.DIRECTIONAL)
		{
			if (side == 0 || side == 1)
				icon = icons[side];
			else
				icon = side == metadata + 2 ? icons[ForgeDirection.SOUTH.ordinal()] : icons[ForgeDirection.EAST.ordinal()];
		}
		else if (icons != null)
			icon = icons[side];

		return icon != null ? icon : blockIcon;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack)
	{
		if (descriptor.type != BlockType.DIRECTIONAL)
			return;

		ForgeDirection dir = EntityUtils.getEntityFacing(player, false);
		world.setBlockMetadataWithNotify(x, y, z, dir.getOpposite().ordinal() - 2, 3);
	}

	@Override
	public boolean isOpaqueCube()
	{
		return opaque;
	}

	@Override
	public int getRenderBlockPass()
	{
		return descriptor.translucent ? 1 : 0;
	}
}
