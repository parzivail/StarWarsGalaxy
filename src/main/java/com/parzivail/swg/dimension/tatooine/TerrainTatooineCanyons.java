package com.parzivail.swg.dimension.tatooine;

import com.parzivail.util.world.ITerrainHeightmap;
import com.parzivail.util.world.ProcNoise;
import net.minecraft.util.MathHelper;

public class TerrainTatooineCanyons implements ITerrainHeightmap
{
	private ProcNoise _noise = new ProcNoise(0);

	@Override
	public double getHeightAt(int x, int z)
	{
		double s = 2;
		double h = get(x / s, z / s);
		double d = 5 * s;

		double blur = 0;
		blur = blur + get(x / s - d, z / s - d);
		blur = blur + get(x / s - d, z / s + d);

		blur = blur + get(x / s + d, z / s - d);
		blur = blur + get(x / s + d, z / s + d);
		blur = blur / 4;

		h = 1 - (h - blur);
		h = 1 - 1 / (2 * h) - 0.48;
		h = h * 35;

		h = MathHelper.clamp_double(h, 0, 1);
		double j = octave(x / 200f, z / 200f, 6) * 90;

		return (h * 0.8 + octave(x / 200f, z / 200f, 3) * 0.8) * (j + 10);
	}

	private double octave(double x, double z, int numOctaves)
	{
		if (numOctaves <= 1)
			return _noise.noise(x, z) / 2;
		return _noise.noise(x, z) / 2 + octave((x + numOctaves * 100) * 2, (z + numOctaves * 100) * 2, numOctaves - 1) / 2;
	}

	private double get(double x, double z)
	{
		double offsetX = _noise.noise(x / 10, z / 10 + 1000) / 10;
		double offsetY = _noise.noise(x / 10 + 1000, z / 10) / 10;

		return _noise.worley(x / 50 + offsetX, z / 50 + offsetY);
	}

	@Override
	public double getBiomeLerpAmount(int x, int z)
	{
		return 1;
	}

	@Override
	public double[] getBiomeWeightsAt(int x, int z)
	{
		return new double[] { 1 };
	}
}
