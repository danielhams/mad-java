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

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import uk.co.modularaudio.mads.base.audioanalyser.mu.AudioAnalyserMadDefinition;
import uk.co.modularaudio.mads.base.audioanalyser.ui.AudioAnalyserMadUiDefinition;
import uk.co.modularaudio.mads.base.audiocvconverter.mu.AudioCvConverterMadDefinition;
import uk.co.modularaudio.mads.base.audiocvconverter.ui.AudioCvConverterMadUiDefinition;
import uk.co.modularaudio.mads.base.bandlimitedoscillator.mu.BandLimitedOscillatorMadDefinition;
import uk.co.modularaudio.mads.base.bandlimitedoscillator.ui.BandLimitedOscillatorMadUiDefinition;
import uk.co.modularaudio.mads.base.bessel4.mu.Bessel4FilterMadDefinition;
import uk.co.modularaudio.mads.base.bessel4.ui.Bessel4FilterMadUiDefinition;
import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadDefinition;
import uk.co.modularaudio.mads.base.controllertocv.ui.ControllerToCvMadUiDefinition;
import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderMadDefinition;
import uk.co.modularaudio.mads.base.crossfader.ui.CrossFaderMadUiDefinition;
import uk.co.modularaudio.mads.base.cvalinear.mu.LinearCVAMadDefinition;
import uk.co.modularaudio.mads.base.cvalinear.ui.LinearCVAMadUiDefinition;
import uk.co.modularaudio.mads.base.cvsurface.mu.CvSurfaceMadDefinition;
import uk.co.modularaudio.mads.base.cvsurface.ui.CvSurfaceMadUiDefinition;
import uk.co.modularaudio.mads.base.dctrap.mu.DCTrapMadDefinition;
import uk.co.modularaudio.mads.base.dctrap.ui.DCTrapMadUiDefinition;
import uk.co.modularaudio.mads.base.djeq.mu.DJEQMadDefinition;
import uk.co.modularaudio.mads.base.djeq.ui.DJEQMadUiDefinition;
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeMadDefinition;
import uk.co.modularaudio.mads.base.envelope.ui.EnvelopeMadUiDefinition;
import uk.co.modularaudio.mads.base.feedbackdelay.mu.FeedbackDelayMadDefinition;
import uk.co.modularaudio.mads.base.feedbackdelay.ui.FeedbackDelayMadUiDefinition;
import uk.co.modularaudio.mads.base.flipflop.mu.FlipFlopMadDefinition;
import uk.co.modularaudio.mads.base.flipflop.ui.FlipFlopMadUiDefinition;
import uk.co.modularaudio.mads.base.foldbackdistortion.mu.FoldbackDistortionMadDefinition;
import uk.co.modularaudio.mads.base.foldbackdistortion.ui.FoldbackDistortionMadUiDefinition;
import uk.co.modularaudio.mads.base.frequencyfilter.mu.FrequencyFilterMadDefinition;
import uk.co.modularaudio.mads.base.frequencyfilter.ui.FrequencyFilterMadUiDefinition;
import uk.co.modularaudio.mads.base.imixer3.mu.IMixer3MadDefinition;
import uk.co.modularaudio.mads.base.imixer3.ui.IMixer3MadUiDefinition;
import uk.co.modularaudio.mads.base.imixer8.mu.IMixer8MadDefinition;
import uk.co.modularaudio.mads.base.imixer8.ui.IMixer8MadUiDefinition;
import uk.co.modularaudio.mads.base.interptester.mu.InterpTesterMadDefinition;
import uk.co.modularaudio.mads.base.interptester.ui.InterpTesterMadUiDefinition;
import uk.co.modularaudio.mads.base.inverter.mu.InverterMadDefinition;
import uk.co.modularaudio.mads.base.inverter.ui.InverterMadUiDefinition;
import uk.co.modularaudio.mads.base.limiter.mu.LimiterMadDefinition;
import uk.co.modularaudio.mads.base.limiter.ui.LimiterMadUiDefinition;
import uk.co.modularaudio.mads.base.midside.mu.MidSideMadDefinition;
import uk.co.modularaudio.mads.base.midside.ui.MidSideMadUiDefinition;
import uk.co.modularaudio.mads.base.mono_compressor.mu.MonoCompressorMadDefinition;
import uk.co.modularaudio.mads.base.mono_compressor.ui.MonoCompressorMadUiDefinition;
import uk.co.modularaudio.mads.base.moogfilter.mu.MoogFilterMadDefinition;
import uk.co.modularaudio.mads.base.moogfilter.ui.MoogFilterMadUiDefinition;
import uk.co.modularaudio.mads.base.ms20filter.mu.Ms20FilterMadDefinition;
import uk.co.modularaudio.mads.base.ms20filter.ui.Ms20FilterMadUiDefinition;
import uk.co.modularaudio.mads.base.notedebug.mu.NoteDebugMadDefinition;
import uk.co.modularaudio.mads.base.notedebug.ui.NoteDebugMadUiDefinition;
import uk.co.modularaudio.mads.base.notemultiplexer.mu.NoteMultiplexerMadDefinition;
import uk.co.modularaudio.mads.base.notemultiplexer.ui.NoteMultiplexerMadUiDefinition;
import uk.co.modularaudio.mads.base.notetocv.mu.NoteToCvMadDefinition;
import uk.co.modularaudio.mads.base.notetocv.ui.NoteToCvMadUiDefinition;
import uk.co.modularaudio.mads.base.oscillator.mu.OscillatorMadDefinition;
import uk.co.modularaudio.mads.base.oscillator.ui.OscillatorMadUiDefinition;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadDefinition;
import uk.co.modularaudio.mads.base.oscilloscope.ui.OscilloscopeMadUiDefinition;
import uk.co.modularaudio.mads.base.pattern_sequencer.mu.PatternSequencerMadDefinition;
import uk.co.modularaudio.mads.base.pattern_sequencer.ui.PatternSequencerMadUiDefinition;
import uk.co.modularaudio.mads.base.prng.mu.PrngMadDefinition;
import uk.co.modularaudio.mads.base.prng.ui.PrngMadUiDefinition;
import uk.co.modularaudio.mads.base.rbjfilter.mu.RBJFilterMadDefinition;
import uk.co.modularaudio.mads.base.rbjfilter.ui.RBJFilterMadUiDefinition;
import uk.co.modularaudio.mads.base.sampleandhold.mu.SampleAndHoldMadDefinition;
import uk.co.modularaudio.mads.base.sampleandhold.ui.SampleAndHoldMadUiDefinition;
import uk.co.modularaudio.mads.base.sampleplayer.mu.SingleSamplePlayerMadDefinition;
import uk.co.modularaudio.mads.base.sampleplayer.ui.SingleSamplePlayerMadUiDefinition;
import uk.co.modularaudio.mads.base.scaleandoffset.mu.ScaleAndOffsetMadDefinition;
import uk.co.modularaudio.mads.base.scaleandoffset.ui.ScaleAndOffsetMadUiDefinition;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player.ui.SoundfilePlayerMadUiDefinition;
import uk.co.modularaudio.mads.base.soundfile_player2.mu.SoundfilePlayer2MadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player2.ui.SoundfilePlayer2MadUiDefinition;
import uk.co.modularaudio.mads.base.specamplarge.mu.SpecAmpLargeMadDefinition;
import uk.co.modularaudio.mads.base.specamplarge.ui.SpecAmpLargeMadUiDefinition;
import uk.co.modularaudio.mads.base.specampsmall.mu.SpecAmpSmallMadDefinition;
import uk.co.modularaudio.mads.base.specampsmall.ui.SpecAmpSmallMadUiDefinition;
import uk.co.modularaudio.mads.base.spectralroll.mu.SpectralRollMadDefinition;
import uk.co.modularaudio.mads.base.spectralroll.ui.SpectralRollMadUiDefinition;
import uk.co.modularaudio.mads.base.staticvalue.mu.StaticValueMadDefinition;
import uk.co.modularaudio.mads.base.staticvalue.ui.StaticValueMadUiDefinition;
import uk.co.modularaudio.mads.base.stereo_compressor.mu.StereoCompressorMadDefinition;
import uk.co.modularaudio.mads.base.stereo_compressor.ui.StereoCompressorMadUiDefinition;
import uk.co.modularaudio.mads.base.stereo_gate.mu.StereoGateMadDefinition;
import uk.co.modularaudio.mads.base.stereo_gate.ui.StereoGateMadUiDefinition;
import uk.co.modularaudio.mads.base.supersawmodule.mu.SuperSawModuleMadDefinition;
import uk.co.modularaudio.mads.base.supersawmodule.ui.SuperSawModuleMadUiDefinition;
import uk.co.modularaudio.mads.base.waveroller.mu.WaveRollerMadDefinition;
import uk.co.modularaudio.mads.base.waveroller.ui.WaveRollerMadUiDefinition;
import uk.co.modularaudio.mads.base.xrunner.mu.XRunnerMadDefinition;
import uk.co.modularaudio.mads.base.xrunner.ui.XRunnerMadUiDefinition;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.service.madcomponentui.AbstractMadComponentUiFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;

public class BaseComponentsUiFactory extends AbstractMadComponentUiFactory
{
	private BaseComponentsFactory baseComponentsFactory = null;

	@SuppressWarnings("rawtypes")
	private final Map<Class, Class> classToUiDefinition = new HashMap<Class, Class>();

	public BaseComponentsUiFactory()
	{
		// Definitions to UiDefinitions
		classToUiDefinition.put( ScaleAndOffsetMadDefinition.class, ScaleAndOffsetMadUiDefinition.class );
		classToUiDefinition.put( StaticValueMadDefinition.class, StaticValueMadUiDefinition.class );
		classToUiDefinition.put( LimiterMadDefinition.class, LimiterMadUiDefinition.class );
		classToUiDefinition.put( OscilloscopeMadDefinition.class, OscilloscopeMadUiDefinition.class );
		classToUiDefinition.put( CrossFaderMadDefinition.class, CrossFaderMadUiDefinition.class );
		classToUiDefinition.put( FrequencyFilterMadDefinition.class, FrequencyFilterMadUiDefinition.class );
		classToUiDefinition.put( Ms20FilterMadDefinition.class, Ms20FilterMadUiDefinition.class );
		classToUiDefinition.put( FoldbackDistortionMadDefinition.class, FoldbackDistortionMadUiDefinition.class );

		classToUiDefinition.put( FeedbackDelayMadDefinition.class, FeedbackDelayMadUiDefinition.class );
		classToUiDefinition.put( SpecAmpSmallMadDefinition.class, SpecAmpSmallMadUiDefinition.class );
		classToUiDefinition.put( SpecAmpLargeMadDefinition.class, SpecAmpLargeMadUiDefinition.class );
		classToUiDefinition.put( SpectralRollMadDefinition.class, SpectralRollMadUiDefinition.class );
		classToUiDefinition.put( InverterMadDefinition.class, InverterMadUiDefinition.class );
		classToUiDefinition.put( CvSurfaceMadDefinition.class, CvSurfaceMadUiDefinition.class );
		classToUiDefinition.put( PatternSequencerMadDefinition.class, PatternSequencerMadUiDefinition.class );
		classToUiDefinition.put( NoteMultiplexerMadDefinition.class, NoteMultiplexerMadUiDefinition.class );
		classToUiDefinition.put( AudioCvConverterMadDefinition.class, AudioCvConverterMadUiDefinition.class );

		classToUiDefinition.put( NoteToCvMadDefinition.class, NoteToCvMadUiDefinition.class );

		classToUiDefinition.put( ControllerToCvMadDefinition.class, ControllerToCvMadUiDefinition.class );

		classToUiDefinition.put( LinearCVAMadDefinition.class, LinearCVAMadUiDefinition.class );

		classToUiDefinition.put( PrngMadDefinition.class, PrngMadUiDefinition.class );

		classToUiDefinition.put( DCTrapMadDefinition.class, DCTrapMadUiDefinition.class );

		classToUiDefinition.put( SampleAndHoldMadDefinition.class, SampleAndHoldMadUiDefinition.class );

		classToUiDefinition.put( FlipFlopMadDefinition.class, FlipFlopMadUiDefinition.class );

		classToUiDefinition.put( StereoGateMadDefinition.class, StereoGateMadUiDefinition.class );
		classToUiDefinition.put( MonoCompressorMadDefinition.class, MonoCompressorMadUiDefinition.class );
		classToUiDefinition.put( StereoCompressorMadDefinition.class, StereoCompressorMadUiDefinition.class );

		classToUiDefinition.put( SingleSamplePlayerMadDefinition.class, SingleSamplePlayerMadUiDefinition.class );

		classToUiDefinition.put( BandLimitedOscillatorMadDefinition.class, BandLimitedOscillatorMadUiDefinition.class );

		classToUiDefinition.put( OscillatorMadDefinition.class, OscillatorMadUiDefinition.class );

		classToUiDefinition.put( EnvelopeMadDefinition.class, EnvelopeMadUiDefinition.class );

		classToUiDefinition.put( SuperSawModuleMadDefinition.class, SuperSawModuleMadUiDefinition.class );

		classToUiDefinition.put( WaveRollerMadDefinition.class, WaveRollerMadUiDefinition.class );

		classToUiDefinition.put( SoundfilePlayerMadDefinition.class, SoundfilePlayerMadUiDefinition.class );

		classToUiDefinition.put( RBJFilterMadDefinition.class, RBJFilterMadUiDefinition.class );

		classToUiDefinition.put( MoogFilterMadDefinition.class, MoogFilterMadUiDefinition.class );

		classToUiDefinition.put( NoteDebugMadDefinition.class, NoteDebugMadUiDefinition.class );

		classToUiDefinition.put( AudioAnalyserMadDefinition.class, AudioAnalyserMadUiDefinition.class );

		classToUiDefinition.put( XRunnerMadDefinition.class, XRunnerMadUiDefinition.class );

		classToUiDefinition.put( InterpTesterMadDefinition.class, InterpTesterMadUiDefinition.class );

		classToUiDefinition.put( IMixer3MadDefinition.class, IMixer3MadUiDefinition.class );
		classToUiDefinition.put( IMixer8MadDefinition.class, IMixer8MadUiDefinition.class );

		classToUiDefinition.put( DJEQMadDefinition.class, DJEQMadUiDefinition.class );

		classToUiDefinition.put( MidSideMadDefinition.class, MidSideMadUiDefinition.class );

		classToUiDefinition.put( SoundfilePlayer2MadDefinition.class, SoundfilePlayer2MadUiDefinition.class );

		classToUiDefinition.put( Bessel4FilterMadDefinition.class, Bessel4FilterMadUiDefinition.class );
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void setupTypeToDefinitionClasses() throws DatastoreException
	{
		try
		{
			final Collection<MadDefinition<?,?>> auds = baseComponentsFactory.listDefinitions();
			for( final MadDefinition<?,?> aud : auds )
			{
				final Class classToInstantiate = classToUiDefinition.get( aud.getClass() );
				final Class[] constructorParamTypes = new Class[] {
						BufferedImageAllocator.class,
						aud.getClass(),
						ComponentImageFactory.class,
						String.class };
				final Object[] constructorParams = new Object[] {
						bufferedImageAllocationService,
						aud,
						componentImageFactory,
						imageRoot };
				final Constructor c = classToInstantiate.getConstructor( constructorParamTypes );
				final Object newInstance = c.newInstance( constructorParams );
				final MadUiDefinition instanceAsUiDefinition = (MadUiDefinition)newInstance;

				componentDefinitionToUiDefinitionMap.put( aud, instanceAsUiDefinition );
			}
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught setting up UI definitions: " + e.toString();
			throw new DatastoreException( msg, e );
		}
	}

	public void setBaseComponentsFactory( final BaseComponentsFactory baseComponentsFactory )
	{
		this.baseComponentsFactory = baseComponentsFactory;
	}

}
