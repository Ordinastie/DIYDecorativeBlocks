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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.AnimationMetadataSectionSerializer;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Ordinastie
 *
 */
public class DDBIcon extends MalisisIcon
{
	private final static IMetadataSerializer serializer = new IMetadataSerializer();
	static
	{
		serializer.registerMetadataSectionType(new AnimationMetadataSectionSerializer(), AnimationMetadataSection.class);
	}

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

		try
		{
			InputStream stream = pack.getInputStream(path + ".png");
			if (stream == null)
			{
				DDB.log.error("Using missing texture, file not found : " + path);
				return true;
			}

			BufferedImage[] textures = new BufferedImage[1 + mipmapLevels];
			textures[0] = ImageIO.read(stream);

			AnimationMetadataSection animMetadata = readAnimation();

			loadSprite(textures, animMetadata);
			return false;
		}
		catch (IOException e)
		{
			DDB.log.error("Using missing texture, unable to load " + getIconName(), e.getMessage());
			return true;
		}
	}

	/**
	 * Reads the .mcmeta file for this {@link DDBIcon}.
	 *
	 * @return
	 */
	private AnimationMetadataSection readAnimation()
	{
		BufferedReader bufferedreader = null;
		try
		{
			InputStream stream = pack.getInputStream(path + ".png.mcmeta");
			if (stream == null)
				return null;
			bufferedreader = new BufferedReader(new InputStreamReader(stream));
			JsonObject json = (new JsonParser()).parse(bufferedreader).getAsJsonObject();

			AnimationMetadataSection animMetadata = (AnimationMetadataSection) serializer.parseMetadataSection("animation", json);
			return animMetadata;
		}
		catch (IOException e)
		{
			return null;
		}
		finally
		{
			IOUtils.closeQuietly(bufferedreader);
		}
	}
}
