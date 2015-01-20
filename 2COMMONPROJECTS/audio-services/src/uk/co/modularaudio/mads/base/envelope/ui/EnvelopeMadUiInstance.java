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
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeMadDefinition;
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeMadInstance;
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeIOQueueBridge;
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeWaveChoice;
import uk.co.modularaudio.mads.base.envelope.ui.WaveTableChoiceAttackCombo.WaveTableChoiceEnum;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEventUiConsumer;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class EnvelopeMadUiInstance extends AbstractNonConfigurableMadUiInstance<EnvelopeMadDefinition, EnvelopeMadInstance>
	implements IOQueueEventUiConsumer<EnvelopeMadInstance>
{
	private static Log log = LogFactory.getLog( EnvelopeMadUiInstance.class.getName() );
	
	private List<TimescaleChangeListener> timescaleChangeListeners = new ArrayList<TimescaleChangeListener>();
	private List<EnvelopeValueListener> envelopeListeners = new ArrayList<EnvelopeValueListener>();
	private List<EnvelopeValueProducer> envelopeValueProducers = new ArrayList<EnvelopeValueProducer>();
	
	private Envelope envelope = new Envelope();

	public EnvelopeMadUiInstance( EnvelopeMadInstance instance,
			EnvelopeMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}
	
	@Override
	public void doDisplayProcessing( ThreadSpecificTemporaryEventStorage guiTemporaryEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTick )
	{
		// Process incoming queue messages before we let the controls have a chance to process;
		localQueueBridge.receiveQueuedEventsToUi( guiTemporaryEventStorage, instance, this );
		
		super.doDisplayProcessing( guiTemporaryEventStorage, timingParameters, currentGuiTick );
	}

	@Override
	public void consumeQueueEntry( EnvelopeMadInstance instance,
			IOQueueEvent nextOutgoingEntry )
	{
		switch( nextOutgoingEntry.command )
		{
			default:
			{
				String msg = "Unknown command to guI: " + nextOutgoingEntry.command;
				log.error( msg );
			}
		}
	}
	
	public void addTimescaleChangeListener( TimescaleChangeListener l )
	{
		timescaleChangeListeners.add( l );
	}
	
	public void removeTimescaleChangeListener( TimescaleChangeListener l )
	{
		timescaleChangeListeners.remove( l );
	}

	public void propogateTimescaleChange( float newValue )
	{
		for( int i = 0 ; i < timescaleChangeListeners.size() ; i++)
		{
			TimescaleChangeListener l = timescaleChangeListeners.get( i );
			l.receiveTimescaleChange( newValue );
		}
	}
	
	public void addEnvelopeListener( EnvelopeValueListener l )
	{
		envelopeListeners.add( l );
		l.receiveEnvelopeChange();
	}
	
	public void removeEnvelopeListener( EnvelopeValueListener l )
	{
		envelopeListeners.remove( l );
	}
	
	public void propogateEnvelopeChange()
	{
		for( int i = 0 ; i < envelopeListeners.size() ; i++ )
		{
			EnvelopeValueListener l = envelopeListeners.get( i );
			l.receiveEnvelopeChange();
		}
	}

	public void addEnvelopeProducer( EnvelopeValueProducer p )
	{
		envelopeValueProducers.add( p );
	}
	
	public void removeEnvelopeProducer( EnvelopeValueProducer p )
	{
		envelopeValueProducers.remove( p );
	}

	public void setAttackFromZero( boolean attackFromZero )
	{
		envelope.setAttackFromZero( attackFromZero );
		propogateEnvelopeChange();

		sendTemporalValueToInstance( EnvelopeIOQueueBridge.COMMAND_IN_ATTACK_FROM_ZERO, (attackFromZero ? 1 : 0 ) );
	}

	public void setAttackMillis( float newValue )
	{
		envelope.setAttackMillis( newValue );
		propogateEnvelopeChange();
		
		sendTemporalValueToInstance( EnvelopeIOQueueBridge.COMMAND_IN_ATTACK_MILLIS, Float.floatToIntBits( newValue ) );
	}

	public void setAttackWaveChoice( WaveTableChoiceEnum enumValue )
	{
		EnvelopeWaveChoice envWaveChoice = mapComboWaveChoiceToEnvelope( enumValue );
		envelope.setAttackWaveChoice( envWaveChoice );
		propogateEnvelopeChange();

		sendTemporalValueToInstance( EnvelopeIOQueueBridge.COMMAND_IN_ATTACK_WAVE_CHOICE, envWaveChoice.ordinal() );
	}

	public void setDecayMillis( float newValue )
	{
		envelope.setDecayMillis( newValue );
		propogateEnvelopeChange();

		sendTemporalValueToInstance( EnvelopeIOQueueBridge.COMMAND_IN_DECAY_MILLIS, Float.floatToIntBits( newValue ) );
	}

	public void setDecayWaveChoice( WaveTableChoiceEnum enumValue )
	{
		EnvelopeWaveChoice envWaveChoice = mapComboWaveChoiceToEnvelope( enumValue );
		envelope.setDecayWaveChoice( envWaveChoice );
		propogateEnvelopeChange();
		
		sendTemporalValueToInstance( EnvelopeIOQueueBridge.COMMAND_IN_DECAY_WAVE_CHOICE, envWaveChoice.ordinal() );
	}

	public void setSustainLevel( float newValue )
	{
		envelope.setSustainLevel( newValue );
		propogateEnvelopeChange();
		
		sendTemporalValueToInstance( EnvelopeIOQueueBridge.COMMAND_IN_SUSTAIN_LEVEL, Float.floatToIntBits( newValue ) );
	}

	public void setReleaseMillis( float newValue )
	{
		envelope.setReleaseMillis( newValue );
		propogateEnvelopeChange();
		
		sendTemporalValueToInstance( EnvelopeIOQueueBridge.COMMAND_IN_RELEASE_MILLIS, Float.floatToIntBits( newValue ) );
	}

	public void setReleaseWaveChoice( WaveTableChoiceEnum enumValue )
	{
		EnvelopeWaveChoice envWaveChoice = mapComboWaveChoiceToEnvelope( enumValue );
		envelope.setReleaseWaveChoice( envWaveChoice );
		propogateEnvelopeChange();
		
		sendTemporalValueToInstance( EnvelopeIOQueueBridge.COMMAND_IN_RELEASE_WAVE_CHOICE, envWaveChoice.ordinal() );
	}

	private EnvelopeWaveChoice mapComboWaveChoiceToEnvelope( WaveTableChoiceEnum wtChoice )
	{
		switch( wtChoice )
		{
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
			default:
			{
				throw new RuntimeException("Unmapped wave table choice enum: " + wtChoice.toString() );
			}
		}
	}

	public Envelope getEnvelope()
	{
		return envelope;
	}
}
