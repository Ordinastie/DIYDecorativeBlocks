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
import java.util.List;

import net.malisis.core.util.ItemUtils;
import net.malisis.ddb.block.DDBBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

/**
 * @author Ordinastie
 *
 */
public class DDBRecipe
{
	public String[][] items;
	public int amount = 1;
	public boolean shapeless = false;

	private Object getItem(String str)
	{
		if (OreDictionary.getOres(str).size() > 0)
			return str;

		if (StringUtils.isEmpty(str))
			return null;

		return ItemUtils.getItemStack(str);
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
		char c = 'A';
		List<Object> recipe = Lists.newArrayList();
		List<Object> strRecipes = Lists.newArrayList();

		for (String[] row : items)
		{
			String strRecipe = "";
			for (String itemString : row)
			{
				Object item = getItem(itemString);
				if (item == null)
					strRecipe += " ";
				else
				{
					strRecipe += c;
					recipe.add(c);
					recipe.add(item);
					c++;
				}
			}

			if (!StringUtils.isEmpty(strRecipe))
				strRecipes.add(strRecipe);
		}

		strRecipes.addAll(recipe);
		return new ShapedOreRecipe(new ItemStack(block, amount, 0), strRecipes.toArray());
	}

	@Override
	public String toString()
	{
		return ArrayUtils.toString(items);
	}
}
