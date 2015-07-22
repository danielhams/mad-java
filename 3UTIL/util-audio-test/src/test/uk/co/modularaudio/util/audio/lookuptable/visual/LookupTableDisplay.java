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

package test.uk.co.modularaudio.util.audio.lookuptable.visual;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

public class LookupTableDisplay extends JFrame
{
//	private static Log log = LogFactory.getLog( LookupTableDisplay.class.getName() );

	private static final long serialVersionUID = 8020002928630617999L;

	private final WaveSurface harmonicsSurface;
	private final WaveSurface waveSurface;

	public LookupTableDisplay()
	{
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setLayout( new MigLayout("fill, flowy") );
		harmonicsSurface = new WaveSurface();
		this.add( harmonicsSurface, "grow");
		waveSurface = new WaveSurface();
		this.add( waveSurface, "grow" );
		this.setSize( new Dimension( 800, 600 ) );

		wmt = new WaveMungingThread( harmonicsSurface, waveSurface );
	}

	private final WaveMungingThread wmt;

	public void doWavey()
	{
		wmt.start();
	}

	public void haltWavey()
	{
		if( wmt != null )
		{
			try
			{
				wmt.join();
			}
			catch (final InterruptedException e)
			{
			}
		}
	}

	public static void main( final String[] args )
	{
		final LookupTableDisplay wtd = new LookupTableDisplay();
		wtd.setVisible( true );
		wtd.doWavey();
		wtd.haltWavey();
	}
}
