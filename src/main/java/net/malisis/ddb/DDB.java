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
import java.util.Collection;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.malisis.core.IMalisisMod;
import net.malisis.core.MalisisCore;
import net.malisis.core.configuration.Settings;
import net.malisis.ddb.block.DDBBlock;
import net.malisis.ddb.json.BlockPackJsonReader;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * @author Ordinastie
 *
 */
@Mod(modid = DDB.modid, name = DDB.modname, version = DDB.version, dependencies = "required-after:malisiscore")
public class DDB implements IMalisisMod
{
	public static String PACKDIR = "ddbpacks";
	public static HashMap<String, BlockPack> packs = new HashMap<>();

	public static final String modid = "ddb";
	public static final String modname = "DIY Decorative Blocks";
	public static final String version = "${version}";

	public static Logger log = LogManager.getLogger(modid);
	public static CreativeTabs tab = new DDBTab();

	@Instance
	public static DDB instance;

	public DDB()
	{
		instance = this;
		MalisisCore.registerMod(this);
		readPackFolder();
	}

	@Override
	public String getModId()
	{
		return modid;
	}

	@Override
	public String getName()
	{
		return modname;
	}

	@Override
	public String getVersion()
	{
		return version;
	}

	@Override
	public Settings getSettings()
	{
		return null;
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		for (BlockPack pack : getListPacks())
			pack.registerBlocks();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		for (BlockPack pack : getListPacks())
			pack.registerRecipes();
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
		{
			BlockPack pack = BlockPackJsonReader.readPack(file);
			if (pack != null)
				register(pack);
		}
	}

	/**
	 * Gets the list of registered <code>BlockPack</code>.
	 *
	 * @return the list packs
	 */
	public static Collection<BlockPack> getListPacks()
	{
		return packs.values();
	}

	/**
	 * Registers the pack if not already present in registry
	 *
	 * @param pack
	 */
	public static void register(BlockPack pack)
	{
		if (DDB.packs.get(pack.getName()) == null)
			DDB.packs.put(pack.getName(), pack);
		else
			DDB.log.error("A DDB pack is already registered with name {}", pack.getName());
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
