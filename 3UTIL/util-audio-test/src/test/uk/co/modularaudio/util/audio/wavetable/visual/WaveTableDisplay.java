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

package test.uk.co.modularaudio.util.audio.wavetable.visual;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.BasicConfigurator;


public class WaveTableDisplay extends JFrame
{
//	private static Log log = LogFactory.getLog( WaveTableDisplay.class.getName() );
	
	private static final long serialVersionUID = 8020002928630617999L;
	
	private WaveSurface harmonicsSurface = null;
	private WaveSurface waveSurface = null;
	
	public WaveTableDisplay()
	{
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setLayout( new MigLayout("fill, flowy") );
		this.add( getHarmonicsSurface(), "grow");
		this.add( getWaveSurface(), "grow" );
		this.setSize( new Dimension( 800, 600 ) );
		
	}
	
	private WaveMungingThread wmt = null;
	
	public void doWavey()
	{
		WaveMungingThread wmt = new WaveMungingThread( harmonicsSurface, waveSurface );
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
			catch (InterruptedException e)
			{
			}
		}
	}
	
	public WaveSurface getWaveSurface()
	{
		if( waveSurface == null )
		{
			waveSurface = new WaveSurface();
		}
		return waveSurface;
	}
	
	public WaveSurface getHarmonicsSurface()
	{
		if( harmonicsSurface == null )
		{
			harmonicsSurface = new WaveSurface();
		}
		return harmonicsSurface;
	}
	
	public static void main( String[] args )
	{
		BasicConfigurator.configure();
		WaveTableDisplay wtd = new WaveTableDisplay();
		wtd.setVisible( true );
		wtd.doWavey();
		wtd.haltWavey();
	}
}
