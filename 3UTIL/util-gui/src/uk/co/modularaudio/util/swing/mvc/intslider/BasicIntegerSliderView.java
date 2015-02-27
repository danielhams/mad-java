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

import javax.swing.JSlider;

import uk.co.modularaudio.util.mvc.intslider.impl.BasicIntegerSliderController;
import uk.co.modularaudio.util.mvc.intslider.impl.BasicIntegerSliderModel;

public class BasicIntegerSliderView extends JSlider
{
	private static final long serialVersionUID = -3261152245183932001L;

//	private static Log log = LogFactory.getLog( BasicIntegerSliderView.class.getName() );

//	private BasicIntegerSliderModel ism;
	private BasicIntegerSliderController isc;
	private BasicIntegerSliderModelAdaptor sma;

	public BasicIntegerSliderView( final BasicIntegerSliderModel ism, final BasicIntegerSliderController isc )
	{
		this.isc = isc;
		this.setModelAndController( ism, isc );
	}

	public void setModelAndController( final BasicIntegerSliderModel ism, final BasicIntegerSliderController isc )
	{
		if( sma != null )
		{
			sma.destroy();
		}
		this.isc = isc;
		sma = new BasicIntegerSliderModelAdaptor( ism, isc, this );
	}

	public void setModel( final BasicIntegerSliderModel ism )
	{
		if( sma != null )
		{
			sma.destroy();
		}
		sma = new BasicIntegerSliderModelAdaptor( ism, isc, this );
	}
}
