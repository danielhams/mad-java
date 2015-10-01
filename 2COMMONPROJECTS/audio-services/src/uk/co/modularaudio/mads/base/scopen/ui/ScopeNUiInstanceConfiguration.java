/**
 *
 * Copyright (C) 2015 - Daniel Hams, Modular Audio Limited
 *                      daniel.hams@gmail.com
 *
 * Mad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mad.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.modularaudio.mads.base.scopen.ui;

import java.awt.Color;
import java.awt.Point;

import uk.co.modularaudio.mads.base.scopen.mu.ScopeNInstanceConfiguration;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;

public class ScopeNUiInstanceConfiguration extends ScopeNInstanceConfiguration
{

	private final int[] chanIndexes;
	private final Point[] chanPosis;

//	new Point( 120,  70 ),
//	new Point( 140,  70 ),
//	new Point( 140,  90 ),
//	new Point( 140, 110 ),
//	new Point( 140, 130 ),

	private final static int TRIGGER_X_OFFSET = 120;
	private final static int CHAN_Y_START = 70;
	private final static int SCOPE_CHAN_X_OFFSET = 140;
	private final static int CHAN_Y_DELTA = 20;

	private final Color[] visColours;

	public ScopeNUiInstanceConfiguration( final ScopeNInstanceConfiguration instanceConfiguration,
			final Color[] visColours )
		throws MadProcessingException
	{
		super( instanceConfiguration );
		final int numScopeChannels = instanceConfiguration.getNumScopeChannels();
		final int numTotalChannels = instanceConfiguration.getNumTotalChannels();

		chanIndexes = new int[ numTotalChannels ];
		chanPosis = new Point[ numTotalChannels ];

		chanIndexes[0] = ScopeNInstanceConfiguration.TRIGGER_INDEX;
		chanPosis[0] = new Point( TRIGGER_X_OFFSET, CHAN_Y_START );

		for( int i = 1 ; i <= numScopeChannels ; ++i )
		{
			chanIndexes[i] = i;
			chanPosis[i] = new Point( SCOPE_CHAN_X_OFFSET, CHAN_Y_START + ((i-1)*CHAN_Y_DELTA) );
		}

		this.visColours = visColours;
	}

	public int[] getChanIndexes()
	{
		return chanIndexes;
	}

	public Point[] getChanPosis()
	{
		return chanPosis;
	}

	public Color[] getVisColours()
	{
		return this.visColours;
	}

}
