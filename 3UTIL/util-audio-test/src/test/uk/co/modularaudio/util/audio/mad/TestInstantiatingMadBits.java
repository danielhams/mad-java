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

package test.uk.co.modularaudio.util.audio.mad;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.uk.co.modularaudio.util.audio.mad.factory.TestMadDefinitionFactory;
import test.uk.co.modularaudio.util.audio.mad.factory.TestMadInstanceFactory;
import test.uk.co.modularaudio.util.audio.mad.units.stereotee.StereoTeeMadDefinition;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOOneChannelSetting;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class TestInstantiatingMadBits
{
	private static Log log = LogFactory.getLog( TestInstantiatingMadBits.class.getName());

	public TestInstantiatingMadBits()
	{
	}

	public void testEm()
		throws Exception
	{
		log.debug("Beginning tests..");
//		int numToLog = 100000;
//		long before = System.nanoTime();
//		for( int i = 0 ; i < numToLog ; i++ )
//		{
//			log.debug("Quick succesive log lines1");
//		}
//		long after = System.nanoTime();
//		long diff = after - before;
//		log.debug("Did " + numToLog + " in " + diff + " nanos or " + (diff / 1000) + " micros or " + (diff / 1000000) + " millis");

		final DataRate dataRate = DataRate.SR_44100;
		final int channelBufferLength = 64;
		final HardwareIOOneChannelSetting coreEngineLatencyConfiguration = new HardwareIOOneChannelSetting( dataRate, channelBufferLength );

		final long nanosOutputLatency = AudioTimingUtils.getNumNanosecondsForBufferLength( dataRate.getValue(), channelBufferLength );
		final int sampleFramesOutputLatency = channelBufferLength;

		final HardwareIOChannelSettings dataRateConfiguration = new HardwareIOChannelSettings( coreEngineLatencyConfiguration, nanosOutputLatency, sampleFramesOutputLatency );

		final MadTimingParameters timingParameters = new MadTimingParameters( 100, 100, 100, 100, 100 );

		final TestMadDefinitionFactory defFactory = new TestMadDefinitionFactory();
		final TestMadInstanceFactory inFactory = new TestMadInstanceFactory();
		final Collection<MadDefinition<?,?>> defs = defFactory.listDefinitions();
		for( final MadDefinition<?,?> def : defs )
		{
			final MadParameterDefinition[] defParameters = def.getParameterDefinitions();
			final Map<MadParameterDefinition, String> parameterValues = new HashMap<MadParameterDefinition, String>();
			if( defParameters.length > 0 )
			{
				for( final MadParameterDefinition paramDef : defParameters )
				{
					if( paramDef.getKey().equals( "numchannels" ) )
					{
						parameterValues.put( paramDef, "2" );
					}
					else if( paramDef.getKey().equals( "numtees" ))
					{
						parameterValues.put( paramDef, "3" );
					}
				}
			}
			final MadInstance<?,?> testInstance = inFactory.createInstanceForDefinition(  def, parameterValues, "Test Instance" );
			log.debug("Got a new instance : " + testInstance.toString() );

			final MadChannelInstance[] channelInstances = testInstance.getChannelInstances();
			final int numChannels = channelInstances.length;

			final long systemCurrentTime = System.currentTimeMillis();
			final MadChannelConnectedFlags channelConnectedFlags = new MadChannelConnectedFlags( numChannels );
			final MadChannelBuffer[] channelBuffers = new MadChannelBuffer[ numChannels ];
			for( int i = 0 ; i < numChannels ; i++ )
			{
				final MadChannelType channelType = channelInstances[ i ].definition.type;
				final MadChannelBuffer newBuffer = new MadChannelBuffer( channelType, dataRateConfiguration.getChannelBufferLengthForChannelType( channelType ) );
				channelBuffers[ i ] = newBuffer;
				channelConnectedFlags.set(  i  );
			}
			if( testInstance.getDefinition().getId().equals(  StereoTeeMadDefinition.DEFINITION_ID ) )
			{
				manyTeeTest( testInstance, timingParameters, systemCurrentTime, channelConnectedFlags, channelBuffers );
				manyTeeTest( testInstance, timingParameters, systemCurrentTime, channelConnectedFlags, channelBuffers );
			}
		}

	}

	private void manyTeeTest( final MadInstance<?,?> testInstance,
			final MadTimingParameters timingParameters,
			final long systemCurrentTime,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers )
			throws MadProcessingException
	{
		// Now test calling the process method
		final int numRounds = 100000;
		final long nanosBefore = System.nanoTime();
		final int numFramesPerPeriod = 1024;
		for( int i = 0 ; i < numRounds ; i++ )
		{
			testInstance.processNoEvents( null,
					timingParameters,
					systemCurrentTime,
					channelConnectedFlags,
					channelBuffers,
					numFramesPerPeriod );
		}
		final long nanosAfter = System.nanoTime();
		final long diff = nanosAfter - nanosBefore;
		log.debug( "For " + numRounds + " rounds it cost " + diff + " nanos" + " or " + (diff / 1000000) + " millis" );
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main( final String[] args ) throws Exception
	{
//		BasicConfigurator.configure();
		final TestInstantiatingMadBits tipb = new TestInstantiatingMadBits();
		tipb.testEm();
	}

}
