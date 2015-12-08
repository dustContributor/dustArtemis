package org.apache.lucene.util;

public class FixedBitSet128 extends FixedBitSet64
{
	public long word1;

	public FixedBitSet128 ()
	{
		this( 2 );
	}

	protected FixedBitSet128 ( int size )
	{
		super( size );
	}

	@Override
	public boolean get ( int index )
	{
		switch (index / 64)
		{
			case 0:
				return (word0 & (1L << index)) != 0L;
			case 1:
				return (word1 & (1L << index)) != 0L;
			default:
				return false;
		}
	}

	@Override
	public int getBit ( int index )
	{
		switch (index / 64)
		{
			case 0:
				return (int) ((word0 >>> index) & 0x01L);
			case 1:
				return (int) ((word1 >>> index) & 0x01L);
			default:
				return 0;
		}
	}

	@Override
	public void clear ( int index )
	{
		switch (index / 64)
		{
			case 0:
				word0 &= ~(1L << index);
				return;
			case 1:
				word1 &= ~(1L << index);
				return;
			default:
				return;
		}
	}

	@Override
	public void set ( int index )
	{
		switch (index / 64)
		{
			case 0:
				word0 |= (1L << index);
				return;
			case 1:
				word1 |= (1L << index);
				return;
			default:
				return;
		}
	}

	@Override
	public void and ( FixedBitSet bits )
	{
		bits.andTo( this );
	}

	public final void and ( FixedBitSet128 bits )
	{
		super.and( bits );
		word1 &= bits.word1;
	}

	@Override
	protected final void andTo ( FixedBitSet128 bits )
	{
		bits.and( this );
	}

	@Override
	public void or ( FixedBitSet bits )
	{
		bits.orTo( this );
	}

	public final void or ( FixedBitSet128 bits )
	{
		super.or( bits );
		word1 |= bits.word1;
	}

	@Override
	protected final void orTo ( FixedBitSet128 bits )
	{
		bits.or( this );
	}

	@Override
	public void andNot ( FixedBitSet bits )
	{
		bits.andNotTo( this );
	}

	public final void andNot ( FixedBitSet128 bits )
	{
		super.andNot( bits );
		word1 &= ~bits.word1;
	}

	@Override
	protected final void andNotTo ( FixedBitSet128 bits )
	{
		bits.andNot( this );
	}

	@Override
	public int intersectCount ( FixedBitSet bits )
	{
		return bits.intersectCountTo( this );
	}

	public final int intersectCount ( FixedBitSet128 bits )
	{
		return super.intersectCount( bits ) + Long.bitCount( bits.word1 & word1 );
	}

	@Override
	protected final int intersectCountTo ( FixedBitSet128 bits )
	{
		return bits.intersectCount( this );
	}

	@Override
	public boolean intersects ( FixedBitSet bits )
	{
		return bits.intersectsTo( this );
	}

	public final boolean intersects ( FixedBitSet128 bits )
	{
		return super.intersects( bits ) || (bits.word1 & word1) != 0L;
	}

	@Override
	protected final boolean intersectsTo ( FixedBitSet128 bits )
	{
		return bits.intersects( this );
	}

	@Override
	public boolean isIntersectionEqual ( FixedBitSet bits )
	{
		return bits.isIntersectionEqualTo( this );
	}

	public final boolean isIntersectionEqual ( FixedBitSet128 bits )
	{
		return super.isIntersectionEqual( bits ) && (bits.word1 & word1) == word1;
	}

	@Override
	protected final boolean isIntersectionEqualTo ( FixedBitSet128 bits )
	{
		return bits.isIntersectionEqual( this );
	}

	@Override
	public void copyFrom ( FixedBitSet bits )
	{
		bits.copyTo( this );
	}

	public final void copyFrom ( FixedBitSet128 bits )
	{
		super.copyFrom( bits );
		this.word1 = bits.word1;
	}

	@Override
	protected final void copyTo ( FixedBitSet128 bits )
	{
		bits.copyFrom( this );
	}

	@Override
	public void clear ()
	{
		super.clear();
		word1 = 0L;
	}

	@Override
	public void fill ()
	{
		super.fill();
		word1 = -1L;
	}

	@Override
	public long getWord ( int index )
	{
		switch (index)
		{
			case 0:
				return word0;
			case 1:
				return word1;
			default:
				return 0L;
		}
	}

	@Override
	public boolean isEmpty ()
	{
		return super.isEmpty() && word1 == 0L;
	}

	@Override
	public int cardinality ()
	{
		return super.cardinality() + Long.bitCount( word1 );
	}
}
