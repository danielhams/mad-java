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

import uk.co.modularaudio.service.apprenderinggraph.AppRenderingGraphService;
import uk.co.modularaudio.service.apprenderinggraph.vos.AppRenderingErrorCallback;
import uk.co.modularaudio.service.apprenderinggraph.vos.AppRenderingErrorQueue;
import uk.co.modularaudio.service.apprenderinggraph.vos.AppRenderingErrorQueue.ErrorSeverity;
import uk.co.modularaudio.service.apprenderinggraph.vos.AppRenderingIO;
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
import uk.co.modularaudio.util.tuple.TwoTuple;

public class JNAJackAppRenderingIO extends AppRenderingIO implements JackProcessCallback, JackShutdownCallback, JackXrunCallback
{
	private static Log log = LogFactory.getLog( JNAJackAppRenderingIO.class.getName() );

//	private final Jack jack;
	private final JackClient client;

//	private JackLatencyRange latencyRange = new JackLatencyRange();

	private int numProducerAudioPorts  = -1;
	private JackPort[] producerAudioPorts = null;
	private int numConsumerAudioPorts  = -1;
	private JackPort[] consumerAudioPorts = null;
	private int numProducerMidiPorts  = -1;
	private JackPort[] producerMidiPorts = null;
	private int numConsumerMidiPorts  = -1;
	private JackPort[] consumerMidiPorts = null;

	private MadChannelBuffer emptyFloatBuffer;
//	private MadChannelBuffer emptyNoteBuffer;

	private JNAJackMIDIMessage jnaJackMidiMessage = new JNAJackMIDIMessage();
	private JackMidi.Event jme = new JackMidi.Event();

	private MidiToHardwareMidiNoteRingDecoder midiToEventRingDecoder = null;
	private LocklessHardwareMidiNoteRingBuffer noteEventRing = null;

	private HardwareMidiNoteEvent[] tmpNoteEventArray;
	private int tmpNoteEventArrayLength = -1;

	public JNAJackAppRenderingIO( AppRenderingGraphService appRenderingGraphService,
			TimingService timingService,
			HardwareIOConfiguration hardwareConfiguration,
			AppRenderingErrorQueue errorQueue,
			AppRenderingErrorCallback errorCallback,
			Jack jack,
			JackClient client ) throws DatastoreException
	{
		super( appRenderingGraphService, timingService, hardwareConfiguration, errorQueue, errorCallback );
//		this.jack = jack;
		this.client =client;
	}

	@Override
	protected TwoTuple<HardwareIOChannelSettings, MadTimingParameters> doProviderInit( HardwareIOConfiguration hardwareConfiguration )
		throws DatastoreException
	{
		try
		{
			int bufferSize = client.getBufferSize();
			int sampleRate = client.getSampleRate();
			log.info("Jack tells us sampleRate(" + sampleRate + ") and bufferSize(" + bufferSize +")");
			DataRate dataRate = DataRate.fromFrequency(sampleRate);

			client.setProcessCallback( this );
			client.setXrunCallback(this);
			client.onShutdown( this );

			// Need to register the various ports for audio/midi channels
			AudioHardwareDevice phs = hardwareConfiguration.getProducerAudioDevice();
			if( phs != null )
			{
				numProducerAudioPorts = phs.getNumChannels();
				producerAudioPorts = new JackPort[ numProducerAudioPorts ];
				for( int c = 0 ; c < numProducerAudioPorts ; ++c )
				{
					String portName = "In " + (c+1);
					JackPortFlags portFlags = JackPortFlags.JackPortIsInput;
					producerAudioPorts[ c ] = client.registerPort(portName, JackPortType.AUDIO, portFlags );
				}
			}

			MidiHardwareDevice pmd = hardwareConfiguration.getProducerMidiDevice();
			if( pmd != null )
			{
				numProducerMidiPorts = 1;
				producerMidiPorts = new JackPort[1];
				String portName = "In";
				JackPortFlags portFlags = JackPortFlags.JackPortIsInput;
				producerMidiPorts[0] = client.registerPort( portName, JackPortType.MIDI, portFlags );
			}

			AudioHardwareDevice chs = hardwareConfiguration.getConsumerAudioDevice();
			if( chs != null )
			{
				numConsumerAudioPorts = chs.getNumChannels();
				consumerAudioPorts = new JackPort[ numConsumerAudioPorts ];
				for( int c = 0 ; c < numConsumerAudioPorts ; ++c )
				{
					String portName = "Out " + (c+1);
					JackPortFlags portFlags = JackPortFlags.JackPortIsOutput;
					consumerAudioPorts[ c ] = client.registerPort(portName, JackPortType.AUDIO, portFlags );
				}
			}

			MidiHardwareDevice cmd = hardwareConfiguration.getConsumerMidiDevice();
			if( cmd != null )
			{
				numConsumerMidiPorts = 1;
				consumerMidiPorts = new JackPort[1];
				String portName = "Out";
				JackPortFlags portFlags = JackPortFlags.JackPortIsOutput;
				consumerMidiPorts[0] = client.registerPort( portName, JackPortType.MIDI, portFlags );
			}

			// Assume 2 periods of buffer length in jack
			int sampleFramesOutputLatency = bufferSize * 2;
			long nanosOutputLatency = AudioTimingUtils.getNumNanosecondsForBufferLength(sampleRate, sampleFramesOutputLatency );

			MadTimingParameters dtp = new MadTimingParameters(
					dataRate,
					bufferSize,
					hardwareConfiguration.getFps(),
					nanosOutputLatency );

			HardwareIOOneChannelSetting oneChannelSetting = new HardwareIOOneChannelSetting(dataRate, bufferSize);
			HardwareIOChannelSettings dcs = new HardwareIOChannelSettings(oneChannelSetting, nanosOutputLatency, sampleFramesOutputLatency);

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
		catch( Exception e )
		{
			log.error( "Exception caught during provider init:" + e.toString(), e );
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
		catch( Exception e )
		{
			log.error( "Exception caught during provider start:" + e.toString(), e );
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
				JackPort pp = producerAudioPorts[ pac ];
				client.unregisterPort( pp );
			}
			for( int pmc = 0 ; pmc < numProducerMidiPorts ; ++pmc )
			{
				JackPort pp = producerMidiPorts[ pmc ];
				client.unregisterPort( pp );
			}
			for( int cac = 0 ; cac < numConsumerAudioPorts ; ++cac )
			{
				JackPort pp = consumerAudioPorts[ cac ];
				client.unregisterPort( pp );
			}
			for( int cmc = 0 ; cmc < numConsumerMidiPorts ; ++cmc )
			{
				JackPort pp = consumerMidiPorts[ cmc ];
				client.unregisterPort( pp );
			}
			client.setProcessCallback( null );
			client.onShutdown( null );
		}
		catch( Exception e )
		{
			throw new DatastoreException( "Exception caught during provider destroy: " + e.toString(), e );
		}
	}

	@Override
	public void clientShutdown(JackClient client)
	{
		log.error("ClientShutdown called");
		errorQueue.queueError( this, ErrorSeverity.FATAL, "JNAJack client shutdown occured" );
	}

//	private long nsToMs( long inNs )
//	{
//		return inNs / 1000000;
//	}
//
//	private long usToMs( long inUs )
//	{
//		return inUs / 1000;
//	}

	@Override
	public boolean process( JackClient client, int numFrames )
	{
		long periodStartFrameTime;
		try
		{
//			long jackTime = jack.getTime();
//			consumerAudioPorts[0].getLatencyRange( latencyRange, JackLatencyCallbackMode.JackPlaybackLatency );
//			log.debug("jack time is " + usToMs(jackTime) );
//			int jackMinLatency = latencyRange.getMin();
//			int jackMaxLatency = latencyRange.getMax();
//			log.debug("MinLatency(" + jackMinLatency +") MaxLatency(" + jackMaxLatency + ")");
			periodStartFrameTime = client.getLastFrameTime();
//			log.debug("Period start frame time is " + periodStartFrameTime );
		}
		catch (JackException e)
		{
			log.error( e );
			return false;
		}
		boolean localShouldRecordPeriods = shouldRecordPeriods;

		if( localShouldRecordPeriods )
		{
			numPeriodsRecorded++;
		}
//		log.debug("Jack client process called with " + numFrames + " frames");

		masterInBuffersResetOrCopy( numFrames, periodStartFrameTime );

		int numConsumersUsed = (masterOutBuffers.numAudioBuffers < numConsumerAudioPorts ? masterOutBuffers.numAudioBuffers : numConsumerAudioPorts );
//		boolean setDestinationBuffers = masterOutBuffersTryPointerMoves(numConsumersUsed);

		// Now call the graph processing on all of that
		RealtimeMethodReturnCodeEnum rc = doClockSourceProcessing( numFrames, periodStartFrameTime );

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

	private void masterInBuffersResetOrCopy( int numFrames, long periodStartFrameTime )
	{
		int numProducersUsed = (masterInBuffers.numAudioBuffers < numProducerAudioPorts ? masterInBuffers.numAudioBuffers : numProducerAudioPorts );
		int pac = 0;
		for( ; pac < numProducersUsed ; ++pac )
		{
			JackPort pp = producerAudioPorts[ pac ];
			FloatBuffer fb = pp.getFloatBuffer();
			MadChannelBuffer aucb = masterInBuffers.audioBuffers[pac];
			float[] mifb = aucb.floatBuffer;
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
				JackPort mp = producerMidiPorts[ pmc ];
				int numMidiEvents = JackMidi.getEventCount(mp);
				if( numMidiEvents > 0 )
				{
//					log.debug("Processing " + numMidiEvents + " midi events");
				}
				for( int i = 0 ; i < numMidiEvents ; ++i )
				{
					JackMidi.eventGet( jme, mp,  i );
					int bufferSize = jme.size();
					byte[] jjmmb = jnaJackMidiMessage.getBuffer();
					if( bufferSize > jjmmb.length )
					{
					    log.error("Failed midi event byte size during get :-(");
					}
					else
					{
						jme.read( jjmmb );

						long jackMidiEventTime = jme.time();
//						log.debug("Within process() call at time " + periodStartFrameTime + " midi event with time " + jackMidiEventTime );

						long timestamp = periodStartFrameTime + jackMidiEventTime;

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
				long startCandidateFrameTime = periodStartFrameTime;
				long endCandidateFrameTime = startCandidateFrameTime + numFrames;
				int numRead = noteEventRing.readUpToMaxNumAndFrameTime( tmpNoteEventArray,
						0,
						tmpNoteEventArrayLength,
						endCandidateFrameTime );

				for( int n = 0 ; n < numRead ; ++n )
				{
					HardwareMidiNoteEvent midiEvent = tmpNoteEventArray[n];
					int eventMidiChannel = midiEvent.channel;
					long eventFrameTime = midiEvent.eventFrameTime;

					if( eventMidiChannel <= masterInBuffers.numMidiBuffers )
					{
						MadChannelBuffer midiChannelBuffer = masterInBuffers.noteBuffers[ eventMidiChannel ];
						MadChannelNoteEvent noteEvent = midiChannelBuffer.noteBuffer[ midiChannelBuffer.numElementsInBuffer++ ];
						int sampleIndex = frameTimeToIndex( startCandidateFrameTime,
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
			catch( JackException je )
			{
				log.error( "JackException caught in midi process ports: " + je.toString(), je );
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

	private void masterOutBuffersCopyOver(int numFrames, int numConsumersUsed)
	{
		int cac = 0;
		for( ; cac < numConsumersUsed ; ++cac )
		{
			JackPort pp = consumerAudioPorts[ cac ];
			FloatBuffer fb = pp.getFloatBuffer();
			MadChannelBuffer aucb = masterOutBuffers.audioBuffers[ cac ];
			fb.put( aucb.floatBuffer,  0,  numFrames );
		}
		// Make sure any remaining output channels are silenced
		for( int r = cac ; r < numConsumerAudioPorts ; ++r )
		{
			JackPort pp = consumerAudioPorts[ r ];
			FloatBuffer fb = pp.getFloatBuffer();
			fb.put( emptyFloatBuffer.floatBuffer, 0, numFrames );
		}
	}

	private int frameTimeToIndex( long sft, long eft, long eventFrameTime )
	{
		long startDiff = eventFrameTime - sft;
		boolean eventBefore = startDiff < 0;
		long endDiff = eventFrameTime - eft;
		boolean eventAfter = endDiff > 0;

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
		catch (JackException e)
		{
			log.error("Failed fetching frame time from jack: " + e.toString(), e );
			// Fall back to current sytem nanos;
			return System.nanoTime();
		}
	}

	@Override
	public void xrunOccured(JackClient client)
	{
		errorQueue.queueError( this, ErrorSeverity.WARNING, "XRun" );
//		errorQueue.queueError( this, ErrorSeverity.FATAL, "XRun" );
	}

}
