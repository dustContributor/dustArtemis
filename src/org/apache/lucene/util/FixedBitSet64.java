package org.apache.lucene.util;

public class FixedBitSet64 extends FixedBitSet
{
	public long word0;

	public FixedBitSet64 ()
	{
		// Empty.
	}

	@Override
	public boolean get ( int index )
	{
		return (word0 & (1L << index)) != 0L;
	}

	@Override
	public int getBit ( int index )
	{
		return (int) ((word0 >>> index) & 0x01L);
	}

	@Override
	public void set ( int index )
	{
		word0 |= (1L << index);
	}

	@Override
	public void clear ( int index )
	{
		word0 &= ~(1L << index);
	}

	@Override
	public void and ( FixedBitSet bits )
	{
		bits.andTo( this );
	}

	public final void and ( FixedBitSet64 bits )
	{
		word0 &= bits.word0;
	}

	@Override
	protected final void andTo ( FixedBitSet64 bits )
	{
		bits.and( this );
	}

	@Override
	protected void andTo ( FixedBitSet128 bits )
	{
		bits.and( this );
	}

	@Override
	protected void andTo ( FixedBitSet192 bits )
	{
		bits.and( this );
	}

	@Override
	protected void andTo ( FixedBitSet256 bits )
	{
		bits.and( this );
	}

	@Override
	public void or ( FixedBitSet bits )
	{
		bits.orTo( this );
	}

	public final void or ( FixedBitSet64 bits )
	{
		word0 |= bits.word0;
	}

	@Override
	protected final void orTo ( FixedBitSet64 bits )
	{
		bits.or( this );
	}

	@Override
	protected void orTo ( FixedBitSet128 bits )
	{
		bits.or( this );
	}

	@Override
	protected void orTo ( FixedBitSet192 bits )
	{
		bits.or( this );
	}

	@Override
	protected void orTo ( FixedBitSet256 bits )
	{
		bits.or( this );
	}

	@Override
	public void andNot ( FixedBitSet bits )
	{
		bits.andNotTo( this );
	}

	public final void andNot ( FixedBitSet64 bits )
	{
		word0 &= ~bits.word0;
	}

	@Override
	protected final void andNotTo ( FixedBitSet64 bits )
	{
		bits.andNot( this );
	}

	@Override
	protected void andNotTo ( FixedBitSet128 bits )
	{
		bits.andNot( this );
	}

	@Override
	protected void andNotTo ( FixedBitSet192 bits )
	{
		bits.andNot( this );
	}

	@Override
	protected void andNotTo ( FixedBitSet256 bits )
	{
		bits.andNot( this );
	}

	@Override
	public boolean intersects ( FixedBitSet bits )
	{
		return bits.intersectsTo( this );
	}

	public final boolean intersects ( FixedBitSet64 bits )
	{
		return (bits.word0 & word0) != 0L;
	}

	@Override
	protected final boolean intersectsTo ( FixedBitSet64 bits )
	{
		return bits.intersects( this );
	}

	@Override
	protected boolean intersectsTo ( FixedBitSet128 bits )
	{
		return bits.intersects( this );
	}

	@Override
	protected boolean intersectsTo ( FixedBitSet192 bits )
	{
		return bits.intersects( this );
	}

	@Override
	protected boolean intersectsTo ( FixedBitSet256 bits )
	{
		return bits.intersects( this );
	}

	@Override
	public int intersectCount ( FixedBitSet bits )
	{
		return bits.intersectCountTo( this );
	}

	public final int intersectCount ( FixedBitSet64 bits )
	{
		return Long.bitCount( bits.word0 & word0 );
	}

	@Override
	protected final int intersectCountTo ( FixedBitSet64 bits )
	{
		return bits.intersectCount( this );
	}

	@Override
	protected int intersectCountTo ( FixedBitSet128 bits )
	{
		return bits.intersectCount( this );
	}

	@Override
	protected int intersectCountTo ( FixedBitSet192 bits )
	{
		return bits.intersectCount( this );
	}

	@Override
	protected int intersectCountTo ( FixedBitSet256 bits )
	{
		return bits.intersectCount( this );
	}

	@Override
	public boolean isIntersectionEqual ( FixedBitSet bits )
	{
		return bits.isIntersectionEqualTo( this );
	}

	public final boolean isIntersectionEqual ( FixedBitSet64 bits )
	{
		return (bits.word0 & word0) == word0;
	}

	@Override
	protected final boolean isIntersectionEqualTo ( FixedBitSet64 bits )
	{
		return bits.isIntersectionEqual( this );
	}

	@Override
	protected boolean isIntersectionEqualTo ( FixedBitSet128 bits )
	{
		return bits.isIntersectionEqual( this );
	}

	@Override
	protected boolean isIntersectionEqualTo ( FixedBitSet192 bits )
	{
		return bits.isIntersectionEqual( this );
	}

	@Override
	protected boolean isIntersectionEqualTo ( FixedBitSet256 bits )
	{
		return bits.isIntersectionEqual( this );
	}

	@Override
	public void copyFrom ( FixedBitSet bits )
	{
		bits.copyTo( this );
	}

	public final void copyFrom ( FixedBitSet64 bits )
	{
		this.word0 = bits.word0;
	}

	@Override
	protected final void copyTo ( FixedBitSet64 bits )
	{
		bits.copyFrom( this );
	}

	@Override
	protected void copyTo ( FixedBitSet128 bits )
	{
		bits.copyFrom( this );
	}

	@Override
	protected void copyTo ( FixedBitSet192 bits )
	{
		bits.copyFrom( this );
	}

	@Override
	protected void copyTo ( FixedBitSet256 bits )
	{
		bits.copyFrom( this );
	}

	@Override
	public long getWord ( int index )
	{
		return word0;
	}

	@Override
	public void clear ()
	{
		word0 = 0L;
	}

	@Override
	public int capacity ()
	{
		return 64;
	}

	@Override
	public int size ()
	{
		return 1;
	}

	@Override
	public boolean isEmpty ()
	{
		return word0 == 0L;
	}

	@Override
	public int cardinality ()
	{
		return Long.bitCount( word0 );
	}
}
