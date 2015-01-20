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

package uk.co.modularaudio.mads.base.spectralroll.ui;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.spectralroll.mu.SpectralRollMadDefinition;
import uk.co.modularaudio.mads.base.spectralroll.mu.SpectralRollMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacComboBox;
import uk.co.modularaudio.util.audio.logdisplay.ampscale.AmpScaleComputer;
import uk.co.modularaudio.util.audio.logdisplay.ampscale.LinearAmpScaleComputer;
import uk.co.modularaudio.util.audio.logdisplay.ampscale.LogLogAmpScaleComputer;
import uk.co.modularaudio.util.audio.logdisplay.ampscale.LogarithmicAmpScaleComputer;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class SpectralRollAmpScaleComboUiJComponent extends PacComboBox<String>
	implements IMadUiControlInstance<SpectralRollMadDefinition, SpectralRollMadInstance, SpectralRollMadUiInstance>
{
	private static final long serialVersionUID = 3571032632219667963L;
	
	private static Log log = LogFactory.getLog( SpectralRollAmpScaleComboUiJComponent.class.getName() );
	
	private SpectralRollMadUiInstance uiInstance = null;
	
	private Map<String, AmpScaleComputer> ampScaleToCalculatorMap = new HashMap<String, AmpScaleComputer> ();

	public SpectralRollAmpScaleComboUiJComponent( SpectralRollMadDefinition definition,
			SpectralRollMadInstance instance,
			SpectralRollMadUiInstance uiInstance,
			int controlIndex )
	{
		this.uiInstance = uiInstance;
		this.setOpaque( false );

		ampScaleToCalculatorMap.put( "Lin", new LinearAmpScaleComputer() );
		ampScaleToCalculatorMap.put( "Log", new LogarithmicAmpScaleComputer() );
		ampScaleToCalculatorMap.put( "Log-Log", new LogLogAmpScaleComputer() );

		DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
		for( String waveName : ampScaleToCalculatorMap.keySet() )
		{
			cbm.addElement( waveName );
		}
		this.setModel( cbm );
		
//		Font f = this.getFont().deriveFont( 9f );
		Font f = this.getFont();
		setFont( f );
		
		this.setSelectedItem( "Log" );
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
		log.debug("receiveIndexUpdate");
		if( previousIndex != newIndex )
		{
			String name = (String)getSelectedItem();
			AmpScaleComputer ampScaleComputer = ampScaleToCalculatorMap.get( name );
			uiInstance.setDesiredAmpScaleComputer( ampScaleComputer );
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
