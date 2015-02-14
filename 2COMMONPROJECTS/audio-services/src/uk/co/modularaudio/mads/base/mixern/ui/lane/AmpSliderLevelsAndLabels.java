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

package uk.co.modularaudio.mads.base.mixern.ui.lane;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JLabel;

import uk.co.modularaudio.util.audio.math.DbToLevelComputer;
import uk.co.modularaudio.util.audio.math.MixdownSliderDbToLevelComputer;
import uk.co.modularaudio.util.math.MathFormatter;

public class AmpSliderLevelsAndLabels
{
//	private static Log log = LogFactory.getLog( AmpSliderLevelsAndLabels.class.getName() );

	public final static int AMP_SLIDER_NUM_STEPS = 1000;

	private final static ReentrantLock CREATION_MUTEX = new ReentrantLock();
	private final DbToLevelComputer dbToLevelComputer;
	private final AmpSliderLabelHashtable labels;

	private static Map<String, AmpSliderLevelsAndLabels> fontAndColorToLabelsMap = new HashMap<String, AmpSliderLevelsAndLabels>();

	public AmpSliderLevelsAndLabels( final Font f, final Color foregroundColour )
	{
		dbToLevelComputer = new MixdownSliderDbToLevelComputer( AMP_SLIDER_NUM_STEPS );
		labels = new AmpSliderLabelHashtable();

		final float[] dbLevelsToLabel = new float[] { 10.0f, 5.0f, 0.0f, -5.0f, -10.0f, -20.0f, -30.0f, -50.0f, -70.0f, Float.NEGATIVE_INFINITY };
		for( int i = 0 ; i < dbLevelsToLabel.length ; i++ )
		{
			final float dbLevel = dbLevelsToLabel[ i ];
			final float sliderVal = dbToLevelComputer.toNormalisedSliderLevelFromDb( dbLevel );
			final int asInt = (int)(sliderVal * AmpSliderLevelsAndLabels.AMP_SLIDER_NUM_STEPS);
			final String floatAsString = (dbLevel == Float.NEGATIVE_INFINITY ? "-Inf" : MathFormatter.fastFloatPrint( dbLevel, 0, true ) );
			labels.put( asInt, buildLabel( f, floatAsString, foregroundColour ) );
		}

	}

	private JLabel buildLabel( final Font f, final String label, final Color foregroundColour )
	{
		final JLabel retVal = new JLabel( label );
		retVal.setFont( f );
		retVal.setForeground( foregroundColour );
		return retVal;
	}

	public static AmpSliderLevelsAndLabels getInstance( final Font f, final Color foregroundColor )
	{
		AmpSliderLevelsAndLabels instance = null;

		CREATION_MUTEX.lock();;

		final String strHash = f.toString() + ":" + foregroundColor.toString();
		instance = fontAndColorToLabelsMap.get( strHash );
		if( instance == null )
		{
			instance = new AmpSliderLevelsAndLabels( f, foregroundColor );
			fontAndColorToLabelsMap.put( strHash, instance );
		}
		else
		{
//				log.debug( "Found, using existing entry" );
		}

		CREATION_MUTEX.unlock();

		return instance;
	}

	public DbToLevelComputer getDbToLevelComputer()
	{
		return dbToLevelComputer;
	}

	public AmpSliderLabelHashtable getLabels()
	{
		return labels;
	}
}
