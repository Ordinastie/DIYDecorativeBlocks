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

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;

/**
 * @author Ordinastie
 * 
 */
public class DDBBlock extends Block
{
	private BlockPack pack;
	private BlockDescriptor descriptor;

	protected DDBBlock(BlockPack pack, BlockDescriptor descriptor)
	{
		super(descriptor.getMaterial());
		this.pack = pack;
		this.descriptor = descriptor;

		setBlockName(pack.getName() + "_" + descriptor.name);
		setHardness(descriptor.hardness);
		setStepSound(descriptor.getSoundType());

		setCreativeTab(DDB.tab);
	}

	public String getName()
	{
		return getUnlocalizedName().substring(5);
	}

	@Override
	public void registerBlockIcons(IIconRegister register)
	{
		DDBIcon icon = new DDBIcon(getName(), getTexturePath());
		icon.register((TextureMap) register);
		this.blockIcon = icon;
	}

	public String getTexturePath()
	{
		return pack.getDirectory() + (descriptor.textureName != null ? descriptor.textureName : descriptor.name);
	}
}
