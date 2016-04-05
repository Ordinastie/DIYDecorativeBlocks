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

import net.malisis.core.block.component.SlabComponent;
import net.malisis.ddb.block.DDBBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;

import com.google.gson.internal.LinkedTreeMap;

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
	public DDBRecipe recipe;
	public DDBSmeltingRecipe furnaceRecipe;

	public void createBlock(BlockPack pack)
	{
		DDBBlock block = new DDBBlock(pack, this);
		if (type == BlockType.SLAB)
			new SlabComponent(block, new DDBBlock(pack, this));

		pack.addBlock(block);
	}

	public Material getMaterial()
	{
		Material mat = materials.get(material);
		return mat != null ? mat : Material.wood;
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
