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

package uk.co.modularaudio.mads.base.spectralamp.ui;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadDefinition;
import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacComboBox;
import uk.co.modularaudio.util.audio.logdisplay.runav.FallComputer;
import uk.co.modularaudio.util.audio.logdisplay.runav.FastFallComputer;
import uk.co.modularaudio.util.audio.logdisplay.runav.LongAverageComputer;
import uk.co.modularaudio.util.audio.logdisplay.runav.NoAverageComputer;
import uk.co.modularaudio.util.audio.logdisplay.runav.RunningAverageComputer;
import uk.co.modularaudio.util.audio.logdisplay.runav.ShortAverageComputer;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class SpectralAmpRunningAverageComboUiJComponent extends PacComboBox<String>
	implements IMadUiControlInstance<SpectralAmpMadDefinition, SpectralAmpMadInstance, SpectralAmpMadUiInstance>
{
	private static final long serialVersionUID = -2025091191521837789L;

	private SpectralAmpMadUiInstance uiInstance = null;
	
	private Map<String, RunningAverageComputer> runAvToCalculatorMap = new HashMap<String, RunningAverageComputer> ();
	private Map<RunningAverageComputer, String> calculatorToNameMap = new HashMap<RunningAverageComputer, String> ();

	public SpectralAmpRunningAverageComboUiJComponent( SpectralAmpMadDefinition definition,
			SpectralAmpMadInstance instance,
			SpectralAmpMadUiInstance uiInstance,
			int controlIndex )
	{
		this.uiInstance = uiInstance;
		this.setOpaque( false );
		
		runAvToCalculatorMap.put( "Off", new NoAverageComputer() );
		runAvToCalculatorMap.put( "Short Average", new ShortAverageComputer() );
		runAvToCalculatorMap.put( "Long Average", new LongAverageComputer() );
		runAvToCalculatorMap.put( "Fall", new FallComputer() );
		runAvToCalculatorMap.put( "Fast Fall", new FastFallComputer() );
		for( String name : runAvToCalculatorMap.keySet() )
		{
			calculatorToNameMap.put( runAvToCalculatorMap.get( name ), name );
		}

		DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
		String[] names = new String[] { "Off", "Short Average", "Long Average", "Fast Fall", "Fall" };
		for( String name : names )
		{
			cbm.addElement( name );
		}

		this.setModel( cbm );
		
//		Font f = this.getFont().deriveFont( 9f );
		Font f = this.getFont();
		setFont( f );
		
		this.setSelectedIndex( -1 );
		this.setSelectedItem( "Fast Fall" );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void doDisplayProcessing(ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
//		log.debug("Received display tick");
	}

	@Override
	protected void receiveIndexUpdate( int previousIndex, int newIndex )
	{
		if( previousIndex != newIndex )
		{
			String name = (String)getSelectedItem();
			if( name != null )
			{
				RunningAverageComputer runAvComputer = runAvToCalculatorMap.get( name );
				uiInstance.setDesiredRunningAverageComputer( runAvComputer );
			}
		}
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}
}
