package com.artemis.utils;

import org.apache.lucene.util.FixedBitSet;
import org.apache.lucene.util.FixedBitSet128;
import org.apache.lucene.util.FixedBitSet192;
import org.apache.lucene.util.FixedBitSet256;
import org.apache.lucene.util.FixedBitSet64;

public final class FixedBitIterator
{
	private FixedBitSet bits;
	private long word;

	private int wordi;
	private int offset;

	public FixedBitIterator ()
	{
		// Empty.
	}

	public FixedBitIterator ( FixedBitSet bits )
	{
		this.setBits( bits );
	}

	public final void setBits ( FixedBitSet bits )
	{
		this.bits = bits;
		reset();
	}

	/**
	 * Resets the position of this iterator to 0.
	 */
	public final void reset ()
	{
		this.word = selectWord( bits, 0 );
		this.wordi = 0;
		this.offset = 0;
	}

	/**
	 * Returns the next set bit of the bits in this iterator.
	 * 
	 * @return the index of the next set bit if there is one, -1 otherwise.
	 */
	public final int nextSetBit ()
	{
		while ( word == 0 )
		{
			// If no words left.
			if ( ++wordi >= bits.size() )
			{
				return -1;
			}
			// Store word.
			word = selectWord( bits, wordi );
			// Increase offset.
			offset = wordi * 64;
		}
		// Word and anti word.
		long val = word & -word;
		// Flip bit.
		word ^= val;
		// Return offset + next set bit of word.
		return offset + Long.bitCount( val - 1 );
	}

	private static final long selectWord ( final FixedBitSet bits, final int wordi )
	{
		switch (wordi)
		{
			case 0:
				return ((FixedBitSet64) bits).word0;
			case 1:
				return ((FixedBitSet128) bits).word1;
			case 2:
				return ((FixedBitSet192) bits).word2;
			case 3:
				return ((FixedBitSet256) bits).word3;
			default:
				return -1;
		}
	}

}
