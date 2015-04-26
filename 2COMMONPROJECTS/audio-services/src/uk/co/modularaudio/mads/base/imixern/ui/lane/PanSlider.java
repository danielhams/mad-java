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

import java.awt.Color;
import java.awt.Font;
import java.util.Hashtable;

import javax.swing.JLabel;

import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacSlider;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDoubleClickMouseListener;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDoubleClickMouseListener.SliderDoubleClickReceiver;

public class PanSlider extends PacSlider implements SliderDoubleClickReceiver
{
	private static final long serialVersionUID = -6056016804015326734L;

	public final static int PAN_SLIDER_NUM_STEPS = 100;

//	private static Log log = LogFactory.getLog( PanSlider.class.getName() );

	private final PanChangeReceiver changeReceiver;

	private final SliderDoubleClickMouseListener sliderDoubleClickMouseListener;

	private final Hashtable<Integer, JLabel> panLabels;

	public PanSlider( final PanChangeReceiver changeReceiver, final Color foregroundColour )
	{
		this.setOpaque( false );
		this.setOrientation( HORIZONTAL );

//		Font f = this.getFont().deriveFont( 9f );
		final Font f = this.getFont();

		setFont( f );
		setForeground( foregroundColour );

		setMinimum( 0 );
		setMaximum( PAN_SLIDER_NUM_STEPS );

		setMajorTickSpacing( PAN_SLIDER_NUM_STEPS / 6 );
//		setPaintTicks( true );
		setPaintLabels( true );

		panLabels = new Hashtable<Integer, JLabel>();
		panLabels.put( 0, buildLabel( f, "L", foregroundColour ) );
		panLabels.put( PAN_SLIDER_NUM_STEPS / 2, buildLabel( f, "|", foregroundColour ) );
		panLabels.put( PAN_SLIDER_NUM_STEPS, buildLabel( f, "R", foregroundColour ) );

		setLabelTable( panLabels );

		setValue( PAN_SLIDER_NUM_STEPS );
		// Only set the change receiver before we are to set the final value - stops spam of the model setup.
		this.changeReceiver = changeReceiver;
		setValue( PAN_SLIDER_NUM_STEPS / 2 );

		sliderDoubleClickMouseListener = new SliderDoubleClickMouseListener( this );

		this.addMouseListener( sliderDoubleClickMouseListener );
	}

	private JLabel buildLabel( final Font f, final String label, final Color foregroundColour )
	{
		final JLabel retVal = new JLabel( label );
		retVal.setFont( f );
		retVal.setForeground( foregroundColour );
		return retVal;
	}

	@Override
	public void processValueChange( final int previousValue, final int newValue )
	{
		final float floatVal = (float)newValue / PAN_SLIDER_NUM_STEPS;
		// Now spread between -1 and 1
		final float normVal = (floatVal - 0.5f) * 2;
		if( changeReceiver != null )
		{
			changeReceiver.receivePanChange( normVal );
		}
	}

	@Override
	public void receiveDoubleClick()
	{
		this.setValue( PAN_SLIDER_NUM_STEPS / 2 );
	}
}
