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
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import cpw.mods.fml.common.registry.GameRegistry;

/**
 * @author Ordinastie
 * 
 */
public class DDBPack
{
	public static String PACKDIR = "ddbpacks";
	public static HashMap<String, DDBPack> packs = new HashMap<>();

	private String name;
	private boolean loaded = false;
	private PackDescriptor packDescriptor;
	private HashMap<String, DDBBlock> blocks = new HashMap<>();

	public DDBPack(String name, File container)
	{
		this.name = name;

		try
		{
			String strJson = FileUtils.readFileToString(new File(container, name + ".json"));
			Gson json = new Gson();
			packDescriptor = json.fromJson(strJson, PackDescriptor.class);

			createBlocks();

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

	public DDBBlock getBlock(String name)
	{
		return blocks.get(name);
	}

	public void createBlocks()
	{
		if (packDescriptor.blocks == null)
			return;

		for (BlockDescriptor desc : packDescriptor.blocks)
		{
			DDBBlock block = new DDBBlock(this, desc);
			GameRegistry.registerBlock(block, block.getUnlocalizedName().substring(5));
			blocks.put(desc.name, block);
		}
	}

	public static void readPackFolder()
	{
		File packDir = new File("./" + PACKDIR);

		for (File file : packDir.listFiles())
		{
			if (file.isDirectory())
				DDBPack.registerPack(new DDBPack(file.getName(), file));
		}
	}

	public static void registerPack(DDBPack pack)
	{
		String name = pack.getName();
		if (packs.get(name) != null)
		{
			DDB.log.error("A DDB pack is already registered with name {}", name);
		}
		else
		{
			packs.put(pack.getName(), pack);
		}
	}

	public static DDBPack getPack(String name)
	{
		return packs.get(name);
	}

	public static DDBBlock getBlock(String packName, String blockName)
	{
		DDBPack pack = getPack(packName);
		if (pack == null)
			return null;
		return pack.getBlock(blockName);
	}
}
