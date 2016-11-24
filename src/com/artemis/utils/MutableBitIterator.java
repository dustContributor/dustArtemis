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
		this.length = bits.length;
		this.bits = bits;
	}

	public final void selectWord ( final int word )
	{
		this.wordi = word;
		this.word = bits[word];
	}

	public final void selectIndex ( final int index )
	{
		final long msk = -1L << (index & 63);
		// Computing starting word index.
		final int wordIndex = index >>> 6;
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

	public final int nextSetBit ()
	{
		return nextSetBit( length );
	}

	/**
	 * Returns the next set bit of the bits in this iterator.
	 *
	 * @param end word to which iterate up to.
	 *
	 * @return the index of the next set bit if there is one, -1 otherwise.
	 */
	public final int nextSetBit ( final int end )
	{
		final long[] bits = this.bits;
		long word = this.word;
		int wordi = this.wordi;

		while ( word == 0L )
		{
			if ( ++wordi >= end )
			{
				// No words left.
				return -1;
			}
			word = bits[wordi];
		}
		// Clear least significant digit.
		final long val = word & -word;
		// Compute offset.
		final int offset = wordi << 6;
		// Store the new word index.
		this.wordi = wordi;
		// Flip bit and store.
		this.word = word ^ val;
		// Return offset + next set bit of word.
		return offset + Long.bitCount( val - 1 );
	}
}
