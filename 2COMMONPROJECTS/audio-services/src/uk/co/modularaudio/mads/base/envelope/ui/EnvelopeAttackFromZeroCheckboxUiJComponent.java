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

package uk.co.modularaudio.mads.base.envelope.ui;

import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeMadDefinition;
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeMadInstance;
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeDefaults;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacCheckBox;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class EnvelopeAttackFromZeroCheckboxUiJComponent extends PacCheckBox
		implements IMadUiControlInstance<EnvelopeMadDefinition, EnvelopeMadInstance, EnvelopeMadUiInstance>,
		EnvelopeValueProducer
{
	private static final long serialVersionUID = 6068897521037173787L;

	private final EnvelopeMadUiInstance uiInstance;

	public EnvelopeAttackFromZeroCheckboxUiJComponent(  final EnvelopeMadDefinition definition,
			final EnvelopeMadInstance instance,
			final EnvelopeMadUiInstance uiInstance,
			final int controlIndex )
	{
		super();
		this.uiInstance = uiInstance;
		this.setOpaque( false );
		setFont( this.getFont().deriveFont( 9f ) );
		this.setText( "Reinit" );

		uiInstance.addEnvelopeProducer( this );
		final boolean isSelected = model.isSelected();
		if( isSelected == EnvelopeDefaults.ATTACK_FROM_ZERO )
		{
			receiveUpdate( !EnvelopeDefaults.ATTACK_FROM_ZERO, EnvelopeDefaults.ATTACK_FROM_ZERO );
		}
		else
		{
			model.setSelected( EnvelopeDefaults.ATTACK_FROM_ZERO );
		}
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( final boolean selected )
	{
		uiInstance.setAttackFromZero( selected );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
	}

	@Override
	public void receiveUpdate( final boolean statusBefore, final boolean newStatus )
	{
		if( statusBefore != newStatus )
		{
			passChangeToInstanceData( newStatus );
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
