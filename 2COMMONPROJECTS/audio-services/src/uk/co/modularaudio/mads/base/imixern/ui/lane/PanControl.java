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

package uk.co.modularaudio.mads.base.imixern.ui.lane;

import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayController;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel.ValueChangeListener;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayKnob;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDoubleClickMouseListener;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDoubleClickMouseListener.RotaryDoubleClickReceiver;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryViewColors;

public class PanControl extends RotaryDisplayKnob implements RotaryDoubleClickReceiver
{
	private static final long serialVersionUID = -6056016804015326734L;

	public final static int PAN_SLIDER_NUM_STEPS = 100;

	// private static Log log = LogFactory.getLog( PanSlider.class.getName() );

	public PanControl( final RotaryDisplayModel model,
			final RotaryDisplayController controller,
			final PanChangeReceiver changeReceiver,
			final RotaryViewColors colors )
	{
		super( model, controller, KnobType.BIPOLAR,
				colors,
				false );

		model.addChangeListener( new ValueChangeListener()
		{

			@Override
			public void receiveValueChange( final Object source, final float newValue )
			{
				changeReceiver.receivePanChange( newValue );
			}
		} );

		this.addMouseListener( new RotaryDoubleClickMouseListener( this ) );
	}

	@Override
	public void receiveDoubleClick()
	{
		sdm.setValue( this, 0.0f );
		repaint();
	}
}
