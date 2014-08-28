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

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import cpw.mods.fml.common.registry.GameRegistry;

/**
 * @author Ordinastie
 * 
 */
public class BlockPack
{
	public static String PACKDIR = "ddbpacks";
	public static HashMap<String, BlockPack> packs = new HashMap<>();

	private String name;
	private boolean loaded = false;
	private HashMap<String, DDBBlock> blocks = new HashMap<>();

	public BlockPack(String name, File container)
	{
		this.name = name;

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(BlockPack.class, new BlockPackDeserializer(this));
		Gson gson = gsonBuilder.create();

		try (Reader reader = new InputStreamReader(FileUtils.openInputStream(new File(container, name + ".json")), "UTF-8"))
		{
			gson.fromJson(reader, BlockPack.class);
			loaded = true;
		}
		catch (IOException | JsonSyntaxException e)
		{
			DDB.log.error("Could not read DDB pack {} : \n{}", name, e);
		}
	}

	public String getName()
	{
		return name;
	}

	public String getDirectory()
	{
		return "./" + PACKDIR + "/" + name + "/";
	}

	public boolean isLoaded()
	{
		return loaded;
	}

	/**
	 * Gets the <code>DDBBlock</code> with the specified <i>name</i>
	 * 
	 * @param name
	 * @return
	 */
	public DDBBlock getBlock(String name)
	{
		return blocks.get(name);
	}

	/**
	 * Registers the <i>block</i> in game registy and this <code>BlockPack</code>
	 * 
	 * @param block
	 */
	public void registerBlock(DDBBlock block)
	{
		GameRegistry.registerBlock(block, block.getName());
		blocks.put(block.getName(), block);
	}

	/**
	 * Reads the pack folder and creates the packs
	 */
	public static void readPackFolder()
	{
		File packDir = new File("./" + PACKDIR);

		for (File file : packDir.listFiles())
		{
			if (file.isDirectory())
				BlockPack.registerPack(new BlockPack(file.getName(), file));
			//TODO: read from zip
		}
	}

	/**
	 * Registers the pack if not already present in registry
	 * 
	 * @param pack
	 */
	public static void registerPack(BlockPack pack)
	{
		String name = pack.getName();
		if (packs.get(name) == null)
			packs.put(pack.getName(), pack);
		else
			DDB.log.error("A DDB pack is already registered with name {}", name);
	}

	/**
	 * Gets a <code>BlockPack</code> with the specified name
	 * 
	 * @param name
	 * @return
	 */
	public static BlockPack getPack(String name)
	{
		return packs.get(name);
	}

	/**
	 * Gets a <code>DDBBlock</code> from the <i>packName</i> <code>BlockPack</code> with the specified <i>blocName</i>
	 * 
	 * @param packName
	 * @param blockName
	 * @return the <code>DDBBlock</code> if found or <b>null</b> if the <i>packName</i> is doesn't match any pack registered of if the
	 *         <i>blockName</i> doesn't match any block registered for that pack
	 */
	public static DDBBlock getBlock(String packName, String blockName)
	{
		BlockPack pack = getPack(packName);
		if (pack == null)
			return null;
		return pack.getBlock(blockName);
	}
}
