package com.artemis.utils;

/**
 * 
 * Mutable bit field iterator.
 * 
 * You can provide it an array of longs representing bit fields, this iterator
 * will iterate from the first word until the last in the passed array.
 * 
 * You can call {@link #reset()} to reset the iterator to the first word and
 * start again.
 * 
 * You can also set other bit fields whenever you want to with
 * {@link #setBits(long[])} method.
 * 
 * @author dustContributor
 *
 */
public final class MutableBitIterator
{
	private long[] bits;
	private long word;

	private int length;
	private int wordi;

	public MutableBitIterator ()
	{
		// Empty.
	}

	public MutableBitIterator ( final long[] bits )
	{
		this.setBits( bits );
	}

	public final void setBits ( final long[] bits )
	{
		this.bits = bits;
		this.length = bits.length;
	}

	public final void startingFrom ( final int index )
	{
		long msk = -1L << (index & 63);
		// Computing starting word index.
		int wordIndex = index >> 6;
		// Masking unwanted bits from that word.
		word = bits[wordIndex] & msk;
		// Storing index.
		wordi = wordIndex;
	}

	/**
	 * Resets the position of this iterator to 0.
	 */
	public final void reset ()
	{
		this.wordi = 0;
		this.word = bits[0];
	}

	/**
	 * Returns the next set bit of the bits in this iterator.
	 * 
	 * @return the index of the next set bit if there is one, -1 otherwise.
	 */
	public final int nextSetBit ()
	{
		final long[] bits = this.bits;
		final int length = this.length;

		long word = this.word;
		int wordi = this.wordi;

		while ( word == 0L )
		{
			if ( ++wordi >= length )
			{
				// No words left.
				return -1;
			}
			word = bits[wordi];
		}
		// Word and anti word.
		long val = word & -word;
		// Compute offset.
		int offset = wordi << 6;
		// Store the new word index.
		this.wordi = wordi;
		// Flip bit and store.
		this.word = word ^ val;
		// Return offset + next set bit of word.
		return offset + Long.bitCount( val - 1 );
	}
}
