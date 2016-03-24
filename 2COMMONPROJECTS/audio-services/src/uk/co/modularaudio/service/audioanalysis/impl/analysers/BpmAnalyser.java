package uk.co.modularaudio.service.audioanalysis.impl.analysers;

import uk.co.modularaudio.service.audioanalysis.AnalysedData;
import uk.co.modularaudio.service.audioanalysis.impl.AnalysisContext;
import uk.co.modularaudio.service.audioanalysis.impl.AnalysisException;
import uk.co.modularaudio.service.audioanalysis.impl.AudioAnalyser;
import uk.co.modularaudio.service.hashedstorage.HashedRef;
import uk.co.modularaudio.util.audio.format.DataRate;

/**
 * BpmAnalyser takes the incoming data stream and creates an estimate
 * for the content BPM, along with a sample based offset for the initial bar begin.
 *
 * It does by:
 * * Low passing to 4khz
 * * Resample to 8khz
 * * Running auto correlation on this signal
 * * Looking for initial onset in the full rate data
 *
 * @author dan
 *
 */
public class BpmAnalyser implements AudioAnalyser
{
//	private final static Log log = LogFactory.getLog( BpmAnalyser.class.getName() );

//	private final static int RESAMPLE_RATE = 8000;

//	private FrequencyFilter lowPasser;
//	private FrequencyFilter lowPasser2;
//	private DataRate dataRate;
//	private int numChannels;
//	private long totalFrames;
//
//	private int maxFramesPerCall;
//	private float[] internalFloatBuffer;
//	private String tmpLpFilename;
//	private WaveFileWriter resampledFileWriter;

	public BpmAnalyser()
	{
	}

	@Override
	public void dataStart( final DataRate dataRate, final int numChannels, final long totalFrames, final int maxFramesPerCall )
		throws AnalysisException
	{
//		this.maxFramesPerCall = maxFramesPerCall * 2;
//		internalFloatBuffer = new float[ maxFramesPerCall ];
//
//		lowPasser = new ButterworthFilter24DB();
//		lowPasser2 = new ButterworthFilter24DB();
//		this.dataRate = dataRate;
//		this.numChannels = numChannels;
//		this.totalFrames = totalFrames;
//
//		try
//		{
//			final File tmpFile = File.createTempFile( "bpmanalyserresampledfile", ".wav" );
//			tmpLpFilename = tmpFile.getAbsolutePath();
//			if( log.isTraceEnabled() )
//			{
//				log.trace( "Outputting resample data to " + tmpLpFilename );
//			}
//
//			resampledFileWriter = new WaveFileWriter( tmpLpFilename, 1, dataRate.getValue(), (short)16 );
//		}
//		catch( final IOException ioe )
//		{
//			final String msg = "Exception caught initialising BPM data: " + ioe.toString();
//			throw new AnalysisException( msg, ioe );
//		}
	}

	@Override
	public void receiveFrames( final float[] data, final int numFrames ) throws AnalysisException
	{
//		for( int f = 0 ; f < numFrames ; ++f )
//		{
//			internalFloatBuffer[f] = data[f*numChannels];
//		}
//		lowPasser.filter( internalFloatBuffer, 0, numFrames, RESAMPLE_RATE / 4.0f, 1.0f, FrequencyFilterMode.LP, dataRate.getValue() );
//		lowPasser2.filter( internalFloatBuffer, 0, numFrames, RESAMPLE_RATE / 4.0f, 1.0f, FrequencyFilterMode.LP, dataRate.getValue() );
//
//		try
//		{
//			resampledFileWriter.writeFrames( internalFloatBuffer, 0, numFrames );
//		}
//		catch( final IOException e )
//		{
//			final String msg = "Exception caught downsampling frames: " + e.toString();
//			throw new AnalysisException( msg, e );
//		}
	}

	@Override
	public void dataEnd( final AnalysisContext context, final AnalysedData analysedData, final HashedRef hashedRef ) throws AnalysisException
	{
//		final float[] tmpBuffer = new float[ 16384 ];
//		final int tmpBufferOffset = 0;
//
//		BlockResamplingClient brc = null;
//
//		try
//		{
//			if( resampledFileWriter == null )
//			{
//				throw new AnalysisException( "No resampled file open" );
//			}
//			resampledFileWriter.close();
//
//			// And open for resample
//			// Linear interpolation should be good enough for bpm detection
//			brc = analysisResamplerService.createResamplingClient( tmpLpFilename, BlockResamplingMethod.LINEAR );
//			final long totalNumFrames = brc.getTotalNumFrames();
//			final SampleCacheClient scc = brc.getSampleCacheClient();
//			final int origSampleRate = scc.getSampleRate();
//
//			final double desiredSpeed = origSampleRate / (double)RESAMPLE_RATE;
//
//			final int initialOffset = (int)desiredSpeed;
//			final double initialFpOffset = desiredSpeed - initialOffset;
//			brc.setFramePosition( -initialOffset );
//			brc.setFpOffset( (float)-initialFpOffset );
//
//			final long numThisRoundLong = totalNumFrames - brc.getFramePosition();
//			final int numThisRoundInt = (int)(numThisRoundLong > maxFramesPerCall ? maxFramesPerCall : numThisRoundLong);
//
//			final RealtimeMethodReturnCodeEnum rtCode = analysisResamplerService.fetchAndResample(
//					brc, RESAMPLE_RATE, 1.0f,
//					internalFloatBuffer, 0, internalFloatBuffer, maxFramesPerCall,
//					numThisRoundInt, tmpBuffer, tmpBufferOffset );
//
//			if( rtCode != RealtimeMethodReturnCodeEnum.SUCCESS )
//			{
//				throw new AnalysisException( "Resampling failed" );
//			}
//
//		}
//		catch( final IOException e )
//		{
//			log.error( "IOException during resample file closure: " + e.toString(), e );
//		}
//		catch( final DatastoreException | UnsupportedAudioFileException e )
//		{
//			log.error( "Exception during resampling: " + e.toString(), e );
//		}
//		finally
//		{
//			if( brc != null )
//			{
//				try
//				{
//					analysisResamplerService.destroyResamplingClient( brc );
//				}
//				catch( final DatastoreException | RecordNotFoundException e )
//				{
//					final String msg = "Exception caught clearing up resampling client: " + e.toString();
//					log.error( msg, e );
//				}
//				brc = null;
//			}
//		}
	}

	@Override
	public void completeAnalysis( final AnalysisContext context, final AnalysedData analysedData, final HashedRef hashedRef )
	{
		// Hardcoded BPM for now
		analysedData.setBpm( 126.0f );
	}
}
