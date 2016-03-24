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

package uk.co.modularaudio.mads.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.mads.base.audiotocv4.mu.AudioToCv4MadDefinition;
import uk.co.modularaudio.mads.base.bandlimitedoscillator.mu.BandLimitedOscillatorMadDefinition;
import uk.co.modularaudio.mads.base.controllerhistogram.mu.ControllerHistogramMadDefinition;
import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadDefinition;
import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderMadDefinition;
import uk.co.modularaudio.mads.base.cvalinear.mu.LinearCVAMadDefinition;
import uk.co.modularaudio.mads.base.cvsurface.mu.CvSurfaceMadDefinition;
import uk.co.modularaudio.mads.base.cvtoaudio4.mu.CvToAudio4MadDefinition;
import uk.co.modularaudio.mads.base.dctrap.mu.DCTrapMadDefinition;
import uk.co.modularaudio.mads.base.djeq.mu.DJEQMadDefinition;
import uk.co.modularaudio.mads.base.frequencyfilter.mu.FrequencyFilterMadDefinition;
import uk.co.modularaudio.mads.base.imixer3.mu.IMixer3MadDefinition;
import uk.co.modularaudio.mads.base.imixer8.mu.IMixer8MadDefinition;
import uk.co.modularaudio.mads.base.interptester.mu.InterpTesterMadDefinition;
import uk.co.modularaudio.mads.base.limiter.mu.LimiterMadDefinition;
import uk.co.modularaudio.mads.base.midside.mu.MidSideMadDefinition;
import uk.co.modularaudio.mads.base.moogfilter.mu.MoogFilterMadDefinition;
import uk.co.modularaudio.mads.base.notedebug.mu.NoteDebugMadDefinition;
import uk.co.modularaudio.mads.base.notemultiplexer.mu.NoteMultiplexerMadDefinition;
import uk.co.modularaudio.mads.base.notetocv.mu.NoteToCvMadDefinition;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadDefinition;
import uk.co.modularaudio.mads.base.prng.mu.PrngMadDefinition;
import uk.co.modularaudio.mads.base.rbjfilter.mu.RBJFilterMadDefinition;
import uk.co.modularaudio.mads.base.scaleandoffset.mu.ScaleAndOffsetMadDefinition;
import uk.co.modularaudio.mads.base.scopelarge.mu.ScopeLargeMadDefinition;
import uk.co.modularaudio.mads.base.scopesmall.mu.ScopeSmallMadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player2.mu.SoundfilePlayer2MadDefinition;
import uk.co.modularaudio.mads.base.specamplarge.mu.SpecAmpLargeMadDefinition;
import uk.co.modularaudio.mads.base.specampsmall.mu.SpecAmpSmallMadDefinition;
import uk.co.modularaudio.mads.base.staticvalue.mu.StaticValueMadDefinition;
import uk.co.modularaudio.mads.base.stereo_compressor.mu.StereoCompressorMadDefinition;
import uk.co.modularaudio.mads.base.waveroller.mu.WaveRollerMadDefinition;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.service.madcomponent.MadComponentFactory;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorFactory;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class BaseComponentsFactory
	implements ComponentWithLifecycle, MadComponentFactory
{
	private static Log log = LogFactory.getLog( BaseComponentsFactory.class.getName() );

	private MadClassificationService classificationService;
	private MadComponentService componentService;

	private AdvancedComponentsFrontController advancedComponentsFrontController;

	private BaseComponentsCreationContext creationContext;

	private ScaleAndOffsetMadDefinition saoMD;
	private StaticValueMadDefinition svMD;
	private LimiterMadDefinition limMD;
	private OscilloscopeMadDefinition oscMD;
	private CrossFaderMadDefinition cfMD;
	private FrequencyFilterMadDefinition ffMD;
	private SpecAmpSmallMadDefinition sasMD;
	private SpecAmpLargeMadDefinition salMD;
	private NoteToCvMadDefinition ntcMD;
	private CvSurfaceMadDefinition cvsMD;
	private LinearCVAMadDefinition lcvaMD;
	private PrngMadDefinition prngMD;
	private DCTrapMadDefinition dctrapMD;
	private StereoCompressorMadDefinition stcompMD;
	private NoteMultiplexerMadDefinition nmpMD;
	private BandLimitedOscillatorMadDefinition bloMD;
	private WaveRollerMadDefinition wrMD;
	private SoundfilePlayerMadDefinition sfpMD;
	private SoundfilePlayer2MadDefinition sfp2MD;
	private RBJFilterMadDefinition rjbMD;
	private MoogFilterMadDefinition moogMD;
	private InterpTesterMadDefinition interpMD;
	private IMixer3MadDefinition mix3MD;
	private IMixer8MadDefinition mix8MD;
	private DJEQMadDefinition djeqMD;
	private MidSideMadDefinition midsideMD;
	private ScopeSmallMadDefinition scopesMD;
	private ScopeLargeMadDefinition scopelMD;
	private AudioToCv4MadDefinition atc4MD;
	private CvToAudio4MadDefinition cta4MD;
	private ControllerHistogramMadDefinition notehMD;
	private NoteDebugMadDefinition notedMD;
	private ControllerToCvMadDefinition con2cvMD;

	private final ArrayList<MadDefinition<?,?>> mds = new ArrayList<MadDefinition<?,?>>();

	private final Map<String, BaseMadDefinition> defIdToImd = new HashMap<String, BaseMadDefinition>();

	public BaseComponentsFactory()
	{
	}

	public void setClassificationService( final MadClassificationService classificationService )
	{
		this.classificationService = classificationService;
	}

	public void setComponentService( final MadComponentService componentService )
	{
		this.componentService = componentService;
	}

	public void setAdvancedComponentsFrontController( final AdvancedComponentsFrontController advancedComponentsFrontController )
	{
		this.advancedComponentsFrontController = advancedComponentsFrontController;
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( classificationService == null ||
				componentService == null ||
				advancedComponentsFrontController == null )
		{
			throw new ComponentConfigurationException( "Service missing dependencies. Check config." );
		}

		final OscillatorFactory oscillatorFactory = advancedComponentsFrontController.getOscillatorFactory();
		creationContext = new BaseComponentsCreationContext( advancedComponentsFrontController, oscillatorFactory );

		try
		{
			saoMD = new ScaleAndOffsetMadDefinition( creationContext, classificationService );
			addDef( saoMD );
			svMD = new StaticValueMadDefinition( creationContext, classificationService );
			addDef( svMD );
			limMD = new LimiterMadDefinition( creationContext, classificationService );
			addDef( limMD );
			oscMD = new OscilloscopeMadDefinition( creationContext, classificationService );
			addDef( oscMD );
			cfMD = new CrossFaderMadDefinition( creationContext, classificationService );
			addDef( cfMD );
			ffMD = new FrequencyFilterMadDefinition( creationContext, classificationService );
			addDef( ffMD );
			sasMD = new SpecAmpSmallMadDefinition( creationContext, classificationService );
			addDef( sasMD );
			salMD = new SpecAmpLargeMadDefinition( creationContext, classificationService );
			addDef( salMD );
			ntcMD = new NoteToCvMadDefinition( creationContext, classificationService );
			addDef( ntcMD );
			cvsMD = new CvSurfaceMadDefinition( creationContext, classificationService );
			addDef( cvsMD );
			lcvaMD = new LinearCVAMadDefinition( creationContext, classificationService );
			addDef( lcvaMD );
			prngMD = new PrngMadDefinition( creationContext, classificationService );
			addDef( prngMD );
			dctrapMD = new DCTrapMadDefinition( creationContext, classificationService );
			addDef( dctrapMD );
			stcompMD = new StereoCompressorMadDefinition( creationContext, classificationService );
			addDef( stcompMD );
			nmpMD = new NoteMultiplexerMadDefinition( creationContext, classificationService );
			addDef( nmpMD );
			bloMD = new BandLimitedOscillatorMadDefinition( creationContext, classificationService );
			addDef( bloMD );
			wrMD = new WaveRollerMadDefinition( creationContext, classificationService );
			addDef( wrMD );
			sfpMD = new SoundfilePlayerMadDefinition( creationContext, classificationService );
			addDef( sfpMD );
			sfp2MD = new SoundfilePlayer2MadDefinition( creationContext, classificationService );
			addDef( sfp2MD );
			rjbMD = new RBJFilterMadDefinition( creationContext, classificationService );
			addDef( rjbMD );
			moogMD = new MoogFilterMadDefinition( creationContext, classificationService );
			addDef( moogMD );
			interpMD = new InterpTesterMadDefinition( creationContext, classificationService );
			addDef( interpMD );
			mix3MD = new IMixer3MadDefinition( creationContext, classificationService );
			addDef( mix3MD );
			mix8MD = new IMixer8MadDefinition( creationContext, classificationService );
			addDef( mix8MD );
			djeqMD = new DJEQMadDefinition( creationContext, classificationService );
			addDef( djeqMD );
			midsideMD = new MidSideMadDefinition( creationContext, classificationService );
			addDef( midsideMD );
			scopesMD = new ScopeSmallMadDefinition( creationContext, classificationService );
			addDef( scopesMD );
			scopelMD = new ScopeLargeMadDefinition( creationContext, classificationService);
			addDef( scopelMD );
			atc4MD = new AudioToCv4MadDefinition( creationContext, classificationService );
			addDef( atc4MD );
			cta4MD = new CvToAudio4MadDefinition( creationContext, classificationService );
			addDef( cta4MD );
			notehMD = new ControllerHistogramMadDefinition( creationContext, classificationService );
			addDef( notehMD );
			notedMD = new NoteDebugMadDefinition( creationContext, classificationService );
			addDef( notedMD );
			con2cvMD = new ControllerToCvMadDefinition( creationContext, classificationService );
			addDef( con2cvMD );

			componentService.registerComponentFactory( this );

		}
		catch( final DatastoreException | RecordNotFoundException | MAConstraintViolationException e )
		{
			throw new ComponentConfigurationException( "Failed instantiating MADS: " + e.toString(), e );
		}

	}

	private <A extends MadDefinition<?, ?> & BaseMadDefinition> void addDef( final A d )
	{
		mds.add( d );
		defIdToImd.put( d.getId(), d );
	}

	@Override
	public void destroy()
	{
		try
		{
			componentService.unregisterComponentFactory( this );
		}
		catch( final DatastoreException e )
		{
			log.error( e );
		}
	}

	@Override
	public Collection<MadDefinition<?, ?>> listDefinitions()
	{
		return mds;
	}

	@Override
	public MadInstance<?, ?> createInstanceForDefinition( final MadDefinition<?, ?> definition,
			final Map<MadParameterDefinition, String> parameterValues, final String instanceName ) throws DatastoreException
	{
		final BaseMadDefinition bmd = defIdToImd.get( definition.getId() );
		if( bmd == null )
		{
			throw new DatastoreException("Unknown mad: " + definition.getId() );
		}
		else
		{
			try
			{
				return bmd.createInstance( parameterValues, instanceName );
			}
			catch( final MadProcessingException e )
			{
				throw new DatastoreException( e );
			}
		}
	}

	@Override
	public void cleanupInstance( final MadInstance<?, ?> instanceToDestroy ) throws DatastoreException
	{
	}
}
