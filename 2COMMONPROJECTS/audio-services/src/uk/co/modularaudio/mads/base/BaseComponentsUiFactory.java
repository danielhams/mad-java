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
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.audiotocv4.mu.AudioToCv4MadDefinition;
import uk.co.modularaudio.mads.base.audiotocv4.ui.AudioToCv4MadUiDefinition;
import uk.co.modularaudio.mads.base.bandlimitedoscillator.mu.BandLimitedOscillatorMadDefinition;
import uk.co.modularaudio.mads.base.bandlimitedoscillator.ui.BandLimitedOscillatorMadUiDefinition;
import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadDefinition;
import uk.co.modularaudio.mads.base.controllertocv.ui.ControllerToCvMadUiDefinition;
import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderMadDefinition;
import uk.co.modularaudio.mads.base.crossfader.ui.CrossFaderMadUiDefinition;
import uk.co.modularaudio.mads.base.cvalinear.mu.LinearCVAMadDefinition;
import uk.co.modularaudio.mads.base.cvalinear.ui.LinearCVAMadUiDefinition;
import uk.co.modularaudio.mads.base.cvsurface.mu.CvSurfaceMadDefinition;
import uk.co.modularaudio.mads.base.cvsurface.ui.CvSurfaceMadUiDefinition;
import uk.co.modularaudio.mads.base.cvtoaudio4.mu.CvToAudio4MadDefinition;
import uk.co.modularaudio.mads.base.cvtoaudio4.ui.CvToAudio4MadUiDefinition;
import uk.co.modularaudio.mads.base.dctrap.mu.DCTrapMadDefinition;
import uk.co.modularaudio.mads.base.dctrap.ui.DCTrapMadUiDefinition;
import uk.co.modularaudio.mads.base.djeq.mu.DJEQMadDefinition;
import uk.co.modularaudio.mads.base.djeq.ui.DJEQMadUiDefinition;
import uk.co.modularaudio.mads.base.frequencyfilter.mu.FrequencyFilterMadDefinition;
import uk.co.modularaudio.mads.base.frequencyfilter.ui.FrequencyFilterMadUiDefinition;
import uk.co.modularaudio.mads.base.imixer3.mu.IMixer3MadDefinition;
import uk.co.modularaudio.mads.base.imixer3.ui.IMixer3MadUiDefinition;
import uk.co.modularaudio.mads.base.imixer8.mu.IMixer8MadDefinition;
import uk.co.modularaudio.mads.base.imixer8.ui.IMixer8MadUiDefinition;
import uk.co.modularaudio.mads.base.interptester.mu.InterpTesterMadDefinition;
import uk.co.modularaudio.mads.base.interptester.ui.InterpTesterMadUiDefinition;
import uk.co.modularaudio.mads.base.limiter.mu.LimiterMadDefinition;
import uk.co.modularaudio.mads.base.limiter.ui.LimiterMadUiDefinition;
import uk.co.modularaudio.mads.base.midside.mu.MidSideMadDefinition;
import uk.co.modularaudio.mads.base.midside.ui.MidSideMadUiDefinition;
import uk.co.modularaudio.mads.base.moogfilter.mu.MoogFilterMadDefinition;
import uk.co.modularaudio.mads.base.moogfilter.ui.MoogFilterMadUiDefinition;
import uk.co.modularaudio.mads.base.notedebug.mu.NoteDebugMadDefinition;
import uk.co.modularaudio.mads.base.notedebug.ui.NoteDebugMadUiDefinition;
import uk.co.modularaudio.mads.base.notehistogram.mu.NoteHistogramMadDefinition;
import uk.co.modularaudio.mads.base.notehistogram.ui.NoteHistogramMadUiDefinition;
import uk.co.modularaudio.mads.base.notemultiplexer.mu.NoteMultiplexerMadDefinition;
import uk.co.modularaudio.mads.base.notemultiplexer.ui.NoteMultiplexerMadUiDefinition;
import uk.co.modularaudio.mads.base.notetocv.mu.NoteToCvMadDefinition;
import uk.co.modularaudio.mads.base.notetocv.ui.NoteToCvMadUiDefinition;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadDefinition;
import uk.co.modularaudio.mads.base.oscilloscope.ui.OscilloscopeMadUiDefinition;
import uk.co.modularaudio.mads.base.prng.mu.PrngMadDefinition;
import uk.co.modularaudio.mads.base.prng.ui.PrngMadUiDefinition;
import uk.co.modularaudio.mads.base.rbjfilter.mu.RBJFilterMadDefinition;
import uk.co.modularaudio.mads.base.rbjfilter.ui.RBJFilterMadUiDefinition;
import uk.co.modularaudio.mads.base.scaleandoffset.mu.ScaleAndOffsetMadDefinition;
import uk.co.modularaudio.mads.base.scaleandoffset.ui.ScaleAndOffsetMadUiDefinition;
import uk.co.modularaudio.mads.base.scopelarge.mu.ScopeLargeMadDefinition;
import uk.co.modularaudio.mads.base.scopelarge.ui.ScopeLargeMadUiDefinition;
import uk.co.modularaudio.mads.base.scopesmall.mu.ScopeSmallMadDefinition;
import uk.co.modularaudio.mads.base.scopesmall.ui.ScopeSmallMadUiDefinition;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player.ui.SoundfilePlayerMadUiDefinition;
import uk.co.modularaudio.mads.base.specamplarge.mu.SpecAmpLargeMadDefinition;
import uk.co.modularaudio.mads.base.specamplarge.ui.SpecAmpLargeMadUiDefinition;
import uk.co.modularaudio.mads.base.specampsmall.mu.SpecAmpSmallMadDefinition;
import uk.co.modularaudio.mads.base.specampsmall.ui.SpecAmpSmallMadUiDefinition;
import uk.co.modularaudio.mads.base.staticvalue.mu.StaticValueMadDefinition;
import uk.co.modularaudio.mads.base.staticvalue.ui.StaticValueMadUiDefinition;
import uk.co.modularaudio.mads.base.stereo_compressor.mu.StereoCompressorMadDefinition;
import uk.co.modularaudio.mads.base.stereo_compressor.ui.StereoCompressorMadUiDefinition;
import uk.co.modularaudio.mads.base.waveroller.mu.WaveRollerMadDefinition;
import uk.co.modularaudio.mads.base.waveroller.ui.WaveRollerMadUiDefinition;
import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.madcomponentui.MadComponentUiFactory;
import uk.co.modularaudio.service.madcomponentui.MadComponentUiService;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.table.Span;

public class BaseComponentsUiFactory
	implements ComponentWithLifecycle, MadComponentUiFactory
{
	private static Log log = LogFactory.getLog( BaseComponentsUiFactory.class.getName() );

	private MadComponentService componentService;
	private MadComponentUiService componentUiService;
	private BufferedImageAllocationService bufferedImageAllocationService;

	private BaseComponentsFactory baseComponentsFactory;

	private final ArrayList<MadUiDefinition<?, ?>> muds = new ArrayList<MadUiDefinition<?,?>>();

	private ScaleAndOffsetMadUiDefinition saoMud;
	private StaticValueMadUiDefinition svMud;
	private LimiterMadUiDefinition limMud;
	private OscilloscopeMadUiDefinition oscMud;
	private CrossFaderMadUiDefinition cfMud;
	private FrequencyFilterMadUiDefinition ffMud;
	private SpecAmpSmallMadUiDefinition sasMud;
	private SpecAmpLargeMadUiDefinition salMud;
	private NoteToCvMadUiDefinition ntcMud;
	private CvSurfaceMadUiDefinition cvsMud;
	private LinearCVAMadUiDefinition lcvaMud;
	private PrngMadUiDefinition prngMud;
	private DCTrapMadUiDefinition dctrapMud;
	private StereoCompressorMadUiDefinition stcompMud;
	private NoteMultiplexerMadUiDefinition nmpMud;
	private BandLimitedOscillatorMadUiDefinition bloMud;
	private WaveRollerMadUiDefinition wrMud;
	private SoundfilePlayerMadUiDefinition sfpMud;
	private RBJFilterMadUiDefinition rjbMud;
	private MoogFilterMadUiDefinition moogMud;
	private InterpTesterMadUiDefinition interpMud;
	private IMixer3MadUiDefinition mix3Mud;
	private IMixer8MadUiDefinition mix8Mud;
	private DJEQMadUiDefinition djeqMud;
	private MidSideMadUiDefinition midsideMud;
	private ScopeSmallMadUiDefinition scopesMud;
	private ScopeLargeMadUiDefinition scopelMud;
	private AudioToCv4MadUiDefinition atc4Mud;
	private CvToAudio4MadUiDefinition cta4Mud;
	private NoteHistogramMadUiDefinition notehMud;
	private NoteDebugMadUiDefinition notedMud;
	private ControllerToCvMadUiDefinition con2cvMud;

	private final HashMap<String, MadUiDefinition<?, ?>> mdIdToMudMap = new HashMap<String, MadUiDefinition<?,?>>();

	public BaseComponentsUiFactory()
	{
	}

	public void setComponentService( final MadComponentService componentService )
	{
		this.componentService = componentService;
	}

	public void setComponentUiService( final MadComponentUiService componentUiService )
	{
		this.componentUiService = componentUiService;
	}

	public void setBaseComponentsFactory( final BaseComponentsFactory baseComponentsFactory )
	{
		this.baseComponentsFactory = baseComponentsFactory;
	}

	public void setBufferedImageAllocationService( final BufferedImageAllocationService bufferedImageAllocationService )
	{
		this.bufferedImageAllocationService = bufferedImageAllocationService;
	}

	@Override
	public List<MadUiDefinition<?, ?>> listComponentUiDefinitions()
	{
		return muds;
	}

	@Override
	public IMadUiInstance<?, ?> createUiInstanceForMad( final MadInstance<?, ?> madInstance )
			throws DatastoreException, RecordNotFoundException
	{
		final MadUiDefinition<?, ?> mud = mdIdToMudMap.get( madInstance.getDefinition().getId() );
		if( mud != null )
		{
			return mud.createNewUiInstanceUT( madInstance );
		}
		else
		{
			throw new RecordNotFoundException( "Unknown mad definition: " + madInstance.getDefinition().getName() );
		}
	}

	@Override
	public void cleanupUiInstance( final IMadUiInstance<?, ?> uiInstance ) throws DatastoreException, RecordNotFoundException
	{
	}

	@Override
	public Span getUiSpanForDefinition( final MadDefinition<?, ?> madDefinition )
			throws DatastoreException, RecordNotFoundException
	{
		final MadUiDefinition<?, ?> mud = mdIdToMudMap.get( madDefinition.getId() );
		if( mud != null )
		{
			return mud.getCellSpan();
		}
		else
		{
			throw new RecordNotFoundException( "Unknown mad definition: " + madDefinition.getName() );
		}
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( componentService == null ||
				componentUiService == null ||
				baseComponentsFactory == null )
		{
			throw new ComponentConfigurationException( "Service missing dependencies. Check config." );
		}

		try
		{
			final ScaleAndOffsetMadDefinition saoMd = (ScaleAndOffsetMadDefinition)
					componentService.findDefinitionById( ScaleAndOffsetMadDefinition.DEFINITION_ID );
            saoMud = new ScaleAndOffsetMadUiDefinition( saoMd );
            muds.add( saoMud );
            mdIdToMudMap.put( ScaleAndOffsetMadDefinition.DEFINITION_ID, saoMud );
			final StaticValueMadDefinition svMd = (StaticValueMadDefinition)
					componentService.findDefinitionById( StaticValueMadDefinition.DEFINITION_ID );
            svMud = new StaticValueMadUiDefinition( svMd );
            muds.add( svMud );
            mdIdToMudMap.put( StaticValueMadDefinition.DEFINITION_ID, svMud );
			final LimiterMadDefinition limMd = (LimiterMadDefinition)
					componentService.findDefinitionById( LimiterMadDefinition.DEFINITION_ID );
            limMud = new LimiterMadUiDefinition( limMd );
            muds.add( limMud );
            mdIdToMudMap.put( LimiterMadDefinition.DEFINITION_ID, limMud );
			final OscilloscopeMadDefinition oscMd = (OscilloscopeMadDefinition)
					componentService.findDefinitionById( OscilloscopeMadDefinition.DEFINITION_ID );
            oscMud = new OscilloscopeMadUiDefinition( bufferedImageAllocationService, oscMd );
            muds.add( oscMud );
            mdIdToMudMap.put( OscilloscopeMadDefinition.DEFINITION_ID, oscMud );
			final CrossFaderMadDefinition cfMd = (CrossFaderMadDefinition)
                componentService.findDefinitionById( CrossFaderMadDefinition.DEFINITION_ID );
            cfMud = new CrossFaderMadUiDefinition( cfMd );
            muds.add( cfMud );
            mdIdToMudMap.put( CrossFaderMadDefinition.DEFINITION_ID, cfMud );
			final FrequencyFilterMadDefinition ffMd = (FrequencyFilterMadDefinition)
                componentService.findDefinitionById( FrequencyFilterMadDefinition.DEFINITION_ID );
            ffMud = new FrequencyFilterMadUiDefinition( ffMd );
            muds.add( ffMud );
            mdIdToMudMap.put( FrequencyFilterMadDefinition.DEFINITION_ID, ffMud );
			final SpecAmpSmallMadDefinition sasMd = (SpecAmpSmallMadDefinition)
                componentService.findDefinitionById( SpecAmpSmallMadDefinition.DEFINITION_ID );
            sasMud = new SpecAmpSmallMadUiDefinition( sasMd );
            muds.add( sasMud );
            mdIdToMudMap.put( SpecAmpSmallMadDefinition.DEFINITION_ID, sasMud );
			final SpecAmpLargeMadDefinition salMd = (SpecAmpLargeMadDefinition)
                componentService.findDefinitionById( SpecAmpLargeMadDefinition.DEFINITION_ID );
            salMud = new SpecAmpLargeMadUiDefinition( salMd );
            muds.add( salMud );
            mdIdToMudMap.put( SpecAmpLargeMadDefinition.DEFINITION_ID, salMud );
			final NoteToCvMadDefinition ntcMd = (NoteToCvMadDefinition)
                componentService.findDefinitionById( NoteToCvMadDefinition.DEFINITION_ID );
            ntcMud = new NoteToCvMadUiDefinition( ntcMd );
            muds.add( ntcMud );
            mdIdToMudMap.put( NoteToCvMadDefinition.DEFINITION_ID, ntcMud );
			final CvSurfaceMadDefinition cvsMd = (CvSurfaceMadDefinition)
                componentService.findDefinitionById( CvSurfaceMadDefinition.DEFINITION_ID );
            cvsMud = new CvSurfaceMadUiDefinition( cvsMd );
            muds.add( cvsMud );
            mdIdToMudMap.put( CvSurfaceMadDefinition.DEFINITION_ID, cvsMud );
			final LinearCVAMadDefinition lcvaMd = (LinearCVAMadDefinition)
                componentService.findDefinitionById( LinearCVAMadDefinition.DEFINITION_ID );
            lcvaMud = new LinearCVAMadUiDefinition( lcvaMd );
            muds.add( lcvaMud );
            mdIdToMudMap.put( LinearCVAMadDefinition.DEFINITION_ID, lcvaMud );
			final PrngMadDefinition prngMd = (PrngMadDefinition)
                componentService.findDefinitionById( PrngMadDefinition.DEFINITION_ID );
            prngMud = new PrngMadUiDefinition( prngMd );
            muds.add( prngMud );
            mdIdToMudMap.put( PrngMadDefinition.DEFINITION_ID, prngMud );
			final DCTrapMadDefinition dctrapMd = (DCTrapMadDefinition)
                componentService.findDefinitionById( DCTrapMadDefinition.DEFINITION_ID );
            dctrapMud = new DCTrapMadUiDefinition( dctrapMd );
            muds.add( dctrapMud );
            mdIdToMudMap.put( DCTrapMadDefinition.DEFINITION_ID, dctrapMud );
			final StereoCompressorMadDefinition stcompMd = (StereoCompressorMadDefinition)
                componentService.findDefinitionById( StereoCompressorMadDefinition.DEFINITION_ID );
            stcompMud = new StereoCompressorMadUiDefinition( bufferedImageAllocationService, stcompMd );
            muds.add( stcompMud );
            mdIdToMudMap.put( StereoCompressorMadDefinition.DEFINITION_ID, stcompMud );
			final NoteMultiplexerMadDefinition nmpMd = (NoteMultiplexerMadDefinition)
                componentService.findDefinitionById( NoteMultiplexerMadDefinition.DEFINITION_ID );
            nmpMud = new NoteMultiplexerMadUiDefinition( nmpMd );
            muds.add( nmpMud );
            mdIdToMudMap.put( NoteMultiplexerMadDefinition.DEFINITION_ID, nmpMud );
			final BandLimitedOscillatorMadDefinition bloMd = (BandLimitedOscillatorMadDefinition)
                componentService.findDefinitionById( BandLimitedOscillatorMadDefinition.DEFINITION_ID );
            bloMud = new BandLimitedOscillatorMadUiDefinition( bloMd );
            muds.add( bloMud );
            mdIdToMudMap.put( BandLimitedOscillatorMadDefinition.DEFINITION_ID, bloMud );
			final WaveRollerMadDefinition wrMd = (WaveRollerMadDefinition)
                componentService.findDefinitionById( WaveRollerMadDefinition.DEFINITION_ID );
            wrMud = new WaveRollerMadUiDefinition( bufferedImageAllocationService, wrMd );
            muds.add( wrMud );
            mdIdToMudMap.put( WaveRollerMadDefinition.DEFINITION_ID, wrMud );
			final SoundfilePlayerMadDefinition sfpMd = (SoundfilePlayerMadDefinition)
                componentService.findDefinitionById( SoundfilePlayerMadDefinition.DEFINITION_ID );
            sfpMud = new SoundfilePlayerMadUiDefinition( bufferedImageAllocationService, sfpMd );
            muds.add( sfpMud );
            mdIdToMudMap.put( SoundfilePlayerMadDefinition.DEFINITION_ID, sfpMud );
			final RBJFilterMadDefinition rjbMd = (RBJFilterMadDefinition)
                componentService.findDefinitionById( RBJFilterMadDefinition.DEFINITION_ID );
            rjbMud = new RBJFilterMadUiDefinition( rjbMd );
            muds.add( rjbMud );
            mdIdToMudMap.put( RBJFilterMadDefinition.DEFINITION_ID, rjbMud );
			final MoogFilterMadDefinition moogMd = (MoogFilterMadDefinition)
                componentService.findDefinitionById( MoogFilterMadDefinition.DEFINITION_ID );
            moogMud = new MoogFilterMadUiDefinition( moogMd );
            muds.add( moogMud );
            mdIdToMudMap.put( MoogFilterMadDefinition.DEFINITION_ID, moogMud );
			final InterpTesterMadDefinition interpMd = (InterpTesterMadDefinition)
                componentService.findDefinitionById( InterpTesterMadDefinition.DEFINITION_ID );
            interpMud = new InterpTesterMadUiDefinition( interpMd );
            muds.add( interpMud );
            mdIdToMudMap.put( InterpTesterMadDefinition.DEFINITION_ID, interpMud );
			final IMixer3MadDefinition mix3Md = (IMixer3MadDefinition)
                componentService.findDefinitionById( IMixer3MadDefinition.DEFINITION_ID );
            mix3Mud = new IMixer3MadUiDefinition( bufferedImageAllocationService, mix3Md );
            muds.add( mix3Mud );
            mdIdToMudMap.put( IMixer3MadDefinition.DEFINITION_ID, mix3Mud );
			final IMixer8MadDefinition mix8Md = (IMixer8MadDefinition)
                componentService.findDefinitionById( IMixer8MadDefinition.DEFINITION_ID );
            mix8Mud = new IMixer8MadUiDefinition( bufferedImageAllocationService, mix8Md );
            muds.add( mix8Mud );
            mdIdToMudMap.put( IMixer8MadDefinition.DEFINITION_ID, mix8Mud );
			final DJEQMadDefinition djeqMd = (DJEQMadDefinition)
                componentService.findDefinitionById( DJEQMadDefinition.DEFINITION_ID );
            djeqMud = new DJEQMadUiDefinition( bufferedImageAllocationService, djeqMd );
            muds.add( djeqMud );
            mdIdToMudMap.put( DJEQMadDefinition.DEFINITION_ID, djeqMud );
			final MidSideMadDefinition midsideMd = (MidSideMadDefinition)
                componentService.findDefinitionById( MidSideMadDefinition.DEFINITION_ID );
            midsideMud = new MidSideMadUiDefinition( midsideMd );
            muds.add( midsideMud );
            mdIdToMudMap.put( MidSideMadDefinition.DEFINITION_ID, midsideMud );
			final ScopeSmallMadDefinition scopesMd = (ScopeSmallMadDefinition)
                componentService.findDefinitionById( ScopeSmallMadDefinition.DEFINITION_ID );
            scopesMud = new ScopeSmallMadUiDefinition( bufferedImageAllocationService, scopesMd );
            muds.add( scopesMud );
            mdIdToMudMap.put( ScopeSmallMadDefinition.DEFINITION_ID, scopesMud );
			final ScopeLargeMadDefinition scopelMd = (ScopeLargeMadDefinition)
                componentService.findDefinitionById( ScopeLargeMadDefinition.DEFINITION_ID );
            scopelMud = new ScopeLargeMadUiDefinition( bufferedImageAllocationService, scopelMd );
            muds.add( scopelMud );
            mdIdToMudMap.put( ScopeLargeMadDefinition.DEFINITION_ID, scopelMud );
			final AudioToCv4MadDefinition atc4Md = (AudioToCv4MadDefinition)
                componentService.findDefinitionById( AudioToCv4MadDefinition.DEFINITION_ID );
            atc4Mud = new AudioToCv4MadUiDefinition( atc4Md );
            muds.add( atc4Mud );
            mdIdToMudMap.put( AudioToCv4MadDefinition.DEFINITION_ID, atc4Mud );
			final CvToAudio4MadDefinition cta4Md = (CvToAudio4MadDefinition)
                componentService.findDefinitionById( CvToAudio4MadDefinition.DEFINITION_ID );
            cta4Mud = new CvToAudio4MadUiDefinition( cta4Md );
            muds.add( cta4Mud );
            mdIdToMudMap.put( CvToAudio4MadDefinition.DEFINITION_ID, cta4Mud );
			final NoteHistogramMadDefinition notehMd = (NoteHistogramMadDefinition)
                componentService.findDefinitionById( NoteHistogramMadDefinition.DEFINITION_ID );
            notehMud = new NoteHistogramMadUiDefinition( notehMd );
            muds.add( notehMud );
            mdIdToMudMap.put( NoteHistogramMadDefinition.DEFINITION_ID, notehMud );
			final NoteDebugMadDefinition notedMd = (NoteDebugMadDefinition)
                componentService.findDefinitionById( NoteDebugMadDefinition.DEFINITION_ID );
            notedMud = new NoteDebugMadUiDefinition( notedMd );
            muds.add( notedMud );
            mdIdToMudMap.put( NoteDebugMadDefinition.DEFINITION_ID, notedMud );
			final ControllerToCvMadDefinition con2cvMd = (ControllerToCvMadDefinition)
                componentService.findDefinitionById( ControllerToCvMadDefinition.DEFINITION_ID );
            con2cvMud = new ControllerToCvMadUiDefinition( con2cvMd );
            muds.add( con2cvMud );
            mdIdToMudMap.put( ControllerToCvMadDefinition.DEFINITION_ID, con2cvMud );

			componentUiService.registerComponentUiFactory( this );
		}
		catch( DatastoreException | RecordNotFoundException | MadProcessingException e )
		{
			throw new ComponentConfigurationException( "Unable to create muds: " + e.toString(), e );
		}
	}

	@Override
	public void destroy()
	{
		try
		{
			componentUiService.unregisterComponentUiFactory( this );
		}
		catch( final DatastoreException e )
		{
			log.error( e );
		}
	}
}
