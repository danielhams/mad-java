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

package test.uk.co.modularaudio.util.swing.lwtc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCSliderKnobImage;

public class TestShowLWTCSliderKnob
{
//	private static Log log = LogFactory.getLog( TestShowLWTCSliderKnob.class.getName() );

	private class KnobContainer extends JPanel
	{
		private static final long serialVersionUID = -3198915514341179594L;
		private final BufferedImage knobImage;

		public KnobContainer( final LWTCSliderKnobImage knob )
		{
			setOpaque( false );
			setBackground( Color.RED );
			this.knobImage = knob.getKnobImage();

			this.setMinimumSize( new Dimension( knobImage.getWidth(), knobImage.getHeight() ) );
		}

		@Override
		public void paint( final Graphics g )
		{
			g.drawImage( knobImage, 0, 0, null );
		}
	}

	private final KnobContainer horizontalKnobContainer;
	private final KnobContainer verticalKnobContainer;

	public TestShowLWTCSliderKnob()
	{
		horizontalKnobContainer = new KnobContainer(
				new LWTCSliderKnobImage( LWTCControlConstants.STD_SLIDER_COLOURS,
						SwingConstants.HORIZONTAL ) );
		verticalKnobContainer = new KnobContainer(
				new LWTCSliderKnobImage( LWTCControlConstants.STD_SLIDER_COLOURS,
						SwingConstants.VERTICAL ) );
	}

	public void go() throws Exception
	{

		final JFrame f = new JFrame();
//		f.getContentPane().setBackground( Color.RED );

		final MigLayoutStringHelper msg = new MigLayoutStringHelper();
//		msg.addLayoutConstraint( "debug" );
		msg.addLayoutConstraint( "fill" );
		msg.addLayoutConstraint( "insets 0" );
		msg.addLayoutConstraint( "gap 0" );
		msg.addColumnConstraint( "[][grow][]" );
		msg.addRowConstraint( "[][grow][grow][]" );
		f.setLayout( msg.createMigLayout() );

		f.add( new JLabel("TL"), "center");
		f.add( new JLabel("TM"), "center");
		f.add( new JLabel("TR"), "center,wrap");

		f.add( new JLabel("ML"), "center");
		f.add( horizontalKnobContainer, "grow" );
		f.add( new JLabel("MR"), "center,wrap");

		f.add( new JLabel("ML"), "center");
		f.add( verticalKnobContainer, "grow" );
		f.add( new JLabel("MR"), "center,wrap");

		f.add( new JLabel("BL"), "center");
		f.add( new JLabel("BM"), "center");
		f.add( new JLabel("BR"), "center");

		f.pack();

		f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		SwingUtilities.invokeLater( new Runnable()
		{

			@Override
			public void run()
			{
				f.setVisible( true );
			}
		} );
	}

	public static void main( final String[] args ) throws Exception
	{
		if( LWTCCtrlTestingConstants.USE_LAF )
		{
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		}
		final TestShowLWTCSliderKnob t = new TestShowLWTCSliderKnob();
		t.go();
	}

}
