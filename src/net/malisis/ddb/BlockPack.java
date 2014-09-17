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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.zip.ZipFile;

import net.malisis.ddb.block.DDBBlock;

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
	public enum Type
	{
		FOLDER, ZIP
	};

	public static String PACKDIR = "ddbpacks";
	public static HashMap<String, BlockPack> packs = new HashMap<>();

	private Type type;
	private String name;
	private ZipFile zipFile;

	private HashMap<String, DDBBlock> blocks = new HashMap<>();

	public BlockPack(File file)
	{
		if (file.isDirectory())
		{
			this.name = file.getName();
			this.type = Type.FOLDER;
		}
		else if (file.getName().endsWith(".zip"))
		{
			this.name = file.getName().substring(0, file.getName().length() - 4);
			this.type = Type.ZIP;
			try
			{
				this.zipFile = new ZipFile(file);
			}
			catch (IOException e)
			{
				DDB.log.error("Could not read zip file {} : \n{}", file.getName(), e.getMessage());
			}
		}
		else
		{
			DDB.log.error("Skipping {}, not a valid DDB pack file.", file.getName());
			return;
		}

		InputStream inputStream;
		try
		{
			inputStream = getInputStream(name + ".json");
		}
		catch (IOException e)
		{
			DDB.log.error("Skipping {}, couldn't read {}.json : \n{}", file.getName(), name, e.getMessage());
			return;
		}

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(BlockPack.class, new BlockPackDeserializer(this));
		Gson gson = gsonBuilder.create();

		try (Reader reader = new InputStreamReader(inputStream, "UTF-8"))
		{
			gson.fromJson(reader, BlockPack.class);
		}
		catch (IOException | JsonSyntaxException e)
		{
			DDB.log.error("Failed to read {}.json : \n{}", name, e.getMessage());
			return;
		}

		registerPack(this);
	}

	/**
	 * Gets the name of this <code>BlockPack</code>.
	 * 
	 * @return
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Gets the type of this <code>BlockPack</code>
	 * 
	 * @return
	 */
	public Type getType()
	{
		return type;
	}

	/**
	 * Gets the working directory of this <code>BlockPack</code>.
	 * 
	 * @return
	 */
	public String getDirectory()
	{
		return "./" + PACKDIR + "/" + name + "/";
	}

	/**
	 * Gets an inputStream from this <code>BlockPack</code> for the <i>path</i>.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public InputStream getInputStream(String path) throws IOException
	{
		if (type == Type.FOLDER)
			return FileUtils.openInputStream(new File(getDirectory() + path));
		else if (type == Type.ZIP && zipFile != null)
			return zipFile.getInputStream(zipFile.getEntry(path));

		throw new IOException();
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
	 * Adds the <i>block</i> in this <code>BlockPack</code>
	 * 
	 * @param block
	 */
	public void addBlock(DDBBlock block)
	{

		blocks.put(block.getName(), block);
	}

	/**
	 * Registers all the block of this <code>BlockPack</code> to the GameRegistry
	 */
	public void registerBlocks()
	{
		for (DDBBlock block : blocks.values())
		{
			if (block.getBlockType() == BlockType.COLORED)
				GameRegistry.registerBlock(block, DDBItemColored.class, block.getName());
			else
				GameRegistry.registerBlock(block, block.getName());
		}
	}

	/**
	 * Reads the pack folder and creates the packs
	 */
	public static void readPackFolder()
	{
		File packDir = new File("./" + PACKDIR);

		for (File file : packDir.listFiles())
			new BlockPack(file);
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
	 * Registers every blocks of every packs
	 */
	public static void registerAllBlocks()
	{
		for (BlockPack pack : packs.values())
			pack.registerBlocks();
	}

	/**
	 * Gets a <code>BlockPack</code> with the specified name
	 * 
	 * @param name
	 * @return the <code>DDBPack</code> if found or <b>null</b> if <i>packName</i> doesn't match any pack registered
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
	 * @return the <code>DDBBlock</code> if found or <b>null</b> if the <i>packName</i> doesn't match any pack registered or if the
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
