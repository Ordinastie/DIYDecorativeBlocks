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

import com.google.gson.internal.LinkedTreeMap;

import net.malisis.ddb.block.DDBBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;

/**
 * @author Ordinastie
 *
 */
public class BlockDescriptor
{
	private transient static HashMap<String, Material> materials = new HashMap<>();
	private transient static HashMap<String, SoundType> soundTypes = new HashMap<>();
	static
	{
		materials.put("air", Material.AIR);
		materials.put("grass", Material.GRASS);
		materials.put("ground", Material.GROUND);
		materials.put("wood", Material.WOOD);
		materials.put("rock", Material.ROCK);
		materials.put("iron", Material.IRON);
		materials.put("anvil", Material.ANVIL);
		materials.put("water", Material.WATER);
		materials.put("lava", Material.LAVA);
		materials.put("leaves", Material.LEAVES);
		materials.put("plants", Material.PLANTS);
		materials.put("vine", Material.VINE);
		materials.put("sponge", Material.SPONGE);
		materials.put("cloth", Material.CLOTH);
		materials.put("fire", Material.FIRE);
		materials.put("sand", Material.SAND);
		materials.put("circuits", Material.CIRCUITS);
		materials.put("carpet", Material.CARPET);
		materials.put("glass", Material.GLASS);
		materials.put("redstoneLight", Material.REDSTONE_LIGHT);
		materials.put("tnt", Material.TNT);
		materials.put("coral", Material.CORAL);
		materials.put("ice", Material.ICE);
		materials.put("packedIce", Material.PACKED_ICE);
		materials.put("snow", Material.SNOW);
		materials.put("craftedSnow", Material.CRAFTED_SNOW);
		materials.put("cactus", Material.CACTUS);
		materials.put("clay", Material.CLAY);
		materials.put("gourd", Material.GOURD);
		materials.put("dragonEgg", Material.DRAGON_EGG);
		materials.put("portal", Material.PORTAL);
		materials.put("cake", Material.CAKE);
		materials.put("web", Material.WEB);

		soundTypes.put("stone", SoundType.STONE);
		soundTypes.put("wood", SoundType.WOOD);
		soundTypes.put("gravel", SoundType.SAND);
		soundTypes.put("grass", SoundType.GROUND);
		soundTypes.put("piston", SoundType.PLANT);
		soundTypes.put("metal", SoundType.METAL);
		soundTypes.put("glass", SoundType.GLASS);
		soundTypes.put("cloth", SoundType.CLOTH);
		soundTypes.put("sand", SoundType.SAND);
		soundTypes.put("snow", SoundType.SNOW);
		soundTypes.put("ladder", SoundType.LADDER);
		soundTypes.put("anvil", SoundType.ANVIL);
		soundTypes.put("slime", SoundType.SLIME);
	}

	public BlockType type = BlockType.STANDARD;
	public String name;
	public String textureName = name;
	public LinkedTreeMap<String, String> textures;
	public LinkedTreeMap<String, String> megatextures;
	public String material;
	public float hardness = 2.0F;
	public String soundType;
	public boolean useColorMultiplier = false;
	public boolean opaque = true;
	public boolean translucent = false;
	public int lightValue = 0;
	public int numBlocks = -1;
	public DDBSmeltingRecipe furnaceRecipe;

	public void createBlock(BlockPack pack)
	{
		DDBBlock block = new DDBBlock(pack, this);

		pack.addBlock(block);
	}

	public Material getMaterial()
	{
		Material mat = materials.get(material);
		return mat != null ? mat : Material.WOOD;
	}

	public SoundType getSoundType()
	{
		SoundType sound = soundTypes.get(soundType);
		return sound != null ? sound : SoundType.WOOD;
	}

	public String getTexture()
	{
		return textureName != null ? textureName : name;
	}

	public String getTexture(String key)
	{
		return textures != null ? textures.get(key) : null;
	}

	public String getTexture(EnumFacing dir)
	{
		if (textures == null)
			return null;

		String textureName = null;
		if (dir == EnumFacing.DOWN)
			textureName = textures.get("bottom");
		else if (dir == EnumFacing.UP)
			textureName = textures.get("top");
		else
		{
			if (dir == EnumFacing.SOUTH)
				textureName = textures.get("front");
			if (textureName == null)
				textureName = textures.get("sides");
		}

		return textureName;
	}
}
