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

import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel.ValueChangeListener;

public class SliderDisplayModelAdaptor implements BoundedRangeModel, ValueChangeListener
{
//	private static Log log = LogFactory.getLog( SliderDisplayModelAdaptor.class.getName() );

	private final Object source;
	private final SliderDisplayModel sdm;
	private final SliderDisplayController sdc;

	private final SliderIntToFloatConverter sitfc;

	private final ArrayList<ChangeListener> cls = new ArrayList<ChangeListener>();

	private final ChangeEvent changeEvent;

	public SliderDisplayModelAdaptor( final Object source, final SliderDisplayModel model, final SliderDisplayController controller )
	{
		this.source = source;
		this.sdm = model;
		this.sdc = controller;
		sitfc = sdm.getIntToFloatConverter();
		sdm.addChangeListener( this );

		changeEvent = new ChangeEvent( source );
	}

	@Override
	public int getMinimum()
	{
//		log.trace( "getMinimum called" );
//		return sitfc.floatValueToSliderIntValue( sdm, sdm.getMinValue() );
		return 0;
	}

	@Override
	public void setMinimum( final int newMinimum )
	{
//		log.trace( "setMinimum called" );
		// N.A.
	}

	@Override
	public int getMaximum()
	{
//		return sitfc.floatValueToSliderIntValue( sdm, sdm.getMaxValue() );
//		log.trace( "getMaximum called" );
		return sdm.getNumSliderSteps();
	}

	@Override
	public void setMaximum( final int newMaximum )
	{
		// N.A.
//		log.trace( "setMaximum called" );
	}

	@Override
	public int getValue()
	{
		final int retVal = sitfc.floatValueToSliderIntValue( sdm, sdm.getValue() );
//		log.debug("GetValue called - returning " + retVal );
		return retVal;
	}

	@Override
	public void setValue( final int newValue )
	{
//		log.trace("SetValue called with " + newValue );
		float newFloatValue = sitfc.sliderIntValueToFloatValue( sdm, newValue );
//		log.trace("Converted to float value is " + newFloatValue );
		final float minValue = sdm.getMinValue();
		final float maxValue = sdm.getMaxValue();
		if( newFloatValue < minValue )
		{
			newFloatValue = minValue;
		}
		else if( newFloatValue > maxValue )
		{
			newFloatValue = maxValue;
		}
		sdc.setValue( source, newFloatValue );
	}

	@Override
	public void setValueIsAdjusting( final boolean b )
	{
//		log.trace( "setValueIsAdjusting called" );
	}

	@Override
	public boolean getValueIsAdjusting()
	{
//		log.trace( "getValueIsAdjusting" );
		return false;
	}

	@Override
	public int getExtent()
	{
//		log.trace( "getExtent called" );
		return 0;
	}

	@Override
	public void setExtent( final int newExtent )
	{
//		log.trace( "setExtent called" );
	}

	@Override
	public void setRangeProperties( final int value, final int extent, final int min, final int max,
			final boolean adjusting )
	{
//		log.trace( "setRangeProperties called" );
	}

	@Override
	public void addChangeListener( final ChangeListener x )
	{
		cls.add( x );
	}

	@Override
	public void removeChangeListener( final ChangeListener x )
	{
		cls.remove( x );
	}

	@Override
	public void receiveValueChange( final Object source, final float newValue )
	{
//		log.debug("Received value change from " + source.getClass().getSimpleName() + " with " + newValue );
		final int numChangeListeners = cls.size();
		for( int i = 0 ; i < numChangeListeners ; ++i )
		{
			final ChangeListener cl = cls.get(i);
			cl.stateChanged( changeEvent );
		}
	}

}
