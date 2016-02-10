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

import net.minecraft.block.material.MapColor;
import net.minecraft.util.EnumChatFormatting;

/**
 * @author Ordinastie
 *
 */
public enum Color
{
	//@formatter:off
    WHITE(0, 15, "white", "white", MapColor.snowColor, EnumChatFormatting.WHITE),
    ORANGE(1, 14, "orange", "orange", MapColor.adobeColor, EnumChatFormatting.GOLD),
    MAGENTA(2, 13, "magenta", "magenta", MapColor.magentaColor, EnumChatFormatting.AQUA),
    LIGHT_BLUE(3, 12, "light_blue", "lightBlue", MapColor.lightBlueColor, EnumChatFormatting.BLUE),
    YELLOW(4, 11, "yellow", "yellow", MapColor.yellowColor, EnumChatFormatting.YELLOW),
    LIME(5, 10, "lime", "lime", MapColor.limeColor, EnumChatFormatting.GREEN),
    PINK(6, 9, "pink", "pink", MapColor.pinkColor, EnumChatFormatting.LIGHT_PURPLE),
    GRAY(7, 8, "gray", "gray", MapColor.grayColor, EnumChatFormatting.DARK_GRAY),
    SILVER(8, 7, "silver", "silver", MapColor.silverColor, EnumChatFormatting.GRAY),
    CYAN(9, 6, "cyan", "cyan", MapColor.cyanColor, EnumChatFormatting.DARK_AQUA),
    PURPLE(10, 5, "purple", "purple", MapColor.purpleColor, EnumChatFormatting.DARK_PURPLE),
    BLUE(11, 4, "blue", "blue", MapColor.blueColor, EnumChatFormatting.DARK_BLUE),
    BROWN(12, 3, "brown", "brown", MapColor.brownColor, EnumChatFormatting.GOLD),
    GREEN(13, 2, "green", "green", MapColor.greenColor, EnumChatFormatting.DARK_GREEN),
    RED(14, 1, "red", "red", MapColor.redColor, EnumChatFormatting.DARK_RED),
    BLACK(15, 0, "black", "black", MapColor.blackColor, EnumChatFormatting.BLACK);
    //@formatter:on

	private static final Color[] META_LOOKUP = new Color[values().length];
	private static final Color[] DYE_DMG_LOOKUP = new Color[values().length];
	private final int meta;
	private final int dyeDamage;
	private final String name;
	private final String unlocalizedName;
	private final MapColor mapColor;
	private final EnumChatFormatting chatColor;

	private Color(int meta, int dyeDamage, String name, String unlocalizedName, MapColor mapColorIn, EnumChatFormatting chatColor)
	{
		this.meta = meta;
		this.dyeDamage = dyeDamage;
		this.name = name;
		this.unlocalizedName = unlocalizedName;
		this.mapColor = mapColorIn;
		this.chatColor = chatColor;
	}

	public int getMetadata()
	{
		return this.meta;
	}

	public int getDyeDamage()
	{
		return this.dyeDamage;
	}

	public String getUnlocalizedName()
	{
		return this.unlocalizedName;
	}

	public MapColor getMapColor()
	{
		return this.mapColor;
	}

	public static Color byDyeDamage(int damage)
	{
		if (damage < 0 || damage >= DYE_DMG_LOOKUP.length)
			damage = 0;

		return DYE_DMG_LOOKUP[damage];
	}

	public static Color byMetadata(int meta)
	{
		if (meta < 0 || meta >= META_LOOKUP.length)
			meta = 0;

		return META_LOOKUP[meta];
	}

	@Override
	public String toString()
	{
		return this.unlocalizedName;
	}

	public String getName()
	{
		return this.name;
	}

	static
	{
		Color[] colors = values();
		for (int i = 0; i < colors.length; ++i)
		{
			META_LOOKUP[colors[i].getMetadata()] = colors[i];
			DYE_DMG_LOOKUP[colors[i].getDyeDamage()] = colors[i];
		}
	}
}