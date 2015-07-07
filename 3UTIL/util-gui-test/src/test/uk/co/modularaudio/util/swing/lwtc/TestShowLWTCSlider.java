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

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCSlider;

public class TestShowLWTCSlider
{
//	private static Log log = LogFactory.getLog( TestShowLWTCSlider.class.getName() );

	private JSlider testSwingJSlider;
	private LWTCSlider testLWTCSlider;
	private Thread t;

	public TestShowLWTCSlider()
	{
	}

	public void go( final int orientation ) throws Exception
	{
		final boolean opaque = false;
		testSwingJSlider = new JSlider( orientation );
		testSwingJSlider.setOpaque( opaque );

		testLWTCSlider = new LWTCSlider( orientation, opaque, true );

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
			f.add( testLWTCSlider, "center, grow" );
			f.add( testSwingJSlider, "center, grow" );
		}
		else
		{
			f.add( testLWTCSlider, "center, grow" );
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

		final Dimension currentSize = f.getSize();
		final Dimension doubleSize = new Dimension( currentSize.width*2, currentSize.height*2 );
		f.setMinimumSize( doubleSize );

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

	public void startThread()
	{
		t = new Thread( new Runnable()
		{

			@Override
			public void run()
			{
				final SliderDisplayModel model = testLWTCSlider.getModel();

				// 60 fps
				final int millis = 16;
				// 120 fps
//				final int millis = 8;

				float value = model.getValue();
				int iValue = 0;
				int dir = 1;

//				for( int i = 0 ; i < 60 * 30 ; ++i )
				while( true )
				{
					model.setValue( this, value );
					testSwingJSlider.setValue( iValue );
					value += dir * 1;
					if( value > 100 )
					{
						value = 100;
						iValue = 100;
						dir = -1;
					}
					else if( value < 0 )
					{
						value = 0;
						iValue = 0;
						dir = 1;
					}
					try
					{
						Thread.sleep( millis );
					}
					catch( final InterruptedException e )
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		} );
		t.start();
	}

	@SuppressWarnings("deprecation")
	public void joinThread() throws Exception
	{
		t.stop();
		t.join();
	}

	public static void main( final String[] args ) throws Exception
	{
		if( LWTCCtrlTestingConstants.USE_LAF )
		{
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
			UIManager.put( "Slider.paintValue",  Boolean.FALSE );
		}
		final TestShowLWTCSlider vt = new TestShowLWTCSlider();
		vt.go( SwingConstants.VERTICAL );

		final TestShowLWTCSlider ht = new TestShowLWTCSlider();
		ht.go( SwingConstants.HORIZONTAL );

//		vt.startThread();
//		ht.startThread();

		// n secs test
		final int millis = 120 * 1000;
		Thread.sleep( millis );

///		vt.joinThread();
//		ht.joinThread();
	}

}
