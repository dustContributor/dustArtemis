package org.apache.lucene.util;

public class FixedBitSet192 extends FixedBitSet128
{
	public long word2;

	public FixedBitSet192 ()
	{
		// Empty.
	}

	@Override
	protected boolean getOnWord ( int word, long msk )
	{
		switch (word)
		{
			case 0:
				return (word0 & msk) != 0L;
			case 1:
				return (word1 & msk) != 0L;
			case 2:
				return (word2 & msk) != 0L;
			default:
				return false;
		}
	}

	@Override
	protected int getBitOnWord ( int word, int index )
	{
		switch (word)
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
	protected void clearOnWord ( int word, long msk )
	{
		switch (word)
		{
			case 0:
				word0 &= ~msk;
				return;
			case 1:
				word1 &= ~msk;
				return;
			case 2:
				word2 &= ~msk;
				return;
			default:
				return;
		}
	}

	@Override
	protected void setOnWord ( int word, long msk )
	{
		switch (word)
		{
			case 0:
				word0 |= msk;
				return;
			case 1:
				word1 |= msk;
				return;
			case 2:
				word2 |= msk;
				return;
			default:
				return;
		}
	}

	@Override
	public boolean intersects ( FixedBitSet bits )
	{
		return intersects( (FixedBitSet192) bits );
	}

	@Override
	public void and ( FixedBitSet bits )
	{
		and( (FixedBitSet192) bits );
	}

	@Override
	public void or ( FixedBitSet bits )
	{
		or( (FixedBitSet192) bits );
	}

	@Override
	public void andNot ( FixedBitSet bits )
	{
		andNot( (FixedBitSet192) bits );
	}

	@Override
	public int intersectCount ( FixedBitSet bits )
	{
		return intersectCount( (FixedBitSet192) bits );
	}

	public final boolean intersects ( FixedBitSet192 bits )
	{
		return super.intersects( bits ) && (bits.word2 & word2) != 0L;
	}

	public final void or ( FixedBitSet192 bits )
	{
		super.or( bits );
		word2 |= bits.word2;
	}

	public final void and ( FixedBitSet192 bits )
	{
		super.and( bits );
		word2 &= bits.word2;
	}

	public final void andNot ( FixedBitSet192 bits )
	{
		super.andNot( bits );
		word2 &= ~bits.word2;
	}

	public final int intersectCount ( FixedBitSet192 bits )
	{
		return super.intersectCount( bits ) + Long.bitCount( bits.word2 & word2 );
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
	public int cardinality ()
	{
		return super.cardinality() + Long.bitCount( word2 );
	}
}
