/*
 * Copyright (c) 2016-2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.cache;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import net.runelite.cache.definitions.SpriteDefinition;
import net.runelite.cache.definitions.exporters.SpriteExporter;
import net.runelite.cache.definitions.loaders.SpriteLoader;
import net.runelite.cache.fs.*;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SimbaSpriteDumper
{
	private final Store store;
	private final Multimap<Integer, SpriteDefinition> sprites = LinkedListMultimap.create();
	private final Map<Integer, Integer> spriteIdsByArchiveNameHash = new HashMap<>();

	public SimbaSpriteDumper(Store store) throws IOException {
		this.store = store;
	}

	public void load() throws IOException
	{
		Storage storage = store.getStorage();
		Index index = store.getIndex(IndexType.SPRITES);

		for (Archive a : index.getArchives())
		{
			byte[] contents = a.decompress(storage.loadArchive(a));

			SpriteLoader loader = new SpriteLoader();
			SpriteDefinition[] defs = loader.load(a.getArchiveId(), contents);

			for (SpriteDefinition sprite : defs)
			{
				sprites.put(sprite.getId(), sprite);
				spriteIdsByArchiveNameHash.put(a.getNameHash(), sprite.getId());
			}
		}
	}

	public void export(File outDir) throws IOException
	{
		File spellDir = new File(outDir + File.separator + "magic" + File.separator);
		Set<Integer> spells = Set.of(
				0, 1, 2, 3,
				5, 6, 7, 8, 9, 10, 11, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
				32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64,
				305, 306, 307,
				319, 320, 321, 322, 323, 324, 325, 326, 327, 328, 329, 330, 331, 332, 333, 334, 335, 336, 337, 338, 339, 340, 341, 342, 343, 344, 345,
				346, 347, 348, 349, 350, 351, 352, 353, 354, 355, 356, 357, 358, 359, 360, 361, 362, 363, 364, 365, 366, 367
		);

		Set<Integer> innactiveSpells = Set.of(
				65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93,
				94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114,
				369, 370, 371, 372, 373, 374, 375, 376, 377, 378, 379, 380, 381, 382, 383, 384, 385, 386, 387, 388, 389, 390, 391, 392,
				393, 394, 395, 396, 397, 398, 399, 400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 411, 412, 413, 414, 415, 416, 417
		);

		Set<Integer> prayers = Set.of(
				115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134,
				502, 503, 504, 505
		);
		Set<Integer> innactivePrayers = Set.of(
				135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154,
				506, 507, 508, 509
		);

		for (SpriteDefinition sprite : sprites.values())
		{
			// Some sprites like ones for non-printable font characters do not have sizes
			if (sprite.getHeight() <= 0 || sprite.getWidth() <= 0)
			{
				continue;
			}

			SpriteExporter exporter = new SpriteExporter(sprite);
			if (sprite.getFrame() == 0)
			{

				if (spells.contains(sprite.getId())) {
					File png = new File(spellDir,  sprite.getId() + ".png");
					exporter.exportTo(png);
				}
				else {
					File png = new File(outDir, sprite.getId() + ".png");
					exporter.exportTo(png);
				}
			}
		}
	}

	private static void dumpSprites(Store store, File spritesDir) throws IOException
	{
		SimbaSpriteDumper dumper = new SimbaSpriteDumper(store);
		dumper.load();
		dumper.export(spritesDir);
	}
	public static void main(String[] args) throws IOException
	{
		Options options = new Options();
		options.addOption(Option.builder().longOpt("cachedir").hasArg().required().build());
		options.addOption(Option.builder().longOpt("cachename").hasArg().required().build());
		options.addOption(Option.builder().longOpt("outputdir").hasArg().required().build());

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try
		{
			cmd = parser.parse(options, args);
		}
		catch (ParseException ex)
		{
			System.err.println("Error parsing command line options: " + ex.getMessage());
			System.exit(-1);
			return;
		}

		final String mainDir = cmd.getOptionValue("cachedir");
		final String cacheName = cmd.getOptionValue("cachename");

		final String cacheDirectory = mainDir + File.separator + cacheName + File.separator + "cache";
		final String outputDirectory = cmd.getOptionValue("outputdir") + File.separator + cacheName + File.separator + "Sprites";

		File base = new File(cacheDirectory);
		File outDir = new File(outputDirectory);
		if (!outDir.exists() && !outDir.mkdirs()) return;

		try (Store store = new Store(base))
		{
			store.load();

			System.out.println("Dumping Sprites to " + outDir);
			dumpSprites(store, outDir);
		}
	}
}
