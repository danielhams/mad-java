package uk.co.modularaudio.componentdesigner.profiling;

public class TimestampFormatter
{
	private static int NUM_CHARS_FOR_NANOS_FORMATTING = 2 + 1 + 3 + 1 + 3 + 1 + 3;

	public static String formatNanos( final long nanos )
	{
		final char output[] = new char[NUM_CHARS_FOR_NANOS_FORMATTING];

		long residual = nanos;
		int currentChar = 0;

		for( int i = 0 ; i < 3 ; ++i )
		{

			for( int d = 0 ; d < 3 ; ++d )
			{
				final int remainder = (int)residual % 10;
				output[currentChar++] = (char)('0' + remainder);
				residual /= 10;
			}
			output[currentChar++] = '.';
		}

		int remainder = (int)residual % 10;
		output[currentChar++] = (char)('0' + remainder);
		residual /= 10;
		remainder = (int)residual % 10;
		output[currentChar++] = (char)('0' + remainder);

		// Now reverse it
		final StringBuilder sb = new StringBuilder();
		for( int i = NUM_CHARS_FOR_NANOS_FORMATTING - 1 ; i >= 0 ; --i )
		{
			sb.append( output[i] );
		}

		return sb.toString();
	}

}
