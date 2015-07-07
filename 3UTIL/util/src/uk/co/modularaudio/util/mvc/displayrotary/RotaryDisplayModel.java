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

package uk.co.modularaudio.util.mvc.displayrotary;

import java.util.ArrayList;

public class RotaryDisplayModel
{
//	private static Log log = LogFactory.getLog( RotaryDisplayModel.class.getName() );

	private float minValue;
	private float maxValue;
	private final float initialValue;
	private float defaultValue;
	private final int numSteps;
	private int majorTickSpacing;

	private final RotaryIntToFloatConverter intToFloatConverter;
	private final int displayNumSigPlaces;
	private final int displayNumDecPlaces;

	private final String displayUnitsStr;

	private float currentValue;

	public interface ValueChangeListener
	{
		void receiveValueChange( Object source, float newValue );
	}

	private final ArrayList<ValueChangeListener> changeListeners = new ArrayList<RotaryDisplayModel.ValueChangeListener>();

	public RotaryDisplayModel( final float minValue,
			final float maxValue,
			final float initialValue,
			final float defaultValue,
			final int numSteps,
			final int majorTickSpacing,
			final RotaryIntToFloatConverter intToFloatConverter,
			final int displayNumSigPlaces,
			final int displayNumDecPlaces,
			final String displayUnitsStr )
	{
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.initialValue = initialValue;
		this.defaultValue = defaultValue;
		this.numSteps = numSteps;
		this.majorTickSpacing = majorTickSpacing;
		this.intToFloatConverter = intToFloatConverter;
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

	public int getNumSteps()
	{
		return numSteps;
	}

	public RotaryIntToFloatConverter getIntToFloatConverter()
	{
		return intToFloatConverter;
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
//		log.debug("setValue " + iNewFloatValue + " called from " + source.getClass().getSimpleName() );
		float newFloatValue = iNewFloatValue;
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

	public int getMajorTickSpacing()
	{
		return majorTickSpacing;
	}

	public void setMajorTickSpacing( final int majorTickSpacing )
	{
		this.majorTickSpacing = majorTickSpacing;
	}

	public void setMaxValue( final float newMaxValue )
	{
		maxValue = newMaxValue;
		notifyOfChange( this );
	}

	public void setMinValue( final float newMinValue )
	{
		minValue = newMinValue;
		notifyOfChange( this );
	}

	public void moveByMajorTick( final Object source, final int direction )
	{
		final int currentValueAsStep = intToFloatConverter.floatValueToSliderIntValue( this, currentValue );
		int newStep = currentValueAsStep + (majorTickSpacing * direction );
		newStep = (newStep > numSteps ? numSteps :
					(newStep < 0 ? 0 : newStep )
					);
		final float newValue = intToFloatConverter.sliderIntValueToFloatValue( this, newStep );
		setValue( source, newValue );
	}

	public float getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue( final float defaultValue )
	{
		this.defaultValue = defaultValue;
	}

	public void moveByMinorTick( final Object source, final int direction )
	{
		final int currentValueAsStep = intToFloatConverter.floatValueToSliderIntValue( this, currentValue );
		int newStep = currentValueAsStep + direction;
		newStep = (newStep > numSteps ? numSteps :
					(newStep < 0 ? 0 : newStep )
					);
		final float newValue = intToFloatConverter.sliderIntValueToFloatValue( this, newStep );
		setValue( source, newValue );
	}
}
