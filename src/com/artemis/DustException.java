package com.artemis;

final class DustException extends RuntimeException
{
	private static final String ERROR_TAG_START = "[dustArtemis:";
	private static final String ERROR_TAG_END = ":ERROR] ";

	private static final long serialVersionUID = 1L;

	DustException ( final Object source, final String message )
	{
		super( makeMessage( source, message ) );
	}

	DustException ( final Object source, final String message, final Throwable throwable )
	{
		super( makeMessage( source, message ), throwable );
	}

	static final <T> T enforceNonNull ( final Object source, final T value, final String fieldName )
	{
		if ( value == null )
		{
			throw new DustException( source, String.valueOf( fieldName ) + " can't be null!" );
		}

		return value;
	}

	static final String makeMessage ( final Object source, final String message )
	{
		// Assume null then check later.
		String sourceName = "null";

		if ( source != null )
		{
			final Class<?> sourceType;
			// Check if we got passed a class or not.
			if ( source instanceof Class )
			{
				sourceType = (Class<?>) source;
			}
			else
			{
				sourceType = source.getClass();
			}
			// Now get the source class name for composing the message.
			sourceName = sourceType.getSimpleName();
		}

		return makeMessage( sourceName, message );
	}

	private static final String makeMessage ( final String sourceName, final String message )
	{
		// Compute size of the message.
		final int size = ERROR_TAG_START.length() + ERROR_TAG_END.length() +
				sourceName.length() + message.length();
		// Initialize message builder.
		final StringBuilder sb = new StringBuilder( size );
		// Compose message.
		sb.append( ERROR_TAG_START );
		sb.append( sourceName );
		sb.append( ERROR_TAG_END );
		sb.append( message );
		// And convert to string.
		return sb.toString();
	}

}
