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
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.malisis.ddb.block.DDBBlock;
import net.malisis.ddb.item.DDBItem;
import net.minecraft.util.StringTranslate;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

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
	private HashMap<String, DDBItem> items = new HashMap<>();

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
			DDB.log.error("Skipping {}, couldn't read {}.json : {}", file.getName(), name, e.getMessage());
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

		readLangFiles();

		registerPack();
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
		{
			ZipEntry entry = zipFile.getEntry(path);
			if (entry == null)
				throw new IOException("File not found : " + path);
			return zipFile.getInputStream(entry);
		}

		throw new IOException("Undetermined pack type : " + type);
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
	 * Gets the <code>DDBItem</code> with the specified <i>name</i>
	 * 
	 * @param name
	 * @return
	 */
	public DDBItem getItem(String name)
	{
		return items.get(name);
	}

	/**
	 * Adds the <i>block</i> in this <code>BlockPack</code>.
	 * 
	 * @param block
	 */
	public void addBlock(DDBBlock block)
	{
		blocks.put(block.getName(), block);
	}

	/**
	 * Adds the <i>item</i> in this <code>BlockPack</code>.
	 * 
	 * @param item
	 */
	public void addItem(DDBItem item)
	{
		items.put(item.getName(), item);
	}

	private HashMap<String, String> listLangFiles()
	{
		HashMap<String, String> list = new HashMap<>();

		if (type == Type.FOLDER)
		{
			String path = "lang/";
			File dir = new File(getDirectory() + path);
			for (File file : dir.listFiles())
			{
				String name = file.getName();
				list.put(name.substring(0, name.length() - 5), path + name);
			}
		}
		else
		{
			Pattern langPattern = Pattern.compile("lang/(.*)\\.lang");
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements())
			{
				String name = entries.nextElement().getName();
				Matcher matcher = langPattern.matcher(name);
				if (matcher.matches())
				{
					list.put(matcher.group(1), name);
				}

			}
		}

		return list;
	}

	private void readLangFiles()
	{
		HashMap<String, String> files = listLangFiles();
		for (Entry<String, String> entry : files.entrySet())
		{
			String lang = entry.getKey();
			String file = entry.getValue();

			try
			{
				LanguageRegistry.instance().injectLanguage(lang, StringTranslate.parseLangFile(getInputStream(file)));
			}
			catch (IOException e)
			{
				DDB.log.error("Failed to read lang file {} : \n{}", name, e.getMessage());
			}
		}
	}

	/**
	 * Registers the pack if not already present in registry
	 * 
	 * @param pack
	 */
	public void registerPack()
	{
		if (packs.get(name) == null)
			packs.put(name, this);
		else
			DDB.log.error("A DDB pack is already registered with name {}", name);
	}

	/**
	 * Registers all the block of this <code>BlockPack</code> to the GameRegistry
	 */
	public void registerBlocks()
	{
		for (DDBBlock block : blocks.values())
		{
			GameRegistry.registerBlock(block, block.getItemClass(), block.getName());
		}
	}

	/**
	 * Reads the pack folder and creates the packs
	 */
	public static void readPackFolder()
	{
		File packDir = new File("./" + PACKDIR);
		if (!packDir.exists())
			packDir.mkdir();

		for (File file : packDir.listFiles())
			new BlockPack(file);
	}

	/**
	 * Gets the list of registered <code>BlockPack</code>.
	 * 
	 * @return
	 */
	public static Collection<BlockPack> getListPacks()
	{
		return packs.values();
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

	/**
	 * Gets a <code>DDBItem</code> from the <i>packName</i> <code>BlockPack</code> with the specified <i>itemName</i>
	 * 
	 * @param packName
	 * @param itemName
	 * @return the <code>DDBItem</code> if found or <b>null</b> if the <i>packName</i> doesn't match any pack registered or if the
	 *         <i>itemName</i> doesn't match any item registered for that pack
	 */
	public static DDBItem getItem(String packName, String itemName)
	{
		BlockPack pack = getPack(packName);
		if (pack == null)
			return null;
		return pack.getItem(itemName);
	}
}
