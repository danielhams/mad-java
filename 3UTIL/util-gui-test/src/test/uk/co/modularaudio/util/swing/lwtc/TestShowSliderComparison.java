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
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.MadControlConstants;
import uk.co.modularaudio.util.swing.lwtc.MadSliderKnobImage;
import uk.co.modularaudio.util.swing.lwtc.MadSliderPainter;

public class TestShowSliderComparison
{
	private static Log log = LogFactory.getLog( TestShowSliderComparison.class.getName() );

	private class KnobContainer extends JPanel
	{
		private final MadSliderPainter painter;
		private final int orientation;

		public KnobContainer( final MadSliderPainter painter, final int orientation )
		{
			this.orientation = orientation;
			setOpaque( false );
			this.painter = painter;
			Dimension minSize;
			if( orientation == SwingConstants.HORIZONTAL )
			{
				minSize = new Dimension(MadSliderKnobImage.H_KNOB_SIZE.width,
						MadSliderKnobImage.H_KNOB_SIZE.height + 3 );
			}
			else
			{
				minSize = new Dimension(MadSliderKnobImage.V_KNOB_SIZE.width + 3,
						MadSliderKnobImage.V_KNOB_SIZE.height );
			}
//			log.debug("Setting min size to " + minSize.toString() );
			setMinimumSize( minSize );
		}

		@Override
		public void paint( final Graphics g )
		{
//			log.debug("In paint size if " + getSize().toString());
			final int width = getWidth();
			final int height = getHeight();
			final Graphics2D g2d = (Graphics2D)g;
			painter.paintSlider( g2d, orientation, width, height );
		}
	}

	private final KnobContainer horizontalKnobContainer;
	private final KnobContainer verticalKnobContainer;

	public TestShowSliderComparison()
	{
		final MadSliderPainter painter = new MadSliderPainter( MadControlConstants.STD_SLIDER_COLOURS );
		horizontalKnobContainer = new KnobContainer( painter,
						SwingConstants.HORIZONTAL );
		verticalKnobContainer = new KnobContainer( painter,
						SwingConstants.VERTICAL );
	}

	public void go( final int orientation ) throws Exception
	{

		final JSlider testSwingJSlider = new JSlider( orientation );
		testSwingJSlider.setOpaque( false );

		final JFrame f = new JFrame();
//		f.getContentPane().setBackground( Color.YELLOW );
		f.getContentPane().setBackground( Color.decode("#3a5555") );

		final MigLayoutStringHelper msg = new MigLayoutStringHelper();
//		msg.addLayoutConstraint( "debug" );
		msg.addLayoutConstraint( "fill" );
		msg.addLayoutConstraint( "insets 0" );
		msg.addLayoutConstraint( "gap 0" );
		if( orientation == SwingConstants.VERTICAL )
		{
			msg.addColumnConstraint( "[][grow][grow][]" );
			msg.addRowConstraint( "[][grow][]" );
		}
		else
		{
			msg.addColumnConstraint( "[][grow][]" );
			msg.addRowConstraint( "[][grow][grow][]" );
		}
		f.setLayout( msg.createMigLayout() );

		f.add( new JLabel("o"), "center");
		f.add( new JLabel("o"), "center");
		if( orientation == SwingConstants.VERTICAL )
		{
			f.add( new JLabel("o"), "center");
		}
		f.add( new JLabel("o"), "center,wrap");

		f.add( new JLabel("o"), "center");
		if( orientation == SwingConstants.VERTICAL )
		{
			f.add( verticalKnobContainer, "center, grow" );
			f.add( testSwingJSlider, "center, grow" );
		}
		else
		{
			f.add( horizontalKnobContainer, "center, grow" );
		}
		f.add( new JLabel("o"), "center,wrap");

		if( orientation == SwingConstants.HORIZONTAL )
		{
			f.add( new JLabel("o"), "center");
			f.add( testSwingJSlider, "center, grow" );
			f.add( new JLabel("o"), "center, wrap");
		}

		f.add( new JLabel("o"), "center");
		if( orientation == SwingConstants.VERTICAL )
		{
			f.add( new JLabel("o"), "center");
		}
		f.add( new JLabel("o"), "center");
		f.add( new JLabel("o"), "center");

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
		if( MadCtrlTestingConstants.USE_LAF )
		{
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
			UIManager.put( "Slider.paintValue",  Boolean.FALSE );
		}
		final TestShowSliderComparison vt = new TestShowSliderComparison();
		vt.go( SwingConstants.VERTICAL );

		final TestShowSliderComparison ht = new TestShowSliderComparison();
		ht.go( SwingConstants.HORIZONTAL );
}

}
