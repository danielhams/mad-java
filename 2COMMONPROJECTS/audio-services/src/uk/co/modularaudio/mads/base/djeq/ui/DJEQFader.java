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

package uk.co.modularaudio.mads.base.djeq.ui;

import uk.co.modularaudio.util.audio.mvc.displayslider.models.DJDeckFaderSliderModel;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayController;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplaySlider;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDoubleClickMouseListener;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDoubleClickMouseListener.SliderDoubleClickReceiver;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderViewColors;

public class DJEQFader extends LWTCSliderDisplaySlider
{
	private static final long serialVersionUID = -3900834931841855564L;

//	private static Log log = LogFactory.getLog( MixerFader.class.getName() );

	public DJEQFader( final DJDeckFaderSliderModel model,
			final SliderDisplayController controller,
			final DisplayOrientation displayOrientation,
			final LWTCSliderViewColors colors,
			final boolean opaque )
	{
		super( model,
				controller,
				displayOrientation,
				colors,
				opaque );

		final SliderDoubleClickReceiver dcr = new SliderDoubleClickReceiver()
		{

			@Override
			public void receiveDoubleClick()
			{
				controller.setValue( this, model.getDefaultValue() );
			}
		};

		this.addMouseListener( new LWTCSliderDoubleClickMouseListener( dcr ) );
	}
}
