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

import java.util.List;

import net.malisis.ddb.BlockDescriptor;
import net.malisis.ddb.BlockPack;
import net.malisis.ddb.DDBIcon;
import net.malisis.ddb.item.DDBItemColored;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public class DDBBlockColored extends DDBBlock
{

	public DDBBlockColored(BlockPack pack, BlockDescriptor descriptor)
	{
		super(pack, descriptor);
	}

	@Override
	public Class getItemClass()
	{
		return DDBItemColored.class;
	}

	/**
	 * Gets the block's texture. Args: side, meta
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata)
	{
		if (descriptor.useColorMultiplier)
			return super.getIcon(side, metadata);
		else
			return icons[metadata];
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack)
	{
		world.setBlockMetadataWithNotify(x, y, z, itemStack.getMetadata(), 2);
	}

	@Override
	public int damageDropped(int metadata)
	{
		return metadata;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list)
	{
		for (int i = 0; i < 16; ++i)
		{
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register)
	{
		if (descriptor.useColorMultiplier)
		{
			super.registerIcons(register);
			return;
		}

		icons = new DDBIcon[16];

		for (int i = 0; i < this.icons.length; i++)
		{
			String color = ItemDye.dyeIcons[~i & 15]; //reverse the order from the array
			String name = getName() + "_" + color;
			DDBIcon icon = new DDBIcon(name, pack, descriptor.getTexture() + "_" + color);
			icon.register((TextureMap) register);
			this.icons[i] = icon;
		}
	}

	@Override
	public int getRenderColor(int metadata)
	{
		if (descriptor.useColorMultiplier)
			return ItemDye.dyeColors[~metadata & 15];
		else
			return 0xFFFFFF;
	}

	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z)
	{
		return getRenderColor(world.getBlockMetadata(x, y, z));
	}

	@Override
	public MapColor getMapColor(int metadata)
	{
		return MapColor.getMapColorForBlockColored(metadata);
	}
}
