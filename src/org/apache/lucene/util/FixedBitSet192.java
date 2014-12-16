package org.apache.lucene.util;

public class FixedBitSet192 extends FixedBitSet128
{
	public long word2;

	public FixedBitSet192 ()
	{
		// Empty.
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
			case 2:
				return (word2 & (1L << index)) != 0L;
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
			case 2:
				return (int) ((word2 >>> index) & 0x01L);
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
			case 2:
				word2 &= ~(1L << index);
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
			case 2:
				word2 |= (1L << index);
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

	public final void and ( FixedBitSet192 bits )
	{
		super.and( bits );
		word2 &= bits.word2;
	}

	@Override
	protected final void andTo ( FixedBitSet192 bits )
	{
		bits.and( this );
	}

	@Override
	public void or ( FixedBitSet bits )
	{
		bits.orTo( this );
	}

	public final void or ( FixedBitSet192 bits )
	{
		super.or( bits );
		word2 |= bits.word2;
	}

	@Override
	protected final void orTo ( FixedBitSet192 bits )
	{
		bits.or( this );
	}

	@Override
	public void andNot ( FixedBitSet bits )
	{
		bits.andNotTo( this );
	}

	public final void andNot ( FixedBitSet192 bits )
	{
		super.andNot( bits );
		word2 &= ~bits.word2;
	}

	@Override
	protected final void andNotTo ( FixedBitSet192 bits )
	{
		bits.andNot( this );
	}

	@Override
	public boolean intersects ( FixedBitSet bits )
	{
		return bits.intersectsTo( this );
	}

	public final boolean intersects ( FixedBitSet192 bits )
	{
		return super.intersects( bits ) || (bits.word2 & word2) != 0L;
	}

	@Override
	protected final boolean intersectsTo ( FixedBitSet192 bits )
	{
		return bits.intersects( this );
	}

	@Override
	public int intersectCount ( FixedBitSet bits )
	{
		return bits.intersectCountTo( this );
	}

	public final int intersectCount ( FixedBitSet192 bits )
	{
		return super.intersectCount( bits ) + Long.bitCount( bits.word2 & word2 );
	}

	@Override
	protected final int intersectCountTo ( FixedBitSet192 bits )
	{
		return bits.intersectCount( this );
	}

	@Override
	public boolean isIntersectionEqual ( FixedBitSet bits )
	{
		return bits.isIntersectionEqualTo( this );
	}

	public final boolean isIntersectionEqual ( FixedBitSet192 bits )
	{
		return super.isIntersectionEqual( bits ) && (bits.word2 & word2) == word2;
	}

	@Override
	protected final boolean isIntersectionEqualTo ( FixedBitSet192 bits )
	{
		return bits.isIntersectionEqual( this );
	}

	@Override
	public void copyFrom ( FixedBitSet bits )
	{
		bits.copyTo( this );
	}

	public final void copyFrom ( FixedBitSet192 bits )
	{
		super.copyFrom( bits );
		this.word2 = bits.word2;
	}

	@Override
	protected final void copyTo ( FixedBitSet192 bits )
	{
		bits.copyFrom( this );
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
				return word2;
		}
	}

	@Override
	public void clear ()
	{
		super.clear();
		word2 = 0L;
	}

	@Override
	public int capacity ()
	{
		return 192;
	}

	@Override
	public int size ()
	{
		return 3;
	}

	@Override
	public boolean isEmpty ()
	{
		return super.isEmpty() && word2 == 0L;
	}

	@Override
	public int cardinality ()
	{
		return super.cardinality() + Long.bitCount( word2 );
	}
}
