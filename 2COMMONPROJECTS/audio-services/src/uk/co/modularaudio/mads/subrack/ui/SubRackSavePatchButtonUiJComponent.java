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

package uk.co.modularaudio.mads.subrack.ui;

import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.subrack.mu.SubRackMadDefinition;
import uk.co.modularaudio.mads.subrack.mu.SubRackMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacButton;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class SubRackSavePatchButtonUiJComponent extends PacButton
	implements IMadUiControlInstance<SubRackMadDefinition, SubRackMadInstance, SubRackMadUiInstance>
{
	private static final long serialVersionUID = -6066972568143292726L;
	
	private static Log log = LogFactory.getLog( SubRackSavePatchButtonUiJComponent.class.getName() );
	
	private SubRackMadUiInstance uiInstance = null;

	public SubRackSavePatchButtonUiJComponent( SubRackMadDefinition definition,
			SubRackMadInstance instance,
			SubRackMadUiInstance uiInstance,
			SubRackSavePatchButtonUiControlDefinition def )
	{
		setOpaque( true );

		this.uiInstance = uiInstance;

//		Font f = getFont().deriveFont( 9.0f );
		Font f = getFont();
		setFont( f );
		setMargin( new Insets( 0, 0, 0, 0 ) );
		setText( "Save" );
	}

	@Override
	public void doDisplayProcessing(ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
	}

	@Override
	public JComponent getControl()
	{
		return getControl();
	}

	@Override
	public void receiveEvent( ActionEvent e )
	{
		try
		{
			uiInstance.saveSubRack( this );
		}
		catch (Exception e1)
		{
			String msg = "Exception caught attempting to save sub rack: " + e1.toString();
			log.error( msg, e1 );
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
