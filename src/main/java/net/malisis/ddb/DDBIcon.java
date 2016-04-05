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
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.malisis.core.asm.AsmUtils;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.renderer.icon.VanillaIcon;
import net.malisis.core.util.ItemUtils;
import net.malisis.core.util.Silenced;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationFrame;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.AnimationMetadataSectionSerializer;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.item.ItemStack;
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
	private final static Field animationMetadataField = AsmUtils.changeFieldAccess(TextureAtlasSprite.class, "animationMetadata",
			"field_110982_k");

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
		try
		{
			loadSpriteFrames(null, Minecraft.getMinecraft().gameSettings.mipmapLevels + 1);
		}
		catch (Exception e)
		{
			DDB.log.error("Using loading texture " + path, e);
		}

		return false;
	}

	private void addFrame(BufferedImage img, int index, int mipmapLevels)
	{
		int[][] mipmaps = new int[mipmapLevels][];
		mipmaps[0] = getFrame(img, index);

		while (framesTextureData.size() <= index)
			framesTextureData.add(null);

		framesTextureData.set(index, mipmaps);
	}

	private int[] getFrame(BufferedImage img, int index)
	{
		int[] pixels = new int[width * height];
		int startY = height * index;
		if (startY < img.getHeight())
			img.getRGB(0, startY, width, height, pixels, 0, width);
		return pixels;
	}

	@Override
	public void loadSpriteFrames(IResource resource, int mipmapLevels) throws IOException
	{
		InputStream stream = pack.getInputStream(path + ".png");
		if (stream == null)
		{
			DDB.log.error("Using missing texture, file not found : " + path);
			return;
		}

		BufferedImage img = TextureUtil.readBufferedImage(stream);
		AnimationMetadataSection animMetadata = readAnimation();

		if (img == null)
		{
			DDB.log.error("Using missing texture, could not read file : " + path);
			return;
		}

		width = img.getWidth();
		height = width;

		if (animMetadata == null)
		{
			addFrame(img, 0, mipmapLevels);
			return;
		}

		int nbFrames = img.getHeight() / this.width;
		boolean hasFrameCount = animMetadata.getFrameCount() > 0;

		//add frames
		IntStream is = hasFrameCount ? getFrameIndexStream(animMetadata, nbFrames) : IntStream.rangeClosed(0, nbFrames);
		is.forEach(index -> addFrame(img, index, mipmapLevels));
		is.close();

		//make the AnimationFrame list
		IntStream is2 = hasFrameCount ? getFrameIndexStream(animMetadata, nbFrames) : IntStream.rangeClosed(0, nbFrames);
		List<AnimationFrame> list = is2.mapToObj(index -> new AnimationFrame(index, -1)).collect(Collectors.toList());
		is2.close();

		if (hasFrameCount)
			animMetadata = new AnimationMetadataSection(list, this.width, this.height, animMetadata.getFrameTime(),
					animMetadata.isInterpolate());

		saveAnimationMetadata(animMetadata);
	}

	private IntStream getFrameIndexStream(AnimationMetadataSection animMetadata, int nbFrames)
	{
		return animMetadata.getFrameIndexSet().stream().filter((index) -> index <= nbFrames).mapToInt(Integer::intValue);
	}

	private void saveAnimationMetadata(AnimationMetadataSection animationMetadata)
	{
		Silenced.exec(() -> animationMetadataField.set(this, animationMetadata));
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

	public static MalisisIcon getIcon(String name, BlockPack pack, String path)
	{
		if (path.indexOf(":") != -1)
		{
			ItemStack itemStack = ItemUtils.getItemStack(path);
			if (itemStack == null)
				return MalisisIcon.missing;

			return new VanillaIcon(itemStack.getItem(), itemStack.getMetadata());
		}

		return new DDBIcon(name, pack, path);
	}
}
