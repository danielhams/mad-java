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
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class SpectralAmpResolutionComboUiJComponent extends PacComboBox<String>
	implements IMadUiControlInstance<SpectralAmpMadDefinition, SpectralAmpMadInstance, SpectralAmpMadUiInstance>
{
	private static final long serialVersionUID = -1751151942321586686L;

//	private static Log log = LogFactory.getLog( SpectralAmpResolutionComboUiJComponent.class.getName());
	
	private SpectralAmpMadUiInstance uiInstance = null;
	
	private int[] resolutionChoices = new int[] { 256, 512, 1024, 2048, 4096, 8192, 16384 };
	
	private Map<String, Integer> runAvToCalculatorMap = new HashMap<String, Integer> ();

	public SpectralAmpResolutionComboUiJComponent( SpectralAmpMadDefinition definition,
			SpectralAmpMadInstance instance,
			SpectralAmpMadUiInstance uiInstance,
			int controlIndex )
	{
		this.uiInstance = uiInstance;
		this.setOpaque( false );
		
		for( int res : resolutionChoices )
		{
			runAvToCalculatorMap.put( res + "", res );
		}

		DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
		for( int res : resolutionChoices )
		{
			cbm.addElement( res + "" );
		}
		this.setModel( cbm );
		
//		Font f = this.getFont().deriveFont( 9f );
		Font f = this.getFont();
		setFont( f );
		
		this.setSelectedIndex( -1 );
		this.setSelectedItem( "4096" );
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
				Integer resolution = runAvToCalculatorMap.get( name );
				uiInstance.setDesiredFftSize( resolution );
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
