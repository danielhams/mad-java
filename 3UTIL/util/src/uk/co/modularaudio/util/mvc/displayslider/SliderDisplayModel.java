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

package uk.co.modularaudio.util.mvc.displayslider;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SliderDisplayModel
{
	private static Log log = LogFactory.getLog( SliderDisplayModel.class.getName() );
	
	private float minValue = 0.0f;
	private float maxValue = 1.0f;
	private float initialValue = 0.0f;
	private int numSliderSteps = 100;
	private int sliderMajorTickSpacing = 10;

	private SliderIntToFloatConverter sliderIntToFloatConverter = new SimpleSliderIntToFloatConverter();
	private int displayNumSigPlaces = 3;
	private int displayNumDecPlaces = 2;
	
	private String displayUnitsStr = "";
	
	private float currentValue = initialValue;

	public interface ValueChangeListener
	{
		void receiveValueChange( Object source, float newValue );
	}
	
	private ArrayList<ValueChangeListener> changeListeners = new ArrayList<SliderDisplayModel.ValueChangeListener>();

	public SliderDisplayModel()
	{
		log.warn("Using default model values - this is probably not what you want.");
		// Uses default values above.
	}
		
	public SliderDisplayModel( float minValue,
			float maxValue,
			float initialValue,
			int numSliderSteps,
			int sliderMajorTickSpacing,
			SliderIntToFloatConverter sliderIntToFloatConverter,
			int displayNumSigPlaces,
			int displayNumDecPlaces,
			String displayUnitsStr )
	{
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.initialValue = initialValue;
		this.numSliderSteps = numSliderSteps;
		this.sliderMajorTickSpacing = sliderMajorTickSpacing;
		this.sliderIntToFloatConverter = sliderIntToFloatConverter;
		this.displayNumSigPlaces = displayNumSigPlaces;
		this.displayNumDecPlaces = displayNumDecPlaces;
		this.displayUnitsStr = displayUnitsStr;

		currentValue = initialValue;
	}

	public float getMinValue()
	{
		return minValue;
	}

	public float getMaxValue()
	{
		return maxValue;
	}

	public float getInitialValue()
	{
		return initialValue;
	}

	public int getNumSliderSteps()
	{
		return numSliderSteps;
	}

	public SliderIntToFloatConverter getSliderIntToFloatConverter()
	{
		return sliderIntToFloatConverter;
	}

	public int getDisplayNumSigPlaces()
	{
		return displayNumSigPlaces;
	}

	public int getDisplayNumDecPlaces()
	{
		return displayNumDecPlaces;
	}

	public String getDisplayUnitsStr()
	{
		return displayUnitsStr;
	}
	
	public void addChangeListener( ValueChangeListener l )
	{
		changeListeners.add( l );
	}
	
	public void removeChangeListener( ValueChangeListener l )
	{
		changeListeners.remove( l );
	}

	public float getValue()
	{
		return currentValue;
	}

	public void setValue( Object source, float newFloatValue )
	{
//		log.debug("setValue " + newFloatValue + " called from " +source.getClass().getSimpleName() );
		boolean wasDifferent = ( currentValue != newFloatValue );
		currentValue = newFloatValue;
//		if( currentValue > maxValue )
//		{
//			currentValue = maxValue;
//		}
//		if( currentValue < minValue )
//		{
//			currentValue = minValue;
//		}
		if( wasDifferent )
		{
			notifyOfChange( source );
		}
	}

	private void notifyOfChange( Object source )
	{
		for( int i = 0 ; i < changeListeners.size() ; ++i )
		{
			ValueChangeListener cl = changeListeners.get( i );
			if( cl != source )
			{
				cl.receiveValueChange( source, currentValue );
			}
		}
	}

	public int getSliderMajorTickSpacing()
	{
		return sliderMajorTickSpacing;
	}

	public void setMaxValue( float newTimescaleUpperLimit )
	{
		maxValue = newTimescaleUpperLimit;
		notifyOfChange( this );
	}

}
