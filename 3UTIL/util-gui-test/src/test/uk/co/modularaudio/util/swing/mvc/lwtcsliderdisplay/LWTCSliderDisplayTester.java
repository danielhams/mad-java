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

package test.uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.MixdownSliderModel;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayController;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.SatelliteOrientation;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderViewColors;

public class LWTCSliderDisplayTester
{
//	private static Log log = LogFactory.getLog( SliderDisplayTester.class.getName() );

	private final JFrame frame;
	private final JPanel panel;

	private LWTCSliderDisplayView staticValueDisplay;

	public LWTCSliderDisplayTester()
	{
		frame = new JFrame();
		final Dimension size = new Dimension( 400, 400 );
		frame.setPreferredSize( size );
//		frame.setMinimumSize( size );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		panel = new JPanel();
//		String layoutStr = "insets 0, gap 0, fill";
		final String layoutStr = "fill";
		final MigLayout l = new MigLayout( layoutStr );
		panel.setLayout( l );
		panel.setBackground( Color.GRAY );
		frame.add( panel );
	}

	public void go() throws Exception
	{
		final SliderDisplayModel mixerModel = new MixdownSliderModel();
		final SliderDisplayController mixerController = new SliderDisplayController( mixerModel );

		final Color bgColor = Color.BLACK;
		final Color fgColor = Color.YELLOW;
		final Color indicatorColor = Color.RED;
		final Color textboxBgColor = LWTCControlConstants.CONTROL_TEXTBOX_BACKGROUND;
		final Color textboxFgColor = LWTCControlConstants.CONTROL_TEXTBOX_FOREGROUND;
		final Color selectionColor = LWTCControlConstants.CONTROL_TEXTBOX_SELECTION;
		final Color selectedTextColor = LWTCControlConstants.CONTROL_TEXTBOX_SELECTED_TEXT;
		final Color labelColor = Color.ORANGE;
		final Color unitsColor = Color.PINK;

		final LWTCSliderViewColors colours = new LWTCSliderViewColors( bgColor,
				fgColor,
				indicatorColor,
				textboxBgColor,
				textboxFgColor,
				selectionColor,
				selectedTextColor,
				labelColor,
				unitsColor );

		staticValueDisplay = new LWTCSliderDisplayView( mixerModel,
				mixerController,
				SatelliteOrientation.BELOW,
				DisplayOrientation.VERTICAL,
				SatelliteOrientation.BELOW,
				colours,
				"Volume",
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
//					UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
//					UIManager.put( "Slider.paintValue",  Boolean.FALSE );
					final LWTCSliderDisplayTester t = new LWTCSliderDisplayTester();
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
