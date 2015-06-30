package uk.co.modularaudio.mads.base.spectralamp.ui;

import java.awt.Color;

public class PeakDisplayColourCache
{
	// 256 greys, 256 grey to red
	private final static int NUM_GREYS = 256;
	private final static int NUM_REDS = 256;
	private final static int NUM_COLOURS = NUM_GREYS + NUM_REDS;
	private final static Color[] COLOUR_MAP = new Color[NUM_COLOURS];

	static
	{
		int cIndex = 0;
		for( int i = 0 ; i < NUM_GREYS ; ++i )
		{
			final int greyLevel = (int)((i / (float)NUM_GREYS) * 256);
			final int colorValue = (greyLevel << 16) | (greyLevel << 8) | (greyLevel);
			COLOUR_MAP[cIndex++] = new Color( colorValue );
		}

		for( int i = 0 ; i < NUM_REDS ; ++i )
		{
			final int redLevel = (int)((i / (float)NUM_REDS) * 256);
			final int greyLevel = 255 - redLevel;
			final int colorValue = (255 << 16) | (greyLevel << 8) | (greyLevel);
			COLOUR_MAP[cIndex++] = new Color( colorValue );
		}
	}

	public static Color getColourForNormalisedValue( final float normalisedValue )
	{
		final int colourIndex = (int)(normalisedValue * NUM_COLOURS);
		return COLOUR_MAP[ colourIndex ];
	}
}
