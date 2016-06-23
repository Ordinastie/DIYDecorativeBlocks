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
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.malisis.core.MalisisCore;
import net.malisis.core.asm.AsmUtils;
import net.malisis.core.block.IComponent;
import net.malisis.core.block.component.SlabComponent;
import net.malisis.core.util.Silenced;
import net.malisis.ddb.block.DDBBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.Language;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Maps;

/**
 * @author Ordinastie
 *
 */

@SuppressWarnings("unchecked")
public class BlockPack
{
	private static Map<String, String> languageList = Maps.newHashMap();
	static
	{
		try
		{
			LanguageMap languageMap = (LanguageMap) AsmUtils.changeFieldAccess(LanguageMap.class, "instance", "field_74817_a").get(null);
			languageList = (Map<String, String>) AsmUtils.changeFieldAccess(LanguageMap.class, "languageList", "field_74816_c")
															.get(languageMap);
		}
		catch (ReflectiveOperationException e)
		{
			DDB.log.error("Could not get language list, localizations unavailable.", e);
		}

	}

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

		if (MalisisCore.isClient())
			((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new Reloader());

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

	private void readLangFiles()
	{
		//always load the english localization
		loadLang("en_US");
		Language current = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage();
		if (current != null && !"en_US".equals(current.getLanguageCode()))
			loadLang(current.getLanguageCode());
	}

	private void loadLang(String lang)
	{
		Map<String, String> map = LanguageMap.parseLangFile(Silenced.get(() -> getInputStream("lang/" + lang + ".lang")));
		languageList.putAll(map);
	}

	/**
	 * Registers all the block of this {@link BlockPack} to the GameRegistry
	 */
	public void registerBlocks()
	{
		for (DDBBlock block : blocks.values())
		{
			SlabComponent sc = IComponent.getComponent(SlabComponent.class, block);
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
			block.registerRecipes();
		}
	}

	@SideOnly(Side.CLIENT)
	public class Reloader implements IResourceManagerReloadListener
	{
		@Override
		public void onResourceManagerReload(IResourceManager resourceManager)
		{
			readLangFiles();
		}
	}
}
