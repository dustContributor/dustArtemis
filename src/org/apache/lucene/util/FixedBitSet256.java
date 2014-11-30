package org.apache.lucene.util;

public class FixedBitSet256 extends FixedBitSet192
{
	public long word3;

	public FixedBitSet256 ()
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
			case 3:
				return (word3 & (1L << index)) != 0L;
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
			case 3:
				return (int) ((word3 >>> index) & 0x01L);
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
			case 3:
				word3 &= ~(1L << index);
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
			case 3:
				word3 |= (1L << index);
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

	public final void and ( FixedBitSet256 bits )
	{
		super.and( bits );
		word3 &= bits.word3;
	}

	@Override
	protected final void andTo ( FixedBitSet256 bits )
	{
		bits.and( this );
	}

	@Override
	public void or ( FixedBitSet bits )
	{
		bits.orTo( this );
	}

	public final void or ( FixedBitSet256 bits )
	{
		super.or( bits );
		word3 |= bits.word3;
	}

	@Override
	protected final void orTo ( FixedBitSet256 bits )
	{
		bits.or( this );
	}

	@Override
	public void andNot ( FixedBitSet bits )
	{
		bits.andNotTo( this );
	}

	protected final void andNot ( FixedBitSet256 bits )
	{
		super.andNot( bits );
		word3 &= ~bits.word3;
	}

	@Override
	public void andNotTo ( FixedBitSet256 bits )
	{
		bits.andNot( this );
	}

	@Override
	public int intersectCount ( FixedBitSet bits )
	{
		return bits.intersectCountTo( this );
	}

	public final int intersectCount ( FixedBitSet256 bits )
	{
		return super.intersectCount( bits ) + Long.bitCount( bits.word3 & word3 );
	}

	@Override
	protected final int intersectCountTo ( FixedBitSet256 bits )
	{
		return bits.intersectCount( this );
	}

	@Override
	public boolean intersects ( FixedBitSet bits )
	{
		return bits.intersectsTo( this );
	}

	public final boolean intersects ( FixedBitSet256 bits )
	{
		return super.intersects( bits ) || (bits.word3 & word3) != 0L;
	}

	@Override
	protected final boolean intersectsTo ( FixedBitSet256 bits )
	{
		return bits.intersects( this );
	}

	@Override
	public boolean isIntersectionEqual ( FixedBitSet bits )
	{
		return bits.isIntersectionEqualTo( this );
	}

	public final boolean isIntersectionEqual ( FixedBitSet256 bits )
	{
		return super.isIntersectionEqual( bits ) && (bits.word3 & word3) == word3;
	}

	@Override
	protected final boolean isIntersectionEqualTo ( FixedBitSet256 bits )
	{
		return bits.isIntersectionEqual( this );
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
			case 2:
				return word2;
			default:
				return word3;
		}
	}

	@Override
	public void clear ()
	{
		super.clear();
		word3 = 0L;
	}

	@Override
	public int capacity ()
	{
		return 256;
	}

	@Override
	public int size ()
	{
		return 4;
	}

	@Override
	public boolean isEmpty ()
	{
		return super.isEmpty() && word3 == 0L;
	}

	@Override
	public int cardinality ()
	{
		return super.cardinality() + Long.bitCount( word3 );
	}
}
