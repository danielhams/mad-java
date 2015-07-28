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

import java.util.HashMap;
import java.util.Map;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.mads.base.audioanalyser.mu.AudioAnalyserMadDefinition;
import uk.co.modularaudio.mads.base.audioanalyser.mu.AudioAnalyserMadInstance;
import uk.co.modularaudio.mads.base.audiocvconverter.mu.AudioCvConverterMadDefinition;
import uk.co.modularaudio.mads.base.audiocvconverter.mu.AudioCvConverterMadInstance;
import uk.co.modularaudio.mads.base.bandlimitedoscillator.mu.BandLimitedOscillatorMadDefinition;
import uk.co.modularaudio.mads.base.bandlimitedoscillator.mu.BandLimitedOscillatorMadInstance;
import uk.co.modularaudio.mads.base.bessel4.mu.Bessel4FilterMadDefinition;
import uk.co.modularaudio.mads.base.bessel4.mu.Bessel4FilterMadInstance;
import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadDefinition;
import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadInstance;
import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderMadDefinition;
import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderMadInstance;
import uk.co.modularaudio.mads.base.cvalinear.mu.LinearCVAMadDefinition;
import uk.co.modularaudio.mads.base.cvalinear.mu.LinearCVAMadInstance;
import uk.co.modularaudio.mads.base.cvsurface.mu.CvSurfaceMadDefinition;
import uk.co.modularaudio.mads.base.cvsurface.mu.CvSurfaceMadInstance;
import uk.co.modularaudio.mads.base.dctrap.mu.DCTrapMadDefinition;
import uk.co.modularaudio.mads.base.dctrap.mu.DCTrapMadInstance;
import uk.co.modularaudio.mads.base.djeq.mu.DJEQMadDefinition;
import uk.co.modularaudio.mads.base.djeq.mu.DJEQMadInstance;
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeMadDefinition;
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeMadInstance;
import uk.co.modularaudio.mads.base.feedbackdelay.mu.FeedbackDelayMadDefinition;
import uk.co.modularaudio.mads.base.feedbackdelay.mu.FeedbackDelayMadInstance;
import uk.co.modularaudio.mads.base.flipflop.mu.FlipFlopMadDefinition;
import uk.co.modularaudio.mads.base.flipflop.mu.FlipFlopMadInstance;
import uk.co.modularaudio.mads.base.foldbackdistortion.mu.FoldbackDistortionMadDefinition;
import uk.co.modularaudio.mads.base.foldbackdistortion.mu.FoldbackDistortionMadInstance;
import uk.co.modularaudio.mads.base.frequencyfilter.mu.FrequencyFilterMadDefinition;
import uk.co.modularaudio.mads.base.frequencyfilter.mu.FrequencyFilterMadInstance;
import uk.co.modularaudio.mads.base.imixer3.mu.IMixer3MadDefinition;
import uk.co.modularaudio.mads.base.imixer3.mu.IMixer3MadInstance;
import uk.co.modularaudio.mads.base.imixer8.mu.IMixer8MadDefinition;
import uk.co.modularaudio.mads.base.imixer8.mu.IMixer8MadInstance;
import uk.co.modularaudio.mads.base.interptester.mu.InterpTesterMadDefinition;
import uk.co.modularaudio.mads.base.interptester.mu.InterpTesterMadInstance;
import uk.co.modularaudio.mads.base.inverter.mu.InverterMadDefinition;
import uk.co.modularaudio.mads.base.inverter.mu.InverterMadInstance;
import uk.co.modularaudio.mads.base.limiter.mu.LimiterMadDefinition;
import uk.co.modularaudio.mads.base.limiter.mu.LimiterMadInstance;
import uk.co.modularaudio.mads.base.midside.mu.MidSideMadDefinition;
import uk.co.modularaudio.mads.base.midside.mu.MidSideMadInstance;
import uk.co.modularaudio.mads.base.mono_compressor.mu.MonoCompressorMadDefinition;
import uk.co.modularaudio.mads.base.mono_compressor.mu.MonoCompressorMadInstance;
import uk.co.modularaudio.mads.base.moogfilter.mu.MoogFilterMadDefinition;
import uk.co.modularaudio.mads.base.moogfilter.mu.MoogFilterMadInstance;
import uk.co.modularaudio.mads.base.ms20filter.mu.Ms20FilterMadDefinition;
import uk.co.modularaudio.mads.base.ms20filter.mu.Ms20FilterMadInstance;
import uk.co.modularaudio.mads.base.notedebug.mu.NoteDebugMadDefinition;
import uk.co.modularaudio.mads.base.notedebug.mu.NoteDebugMadInstance;
import uk.co.modularaudio.mads.base.notemultiplexer.mu.NoteMultiplexerMadDefinition;
import uk.co.modularaudio.mads.base.notemultiplexer.mu.NoteMultiplexerMadInstance;
import uk.co.modularaudio.mads.base.notetocv.mu.NoteToCvMadDefinition;
import uk.co.modularaudio.mads.base.notetocv.mu.NoteToCvMadInstance;
import uk.co.modularaudio.mads.base.oscillator.mu.OscillatorMadDefinition;
import uk.co.modularaudio.mads.base.oscillator.mu.OscillatorMadInstance;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadDefinition;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadInstance;
import uk.co.modularaudio.mads.base.pattern_sequencer.mu.PatternSequencerMadDefinition;
import uk.co.modularaudio.mads.base.pattern_sequencer.mu.PatternSequencerMadInstance;
import uk.co.modularaudio.mads.base.prng.mu.PrngMadDefinition;
import uk.co.modularaudio.mads.base.prng.mu.PrngMadInstance;
import uk.co.modularaudio.mads.base.rbjfilter.mu.RBJFilterMadDefinition;
import uk.co.modularaudio.mads.base.rbjfilter.mu.RBJFilterMadInstance;
import uk.co.modularaudio.mads.base.sampleandhold.mu.SampleAndHoldMadDefinition;
import uk.co.modularaudio.mads.base.sampleandhold.mu.SampleAndHoldMadInstance;
import uk.co.modularaudio.mads.base.sampleplayer.mu.SingleSamplePlayerMadDefinition;
import uk.co.modularaudio.mads.base.sampleplayer.mu.SingleSamplePlayerMadInstance;
import uk.co.modularaudio.mads.base.scaleandoffset.mu.ScaleAndOffsetMadDefinition;
import uk.co.modularaudio.mads.base.scaleandoffset.mu.ScaleAndOffsetMadInstance;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadInstance;
import uk.co.modularaudio.mads.base.soundfile_player2.mu.SoundfilePlayer2MadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player2.mu.SoundfilePlayer2MadInstance;
import uk.co.modularaudio.mads.base.specamplarge.mu.SpecAmpLargeMadDefinition;
import uk.co.modularaudio.mads.base.specamplarge.mu.SpecAmpLargeMadInstance;
import uk.co.modularaudio.mads.base.specampsmall.mu.SpecAmpSmallMadDefinition;
import uk.co.modularaudio.mads.base.specampsmall.mu.SpecAmpSmallMadInstance;
import uk.co.modularaudio.mads.base.spectralroll.mu.SpectralRollMadDefinition;
import uk.co.modularaudio.mads.base.spectralroll.mu.SpectralRollMadInstance;
import uk.co.modularaudio.mads.base.staticvalue.mu.StaticValueMadDefinition;
import uk.co.modularaudio.mads.base.staticvalue.mu.StaticValueMadInstance;
import uk.co.modularaudio.mads.base.stereo_compressor.mu.StereoCompressorMadDefinition;
import uk.co.modularaudio.mads.base.stereo_compressor.mu.StereoCompressorMadInstance;
import uk.co.modularaudio.mads.base.stereo_gate.mu.StereoGateMadDefinition;
import uk.co.modularaudio.mads.base.stereo_gate.mu.StereoGateMadInstance;
import uk.co.modularaudio.mads.base.supersawmodule.mu.SuperSawModuleMadDefinition;
import uk.co.modularaudio.mads.base.supersawmodule.mu.SuperSawModuleMadInstance;
import uk.co.modularaudio.mads.base.waveroller.mu.WaveRollerMadDefinition;
import uk.co.modularaudio.mads.base.waveroller.mu.WaveRollerMadInstance;
import uk.co.modularaudio.mads.base.xrunner.mu.XRunnerMadDefinition;
import uk.co.modularaudio.mads.base.xrunner.mu.XRunnerMadInstance;
import uk.co.modularaudio.service.madcomponent.AbstractMadComponentFactory;
import uk.co.modularaudio.util.audio.mad.MadCreationContext;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;

public class BaseComponentsFactory extends AbstractMadComponentFactory
{
//	private static Log log = LogFactory.getLog( BaseComponentsFactory.class.getName() );

	// Definitions to instances
	private final Map<Class<? extends MadDefinition<?,?>>, Class<? extends MadInstance<?,?>> > defClassToInsClassMap =
		new HashMap<Class<? extends MadDefinition<?,?>>, Class<? extends MadInstance<?,?>>>();

	private BaseComponentsCreationContext creationContext = null;

	private AdvancedComponentsFrontController advancedComponentsFrontController = null;

	public BaseComponentsFactory()
	{
		defClassToInsClassMap.put( ScaleAndOffsetMadDefinition.class, ScaleAndOffsetMadInstance.class );
		defClassToInsClassMap.put( StaticValueMadDefinition.class, StaticValueMadInstance.class );
		defClassToInsClassMap.put( LimiterMadDefinition.class, LimiterMadInstance.class );
		defClassToInsClassMap.put( OscilloscopeMadDefinition.class, OscilloscopeMadInstance.class );
		defClassToInsClassMap.put( CrossFaderMadDefinition.class, CrossFaderMadInstance.class );
		defClassToInsClassMap.put( FrequencyFilterMadDefinition.class, FrequencyFilterMadInstance.class );
		defClassToInsClassMap.put( Ms20FilterMadDefinition.class, Ms20FilterMadInstance.class );
		defClassToInsClassMap.put( FoldbackDistortionMadDefinition.class, FoldbackDistortionMadInstance.class );

		defClassToInsClassMap.put( FeedbackDelayMadDefinition.class, FeedbackDelayMadInstance.class );
		defClassToInsClassMap.put( SpecAmpSmallMadDefinition.class, SpecAmpSmallMadInstance.class );
		defClassToInsClassMap.put( SpecAmpLargeMadDefinition.class, SpecAmpLargeMadInstance.class );
		defClassToInsClassMap.put( SpectralRollMadDefinition.class, SpectralRollMadInstance.class );
		defClassToInsClassMap.put( InverterMadDefinition.class, InverterMadInstance.class );

		defClassToInsClassMap.put( PatternSequencerMadDefinition.class, PatternSequencerMadInstance.class );

		defClassToInsClassMap.put( NoteToCvMadDefinition.class, NoteToCvMadInstance.class );

		defClassToInsClassMap.put( ControllerToCvMadDefinition.class, ControllerToCvMadInstance.class );

		defClassToInsClassMap.put( CvSurfaceMadDefinition.class, CvSurfaceMadInstance.class );

		defClassToInsClassMap.put( LinearCVAMadDefinition.class, LinearCVAMadInstance.class );

		defClassToInsClassMap.put( PrngMadDefinition.class, PrngMadInstance.class );

		defClassToInsClassMap.put( DCTrapMadDefinition.class, DCTrapMadInstance.class );

		defClassToInsClassMap.put( SampleAndHoldMadDefinition.class, SampleAndHoldMadInstance.class );

		defClassToInsClassMap.put( FlipFlopMadDefinition.class, FlipFlopMadInstance.class );

		defClassToInsClassMap.put( StereoGateMadDefinition.class, StereoGateMadInstance.class );
		defClassToInsClassMap.put( MonoCompressorMadDefinition.class, MonoCompressorMadInstance.class );
		defClassToInsClassMap.put( StereoCompressorMadDefinition.class, StereoCompressorMadInstance.class );

		// Parameterised instances
		defClassToInsClassMap.put( NoteMultiplexerMadDefinition.class, NoteMultiplexerMadInstance.class );
		defClassToInsClassMap.put( AudioCvConverterMadDefinition.class, AudioCvConverterMadInstance.class );

		defClassToInsClassMap.put( SingleSamplePlayerMadDefinition.class, SingleSamplePlayerMadInstance.class );

		defClassToInsClassMap.put( BandLimitedOscillatorMadDefinition.class, BandLimitedOscillatorMadInstance.class );

		defClassToInsClassMap.put( OscillatorMadDefinition.class, OscillatorMadInstance.class );

		defClassToInsClassMap.put( EnvelopeMadDefinition.class, EnvelopeMadInstance.class );

		defClassToInsClassMap.put( SuperSawModuleMadDefinition.class, SuperSawModuleMadInstance.class );

		defClassToInsClassMap.put( WaveRollerMadDefinition.class, WaveRollerMadInstance.class );

		defClassToInsClassMap.put( SoundfilePlayerMadDefinition.class, SoundfilePlayerMadInstance.class );

		defClassToInsClassMap.put( RBJFilterMadDefinition.class, RBJFilterMadInstance.class );

		defClassToInsClassMap.put( MoogFilterMadDefinition.class, MoogFilterMadInstance.class );

		defClassToInsClassMap.put( NoteDebugMadDefinition.class, NoteDebugMadInstance.class );

		defClassToInsClassMap.put( AudioAnalyserMadDefinition.class, AudioAnalyserMadInstance.class );

		defClassToInsClassMap.put( XRunnerMadDefinition.class, XRunnerMadInstance.class );

		defClassToInsClassMap.put( InterpTesterMadDefinition.class, InterpTesterMadInstance.class );

		defClassToInsClassMap.put( IMixer3MadDefinition.class, IMixer3MadInstance.class );
		defClassToInsClassMap.put( IMixer8MadDefinition.class, IMixer8MadInstance.class );

		defClassToInsClassMap.put( DJEQMadDefinition.class, DJEQMadInstance.class );

		defClassToInsClassMap.put( MidSideMadDefinition.class, MidSideMadInstance.class );

		defClassToInsClassMap.put( SoundfilePlayer2MadDefinition.class, SoundfilePlayer2MadInstance.class );

		defClassToInsClassMap.put( Bessel4FilterMadDefinition.class, Bessel4FilterMadInstance.class );
	}

	@Override
	public Map<Class<? extends MadDefinition<?, ?>>, Class<? extends MadInstance<?, ?>>> provideDefClassToInsClassMap()
			throws ComponentConfigurationException
	{
		return defClassToInsClassMap;
	}

	@Override
	public MadCreationContext getCreationContext()
	{
		return creationContext;
	}

	public void setAdvancedComponentsFrontController(
			final AdvancedComponentsFrontController advancedComponentsFrontController )
	{
		this.advancedComponentsFrontController = advancedComponentsFrontController;
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( advancedComponentsFrontController == null )
		{
			final String msg = "BaseComponentsFactory has missing service dependencies. Check configuration";
			throw new ComponentConfigurationException( msg );
		}

		creationContext = new BaseComponentsCreationContext( advancedComponentsFrontController,
				advancedComponentsFrontController.getOscillatorFactory() );

		super.init();
	}
}
