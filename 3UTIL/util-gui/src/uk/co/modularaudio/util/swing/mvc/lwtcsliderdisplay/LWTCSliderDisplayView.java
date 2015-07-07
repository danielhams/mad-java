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

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayController;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class LWTCSliderDisplayView extends JPanel
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

	private final LWTCSliderDisplayLabel label;
	private final LWTCSliderDisplaySlider slider;
	private final LWTCSliderDisplayTextbox textbox;

	public LWTCSliderDisplayView( final SliderDisplayModel model,
			final SliderDisplayController controller,
			final SatelliteOrientation labelOrientation,
			final DisplayOrientation displayOrientation,
			final SatelliteOrientation textboxOrientation,
			final LWTCSliderViewColors colours,
			final String labelText,
			final boolean opaque )
	{
		this( model,
				controller,
				labelOrientation,
				displayOrientation,
				textboxOrientation,
				colours,
				labelText,
				opaque,
				false );
	}

	public LWTCSliderDisplayView( final SliderDisplayModel model,
			final SliderDisplayController controller,
			final SatelliteOrientation labelOrientation,
			final DisplayOrientation displayOrientation,
			final SatelliteOrientation textboxOrientation,
			final LWTCSliderViewColors colours,
			final String labelText,
			final boolean opaque,
			final boolean rightClickToReset )
	{
		this.setOpaque( opaque );
		this.setBackground( colours.bgColor );

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

		label = new LWTCSliderDisplayLabel( colours,
				labelText,
				opaque );


		slider = new LWTCSliderDisplaySlider( model,
				controller,
				displayOrientation,
				colours,
				opaque,
				rightClickToReset );
		textbox = new LWTCSliderDisplayTextbox( model,
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
			label.setVerticalAlignment( SwingConstants.BOTTOM );
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
			label.setHorizontalAlignment( SwingConstants.RIGHT );
			this.add( label, "cell " + curColCounter + " " + curRowCounter + ", alignx right, aligny center" );
			curColCounter++;
		}

		if( textboxOrientation == SatelliteOrientation.LEFT )
		{
			this.add( textbox, "cell " + curColCounter + " " + curRowCounter + ", align center" );
			curColCounter++;
		}

		this.add( slider, "cell " + displayCol + " " + displayRow + ", center, grow, shrink 100, push, wrap");
		curColCounter++;

		// Right
		if( textboxOrientation == SatelliteOrientation.RIGHT )
		{
			this.add( textbox, "cell " + curColCounter + " " + displayRow + ", align center, pushx 0, shrink 0");
			curColCounter++;
		}

		if( labelOrientation == SatelliteOrientation.RIGHT )
		{
			label.setHorizontalAlignment( SwingConstants.LEFT );
			this.add( label, "cell " + curColCounter + " " + displayRow + ", align left, grow 0" );
			curColCounter++;
		}

		curRowCounter++;

		// Bottom
		if( textboxOrientation == SatelliteOrientation.BELOW )
		{
			this.add( textbox, "cell " + displayCol + " " + curRowCounter + ", align center, grow 0" );
			curRowCounter++;
		}

		if( labelOrientation == SatelliteOrientation.BELOW )
		{
			label.setVerticalAlignment( SwingConstants.TOP );
			this.add( label, "cell " + displayCol + " " + curRowCounter + ", align center, top, grow 0" );
			curRowCounter++;
		}

		this.validate();
	}

	public void changeModel( final SliderDisplayModel newModel )
	{
		slider.changeModel( newModel );
		textbox.changeModel( newModel );
	}

	public void setLabelMinSize( final int width, final int height )
	{
		label.setMinimumSize( new Dimension( width, height ) );

	}
}
