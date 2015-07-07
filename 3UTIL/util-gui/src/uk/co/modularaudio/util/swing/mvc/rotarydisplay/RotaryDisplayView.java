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

package uk.co.modularaudio.util.swing.mvc.rotarydisplay;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayController;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayKnob.KnobType;

public class RotaryDisplayView extends JPanel
{
	private static final long serialVersionUID = 3201519946309189476L;
//	private static Log log = LogFactory.getLog( RotaryDisplayView.class.getName() );

	public enum SatelliteOrientation
	{
		ABOVE,
		RIGHT,
		BELOW,
		LEFT
	};

	private final RotaryDisplayLabel label;
	private final RotaryDisplayKnob knob;
	private final RotaryDisplayTextbox textbox;

	public RotaryDisplayView( final RotaryDisplayModel model,
			final RotaryDisplayController controller,
			final KnobType knobType,
			final SatelliteOrientation labelOrientation,
			final SatelliteOrientation textboxOrientation,
			final String labelText,
			final RotaryViewColors colours,
			final boolean opaque )
	{
		this( model,
				controller,
				knobType,
				labelOrientation,
				textboxOrientation,
				labelText,
				colours,
				opaque,
				false );
	}

	public RotaryDisplayView( final RotaryDisplayModel model,
			final RotaryDisplayController controller,
			final KnobType knobType,
			final SatelliteOrientation labelOrientation,
			final SatelliteOrientation textboxOrientation,
			final String labelText,
			final RotaryViewColors colours,
			final boolean opaque,
			final boolean rightClickToReset )
	{
		this.setOpaque( opaque );

		final int numOnLeft = ( labelOrientation == SatelliteOrientation.LEFT ? 1 : 0 ) +
				(textboxOrientation == SatelliteOrientation.LEFT ? 1 : 0 );
		final int numAbove = ( labelOrientation == SatelliteOrientation.ABOVE ? 1 : 0 ) +
				(textboxOrientation == SatelliteOrientation.ABOVE ? 1 : 0 );

		final MigLayoutStringHelper lh = new MigLayoutStringHelper();

//		lh.addLayoutConstraint( "debug" );
		lh.addLayoutConstraint( "insets 0" );
		lh.addLayoutConstraint( "gap 0" );
		lh.addLayoutConstraint( "fill" );

		final MigLayout layout = lh.createMigLayout();
		setLayout( layout );

		label = new RotaryDisplayLabel( labelText, colours, opaque );
		knob = new RotaryDisplayKnob( model,
				controller,
				knobType,
				colours,
				opaque,
				rightClickToReset );
		textbox = new RotaryDisplayTextbox( model,
				controller,
				colours,
				opaque );

		int curColCounter = 0;
		int curRowCounter = 0;

		final int displayCol = numOnLeft;
		final int displayRow = numAbove;

		// Above
		if( labelOrientation == SatelliteOrientation.ABOVE )
		{
			this.add( label, "cell " + displayCol + " " + curRowCounter + ", center, bottom, grow 0" );
			curRowCounter++;
		}

		if( textboxOrientation == SatelliteOrientation.ABOVE )
		{
			this.add( textbox, "cell " + displayCol + " " + curRowCounter + ", center, grow 0" );
			curRowCounter++;
		}

		// Left
		if( labelOrientation == SatelliteOrientation.LEFT )
		{
			this.add( label, "cell " + curColCounter + " " + curRowCounter + ", alignx right, aligny center" );
			curColCounter++;
		}
		if( textboxOrientation == SatelliteOrientation.LEFT )
		{
			this.add( textbox, "cell " + curColCounter + " " + curRowCounter + ", align center" );
			curColCounter++;
		}

		this.add( knob, "cell " + displayCol + " " + displayRow + ", center, grow, shrink 100, push, wrap");
		curColCounter++;

		// Right
		if( textboxOrientation == SatelliteOrientation.RIGHT )
		{
			this.add( textbox, "cell " + curColCounter + " " + displayRow + ", align center, pushx 0, shrink 0");
			curColCounter++;
		}

		if( labelOrientation == SatelliteOrientation.RIGHT )
		{
			this.add( label, "cell " + curColCounter + " " + displayRow + ", align left, grow 0" );
		}

		curRowCounter++;

		// And bottom
		if( textboxOrientation == SatelliteOrientation.BELOW )
		{
			this.add( textbox, "cell " + displayCol + " " + curRowCounter + ", align center, grow 0" );
			curRowCounter++;
		}

		if( labelOrientation == SatelliteOrientation.BELOW )
		{
			this.add( label, "cell " + displayCol + " " + curRowCounter + ", align center, top, grow 0" );
			curRowCounter++;
		}

		this.validate();
	}

	public void setDiameter( final int diameter )
	{
		knob.setDiameter( diameter );
	}
}
