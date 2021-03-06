package com.parzivail.pswg.util;

import com.parzivail.pswg.access.IQuaternionAccess;
import com.parzivail.util.math.MathUtil;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.*;

public class QuatUtil
{
	public static EulerAngle toEulerAngles(Quaternion q)
	{
		var forward = rotate(com.parzivail.util.math.MathUtil.POSZ, q);

		var yaw = -(float)Math.atan2(forward.x, forward.z);
		var pitch = (float)Math.atan2(forward.y, Math.sqrt(forward.x * forward.x + forward.z * forward.z));

		return new EulerAngle(pitch * com.parzivail.util.math.MathUtil.toDegreesf, yaw * MathUtil.toDegreesf + 180, 0);
	}

	public static Vec3d rotate(Vec3d self, Quaternion q)
	{
		// Extract the vector part of the quaternion
		var u = new Vec3d(q.getX(), q.getY(), q.getZ());

		// Extract the scalar part of the quaternion
		var s = q.getW();

		// Do the math
		return u.multiply(2.0f * u.dotProduct(self)).add(self.multiply(s * s - u.dotProduct(u))).add(u.crossProduct(self).multiply(2.0f * s));
	}

	public static Quaternion of(float x, float y, float z, boolean degrees)
	{
		if (degrees)
		{
			x *= 0.017453292F;
			y *= 0.017453292F;
			z *= 0.017453292F;
		}

		var f = MathHelper.sin(0.5F * x);
		var g = MathHelper.cos(0.5F * x);
		var h = MathHelper.sin(0.5F * y);
		var i = MathHelper.cos(0.5F * y);
		var j = MathHelper.sin(0.5F * z);
		var k = MathHelper.cos(0.5F * z);
		return new Quaternion(f * i * k + g * h * j, g * h * k - f * i * j, f * h * k + g * i * j, g * i * k - f * h * j);
	}

	public static void putQuaternion(NbtCompound tag, Quaternion q)
	{
		tag.putFloat("a", q.getW());
		tag.putFloat("b", q.getX());
		tag.putFloat("c", q.getY());
		tag.putFloat("d", q.getZ());
	}

	public static Quaternion getQuaternion(NbtCompound tag)
	{
		return new Quaternion(tag.getFloat("b"), tag.getFloat("c"), tag.getFloat("d"), tag.getFloat("a"));
	}

	public static void rotateTowards(Quaternion self, Vec3d orientation, float speed, Matrix4f mat, VertexConsumer debugConsumer)
	{
		normalize(self);
		var vec2 = rotate(orientation, self);
		var cross = orientation.crossProduct(vec2).multiply(-1.0);
		var axis = cross.normalize();
		var f1 = (float)cross.length();
		if (debugConsumer != null)
		{
			debugConsumer.vertex(mat, 0.0f, 0.0f, 0.0f).color(1f, 0f, 0f, 1f).next();
			debugConsumer.vertex(mat, (float)orientation.x, (float)orientation.y, (float)orientation.z).color(1f, 0f, 0f, 1f).next();

			debugConsumer.vertex(mat, 0.0f, 0.0f, 0.0f).color(0f, 0f, 1f, 1f).next();
			debugConsumer.vertex(mat, (float)vec2.x, (float)vec2.y, (float)vec2.z).color(0f, 0f, 1f, 1f).next();

			debugConsumer.vertex(mat, 0.0f, 0.0f, 0.0f).color(0f, 1f, 0f, 1f).next();
			debugConsumer.vertex(mat, (float)cross.x, (float)cross.y, (float)cross.z).color(0f, 1f, 0f, 1f).next();
		}
		var other = new Quaternion(new Vec3f(axis), speed * f1, false);
		other.hamiltonProduct(self);
		set(self, other);
	}

	public static Quaternion invertCopy(Quaternion q)
	{
		var q1 = new Quaternion(q);
		invert(q1);
		return q1;
	}

	/**
	 * Finds a global vector in local terms
	 *
	 * @param v
	 * @param q
	 *
	 * @return
	 */
	public static Vec3d project(Vec3d v, Quaternion q)
	{
		var c = new Quaternion(q);
		c.conjugate();
		return rotate(v, c);
	}

	public static void normalize(Quaternion self)
	{
		var f = self.getX() * self.getX() + self.getY() * self.getY() + self.getZ() * self.getZ() + self.getW() * self.getW();
		if (f > 1.0E-6F)
		{
			var g = (float)MathHelper.fastInverseSqrt((double)f);
			scale(self, g);
		}
		else
		{
			scale(self, 0f);
		}
	}

	public static float dot_product(Quaternion q1, Quaternion q2)
	{
		return q1.getW() * q2.getW() + q1.getX() * q2.getX() + q1.getY() * q2.getY() + q1.getZ() * q2.getZ();
	}

	public static Quaternion slerp(Quaternion start, Quaternion end, float t)
	{
		// Only unit quaternions are valid rotations.
		// Normalize to avoid undefined behavior.
		normalize(start);
		normalize(end);

		// Compute the cosine of the angle between the two vectors.
		double dot = dot_product(start, end);

		// If the dot product is negative, slerp won't take
		// the shorter path. Note that end and -end are equivalent when
		// the negation is applied to all four components. Fix by
		// reversing one quaternion.
		if (dot < 0.0f)
		{
			scale(end, -1);
			dot = -dot;
		}

		if (dot > 0.9995)
		{
			// If the inputs are too close for comfort, linearly interpolate
			// and normalize the result.

			var f = 1 - t;

			var a = f * start.getW() + t * end.getW();
			var b = f * start.getX() + t * end.getX();
			var c = f * start.getY() + t * end.getY();
			var d = f * start.getZ() + t * end.getZ();

			var result = new Quaternion(b, c, d, a);
			normalize(result);
			return result;
		}

		// Since dot is in range [0, DOT_THRESHOLD], acos is safe
		var theta_0 = Math.acos(dot);        // theta_0 = angle between input vectors
		var theta = theta_0 * t;          // theta = angle between start and result
		var sin_theta = Math.sin(theta);     // compute this value only once
		var sin_theta_0 = Math.sin(theta_0); // compute this value only once

		var f1 = Math.cos(theta) - dot * sin_theta / sin_theta_0;  // == sin(theta_0 - theta) / sin(theta_0)
		var f2 = sin_theta / sin_theta_0;

		var a = (float)(f1 * start.getW() + f2 * end.getW());
		var b = (float)(f1 * start.getX() + f2 * end.getX());
		var c = (float)(f1 * start.getY() + f2 * end.getY());
		var d = (float)(f1 * start.getZ() + f2 * end.getZ());

		var result = new Quaternion(b, c, d, a);
		normalize(result);
		return result;
	}

	public static void set(Quaternion self, float x, float y, float z, float w)
	{
		IQuaternionAccess.from(self).set0(x, y, z, w);
	}

	public static Quaternion getRotationTowards(Vec3d from, Vec3d to)
	{
		var q = new Quaternion(Quaternion.IDENTITY);
		var cross = from.crossProduct(to);
		var w = (float)(Math.sqrt(from.lengthSquared() * to.lengthSquared()) + from.dotProduct(to));
		set(q, w, (float)cross.x, (float)cross.y, (float)cross.z);
		normalize(q);
		return q;
	}

	public static void set(Quaternion self, Quaternion other)
	{
		set(self, other.getW(), other.getX(), other.getY(), other.getZ());
	}

	public static void invert(Quaternion self)
	{
		var l = self.getW() * self.getW() + self.getX() * self.getX() + self.getY() * self.getY() + self.getZ() * self.getZ();
		set(self, self.getW() / l, -self.getX() / l, -self.getY() / l, -self.getZ() / l);
	}

	public static void scale(Quaternion self, float factor)
	{
		set(self, self.getX() * factor, self.getY() * factor, self.getZ() * factor, self.getW() * factor);
	}

	public static void updateEulerRotation(Entity entity, Quaternion rotation)
	{
		var eulerAngle = toEulerAngles(rotation);
		entity.setYaw(eulerAngle.getYaw());
		entity.setPitch(eulerAngle.getPitch());

		while (entity.getPitch() - entity.prevPitch >= 180.0F)
		{
			entity.prevPitch += 360.0F;
		}

		while (entity.getYaw() - entity.prevYaw < -180.0F)
		{
			entity.prevYaw -= 360.0F;
		}

		while (entity.getYaw() - entity.prevYaw >= 180.0F)
		{
			entity.prevYaw += 360.0F;
		}
	}
}
