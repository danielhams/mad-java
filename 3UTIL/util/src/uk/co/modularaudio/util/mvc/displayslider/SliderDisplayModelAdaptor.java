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
	
	private Object source = null;
	private SliderDisplayModel sdm = null;
	private SliderDisplayController sdc = null;
	
	private SliderIntToFloatConverter sitfc = null;
	
	private ArrayList<ChangeListener> cls = new ArrayList<ChangeListener>();
	
	private ChangeEvent changeEvent = null;

	public SliderDisplayModelAdaptor( Object source, SliderDisplayModel model, SliderDisplayController controller )
	{
		this.source = source;
		this.sdm = model;
		this.sdc = controller;
		sitfc = sdm.getSliderIntToFloatConverter();
		sdm.addChangeListener( this );
		
		changeEvent = new ChangeEvent( source );
	}

	@Override
	public int getMinimum()
	{
//		return sitfc.floatValueToSliderIntValue( sdm, sdm.getMinValue() );
		return 0;
	}

	@Override
	public void setMinimum( int newMinimum )
	{
		// N.A.
	}

	@Override
	public int getMaximum()
	{
//		return sitfc.floatValueToSliderIntValue( sdm, sdm.getMaxValue() );
		return sdm.getNumSliderSteps();
	}

	@Override
	public void setMaximum( int newMaximum )
	{
		// N.A.
	}

	@Override
	public int getValue()
	{
		int retVal = sitfc.floatValueToSliderIntValue( sdm, sdm.getValue() );
//		log.debug("GetValue called - returning " + retVal );
		return retVal;
	}

	@Override
	public void setValue( int newValue )
	{
//		log.debug("SetValue called with " + newValue );
		float newFloatValue = sitfc.sliderIntValueToFloatValue( sdm, newValue );
		if( newFloatValue < sdm.getMinValue() )
		{
			newFloatValue = sdm.getMinValue();
		}
		else if( newFloatValue > sdm.getMaxValue() )
		{
			newFloatValue = sdm.getMaxValue();
		}
		sdc.setValue( source, newFloatValue );
	}

	@Override
	public void setValueIsAdjusting( boolean b )
	{
	}

	@Override
	public boolean getValueIsAdjusting()
	{
		return false;
	}

	@Override
	public int getExtent()
	{
		return 0;
	}

	@Override
	public void setExtent( int newExtent )
	{
	}

	@Override
	public void setRangeProperties( int value, int extent, int min, int max,
			boolean adjusting )
	{
	}

	@Override
	public void addChangeListener( ChangeListener x )
	{
		cls.add( x );
	}

	@Override
	public void removeChangeListener( ChangeListener x )
	{
		cls.remove( x );
	}

	@Override
	public void receiveValueChange( Object source, float newValue )
	{
//		log.debug("Received value change from " + source.getClass().getSimpleName() + " with " + newValue );
		for( int i = 0 ; i < cls.size() ; ++i )
		{
			ChangeListener cl = cls.get(i);
			cl.stateChanged( changeEvent );
		}
	}

}
