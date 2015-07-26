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

package uk.co.modularaudio.service.jnajackaudioprovider;

import java.nio.FloatBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaudiolibs.jnajack.Jack;
import org.jaudiolibs.jnajack.JackClient;
import org.jaudiolibs.jnajack.JackException;
import org.jaudiolibs.jnajack.JackMidi;
import org.jaudiolibs.jnajack.JackPort;
import org.jaudiolibs.jnajack.JackPortFlags;
import org.jaudiolibs.jnajack.JackPortType;
import org.jaudiolibs.jnajack.JackProcessCallback;
import org.jaudiolibs.jnajack.JackShutdownCallback;
import org.jaudiolibs.jnajack.JackXrunCallback;

import uk.co.modularaudio.service.apprendering.AppRenderingService;
import uk.co.modularaudio.service.apprendering.util.session.AbstractAppRenderingSession;
import uk.co.modularaudio.service.audioproviderregistry.AppRenderingErrorCallback;
import uk.co.modularaudio.service.audioproviderregistry.AppRenderingErrorQueue;
import uk.co.modularaudio.service.audioproviderregistry.AppRenderingErrorQueue.ErrorSeverity;
import uk.co.modularaudio.service.timing.TimingService;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEvent;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEventType;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.hardwareio.AudioHardwareDevice;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOConfiguration;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOOneChannelSetting;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareMidiNoteEvent;
import uk.co.modularaudio.util.audio.mad.hardwareio.LocklessHardwareMidiNoteRingBuffer;
import uk.co.modularaudio.util.audio.mad.hardwareio.MidiHardwareDevice;
import uk.co.modularaudio.util.audio.mad.hardwareio.MidiToHardwareMidiNoteRingDecoder;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;
import uk.co.modularaudio.util.thread.ThreadUtils;
import uk.co.modularaudio.util.thread.ThreadUtils.MAThreadPriority;
import uk.co.modularaudio.util.tuple.TwoTuple;

public class JNAJackAppRenderingSession extends AbstractAppRenderingSession implements JackProcessCallback, JackShutdownCallback, JackXrunCallback
{
	private static Log log = LogFactory.getLog( JNAJackAppRenderingSession.class.getName() );

//	private final Jack jack;
	private final JackClient client;

//	private final JackLatencyRange latencyRange = new JackLatencyRange();

	private int numProducerAudioPorts;
	private JackPort[] producerAudioPorts;
	private int numConsumerAudioPorts;
	private JackPort[] consumerAudioPorts;
	private int numProducerMidiPorts;
	private JackPort[] producerMidiPorts;
	private int numConsumerMidiPorts;
	private JackPort[] consumerMidiPorts;

	private MadChannelBuffer emptyFloatBuffer;
//	private MadChannelBuffer emptyNoteBuffer;

	private final JNAJackMIDIMessage jnaJackMidiMessage = new JNAJackMIDIMessage();
	private final JackMidi.Event jme = new JackMidi.Event();

	private MidiToHardwareMidiNoteRingDecoder midiToEventRingDecoder;
	private LocklessHardwareMidiNoteRingBuffer noteEventRing;

	private HardwareMidiNoteEvent[] tmpNoteEventArray;
	private int tmpNoteEventArrayLength;

	public JNAJackAppRenderingSession( final AppRenderingService appRenderingService,
			final TimingService timingService,
			final HardwareIOConfiguration hardwareConfiguration,
			final AppRenderingErrorQueue errorQueue,
			final AppRenderingErrorCallback errorCallback,
			final Jack jack,
			final JackClient client ) throws DatastoreException
	{
		super( appRenderingService, timingService, hardwareConfiguration, errorQueue, errorCallback );
//		this.jack = jack;
		this.client =client;
	}

	@Override
	protected TwoTuple<HardwareIOChannelSettings, MadTimingParameters> doProviderInit( final HardwareIOConfiguration hardwareConfiguration )
		throws DatastoreException
	{
		try
		{
			final int bufferSize = client.getBufferSize();
			final int sampleRate = client.getSampleRate();
			if( log.isDebugEnabled() )
			{
				log.debug("Jack tells us sampleRate(" + sampleRate + ") and bufferSize(" + bufferSize +")"); // NOPMD by dan on 01/02/15 07:06
			}
			final DataRate dataRate = DataRate.fromFrequency(sampleRate);

			client.setProcessCallback( this );
			client.setXrunCallback(this);
			client.onShutdown( this );

			// Need to register the various ports for audio/midi channels
			final AudioHardwareDevice phs = hardwareConfiguration.getProducerAudioDevice();
			if( phs != null )
			{
				numProducerAudioPorts = phs.getNumChannels();
				producerAudioPorts = new JackPort[ numProducerAudioPorts ];
				for( int c = 0 ; c < numProducerAudioPorts ; ++c )
				{
					final String portName = "In " + (c+1);
					final JackPortFlags portFlags = JackPortFlags.JackPortIsInput;
					producerAudioPorts[ c ] = client.registerPort(portName, JackPortType.AUDIO, portFlags );
				}
			}

			final MidiHardwareDevice pmd = hardwareConfiguration.getProducerMidiDevice();
			if( pmd != null )
			{
				numProducerMidiPorts = 1;
				producerMidiPorts = new JackPort[1];
				final String portName = "In";
				final JackPortFlags portFlags = JackPortFlags.JackPortIsInput;
				producerMidiPorts[0] = client.registerPort( portName, JackPortType.MIDI, portFlags );
			}

			final AudioHardwareDevice chs = hardwareConfiguration.getConsumerAudioDevice();
			if( chs != null )
			{
				numConsumerAudioPorts = chs.getNumChannels();
				consumerAudioPorts = new JackPort[ numConsumerAudioPorts ];
				for( int c = 0 ; c < numConsumerAudioPorts ; ++c )
				{
					final String portName = "Out " + (c+1);
					final JackPortFlags portFlags = JackPortFlags.JackPortIsOutput;
					consumerAudioPorts[ c ] = client.registerPort(portName, JackPortType.AUDIO, portFlags );
				}
			}

			final MidiHardwareDevice cmd = hardwareConfiguration.getConsumerMidiDevice();
			if( cmd != null )
			{
				numConsumerMidiPorts = 1;
				consumerMidiPorts = new JackPort[1];
				final String portName = "Out";
				final JackPortFlags portFlags = JackPortFlags.JackPortIsOutput;
				consumerMidiPorts[0] = client.registerPort( portName, JackPortType.MIDI, portFlags );
			}

			// Assume 2 periods of buffer length in jack
			final int sampleFramesOutputLatency = bufferSize * 2;
			final long nanosOutputLatency = AudioTimingUtils.getNumNanosecondsForBufferLength(sampleRate, sampleFramesOutputLatency );

			final MadTimingParameters dtp = new MadTimingParameters(
					dataRate,
					bufferSize,
					hardwareConfiguration.getFps() );

			final HardwareIOOneChannelSetting oneChannelSetting = new HardwareIOOneChannelSetting(dataRate, bufferSize);
			final HardwareIOChannelSettings dcs = new HardwareIOChannelSettings(oneChannelSetting, nanosOutputLatency, sampleFramesOutputLatency);

			emptyFloatBuffer = new MadChannelBuffer( MadChannelType.AUDIO,  bufferSize );
//			emptyNoteBuffer = new MadChannelBuffer( MadChannelType.NOTE, dcs.getNoteChannelSetting().getChannelBufferLength() );

			tmpNoteEventArrayLength = dcs.getNoteChannelSetting().getChannelBufferLength();
			noteEventRing = new LocklessHardwareMidiNoteRingBuffer( tmpNoteEventArrayLength );
			midiToEventRingDecoder = new MidiToHardwareMidiNoteRingDecoder( noteEventRing );

			tmpNoteEventArray = new HardwareMidiNoteEvent[ tmpNoteEventArrayLength ];
			for( int i = 0 ; i < tmpNoteEventArrayLength ; ++i )
			{
				tmpNoteEventArray[ i ] = new HardwareMidiNoteEvent();
				tmpNoteEventArray[ i ].eventType = MadChannelNoteEventType.EMPTY;
			}

			return new TwoTuple<HardwareIOChannelSettings, MadTimingParameters>( dcs,  dtp );
		}
		catch( final Exception e )
		{
			if( log.isErrorEnabled() )
			{
				log.error( "Exception caught during provider init:" + e.toString(), e );
			}
			errorQueue.queueError( this, ErrorSeverity.FATAL, "Exception caught during provider init" );
			throw new DatastoreException( "Exception  caught during provider init: " + e.toString(), e );
		}
	}

	@Override
	protected void doProviderStart()
		throws DatastoreException
	{
		try
		{
			client.activate();
		}
		catch( final Exception e )
		{
			if( log.isErrorEnabled() )
			{
				log.error( "Exception caught during provider start:" + e.toString(), e );
			}
			errorQueue.queueError( this, ErrorSeverity.FATAL, "Exception caught during provider start" );
		}
	}

	@Override
	protected void doProviderStop()
	{
		client.deactivate();
	}

	@Override
	protected void doProviderDestroy()
		throws DatastoreException
	{
		// Need to unregister the ports for audio/midi channels
		try
		{
			for( int pac = 0 ; pac < numProducerAudioPorts ; ++pac )
			{
				final JackPort pp = producerAudioPorts[ pac ];
				client.unregisterPort( pp );
			}
			for( int pmc = 0 ; pmc < numProducerMidiPorts ; ++pmc )
			{
				final JackPort pp = producerMidiPorts[ pmc ];
				client.unregisterPort( pp );
			}
			for( int cac = 0 ; cac < numConsumerAudioPorts ; ++cac )
			{
				final JackPort pp = consumerAudioPorts[ cac ];
				client.unregisterPort( pp );
			}
			for( int cmc = 0 ; cmc < numConsumerMidiPorts ; ++cmc )
			{
				final JackPort pp = consumerMidiPorts[ cmc ];
				client.unregisterPort( pp );
			}
			client.setProcessCallback( null );
			client.onShutdown( null );
		}
		catch( final Exception e )
		{
			throw new DatastoreException( "Exception caught during provider destroy: " + e.toString(), e );
		}
	}

	@Override
	public void clientShutdown(final JackClient client)
	{
		log.error("ClientShutdown called");
		errorQueue.queueError( this, ErrorSeverity.FATAL, "JNAJack client shutdown occured" );
	}

	@Override
	public boolean process( final JackClient client, final int numFrames )
	{
		final long clockCallbackStartTimestamp = System.nanoTime();
		long periodStartFrameTime;
//		int jackMaxLatency;
		try
		{
//			consumerAudioPorts[0].getLatencyRange( latencyRange, JackLatencyCallbackMode.JackPlaybackLatency );
//			jackMaxLatency = latencyRange.getMax();
//			log.debug("MaxLatency(" + jackMaxLatency + ")");

//			final long jackTime = client.getFrameTime();
			periodStartFrameTime = client.getLastFrameTime();

//			log.debug("jack time is " + jackTime );
//			log.debug("Period start frame time is " + periodStartFrameTime );
		}
		catch (final JackException e)
		{
			log.error( e );
			return false;
		}
		final boolean localShouldRecordPeriods = shouldRecordPeriods;

		if( localShouldRecordPeriods )
		{
			numPeriodsRecorded++;
		}
//		log.debug("Jack client process called with " + numFrames + " frames");

		masterInBuffersResetOrCopy( numFrames, periodStartFrameTime );

		final int numConsumersUsed = (masterOutBuffers.numAudioBuffers < numConsumerAudioPorts ? masterOutBuffers.numAudioBuffers : numConsumerAudioPorts );
//		boolean setDestinationBuffers = masterOutBuffersTryPointerMoves(numConsumersUsed);

		final long clockCallbackPostProducerTimestamp = System.nanoTime();

		// Now call the graph processing on all of that
		final RealtimeMethodReturnCodeEnum rc = doClockSourceProcessing(
				clockCallbackStartTimestamp,
				clockCallbackPostProducerTimestamp,
				numFrames,
				periodStartFrameTime );

		if( rc != RealtimeMethodReturnCodeEnum.SUCCESS )
		{
			return false;
		}

		// Now if we didn't reset the buffers, do the necessary copies
//		if( !setDestinationBuffers )
//		{
			masterOutBuffersCopyOver( numFrames, numConsumersUsed);
//		}
		// Need to handle midi here, too

		return true;
	}

	private void masterInBuffersResetOrCopy( final int numFrames, final long periodStartFrameTime )
	{
		final int numProducersUsed = (masterInBuffers.numAudioBuffers < numProducerAudioPorts ? masterInBuffers.numAudioBuffers : numProducerAudioPorts );
		int pac = 0;
		for( ; pac < numProducersUsed ; ++pac )
		{
			final JackPort pp = producerAudioPorts[ pac ];
			final FloatBuffer fb = pp.getFloatBuffer();
			final MadChannelBuffer aucb = masterInBuffers.audioBuffers[pac];
			final float[] mifb = aucb.floatBuffer;
//			if( fb.hasArray() )
//			{
//				aucb.floatBuffer = fb.array();
//			}
//			else
//			{
				// Slow copy
				fb.get( mifb, 0, numFrames );
//			}
		}
		// Point the other channels at silence
		for( int r = pac ; r < masterInBuffers.numAudioBuffers ; ++r )
		{
			masterInBuffers.audioBuffers[r].floatBuffer = emptyFloatBuffer.floatBuffer;
		}
		// Should do midi in here
		for( int pmc = 0 ; pmc < numProducerMidiPorts ; ++pmc )
		{
			try
			{
				final JackPort mp = producerMidiPorts[ pmc ];
				final int numMidiEvents = JackMidi.getEventCount(mp);
				if( numMidiEvents > 0 )
				{
//					log.debug("Processing " + numMidiEvents + " midi events");
				}
				for( int i = 0 ; i < numMidiEvents ; ++i )
				{
					JackMidi.eventGet( jme, mp,  i );
					final int bufferSize = jme.size();
					final byte[] jjmmb = jnaJackMidiMessage.getBuffer();
					if( bufferSize > jjmmb.length )
					{
					    log.error("Failed midi event byte size during get :-(");
					}
					else
					{
						jme.read( jjmmb );

						final long jackMidiEventTime = jme.time();
//						log.debug("Within process() call at time " + periodStartFrameTime + " midi event with time " + jackMidiEventTime );

						final long timestamp = periodStartFrameTime + jackMidiEventTime;

						midiToEventRingDecoder.decodeMessage( jnaJackMidiMessage.getCommand(),
								jnaJackMidiMessage.getChannel(),
								jnaJackMidiMessage.getData1(),
								jnaJackMidiMessage.getData2(),
								timestamp );
					}
				}
				// Now push from the ring into the master in buffer
				// Should do some magic to massage them into the right channels....
				for( int c = 0 ; c < masterInBuffers.numMidiBuffers ; ++c )
				{
					masterInBuffers.noteBuffers[ c ].numElementsInBuffer = 0;
				}
//				MadChannelNoteEvent[] noteBuf = masterInBuffers.noteBuffers[0].noteBuffer;
//				noteEventRing.readUpToMaxNum(target, pos, maxNum)
				final long startCandidateFrameTime = periodStartFrameTime;
				final long endCandidateFrameTime = startCandidateFrameTime + numFrames;
				final int numRead = noteEventRing.readUpToMaxNumAndFrameTime( tmpNoteEventArray,
						0,
						tmpNoteEventArrayLength,
						endCandidateFrameTime );

				for( int n = 0 ; n < numRead ; ++n )
				{
					final HardwareMidiNoteEvent midiEvent = tmpNoteEventArray[n];
					final int eventMidiChannel = midiEvent.channel;
					final long eventFrameTime = midiEvent.eventFrameTime;

					if( eventMidiChannel <= masterInBuffers.numMidiBuffers )
					{
						final MadChannelBuffer midiChannelBuffer = masterInBuffers.noteBuffers[ eventMidiChannel ];
						final MadChannelNoteEvent noteEvent = midiChannelBuffer.noteBuffer[ midiChannelBuffer.numElementsInBuffer++ ];
						final int sampleIndex = frameTimeToIndex( startCandidateFrameTime,
								endCandidateFrameTime,
								eventFrameTime );

						noteEvent.set( midiEvent.channel,
								sampleIndex,
								midiEvent.eventType,
								midiEvent.paramOne,
								midiEvent.paramTwo,
								midiEvent.paramThree );
					}
				}
			}
			catch( final JackException je )
			{
				if( log.isErrorEnabled() )
				{
					log.error( "JackException caught in midi process ports: " + je.toString(), je );
				}
			}
		}
	}

//	private boolean masterOutBuffersTryPointerMoves(int numConsumersUsed)
//	{
//		boolean setDestinationBuffers = true;
//		for( int cac = 0 ; cac < numConsumersUsed ; ++cac )
//		{
//			JackPort pp = consumerAudioPorts[ cac ];
//			FloatBuffer fb = pp.getFloatBuffer();
//			MadChannelBuffer aucb = masterOutBuffers.audioBuffers[ cac ];
//
//			if( fb.hasArray() )
//			{
//				aucb.floatBuffer = fb.array();
//			}
//			else
//			{
//				setDestinationBuffers = false;
//				break;
//			}
//		}
//		return setDestinationBuffers;
//	}

	private void masterOutBuffersCopyOver(final int numFrames, final int numConsumersUsed)
	{
		int cac = 0;
		for( ; cac < numConsumersUsed ; ++cac )
		{
			final JackPort pp = consumerAudioPorts[ cac ];
			final FloatBuffer fb = pp.getFloatBuffer();
			final MadChannelBuffer aucb = masterOutBuffers.audioBuffers[ cac ];
			fb.put( aucb.floatBuffer,  0,  numFrames );
		}
		// Make sure any remaining output channels are silenced
		for( int r = cac ; r < numConsumerAudioPorts ; ++r )
		{
			final JackPort pp = consumerAudioPorts[ r ];
			final FloatBuffer fb = pp.getFloatBuffer();
			fb.put( emptyFloatBuffer.floatBuffer, 0, numFrames );
		}
	}

	private int frameTimeToIndex( final long sft, final long eft, final long eventFrameTime )
	{
		final long startDiff = eventFrameTime - sft;
		final boolean eventBefore = startDiff < 0;
		final long endDiff = eventFrameTime - eft;
		final boolean eventAfter = endDiff > 0;

		if( eventBefore )
		{
			return 0;
		}
		else if( eventAfter )
		{
			return (int)(eft-sft);
		}

		return (int)startDiff;
	}

	@Override
	public long getCurrentUiFrameTime()
	{
		try
		{
			return client.getFrameTime();
		}
		catch (final JackException e)
		{
			if( log.isErrorEnabled() )
			{
				log.error("Failed fetching frame time from jack: " + e.toString(), e );
			}
			// Fall back to current sytem nanos;
			return System.nanoTime();
		}
	}

	@Override
	public void xrunOccured(final JackClient client)
	{
		errorQueue.queueError( this, ErrorSeverity.WARNING, "XRun" );
//		errorQueue.queueError( this, ErrorSeverity.FATAL, "XRun" );
	}

	@Override
	protected void setThreadPriority()
	{
		try
		{
			ThreadUtils.setCurrentThreadPriority( MAThreadPriority.REALTIME );
		}
		catch( final DatastoreException e )
		{
			log.error( e );
		}
	}

}
