package com.parzivail.swg.render.pipeline;

import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.MathHelper;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.util.Map;

@SideOnly(Side.CLIENT)
public enum ModelRotation implements ITransformation
{
	X0_Y0(0, 0),
	X0_Y90(0, 90),
	X0_Y180(0, 180),
	X0_Y270(0, 270),
	X90_Y0(90, 0),
	X90_Y90(90, 90),
	X90_Y180(90, 180),
	X90_Y270(90, 270),
	X180_Y0(180, 0),
	X180_Y90(180, 90),
	X180_Y180(180, 180),
	X180_Y270(180, 270),
	X270_Y0(270, 0),
	X270_Y90(270, 90),
	X270_Y180(270, 180),
	X270_Y270(270, 270);

	private static final Map<Integer, ModelRotation> MAP_ROTATIONS = Maps.newHashMap();
	private final int combinedXY;
	private final Matrix4f matrix4d;
	private final int quartersX;
	private final int quartersY;

	private static int combineXY(int p_177521_0_, int p_177521_1_)
	{
		return p_177521_0_ * 360 + p_177521_1_;
	}

	ModelRotation(int x, int y)
	{
		combinedXY = combineXY(x, y);
		matrix4d = new Matrix4f();
		Matrix4f matrix4f = new Matrix4f();
		matrix4f.setIdentity();
		Matrix4f.rotate((float)(-x) * 0.017453292F, new Vector3f(1.0F, 0.0F, 0.0F), matrix4f, matrix4f);
		quartersX = (int)MathHelper.abs(x / 90f);
		Matrix4f matrix4f1 = new Matrix4f();
		matrix4f1.setIdentity();
		Matrix4f.rotate((float)(-y) * 0.017453292F, new Vector3f(0.0F, 1.0F, 0.0F), matrix4f1, matrix4f1);
		quartersY = (int)MathHelper.abs(y / 90f);
		Matrix4f.mul(matrix4f1, matrix4f, matrix4d);
	}

	public Matrix4f getMatrix4d()
	{
		return matrix4d;
	}

	public EnumFacing rotateFace(EnumFacing facing)
	{
		EnumFacing enumfacing = facing;

		for (int i = 0; i < quartersX; ++i)
		{
			enumfacing = enumfacing.rotateAround(EnumFacing.Axis.X);
		}

		if (enumfacing.getAxis() != EnumFacing.Axis.Y)
		{
			for (int j = 0; j < quartersY; ++j)
			{
				enumfacing = enumfacing.rotateAround(EnumFacing.Axis.Y);
			}
		}

		return enumfacing;
	}

	public int rotateVertex(EnumFacing facing, int vertexIndex)
	{
		int i = vertexIndex;

		if (facing.getAxis() == EnumFacing.Axis.X)
		{
			i = (vertexIndex + quartersX) % 4;
		}

		EnumFacing enumfacing = facing;

		for (int j = 0; j < quartersX; ++j)
		{
			enumfacing = enumfacing.rotateAround(EnumFacing.Axis.X);
		}

		if (enumfacing.getAxis() == EnumFacing.Axis.Y)
		{
			i = (i + quartersY) % 4;
		}

		return i;
	}

	public static ModelRotation getModelRotation(int x, int y)
	{
		return MAP_ROTATIONS.get(combineXY(normalizeAngle(x, 360), normalizeAngle(y, 360)));
	}

	public static int normalizeAngle(int p_180184_0_, int p_180184_1_)
	{
		return (p_180184_0_ % p_180184_1_ + p_180184_1_) % p_180184_1_;
	}

	static
	{
		for (ModelRotation modelrotation : values())
		{
			MAP_ROTATIONS.put(modelrotation.combinedXY, modelrotation);
		}
	}

	@SideOnly(Side.CLIENT)
	public static javax.vecmath.Matrix4f toVecmath(org.lwjgl.util.vector.Matrix4f m)
	{
		return new javax.vecmath.Matrix4f(m.m00, m.m10, m.m20, m.m30, m.m01, m.m11, m.m21, m.m31, m.m02, m.m12, m.m22, m.m32, m.m03, m.m13, m.m23, m.m33);
	}

	public javax.vecmath.Matrix4f getMatrix()
	{
		javax.vecmath.Matrix4f ret = new javax.vecmath.Matrix4f(toVecmath(getMatrix4d())), tmp = new javax.vecmath.Matrix4f();
		tmp.setIdentity();
		tmp.m03 = tmp.m13 = tmp.m23 = .5f;
		ret.mul(tmp, ret);
		tmp.invert();
		//tmp.m03 = tmp.m13 = tmp.m23 = -.5f;
		ret.mul(tmp);
		return ret;
	}

	public EnumFacing rotate(EnumFacing facing)
	{
		return rotateFace(facing);
	}

	public int rotate(EnumFacing facing, int vertexIndex)
	{
		return rotateVertex(facing, vertexIndex);
	}
}
