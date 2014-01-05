package com.artemis.utils;

// Thanks to Riven
// From: http://riven8192.blogspot.com/2009/08/fastmath-sincos-lookup-tables.html
public class TrigLUT
{
	public static final float sin ( final float rad )
	{
		return sin[(int) ( rad * radToIndex ) & SIN_MASK];
	}

	public static final float cos ( final float rad )
	{
		return cos[(int) ( rad * radToIndex ) & SIN_MASK];
	}

	public static final float sinDeg ( final float deg )
	{
		return sin[(int) ( deg * degToIndex ) & SIN_MASK];
	}

	public static final float cosDeg ( final float deg )
	{
		return cos[(int) ( deg * degToIndex ) & SIN_MASK];
	}

	// Not used.
//	private static final float RAD, DEG;
	private static final int SIN_MASK;
	private static final float radToIndex;
	private static final float degToIndex;
	private static final float[] sin, cos;

	static 
	{
		// RAD = (float) Math.PI / 180.0f;
		// DEG = 180.0f / (float) Math.PI;

		final int SIN_BITS = 12;
		
		SIN_MASK = ~ ( -1 << SIN_BITS );
		
		final int SIN_COUNT = SIN_MASK + 1;

		final float radFull = (float) ( Math.PI * 2.0 );
		final float degFull = 360.0f;
		
		radToIndex = SIN_COUNT / radFull;
		degToIndex = SIN_COUNT / degFull;

		sin = new float[SIN_COUNT];
		cos = new float[SIN_COUNT];

		for ( int i = 0; i < SIN_COUNT; ++i )
		{
			sin[i] = (float) Math.sin( ( i + 0.5f ) / SIN_COUNT * radFull );
			cos[i] = (float) Math.cos( ( i + 0.5f ) / SIN_COUNT * radFull );
		}

		// Four cardinal directions (credits: Nate)
		for ( int i = 0; i < 360; i += 90 )
		{
			sin[(int) ( i * degToIndex ) & SIN_MASK] = (float) Math.sin( i * Math.PI / 180.0 );
			cos[(int) ( i * degToIndex ) & SIN_MASK] = (float) Math.cos( i * Math.PI / 180.0 );
		}
	}
}
