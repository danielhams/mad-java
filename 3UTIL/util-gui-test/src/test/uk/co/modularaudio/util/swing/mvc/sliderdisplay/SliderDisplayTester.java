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

package test.uk.co.modularaudio.util.swing.mvc.sliderdisplay;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.util.mvc.displayslider.SimpleSliderIntToFloatConverter;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayController;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView.SatelliteOrientation;

public class SliderDisplayTester
{
//	private static Log log = LogFactory.getLog( SliderDisplayTester.class.getName() );

	private JFrame frame = null;
	private JPanel panel = null;

	private SliderDisplayView staticValueDisplay;

	public SliderDisplayTester()
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
		panel.setBackground( Color.green );
		frame.add( panel );
	}

	public void go() throws Exception
	{
//		SliderDisplayModel attackModel = new SliderDisplayModel( 0.0f,
//				10.0f,
//				0.0f,
//				1000,
//				100,
//				new SimpleSliderIntToFloatConverter(),
//				2,
//				2,
//				"ms");
//		SliderDisplayController attackController = new SliderDisplayController( attackModel );
//		SliderDisplayView attackSliderDisplay = new SliderDisplayView(
//				attackModel,
//				attackController,
//				SatelliteOrientation.LEFT,
//				DisplayOrientation.VERTICAL,
//				SatelliteOrientation.BELOW,
//				"A:",
//				9.0f,
//				true );
//
//		panel.add( attackSliderDisplay, "grow" );
//
//		SliderDisplayModel decayModel = new SliderDisplayModel( 0.0f,
//				10.0f,
//				0.0f,
//				1000,
//				100,
//				new SimpleSliderIntToFloatConverter(),
//				2,
//				2,
//				"ms");
//		SliderDisplayController decayController = new SliderDisplayController( decayModel );
//		SliderDisplayView decaySliderDisplay = new SliderDisplayView(
//				decayModel,
//				decayController,
//				SatelliteOrientation.LEFT,
//				DisplayOrientation.VERTICAL,
//				SatelliteOrientation.BELOW,
//				"D:",
//				9.0f,
//				true );
//
//		panel.add( decaySliderDisplay, "grow, wrap" );
//
//		float maxFrequency = 22050.0f;
//		SliderDisplayModel frequencyModel = new SliderDisplayModel( 0.0f,
//				maxFrequency,
//				0.0f,
//				1000,
//				100,
//				new FrequencySliderIntToFloatConverter( maxFrequency ),
//				5,
//				2,
//				"Hz");
//		SliderDisplayController frequencyController = new SliderDisplayController( frequencyModel );
//		SliderDisplayView frequencySliderDisplay = new SliderDisplayView( frequencyModel,
//				frequencyController,
//				SatelliteOrientation.LEFT,
//				DisplayOrientation.HORIZONTAL,
//				SatelliteOrientation.RIGHT,
//				"Frequency:",
//				9.0f, true );
//
//		panel.add( frequencySliderDisplay, "spanx 2, grow, wrap" );

		final SliderDisplayModel staticValueModel = new SliderDisplayModel( 0.01f,
				10.0f,
				1.0f,
				1.0f,
				1000,
				100,
				new SimpleSliderIntToFloatConverter(),
				2,
				2,
				"Hz" );
		final SliderDisplayController staticValueController = new SliderDisplayController( staticValueModel );
		staticValueDisplay = new SliderDisplayView( staticValueModel,
				staticValueController,
				SatelliteOrientation.LEFT,
				DisplayOrientation.HORIZONTAL,
				SatelliteOrientation.RIGHT,
				"Frequency:",
				Color.white,
				Color.white,
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
					final SliderDisplayTester t = new SliderDisplayTester();
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
