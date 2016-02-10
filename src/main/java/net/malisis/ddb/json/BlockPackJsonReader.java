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

package net.malisis.ddb.json;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Map.Entry;
import java.util.zip.ZipFile;

import net.malisis.ddb.BlockDescriptor;
import net.malisis.ddb.BlockPack;
import net.malisis.ddb.BlockType;
import net.malisis.ddb.DDB;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

/**
 * @author Ordinastie
 *
 */
public class BlockPackJsonReader
{
	public static BlockPack readPack(File file)
	{
		BlockPack.Type type;
		String name;
		ZipFile zipFile = null;

		if (file.isDirectory())
		{
			name = file.getName();
			type = BlockPack.Type.FOLDER;
		}
		else if (file.getName().endsWith(".zip"))
		{
			name = file.getName().substring(0, file.getName().length() - 4);
			type = BlockPack.Type.ZIP;
			try
			{
				zipFile = new ZipFile(file);
			}
			catch (IOException e)
			{
				DDB.log.error("Could not read zip file {} :\n", file.getName(), e);
			}
		}
		else
		{
			DDB.log.error("Skipping {}, not a valid DDB pack file.", file.getName());
			return null;
		}

		BlockPack pack = new BlockPack(type, name, zipFile);

		InputStream inputStream;
		try
		{
			inputStream = pack.getInputStream(name + ".json");
		}
		catch (IOException e)
		{
			DDB.log.error("Skipping {}, couldn't read {}.json : {}", file.getName(), name, e.getMessage());
			return null;
		}

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(BlockPack.class, new BlockPackDeserializer(pack));
		//gsonBuilder.registerTypeAdapter(DDBRecipe.class, new DDBRecipe.DDBRecipeDeserializer());
		Gson gson = gsonBuilder.create();

		try (Reader reader = new InputStreamReader(inputStream, "UTF-8"))
		{
			JsonReader jsonReader = new JsonReader(reader);
			jsonReader.setLenient(true);
			gson.fromJson(jsonReader, BlockPack.class);
		}
		catch (IOException | JsonSyntaxException e)
		{
			DDB.log.error("Failed to read {}.json : {}", name, e.getMessage());
			return null;
		}

		return pack;
	}

	public static class BlockPackDeserializer implements JsonDeserializer<BlockPack>
	{
		private BlockPack pack;

		public BlockPackDeserializer(BlockPack pack)
		{
			this.pack = pack;
		}

		@Override
		public BlockPack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
		{
			JsonObject blocks = json.getAsJsonObject();

			for (Entry<String, JsonElement> entry : blocks.entrySet())
			{
				BlockDescriptor desc = context.deserialize(entry.getValue(), BlockDescriptor.class);
				desc.name = entry.getKey();
				if (desc.textures != null && desc.textures.get("front") != null && desc.type == BlockType.STANDARD)
					desc.type = BlockType.DIRECTIONAL;
				if (desc.type == BlockType.STAIRS)
					desc.opaque = false;

				desc.createBlock(pack);
			}
			return null;
		}

	}
}
