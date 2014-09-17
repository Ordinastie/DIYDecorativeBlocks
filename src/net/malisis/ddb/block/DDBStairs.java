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

import java.util.ArrayList;
import java.util.List;

import net.malisis.ddb.BlockDescriptor;
import net.malisis.ddb.BlockPack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Ordinastie
 * 
 */
public class DDBStairs extends DDBBlock
{
	private static final int FLAG_TOPBLOCK = 4;
	private static ForgeDirection[] directions = new ForgeDirection[] { ForgeDirection.EAST, ForgeDirection.WEST, ForgeDirection.SOUTH,
			ForgeDirection.NORTH };

	public static int renderId;

	public DDBStairs(BlockPack pack, BlockDescriptor descriptor)
	{
		super(pack, descriptor);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack)
	{
		int dir = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		int topBlock = world.getBlockMetadata(x, y, z) & FLAG_TOPBLOCK;
		int[] dirs = { 2, 1, 3, 0 };
		world.setBlockMetadataWithNotify(x, y, z, dirs[dir] | topBlock, 2);
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{

		return side != 0 && (side == 1 || hitY <= 0.5D) ? metadata : metadata | FLAG_TOPBLOCK;
	}

	public ForgeDirection getDirection(int metadata)
	{
		return directions[metadata & 3];
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		//this.setBlockBounds(0, 0, 0, 1, 1, 1);
	}

	private boolean shouldConnect(Block block, ForgeDirection from, ForgeDirection to)
	{
		if (!(block instanceof BlockStairs || block instanceof DDBStairs))
			return false;

		switch (from)
		{
			case NORTH:
			case SOUTH:
				return to == ForgeDirection.EAST || to == ForgeDirection.WEST;
			case EAST:
			case WEST:
				return to == ForgeDirection.NORTH || to == ForgeDirection.SOUTH;
			default:
				return false;
		}
	}

	public List<AxisAlignedBB> getBounds(IBlockAccess world, int x, int y, int z)
	{
		List<AxisAlignedBB> list = new ArrayList<>();
		list.add(getBaseBounds(world, x, y, z));
		list.addAll(getStepBounds(world, x, y, z));

		return list;
	}

	private AxisAlignedBB getBaseBounds(IBlockAccess world, int x, int y, int z)
	{
		int metadata = world.getBlockMetadata(x, y, z);
		float oy = (metadata & FLAG_TOPBLOCK) != 0 ? 0.5F : 0;
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(0, oy, 0, 1, oy + 0.5F, 1);
		return aabb;
	}

	private List<AxisAlignedBB> getStepBounds(IBlockAccess world, int x, int y, int z)
	{
		List<AxisAlignedBB> list = new ArrayList<>();
		Block block;
		int metadata = world.getBlockMetadata(x, y, z);
		ForgeDirection dir = getDirection(metadata);
		boolean topBlock = (metadata & FLAG_TOPBLOCK) != 0;

		float ox = 0;
		float oy = topBlock ? 0 : 0.5F;
		float oz = 0;
		float oX = 1;
		float oY = topBlock ? 0.5F : 1;
		float oZ = 1;

		switch (dir)
		{
			case NORTH:
				oZ = 0.5F;
				break;
			case SOUTH:
				oz = 0.5F;
				break;
			case EAST:
				ox = 0.5F;
				break;
			case WEST:
				oX = 0.5F;
				break;

			default:
				break;
		}

		//check same side : 
		block = world.getBlock(x + dir.offsetX, y, z + dir.offsetZ);
		ForgeDirection dir2 = getDirection(world.getBlockMetadata(x + dir.offsetX, y, z + dir.offsetZ));
		if (shouldConnect(block, dir, dir2))
		{
			//cut the corner from default bb
			switch (dir2)
			{
				case WEST:
					oX = 0.5F;
					break;
				case EAST:
					ox = 0.5F;
					break;
				case NORTH:
					oZ = 0.5F;
					break;
				case SOUTH:
					oz = 0.5F;
					break;
				default:
					break;
			}

			list.add(AxisAlignedBB.getBoundingBox(ox, oy, oz, oX, oY, oZ));
			return list;
		}

		//check other side : 
		block = world.getBlock(x + dir.getOpposite().offsetX, y, z + dir.getOpposite().offsetZ);
		dir2 = getDirection(world.getBlockMetadata(x + dir.getOpposite().offsetX, y, z + dir.getOpposite().offsetZ));
		if (shouldConnect(block, dir, dir2))
		{
			//add default bb
			list.add(AxisAlignedBB.getBoundingBox(ox, oy, oz, oX, oY, oZ));

			//add extra corner
			ox += dir.getOpposite().offsetX * 0.5F;
			oX += dir.getOpposite().offsetX * 0.5F;
			oy += dir.getOpposite().offsetY * 0.5F;
			oY += dir.getOpposite().offsetY * 0.5F;
			oz += dir.getOpposite().offsetZ * 0.5F;
			oZ += dir.getOpposite().offsetZ * 0.5F;

			switch (dir2)
			{
				case WEST:
					oX = 0.5F;
					break;
				case EAST:
					ox = 0.5F;
					break;
				case NORTH:
					oZ = 0.5F;
					break;
				case SOUTH:
					oz = 0.5F;
					break;
				default:
					break;
			}

			list.add(AxisAlignedBB.getBoundingBox(ox, oy, oz, oX, oY, oZ));
			return list;
		}

		list.add(AxisAlignedBB.getBoundingBox(ox, oy, oz, oX, oY, oZ));
		return list;
	}

	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List list, Entity entity)
	{
		for (AxisAlignedBB aabb : getBounds(world, x, y, z))
		{
			aabb.offset(x, y, z);
			if (mask.intersectsWith(aabb))
				list.add(aabb);
		}
	}

	/**
	 * Ray traces through the blocks collision from start vector to end vector returning a ray trace hit. Args: world, x, y, z, startVec,
	 * endVec
	 */
	@Override
	public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 src, Vec3 dest)
	{
		List<AxisAlignedBB> listBounds = getBounds(world, x, y, z);
		MovingObjectPosition[] listMop = new MovingObjectPosition[listBounds.size()];

		int i = 0;
		for (AxisAlignedBB aabb : listBounds)
		{
			setBlockBounds((float) aabb.minX, (float) aabb.minY, (float) aabb.minZ, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ);
			listMop[i++] = super.collisionRayTrace(world, x, y, z, src, dest);
		}

		double distance = 1;

		MovingObjectPosition mop = null;
		for (MovingObjectPosition m : listMop)
		{
			if (m != null)
			{
				double d = m.hitVec.squareDistanceTo(dest);
				if (d > distance)
				{
					mop = m;
					distance = d;
				}
			}
		}

		return mop;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return renderId;
	}
}
