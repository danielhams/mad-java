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

import java.awt.Color;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayController;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.SliderDoubleClickMouseListener.SliderDoubleClickReceiver;

public class SliderDisplayView extends JPanel
{
	private static final long serialVersionUID = 3201519946309189476L;
//	private static Log log = LogFactory.getLog( SliderDisplayView.class.getName() );

	public enum DisplayOrientation
	{
		VERTICAL,
		HORIZONTAL
	};

	public enum SatelliteOrientation
	{
		ABOVE,
		RIGHT,
		BELOW,
		LEFT
	};

	private int numColumns = 1;

	private final SliderDisplayLabel label;
	private final LWTCSliderDisplaySlider slider;
	private final SliderDisplayTextbox textbox;

	public SliderDisplayView( final SliderDisplayModel model,
			final SliderDisplayController controller,
			final SatelliteOrientation labelOrientation,
			final DisplayOrientation displayOrientation,
			final SatelliteOrientation textboxOrientation,
			final String labelText,
			final Color labelColor,
			final Color unitsColor,
			final boolean opaque )
	{
		this( model,
				controller,
				labelOrientation,
				displayOrientation,
				textboxOrientation,
				labelText,
				labelColor,
				unitsColor,
				opaque,
				false );
	}

	public SliderDisplayView( final SliderDisplayModel model,
			final SliderDisplayController controller,
			final SatelliteOrientation labelOrientation,
			final DisplayOrientation displayOrientation,
			final SatelliteOrientation textboxOrientation,
			final String labelText,
			final Color labelColor,
			final Color unitsColor,
			final boolean opaque,
			final boolean doubleClickToReset )
	{
		this.setOpaque( opaque );

		// If the label orientation or textbox orientation is left/right
		// we use a two column mode
		numColumns = 1 + ( labelOrientation == SatelliteOrientation.LEFT || textboxOrientation == SatelliteOrientation.LEFT ? 1 : 0 ) +
				(labelOrientation == SatelliteOrientation.RIGHT || textboxOrientation == SatelliteOrientation.RIGHT ? 1 : 0 );
		final MigLayoutStringHelper lh = new MigLayoutStringHelper();

//		lh.addLayoutConstraint( "debug" );
		lh.addLayoutConstraint( "insets 0" );
		lh.addLayoutConstraint( "gap 0" );
		if( numColumns == 1 )
		{
			lh.addLayoutConstraint( "flowy" );
			if( labelOrientation == SatelliteOrientation.ABOVE || textboxOrientation == SatelliteOrientation.ABOVE )
			{
				lh.addRowConstraint( "[fill]" );
				lh.addRowConstraint( "[]" );
			}
			else
			{
				lh.addRowConstraint( "[]" );
			}
			if( labelOrientation == SatelliteOrientation.BELOW || textboxOrientation == SatelliteOrientation.BELOW )
			{
				lh.addRowConstraint( "[fill]" );
			}
		}

		lh.addLayoutConstraint( "fill" );

		final MigLayout layout = lh.createMigLayout();
		setLayout( layout );

		label = new SliderDisplayLabel( labelText, labelColor, opaque );
		slider = new LWTCSliderDisplaySlider( model, controller, displayOrientation, labelColor, opaque );
		textbox = new SliderDisplayTextbox( model, controller, unitsColor, opaque );

		// Any needed components at top
		if( labelOrientation == SatelliteOrientation.ABOVE )
		{
			this.add( label, "center, bottom, grow 0" );
		}
		if( textboxOrientation == SatelliteOrientation.ABOVE )
		{
			this.add( textbox, "center, grow 0" );
		}

		// Left
		if( labelOrientation == SatelliteOrientation.LEFT )
		{
			this.add( label, "alignx right, aligny center" );
		}
		if( textboxOrientation == SatelliteOrientation.LEFT )
		{
			this.add( textbox, "align center" );
		}

		// Main slider
		if( numColumns == 2 )
		{
			if( displayOrientation == DisplayOrientation.HORIZONTAL )
			{
				this.add( slider, "center, grow, shrink 100, pushx, wrap");
			}
			else
			{
				this.add( slider, "center, grow, shrink 100, pushy, wrap");
			}
		}
		else
		{
			if( displayOrientation == DisplayOrientation.HORIZONTAL )
			{
//				log.debug("Adding slider with center pushx 50 shrink 100");
				this.add( slider, "center, growx 50, pushx 50, shrink 100" );
			}
			else
			{
				this.add( slider, "center, pushy 50, shrink 100" );
			}
		}

		// Now right
		if( textboxOrientation == SatelliteOrientation.RIGHT )
		{
			if( numColumns > 1 )
			{
//				log.debug("Adding textbox with pushx100 shrink 0 align center and wrap");
				this.add( textbox, "align center, pushx 0, shrink 0, wrap" );
			}
			else
			{
//				log.debug("Adding textbox with grow 0 shrink 0 align center");
				this.add( textbox, "align center, pushx 100, shrink 0" );
			}
		}
		if( labelOrientation == SatelliteOrientation.RIGHT )
		{
			if( numColumns > 1 )
			{
				this.add( label, "align left, grow 0, wrap" );
			}
			else
			{
				this.add( label, "align left, grow 0" );
			}
		}

		// And bottom
		if( textboxOrientation == SatelliteOrientation.BELOW )
		{
			if( numColumns > 1 )
			{
				this.add( textbox, "align center, grow 0, spanx " + numColumns );
			}
			else
			{
				this.add( textbox, "align center, grow 0" );
			}
		}
		if( labelOrientation == SatelliteOrientation.BELOW )
		{
			if( numColumns > 1 )
			{
				this.add( label, "center, top, growx, spanx " + numColumns );
			}
			else
			{
				this.add( label, "center, top, growx" );
			}
		}

		this.validate();

		if( doubleClickToReset )
		{
			addDoubleClickReceiver( new SliderDoubleClickReceiver()
			{

				@Override
				public void receiveDoubleClick()
				{
					controller.setValue( this, controller.getModel().getInitialValue() );
				}
			} );
		}
	}

	public void addDoubleClickReceiver( final SliderDoubleClickReceiver receiver )
	{
		final SliderDoubleClickMouseListener doubleClickMouseListener = new SliderDoubleClickMouseListener( receiver );
		slider.addMouseListener( doubleClickMouseListener );
	}

	public void changeModel( final SliderDisplayModel newModel )
	{
		slider.changeModel( newModel );
		textbox.changeModel( newModel );
	}
}
