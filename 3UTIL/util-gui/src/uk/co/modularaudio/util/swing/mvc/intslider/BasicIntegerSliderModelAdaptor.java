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

package uk.co.modularaudio.util.swing.mvc.intslider;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.co.modularaudio.util.mvc.intslider.IntegerSliderModelListener;
import uk.co.modularaudio.util.mvc.intslider.IntegerSliderModelListenerEvent;
import uk.co.modularaudio.util.mvc.intslider.ValueOutOfRangeException;
import uk.co.modularaudio.util.mvc.intslider.impl.BasicIntegerSliderController;
import uk.co.modularaudio.util.mvc.intslider.impl.BasicIntegerSliderModel;

public class BasicIntegerSliderModelAdaptor implements IntegerSliderModelListener, ChangeListener
{
//	private static Log log = LogFactory.getLog( BasicIntegerSliderModelAdaptor.class.getName() );

	private final BasicIntegerSliderModel ism;
	private final BasicIntegerSliderController isc;
	private final BasicIntegerSliderView isv;

	public BasicIntegerSliderModelAdaptor(final BasicIntegerSliderModel ism,
			final BasicIntegerSliderController isc,
			final BasicIntegerSliderView isv )
	{
		this.ism = ism;
		this.isc = isc;
		this.isv = isv;

		updateValues();

		// Register us as a listener
		ism.addListener( this );

		// Add a listener to the slider that calls the controller
		isv.addChangeListener( this );
	}

	private void updateValues()
	{
		isv.setMinimum( ism.getMinVaue() );
		isv.setMaximum( ism.getMaxValue() );
		isv.setValue( ism.getValue() );
	}

	@Override
	public void valueChanged( final IntegerSliderModelListenerEvent e )
	{
//		log.debug("Got a value changed from ism");
		updateValues();
	}

	@Override
	public void boundsChanged(final IntegerSliderModelListenerEvent e)
	{
//		log.debug("Got a bounds changed from ism");
		updateValues();
	}

	@Override
	public void stateChanged(final ChangeEvent e)
	{
//		log.debug("Got a state changed from slider: " + e.toString() );
		try
		{
			isc.setValue( isv.getValue() );
		}
		catch (final ValueOutOfRangeException e1)
		{
			e1.printStackTrace();
		}
	}

	public void destroy()
	{
		ism.removeListener( this );
		isv.removeChangeListener( this );
	}
}
