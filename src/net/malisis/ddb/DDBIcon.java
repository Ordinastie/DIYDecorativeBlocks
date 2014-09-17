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

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.malisis.core.renderer.MalisisIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

/**
 * @author Ordinastie
 * 
 */
public class DDBIcon extends MalisisIcon
{
	private String path;
	private BlockPack pack;

	public DDBIcon(String name, BlockPack pack, String path)
	{
		super(name);
		this.pack = pack;
		this.path = path;
	}

	@Override
	public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location)
	{
		return true;
	}

	@Override
	public boolean load(IResourceManager manager, ResourceLocation location)
	{
		// get mipmapping level
		int mipmapLevels = Minecraft.getMinecraft().gameSettings.mipmapLevels;
		boolean anisotropic = Minecraft.getMinecraft().gameSettings.anisotropicFiltering > 1.0F;

		try
		{
			BufferedImage[] textures = new BufferedImage[1 + mipmapLevels];
			textures[0] = ImageIO.read(pack.getInputStream(path + ".png"));
			loadSprite(textures, null, anisotropic);
			return false;
		}
		catch (RuntimeException e)
		{
			DDB.log.error("Unable to parse metadata from " + getIconName(), e.getMessage());
			return true;
		}
		catch (IOException e)
		{
			DDB.log.error("Using missing texture, unable to load " + getIconName(), e.getMessage());
			return true;
		}
	}

	@Override
	public void initSprite(int width, int height, int x, int y, boolean rotated)
	{
		super.initSprite(width, height, x, y, rotated);
	}
}
