package org.apache.lucene.util;

public class FixedBitSet64 extends FixedBitSet
{
	public long word0;

	public FixedBitSet64 ()
	{
		// Empty.
	}

	@Override
	protected boolean getOnWord ( int word, long msk )
	{
		return (word0 & msk) != 0L;
	}

	@Override
	protected int getBitOnWord ( int word, int index )
	{
		return (int) ((word0 >>> index) & 0x01L);
	}

	@Override
	protected void setOnWord ( int word, long msk )
	{
		word0 |= msk;
	}

	@Override
	protected void clearOnWord ( int word, long msk )
	{
		word0 &= ~msk;
	}

	@Override
	public void and ( FixedBitSet bits )
	{
		and( (FixedBitSet64) bits );
	}

	@Override
	public void or ( FixedBitSet bits )
	{
		or( (FixedBitSet64) bits );
	}

	@Override
	public void andNot ( FixedBitSet bits )
	{
		andNot( (FixedBitSet64) bits );
	}

	@Override
	public boolean intersects ( FixedBitSet bits )
	{
		return intersects( (FixedBitSet64) bits );
	}

	@Override
	public int intersectCount ( FixedBitSet bits )
	{
		return intersectCount( (FixedBitSet64) bits );
	}

	public final boolean intersects ( FixedBitSet64 bits )
	{
		return (bits.word0 & word0) != 0L;
	}

	public final void and ( FixedBitSet64 bits )
	{
		word0 &= bits.word0;
	}

	public final void or ( FixedBitSet64 bits )
	{
		word0 |= bits.word0;
	}

	public final void andNot ( FixedBitSet64 bits )
	{
		word0 &= ~bits.word0;
	}

	public final int intersectCount ( FixedBitSet64 bits )
	{
		return Long.bitCount( bits.word0 & word0 );
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
	public int cardinality ()
	{
		return Long.bitCount( word0 );
	}
}
