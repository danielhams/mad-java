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

package test.uk.co.modularaudio.util.audio.gui.wavetablecombo;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.gui.wavetablecombo.WaveTableComboController;
import uk.co.modularaudio.util.audio.gui.wavetablecombo.WaveTableComboItem;
import uk.co.modularaudio.util.audio.gui.wavetablecombo.WaveTableComboModel;
import uk.co.modularaudio.util.audio.gui.wavetablecombo.WaveTableComboView;
import uk.co.modularaudio.util.audio.lookuptable.valuemapping.StandardValueMappingWaveTables;

public class WaveTableComboTester
{
	public static Log log = LogFactory.getLog( WaveTableComboTester.class.getName() );
	
	private WaveTableComboModel model = null;
	private WaveTableComboView view = null;
	private WaveTableComboController controller = null;
	
	public WaveTableComboTester()
	{
		ArrayList<WaveTableComboItem> startupItems = new ArrayList<WaveTableComboItem>();
		WaveTableComboItem linItem = new WaveTableComboItem( "linear",
				"Linear Mapping",
				StandardValueMappingWaveTables.getLinearAttackMappingWaveTable(),
				false );
		startupItems.add( linItem );
		
		WaveTableComboItem expItem = new WaveTableComboItem( "exp",
				"Exponential Mapping",
				StandardValueMappingWaveTables.getExpAttackMappingWaveTable(),
				false );
		startupItems.add( expItem );
		
		model = new WaveTableComboModel( startupItems );
		controller = new WaveTableComboController( model );
		view = new WaveTableComboView( model, controller );
	}
	
	public void go()
	{
		JFrame frame = new JFrame("Test sample editor");
//		Dimension size = new Dimension( 640, 960);
//		Dimension size = new Dimension( 960, 640);
		Dimension size = new Dimension( 50, 45);
		frame.setPreferredSize( size);
		frame.setMinimumSize( size);

		frame.add( view );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setVisible( true );
	}
	
	public static void main( String[] args )
	{
		WaveTableComboTester pstt = new WaveTableComboTester();
		pstt.go();
	}
}
