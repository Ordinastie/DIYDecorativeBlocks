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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.malisis.core.block.IBlockComponent;
import net.malisis.core.block.component.SlabComponent;
import net.malisis.ddb.block.DDBBlock;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.StringTranslate;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.LanguageRegistry;

import org.apache.commons.io.FileUtils;

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

	private Type type;
	private String name;
	private ZipFile zipFile;

	private HashMap<String, DDBBlock> blocks = new HashMap<>();

	public BlockPack(Type type, String name, ZipFile zipFile)
	{
		this.type = type;
		this.name = name;
		this.zipFile = zipFile;

		readLangFiles();
	}

	/**
	 * Gets the name of this {@link BlockPack}.
	 *
	 * @return
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Gets the type of this {@link BlockPack}
	 *
	 * @return
	 */
	public Type getType()
	{
		return type;
	}

	/**
	 * Gets the working directory of this {@link BlockPack}.
	 *
	 * @return
	 */
	public String getDirectory()
	{
		return "./" + DDB.PACKDIR + "/" + name + "/";
	}

	/**
	 * Gets an inputStream from this {@link BlockPack} for the <i>path</i>.
	 *
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public InputStream getInputStream(String path) throws IOException
	{
		if (type == Type.FOLDER)
		{
			File file = new File(getDirectory() + path);
			if (!file.isFile())
				return null;
			return FileUtils.openInputStream(file);
		}
		else if (type == Type.ZIP && zipFile != null)
		{
			ZipEntry entry = zipFile.getEntry(path);
			if (entry == null)
				return null;
			return zipFile.getInputStream(entry);
		}

		throw new IOException("Undetermined pack type : " + type);
	}

	/**
	 * Gets the {@link DDBBlock} with the specified <i>name</i>.
	 *
	 * @param name
	 * @return
	 */
	public DDBBlock getBlock(String name)
	{
		return blocks.get(name);
	}

	/**
	 * Adds the <i>block</i> in this {@link BlockPack}.
	 *
	 * @param block
	 */
	public void addBlock(DDBBlock block)
	{
		blocks.put(block.getName(), block);
	}

	/**
	 * List all lang files inside this {@link BlockPack}.
	 *
	 * @return the hash map
	 */
	private HashMap<String, String> listLangFiles()
	{
		HashMap<String, String> list = new HashMap<>();

		if (type == Type.FOLDER)
		{
			String path = "lang/";
			File dir = new File(getDirectory() + path);
			if (dir.isDirectory())
			{
				for (File file : dir.listFiles())
				{
					String name = file.getName();
					list.put(name.substring(0, name.length() - 5), path + name);
				}
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

	/**
	 * Read and load all lang files in this {@link BlockPack}.
	 */
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
	 * Registers all the block of this {@link BlockPack} to the GameRegistry
	 */
	public void registerBlocks()
	{
		for (DDBBlock block : blocks.values())
		{
			SlabComponent sc = IBlockComponent.getComponent(SlabComponent.class, block);
			if (sc != null)
				sc.register();
			else
				block.register();
		}
	}

	public void registerRecipes()
	{
		for (DDBBlock block : blocks.values())
		{
			IRecipe recipe = block.getRecipe();
			if (recipe != null)
				GameRegistry.addRecipe(recipe);
		}
	}

}
