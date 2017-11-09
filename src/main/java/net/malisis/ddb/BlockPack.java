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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import net.malisis.core.MalisisCore;
import net.malisis.core.asm.AsmUtils;
import net.malisis.core.util.Silenced;
import net.malisis.ddb.block.DDBBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.Language;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
			languageList = (Map<String, String>) AsmUtils	.changeFieldAccess(LanguageMap.class, "languageList", "field_74816_c")
															.get(languageMap);
		}
		catch (ReflectiveOperationException e)
		{
			DDB.log.error("Could not get language list, localizations unavailable.", e);
		}

	}

	public enum Type
	{
		FOLDER,
		ZIP
	};

	private Type type;
	private String name;
	private ZipFile zipFile;

	private HashMap<ResourceLocation, DDBBlock> blocks = new HashMap<>();

	public BlockPack(Type type, String name, ZipFile zipFile)
	{
		this.type = type;
		this.name = name;
		this.zipFile = zipFile;

		if (MalisisCore.isClient())
			registerReloadListener();
	}

	@SideOnly(Side.CLIENT)
	private void registerReloadListener()
	{
		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new Reloader());
	}

	/**
	 * Gets the name of this {@link BlockPack}.
	 *
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Gets the type of this {@link BlockPack}.
	 *
	 * @return the type
	 */
	public Type getType()
	{
		return type;
	}

	/**
	 * Gets the working directory of this {@link BlockPack}.
	 *
	 * @return the directory
	 */
	public String getDirectory()
	{
		return "./" + DDB.PACKDIR + "/" + name + "/";
	}

	/**
	 * Gets an inputStream from this {@link BlockPack} for the <i>path</i>.
	 *
	 * @param path the path
	 * @return the input stream
	 * @throws IOException Signals that an I/O exception has occurred.
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

	public List<String> listFiles(String path)
	{
		return listFiles(path, p -> true);
	}

	public List<String> listFiles(String path, Predicate<String> predicate)
	{
		if (type == Type.FOLDER)
		{
			File file = new File(getDirectory() + path);
			if (!file.isDirectory())
				return Collections.emptyList();
			Collection<File> files = FileUtils.listFiles(file, null, true);
			return files.stream()
						.map(File::getPath)
						.map(p -> p.replace(getDirectory().replace("/", "\\"), ""))
						.filter(predicate)
						.collect(Collectors.toList());

		}
		else
		{
			return zipFile.stream().map(ZipEntry::getName).filter(p -> p.startsWith(path)).filter(predicate).collect(Collectors.toList());
		}
	}

	/**
	 * Gets the {@link DDBBlock} with the specified <i>name</i>.
	 *
	 * @param name the name
	 * @return the block
	 */
	public DDBBlock getBlock(ResourceLocation name)
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
		//always load the English localization
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

	private void readRecipes()
	{
		JsonContext ctx = new JsonContext(DDB.modid);
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		List<String> files = listFiles("recipes/", p -> p.endsWith(".json"));

		for (String fileName : files)
		{
			ResourceLocation key = new ResourceLocation(ctx.getModId(), fileName.replace("recipes\\", name + "_").replace(".json", ""));
			//key = ddb:{packname}_{file}

			try (Reader reader = new InputStreamReader(getInputStream(fileName), "UTF-8"))
			{
				JsonObject json = JsonUtils.fromJson(gson, reader, JsonObject.class);
				if (json.has("conditions") && !CraftingHelper.processConditions(JsonUtils.getJsonArray(json, "conditions"), ctx))
					continue;
				IRecipe recipe = CraftingHelper.getRecipe(json, ctx);
				ForgeRegistries.RECIPES.register(recipe.setRegistryName(key));
			}
			catch (Exception e)
			{
				DDB.log.error("Failed to read recipe {}.json in pack {} : {}", fileName, this.name, e.getMessage());
			}
		}

	}

	/**
	 * Registers all the block of this {@link BlockPack} to the GameRegistry
	 */
	public void registerBlocks()
	{
		blocks.values().forEach(DDBBlock::register);
	}

	public void registerRecipes()
	{
		readRecipes();

		//register furnace recipes
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
