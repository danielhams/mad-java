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

import uk.co.modularaudio.mads.base.spectralroll.mu.SpectralRollMadDefinition;
import uk.co.modularaudio.mads.base.spectralroll.mu.SpectralRollMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacComboBox;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class SpectralRollResolutionComboUiJComponent extends PacComboBox<String>
	implements IMadUiControlInstance<SpectralRollMadDefinition, SpectralRollMadInstance, SpectralRollMadUiInstance>
{
	private static final long serialVersionUID = -1751151942321586686L;

//	private static Log log = LogFactory.getLog( SpectralRollResolutionComboUiJComponent.class.getName());

	private final SpectralRollMadUiInstance uiInstance;

	private final int[] resolutionChoices = new int[] { 256, 512, 1024, 2048, 4096, 8192, 16384 };

	private final Map<String, Integer> runAvToCalculatorMap = new HashMap<String, Integer> ();

	public SpectralRollResolutionComboUiJComponent( final SpectralRollMadDefinition definition,
			final SpectralRollMadInstance instance,
			final SpectralRollMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;
		this.setOpaque( false );

		for( final int res : resolutionChoices )
		{
			runAvToCalculatorMap.put( res + "", res );
		}

		final DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
		for( final int res : resolutionChoices )
		{
			cbm.addElement( res + "" );
		}
		this.setModel( cbm );

//		Font f = this.getFont().deriveFont( 9f );
		final Font f = this.getFont();
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
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
//		log.debug("Received display tick");
	}

	@Override
	protected void receiveIndexUpdate( final int previousIndex, final int newIndex )
	{
		if( previousIndex != newIndex )
		{
			final String name = (String)getSelectedItem();
			if( name != null )
			{
				final Integer resolution = runAvToCalculatorMap.get( name );
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
