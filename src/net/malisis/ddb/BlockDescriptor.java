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

package net.malisis.ddb;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.block.material.Material;

/**
 * @author Ordinastie
 * 
 */
public class BlockDescriptor
{
	private static HashMap<String, Material> materials = new HashMap<>();
	private static HashMap<String, SoundType> soundTypes = new HashMap<>();
	static
	{
		materials.put("air", Material.air);
		materials.put("grass", Material.grass);
		materials.put("ground", Material.ground);
		materials.put("wood", Material.wood);
		materials.put("rock", Material.rock);
		materials.put("iron", Material.iron);
		materials.put("anvil", Material.anvil);
		materials.put("water", Material.water);
		materials.put("lava", Material.lava);
		materials.put("leaves", Material.leaves);
		materials.put("plants", Material.plants);
		materials.put("vine", Material.vine);
		materials.put("sponge", Material.sponge);
		materials.put("cloth", Material.cloth);
		materials.put("fire", Material.fire);
		materials.put("sand", Material.sand);
		materials.put("circuits", Material.circuits);
		materials.put("carpet", Material.carpet);
		materials.put("glass", Material.glass);
		materials.put("redstoneLight", Material.redstoneLight);
		materials.put("tnt", Material.tnt);
		materials.put("coral", Material.coral);
		materials.put("ice", Material.ice);
		materials.put("packedIce", Material.packedIce);
		materials.put("snow", Material.snow);
		materials.put("craftedSnow", Material.craftedSnow);
		materials.put("cactus", Material.cactus);
		materials.put("clay", Material.clay);
		materials.put("gourd", Material.gourd);
		materials.put("dragonEgg", Material.dragonEgg);
		materials.put("portal", Material.portal);
		materials.put("cake", Material.cake);
		materials.put("web", Material.web);

		soundTypes.put("stone", Block.soundTypeStone);
		soundTypes.put("wood", Block.soundTypeWood);
		soundTypes.put("gravel", Block.soundTypeGravel);
		soundTypes.put("grass", Block.soundTypeGrass);
		soundTypes.put("piston", Block.soundTypePiston);
		soundTypes.put("metal", Block.soundTypeMetal);
		soundTypes.put("glass", Block.soundTypeGlass);
		soundTypes.put("cloth", Block.soundTypeCloth);
		soundTypes.put("sand", Block.soundTypeSand);
		soundTypes.put("snow", Block.soundTypeSnow);
		soundTypes.put("ladder", Block.soundTypeLadder);
		soundTypes.put("anvil", Block.soundTypeAnvil);
	}

	public BlockType type = BlockType.STANDARD;
	public String name;
	public String textureName = name;
	public String material;
	public float hardness = 3.0F;
	public String soundType;

	public DDBBlock createBlock(BlockPack pack)
	{
		DDBBlock block = null;
		switch (type)
		{
			case STANDARD:
				block = new DDBBlock(pack, this);
		}

		return block;
	}

	public Material getMaterial()
	{
		Material mat = materials.get(material);
		return mat != null ? mat : Material.wood;
	}

	public SoundType getSoundType()
	{
		SoundType sound = soundTypes.get(soundType);
		return sound != null ? sound : Block.soundTypeWood;
	}
}
