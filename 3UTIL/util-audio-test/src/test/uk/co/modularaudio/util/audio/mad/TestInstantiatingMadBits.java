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

		DataRate dataRate = DataRate.SR_44100;
		int channelBufferLength = 64;
		HardwareIOOneChannelSetting coreEngineLatencyConfiguration = new HardwareIOOneChannelSetting( dataRate, channelBufferLength );

		long nanosOutputLatency = AudioTimingUtils.getNumNanosecondsForBufferLength( dataRate.getValue(), channelBufferLength );
		int sampleFramesOutputLatency = channelBufferLength;

		HardwareIOChannelSettings dataRateConfiguration = new HardwareIOChannelSettings( coreEngineLatencyConfiguration, nanosOutputLatency, sampleFramesOutputLatency );

		MadTimingParameters timingParameters = new MadTimingParameters( 100, 100, 100, 100, 100 );

		TestMadDefinitionFactory defFactory = new TestMadDefinitionFactory();
		TestMadInstanceFactory inFactory = new TestMadInstanceFactory();
		Collection<MadDefinition<?,?>> defs = defFactory.listDefinitions();
		for( MadDefinition<?,?> def : defs )
		{
			MadParameterDefinition[] defParameters = def.getParameterDefinitions();
			Map<MadParameterDefinition, String> parameterValues = new HashMap<MadParameterDefinition, String>();
			if( defParameters.length > 0 )
			{
				for( MadParameterDefinition paramDef : defParameters )
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
			MadInstance<?,?> testInstance = inFactory.createInstanceForDefinition(  def, parameterValues, "Test Instance" );
			log.debug("Got a new instance : " + testInstance.toString() );

			MadChannelInstance[] channelInstances = testInstance.getChannelInstances();
			int numChannels = channelInstances.length;

			long systemCurrentTime = System.currentTimeMillis();
			MadChannelConnectedFlags channelConnectedFlags = new MadChannelConnectedFlags( numChannels );
			MadChannelBuffer[] channelBuffers = new MadChannelBuffer[ numChannels ];
			for( int i = 0 ; i < numChannels ; i++ )
			{
				MadChannelType channelType = channelInstances[ i ].definition.type;
				MadChannelBuffer newBuffer = new MadChannelBuffer( channelType, dataRateConfiguration.getChannelBufferLengthForChannelType( channelType ) );
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

	private void manyTeeTest( MadInstance<?,?> testInstance,
			MadTimingParameters timingParameters,
			long systemCurrentTime,
			MadChannelConnectedFlags channelConnectedFlags,
			MadChannelBuffer[] channelBuffers )
			throws MadProcessingException
	{
		// Now test calling the process method
		int numRounds = 100000;
		long nanosBefore = System.nanoTime();
		int numFramesPerPeriod = 1024;
		for( int i = 0 ; i < numRounds ; i++ )
		{
			testInstance.process( null, timingParameters, systemCurrentTime, channelConnectedFlags, channelBuffers, numFramesPerPeriod );
		}
		long nanosAfter = System.nanoTime();
		long diff = nanosAfter - nanosBefore;
		log.debug( "For " + numRounds + " rounds it cost " + diff + " nanos" + " or " + (diff / 1000000) + " millis" );
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main( String[] args ) throws Exception
	{
//		BasicConfigurator.configure();
		TestInstantiatingMadBits tipb = new TestInstantiatingMadBits();
		tipb.testEm();
	}

}
