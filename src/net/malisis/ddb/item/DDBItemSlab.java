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

package net.malisis.ddb.item;

import net.malisis.ddb.block.DDBBlock;
import net.malisis.ddb.block.DDBSlab;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public class DDBItemSlab extends DDBItem
{

	public DDBItemSlab(Block block)
	{
		super((DDBBlock) block);
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if (itemStack.stackSize == 0 || !player.canPlayerEdit(x, y, z, side, itemStack))
			return false;

		ForgeDirection dir = ForgeDirection.getOrientation(side);
		x = (int) Math.floor(x + hitX + dir.offsetX * 0.1F);
		y = (int) Math.floor(y + hitY + dir.offsetY * 0.1F);
		z = (int) Math.floor(z + hitZ + dir.offsetZ * 0.1F);

		Block block = world.getBlock(x, y, z);
		int metadata = world.getBlockMetadata(x, y, z);
		boolean top = (metadata & DDBSlab.TOP_BLOCK) != 0;

		if (block == getBlock())
			metadata |= top ? DDBSlab.BOTTOM_BLOCK : DDBSlab.TOP_BLOCK;
		else
		{
			if (side != 0 && side != 1)
				metadata = hitY < 0.5D ? DDBSlab.BOTTOM_BLOCK : DDBSlab.TOP_BLOCK;
			else
				metadata = side == 1 ? DDBSlab.BOTTOM_BLOCK : DDBSlab.TOP_BLOCK;
		}

		if ((block == getBlock() || block == Blocks.air) && isCollidingEntity(world, x, y, z) && placeBlock(world, x, y, z, metadata))
		{
			playSound(world, x, y, z);
			--itemStack.stackSize;
			return true;
		}

		return super.onItemUse(itemStack, player, world, x, y, z, side, hitX, hitY, hitZ);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean func_150936_a(World world, int x, int y, int z, int side, EntityPlayer p_150936_6_, ItemStack p_150936_7_)
	{
		return true;
	}

	private boolean isCollidingEntity(World world, int x, int y, int z)
	{
		return world.checkNoEntityCollision(getBlock().getCollisionBoundingBoxFromPool(world, x, y, z));
	}

	private boolean placeBlock(World world, int x, int y, int z, int metadata)
	{
		return world.setBlock(x, y, z, getBlock(), world.getBlockMetadata(x, y, z) | metadata, 3);
	}

	private void playSound(World world, int x, int y, int z)
	{
		world.playSoundEffect(x + 0.5F, y + 0.5F, z + 0.5F, getBlock().stepSound.getPlaceSound(),
				(getBlock().stepSound.getVolume() + 1.0F) / 2.0F, getBlock().stepSound.getFrequency() * 0.8F);
	}
}
