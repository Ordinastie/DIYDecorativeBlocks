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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.malisis.ddb.block.DDBBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Ordinastie
 *
 */
public class DDBRecipe
{
	private static char DAMAGE_CHAR = '@';

	public String[][] items;
	public int amount = 1;
	public boolean shapeless = false;

	private transient HashMap<String, Character> charList = new HashMap<>();
	private transient HashMap<String, Item> itemList = new HashMap<>();
	private transient char currentChar = 'A';

	private char getChar(String itemString)
	{
		if (StringUtils.isEmpty(itemString))
			return ' ';
		Character c = charList.get(itemString);
		if (c == null)
		{
			c = currentChar;
			currentChar++;
			charList.put(itemString, c);
		}

		return c;
	}

	private String getItemString(String str)
	{
		int idx = str.indexOf(DAMAGE_CHAR);
		if (idx == -1)
			return str;
		return str.substring(0, idx);
	}

	private int getDamage(String str)
	{
		int idx = str.indexOf(DAMAGE_CHAR);
		if (idx == -1)
			return 0;
		String meta = str.substring(idx + 1);
		if (meta.equals("*"))
			return OreDictionary.WILDCARD_VALUE;

		try
		{
			return Integer.parseInt(meta);
		}
		catch (NumberFormatException e)
		{
			DDB.log.error("Error parsing the damage value for {} : {}, using 0.", str, meta);
			return 0;
		}
	}

	private Object getItem(String str)
	{
		String itemString = getItemString(str);
		int damage = getDamage(str);

		Item item = itemList.get(itemString);
		if (item == null)
		{
			if (OreDictionary.getOres(itemString).size() > 0)
				return itemString;
			else
				item = Item.getByNameOrId(itemString);
			if (item == null)
			{
				DDB.log.error("Couldn't find entry for item {} ({}), recipe ignored.", itemString, str);
				return null;
			}
			if (!shapeless)
				itemList.put(itemString, item);
		}

		return new ItemStack(item, 1, damage);
	}

	public IRecipe createRecipe(DDBBlock block)
	{
		if (shapeless)
			return createShapelessRecipe(block);
		else
			return createShapedRecipe(block);
	}

	public IRecipe createShapelessRecipe(DDBBlock block)
	{
		List<Object> recipe = new ArrayList<>();
		for (String[] row : items)
			for (String itemString : row)
			{
				Object item = getItem(itemString);
				if (item == null)
					return null;
				recipe.add(item);
			}

		return new ShapelessOreRecipe(new ItemStack(block, amount, 0), recipe.toArray());
	}

	public IRecipe createShapedRecipe(DDBBlock block)
	{
		int height = items.length;
		int width = 0;
		for (String[] row : items)
			width = Math.max(width, row.length);

		List<Object> recipe = new ArrayList<>();
		for (int j = 0; j < height; j++)
		{
			String row = "";
			for (int i = 0; i < width; i++)
				row += getChar(items[j][i]);

			recipe.add(row);
		}

		for (Entry<String, Character> entry : charList.entrySet())
		{
			recipe.add(entry.getValue());
			Object item = getItem(entry.getKey());
			if (item == null)
				return null;
			recipe.add(item);
		}

		return new ShapedOreRecipe(new ItemStack(block, amount, 0), recipe.toArray());
	}

	@Override
	public String toString()
	{
		return ArrayUtils.toString(items);
	}
}
