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

package uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.SwingConstants;

import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayController;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModelAdaptor;
import uk.co.modularaudio.util.swing.lwtc.LWTCSlider;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.DisplayOrientation;

public class LWTCSliderDisplaySlider extends LWTCSlider
{
//	private static Log log = LogFactory.getLog( LWTCSliderDisplaySlider.class.getName() );

	private static final long serialVersionUID = 7532750303295733460L;

	private final SliderDisplayController controller;

	private class ResetToDefaultMouseListener implements MouseListener
	{

		@Override
		public void mouseClicked( final MouseEvent e )
		{
		}

		@Override
		public void mousePressed( final MouseEvent e )
		{
			switch( e.getButton() )
			{
				case 3:
				{
					final SliderDisplayModel sdm = controller.getModel();
					controller.setValue( this, sdm.getDefaultValue() );
					break;
				}
				default:
				{
					break;
				}
			}
		}

		@Override
		public void mouseReleased( final MouseEvent e )
		{
		}

		@Override
		public void mouseEntered( final MouseEvent e )
		{
		}

		@Override
		public void mouseExited( final MouseEvent e )
		{
		}
	};

	public LWTCSliderDisplaySlider( final SliderDisplayModel model,
			final SliderDisplayController controller,
			final DisplayOrientation displayOrientation,
			final LWTCSliderViewColors colours,
			final boolean opaque,
			final boolean rightClickToReset )
	{
		super( ( displayOrientation == DisplayOrientation.HORIZONTAL ?
				SwingConstants.HORIZONTAL :
				SwingConstants.VERTICAL ),
				opaque );

		this.setSliderColours( colours );

		this.controller = controller;

		this.setModel( new SliderDisplayModelAdaptor( this, model, controller ) );

		final int sliderMajorTickSpacing = model.getSliderMajorTickSpacing();
//		log.debug("Setting major tick spacing to " + sliderMajorTickSpacing + " with " + model.getNumSliderSteps() + " steps");
		this.setMajorTickSpacing( sliderMajorTickSpacing );
		this.setBackground( colours.bgColor );
		this.setForeground( colours.fgColor );

		if( rightClickToReset )
		{
			this.addMouseListener( new ResetToDefaultMouseListener() );
		}
	}

	public void changeModel( final SliderDisplayModel newModel )
	{
		this.setModel( new SliderDisplayModelAdaptor( this, newModel, controller ) );

		final int sliderMajorTickSpacing = newModel.getSliderMajorTickSpacing();
		this.setMajorTickSpacing( sliderMajorTickSpacing );
	}
}
