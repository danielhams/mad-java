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

package test.uk.co.modularaudio.util.swing.mvc.rotarydisplay;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.util.audio.mvc.rotarydisplay.models.LogarithmicTimeMillisMinZeroRotaryDisplayModel;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayController;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayKnob.KnobType;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayView;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayView.SatelliteOrientation;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryViewColors;

public class RotaryDisplayTester
{
//	private static Log log = LogFactory.getLog( RotaryDisplayTester.class.getName() );

	private JFrame frame = null;
	private JPanel panel = null;

	private RotaryDisplayView staticValueDisplay;

	public RotaryDisplayTester()
	{
		frame = new JFrame();
		final Dimension size = new Dimension( 600, 400 );
		frame.setPreferredSize( size );
//		frame.setMinimumSize( size );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		panel = new JPanel();
//		String layoutStr = "insets 0, gap 0, fill";
		final String layoutStr = "fill";
		final MigLayout l = new MigLayout( layoutStr );
		panel.setLayout( l );
//		panel.setBackground( Color.green );
		frame.add( panel );
	}

	public void go() throws Exception
	{
//		// Sample model that mimics what pan would be
//		// i.e. -1 -> 1
//		final RotaryDisplayModel model = new RotaryDisplayModel( -10.0f,
//				10.0f,
//				0.0f,
//				5.0f,
//				2000,
//				100,
//				new SimpleRotaryIntToFloatConverter(),
//				3,
//				3,
//				"unit" );
		final LogarithmicTimeMillisMinZeroRotaryDisplayModel model = new LogarithmicTimeMillisMinZeroRotaryDisplayModel();

		final RotaryDisplayController staticValueController = new RotaryDisplayController( model );

		final Color bgColor = new Color(72,72,72);
		final Color fgColor = new Color(100,100,100);
		final Color textboxBgColor = LWTCControlConstants.CONTROL_TEXTBOX_BACKGROUND;
		final Color textboxFgColor = LWTCControlConstants.CONTROL_TEXTBOX_FOREGROUND;
		final Color selectionColor = LWTCControlConstants.CONTROL_TEXTBOX_SELECTION;
		final Color selectedTextColor = LWTCControlConstants.CONTROL_TEXTBOX_SELECTED_TEXT;
		final Color knobFillColor = new Color(62,69,69);
		final Color knobOutlineColor = new Color(27,29,29);
		final Color knobExtentColor = new Color( 80, 80, 80 );
		final Color knobIndicatorColor = new Color(0,255,0);
		final Color knobFocusColor = new Color( 100, 100, 100);
		final Color labelColor = LWTCControlConstants.CONTROL_LABEL_FOREGROUND;
		final Color unitsColor = fgColor;

		final RotaryViewColors colours = new RotaryViewColors( bgColor,
				fgColor,
				textboxBgColor,
				textboxFgColor,
				selectionColor,
				selectedTextColor,
				knobOutlineColor,
				knobFillColor,
				knobExtentColor,
				knobIndicatorColor,
				knobFocusColor,
				labelColor,
				unitsColor );

		staticValueDisplay = new RotaryDisplayView( model,
				staticValueController,
				KnobType.BIPOLAR,
//				KnobType.UNIPOLAR,
				SatelliteOrientation.BELOW,
				SatelliteOrientation.RIGHT,
				"label",
				colours,
				false,
				true );

		panel.add( staticValueDisplay, "spanx 2, grow" );

		frame.setSize( 200, 200 );
		frame.setVisible( true );
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args)
		throws Exception
	{
		SwingUtilities.invokeAndWait( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
					UIManager.put( "Slider.paintValue",  Boolean.FALSE );
					final RotaryDisplayTester t = new RotaryDisplayTester();
					t.go();
				}
				catch (final Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} );
	}

}
