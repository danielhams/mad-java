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

package uk.co.modularaudio.util.mvc.intslider.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import uk.co.modularaudio.util.mvc.MvcListenerEvent.EventType;
import uk.co.modularaudio.util.mvc.intslider.IntegerSliderModel;
import uk.co.modularaudio.util.mvc.intslider.IntegerSliderModelListener;
import uk.co.modularaudio.util.mvc.intslider.IntegerSliderModelListenerEvent;
import uk.co.modularaudio.util.mvc.intslider.ValueOutOfRangeException;

public class BasicIntegerSliderModel implements IntegerSliderModel
{
//	private static Log log = LogFactory.getLog( BasicIntegerSliderModel.class.getName() );

	private int minValue = Integer.MIN_VALUE;
	private int maxValue = Integer.MAX_VALUE;
	private int value = -1;

	private final Set<IntegerSliderModelListener> listeners = new HashSet<IntegerSliderModelListener>();

	public BasicIntegerSliderModel()
	{
	}

	public BasicIntegerSliderModel( final int minValue, final int maxValue,
			final int startValue )
	{
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.value = startValue;
	}

	@Override
	public void setMaxValue(final int maxValue)
	{
		this.maxValue = maxValue;
	}

	@Override
	public void setMinValue(final int minValue)
	{
		this.minValue = minValue;
	}

	@Override
	public int getMaxValue()
	{
		return maxValue;
	}

	@Override
	public int getMinVaue()
	{
		return minValue;
	}

	@Override
	public void setValue(final int value) throws ValueOutOfRangeException
	{
		if( value < minValue || value > maxValue )
		{
			throw new ValueOutOfRangeException();
		}
		else
		{
			this.value = value;
			for( final IntegerSliderModelListener l : listeners )
			{
				final IntegerSliderModelListenerEvent e = new IntegerSliderModelListenerEvent( EventType.SELECTION_CHANGED, -1, -1 );
				l.valueChanged( e );
			}
		}
	}

	@Override
	public int getValue()
	{
		return value;
	}

	@Override
	public void addListener(final IntegerSliderModelListener listener)
	{
		listeners.add( listener );
	}

	@Override
	public void removeListener(final IntegerSliderModelListener listener)
	{
		listeners.remove( listener );
	}

	@Override
	public Collection<IntegerSliderModelListener> getListeners()
	{
		return listeners;
	}

}
