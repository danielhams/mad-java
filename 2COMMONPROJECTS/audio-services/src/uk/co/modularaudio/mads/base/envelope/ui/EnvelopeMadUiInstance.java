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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.envelope.mu.Envelope;
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeIOQueueBridge;
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeMadDefinition;
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeMadInstance;
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeWaveChoice;
import uk.co.modularaudio.mads.base.envelope.ui.WaveTableChoiceAttackCombo.WaveTableChoiceEnum;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEventUiConsumer;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class EnvelopeMadUiInstance extends AbstractNoNameChangeNonConfigurableMadUiInstance<EnvelopeMadDefinition, EnvelopeMadInstance>
	implements IOQueueEventUiConsumer<EnvelopeMadInstance>
{
	private static Log log = LogFactory.getLog( EnvelopeMadUiInstance.class.getName() );

	private final List<TimescaleChangeListener> timescaleChangeListeners = new ArrayList<TimescaleChangeListener>();
	private final List<EnvelopeValueListener> envelopeListeners = new ArrayList<EnvelopeValueListener>();
	private final List<EnvelopeValueProducer> envelopeValueProducers = new ArrayList<EnvelopeValueProducer>();

	private final Envelope envelope = new Envelope();

	public EnvelopeMadUiInstance( final EnvelopeMadInstance instance,
			final EnvelopeMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage guiTemporaryEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTick )
	{
		// Process incoming queue messages before we let the controls have a chance to process;
		localQueueBridge.receiveQueuedEventsToUi( guiTemporaryEventStorage, instance, this );

		super.doDisplayProcessing( guiTemporaryEventStorage, timingParameters, currentGuiTick );
	}

	@Override
	public void consumeQueueEntry( final EnvelopeMadInstance instance,
			final IOQueueEvent nextOutgoingEntry )
	{
		switch( nextOutgoingEntry.command )
		{
			default:
			{
				final String msg = "Unknown command to guI: " + nextOutgoingEntry.command;
				log.error( msg );
			}
		}
	}

	public void addTimescaleChangeListener( final TimescaleChangeListener l )
	{
		timescaleChangeListeners.add( l );
	}

	public void removeTimescaleChangeListener( final TimescaleChangeListener l )
	{
		timescaleChangeListeners.remove( l );
	}

	public void propogateTimescaleChange( final float newValue )
	{
		for( int i = 0 ; i < timescaleChangeListeners.size() ; i++)
		{
			final TimescaleChangeListener l = timescaleChangeListeners.get( i );
			l.receiveTimescaleChange( newValue );
		}
	}

	public void addEnvelopeListener( final EnvelopeValueListener l )
	{
		envelopeListeners.add( l );
		l.receiveEnvelopeChange();
	}

	public void removeEnvelopeListener( final EnvelopeValueListener l )
	{
		envelopeListeners.remove( l );
	}

	public void propogateEnvelopeChange()
	{
		for( int i = 0 ; i < envelopeListeners.size() ; i++ )
		{
			final EnvelopeValueListener l = envelopeListeners.get( i );
			l.receiveEnvelopeChange();
		}
	}

	public void addEnvelopeProducer( final EnvelopeValueProducer p )
	{
		envelopeValueProducers.add( p );
	}

	public void removeEnvelopeProducer( final EnvelopeValueProducer p )
	{
		envelopeValueProducers.remove( p );
	}

	public void setAttackFromZero( final boolean attackFromZero )
	{
		envelope.setAttackFromZero( attackFromZero );
		propogateEnvelopeChange();

		sendTemporalValueToInstance( EnvelopeIOQueueBridge.COMMAND_IN_ATTACK_FROM_ZERO, (attackFromZero ? 1 : 0 ) );
	}

	public void setAttackMillis( final float newValue )
	{
		envelope.setAttackMillis( newValue );
		propogateEnvelopeChange();

		sendTemporalValueToInstance( EnvelopeIOQueueBridge.COMMAND_IN_ATTACK_MILLIS, Float.floatToIntBits( newValue ) );
	}

	public void setAttackWaveChoice( final WaveTableChoiceEnum enumValue )
	{
		final EnvelopeWaveChoice envWaveChoice = mapComboWaveChoiceToEnvelope( enumValue );
		envelope.setAttackWaveChoice( envWaveChoice );
		propogateEnvelopeChange();

		sendTemporalValueToInstance( EnvelopeIOQueueBridge.COMMAND_IN_ATTACK_WAVE_CHOICE, envWaveChoice.ordinal() );
	}

	public void setDecayMillis( final float newValue )
	{
		envelope.setDecayMillis( newValue );
		propogateEnvelopeChange();

		sendTemporalValueToInstance( EnvelopeIOQueueBridge.COMMAND_IN_DECAY_MILLIS, Float.floatToIntBits( newValue ) );
	}

	public void setDecayWaveChoice( final WaveTableChoiceEnum enumValue )
	{
		final EnvelopeWaveChoice envWaveChoice = mapComboWaveChoiceToEnvelope( enumValue );
		envelope.setDecayWaveChoice( envWaveChoice );
		propogateEnvelopeChange();

		sendTemporalValueToInstance( EnvelopeIOQueueBridge.COMMAND_IN_DECAY_WAVE_CHOICE, envWaveChoice.ordinal() );
	}

	public void setSustainLevel( final float newValue )
	{
		envelope.setSustainLevel( newValue );
		propogateEnvelopeChange();

		sendTemporalValueToInstance( EnvelopeIOQueueBridge.COMMAND_IN_SUSTAIN_LEVEL, Float.floatToIntBits( newValue ) );
	}

	public void setReleaseMillis( final float newValue )
	{
		envelope.setReleaseMillis( newValue );
		propogateEnvelopeChange();

		sendTemporalValueToInstance( EnvelopeIOQueueBridge.COMMAND_IN_RELEASE_MILLIS, Float.floatToIntBits( newValue ) );
	}

	public void setReleaseWaveChoice( final WaveTableChoiceEnum enumValue )
	{
		final EnvelopeWaveChoice envWaveChoice = mapComboWaveChoiceToEnvelope( enumValue );
		envelope.setReleaseWaveChoice( envWaveChoice );
		propogateEnvelopeChange();

		sendTemporalValueToInstance( EnvelopeIOQueueBridge.COMMAND_IN_RELEASE_WAVE_CHOICE, envWaveChoice.ordinal() );
	}

	private EnvelopeWaveChoice mapComboWaveChoiceToEnvelope( final WaveTableChoiceEnum wtChoice )
	{
		switch( wtChoice )
		{
			default:
			case LINEAR:
			{
				return EnvelopeWaveChoice.LINEAR;
			}
			case EXP:
			{
				return EnvelopeWaveChoice.EXP;
			}
			case EXP_FREQ:
			{
				return EnvelopeWaveChoice.EXP_FREQ;
			}
			case LOG:
			{
				return EnvelopeWaveChoice.LOG;
			}
			case LOG_FREQ:
			{
				return EnvelopeWaveChoice.LOG_FREQ;
			}
		}
	}

	public Envelope getEnvelope()
	{
		return envelope;
	}
}
