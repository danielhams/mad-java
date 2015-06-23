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
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog( SliderDisplayModel.class.getName() );

	private final float minValue;
	private float maxValue;
	private final float initialValue;
	private final float defaultValue;
	private final int numSliderSteps;
	private final int sliderMajorTickSpacing;

	private final SliderIntToFloatConverter sliderIntToFloatConverter;
	private final int displayNumSigPlaces;
	private final int displayNumDecPlaces;

	private final String displayUnitsStr;

	private float currentValue;

	public interface ValueChangeListener
	{
		void receiveValueChange( Object source, float newValue );
	}

	private final ArrayList<ValueChangeListener> changeListeners = new ArrayList<SliderDisplayModel.ValueChangeListener>();

	public SliderDisplayModel( final float minValue,
			final float maxValue,
			final float initialValue,
			final float defaultValue,
			final int numSliderSteps,
			final int sliderMajorTickSpacing,
			final SliderIntToFloatConverter sliderIntToFloatConverter,
			final int displayNumSigPlaces,
			final int displayNumDecPlaces,
			final String displayUnitsStr )
	{
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.initialValue = initialValue;
		this.defaultValue = defaultValue;
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

	public float getDefaultValue()
	{
		return defaultValue;
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

	public void addChangeListener( final ValueChangeListener l )
	{
		changeListeners.add( l );
	}

	public void removeChangeListener( final ValueChangeListener l )
	{
		changeListeners.remove( l );
	}

	public float getValue()
	{
		return currentValue;
	}

	public void setValue( final Object source, final float iNewFloatValue )
	{
		float newFloatValue = iNewFloatValue;
//		log.debug("setValue " + newFloatValue + " called from " + source.getClass().getSimpleName() );
		if( newFloatValue > maxValue )
		{
			newFloatValue = maxValue;
		}
		else if( newFloatValue < minValue )
		{
			newFloatValue = minValue;
		}
		currentValue = newFloatValue;
		notifyOfChange( source );
	}

	private void notifyOfChange( final Object source )
	{
		for( int i = 0 ; i < changeListeners.size() ; ++i )
		{
			final ValueChangeListener cl = changeListeners.get( i );
			cl.receiveValueChange( source, currentValue );
		}
	}

	public int getSliderMajorTickSpacing()
	{
		return sliderMajorTickSpacing;
	}

	public void setMaxValue( final float newTimescaleUpperLimit )
	{
		maxValue = newTimescaleUpperLimit;
		notifyOfChange( this );
	}

}
