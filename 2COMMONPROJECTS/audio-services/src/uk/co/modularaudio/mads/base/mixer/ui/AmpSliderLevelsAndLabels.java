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

package uk.co.modularaudio.mads.base.mixer.ui;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;

import uk.co.modularaudio.util.audio.math.DbToLevelComputer;
import uk.co.modularaudio.util.audio.math.MixdownSliderDbToLevelComputer;
import uk.co.modularaudio.util.math.MathFormatter;

public class AmpSliderLevelsAndLabels
{
//	private static Log log = LogFactory.getLog( AmpSliderLevelsAndLabels.class.getName() );
	
	public final static int AMP_SLIDER_NUM_STEPS = 1000;
	
	private static Integer creationMutex = new Integer(0);
	private DbToLevelComputer dbToLevelComputer = null;
	private AmpSliderLabelHashtable labels = null;
	
	private static Map<String, AmpSliderLevelsAndLabels> fontAndColorToLabelsMap = new HashMap<String, AmpSliderLevelsAndLabels>();

	public AmpSliderLevelsAndLabels( Font f, Color foregroundColour )
	{
		dbToLevelComputer = new MixdownSliderDbToLevelComputer( AMP_SLIDER_NUM_STEPS );
		labels = new AmpSliderLabelHashtable();
		
		float[] dbLevelsToLabel = new float[] { 10.0f, 5.0f, 0.0f, -5.0f, -10.0f, -20.0f, -30.0f, -50.0f, -70.0f, Float.NEGATIVE_INFINITY };
		for( int i = 0 ; i < dbLevelsToLabel.length ; i++ )
		{
			float dbLevel = dbLevelsToLabel[ i ];
			float sliderVal = dbToLevelComputer.toNormalisedSliderLevelFromDb( dbLevel );
			int asInt = (int)(sliderVal * AmpSliderLevelsAndLabels.AMP_SLIDER_NUM_STEPS);
			String floatAsString = (dbLevel == Float.NEGATIVE_INFINITY ? "-Inf" : MathFormatter.fastFloatPrint( dbLevel, 0, true ) );
			labels.put( asInt, buildLabel( f, floatAsString, foregroundColour ) );
		}

	}
	
	private JLabel buildLabel( Font f, String label, Color foregroundColour )
	{
		JLabel retVal = new JLabel( label );
		retVal.setFont( f );
		retVal.setForeground( foregroundColour );
		return retVal;
	}
	
	public static AmpSliderLevelsAndLabels getInstance( Font f, Color foregroundColor )
	{
		AmpSliderLevelsAndLabels instance = null;
		synchronized( creationMutex )
		{
			String strHash = f.toString() + ":" + foregroundColor.toString();
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
		}
		
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
