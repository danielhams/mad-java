package uk.co.modularaudio.mads.base.interptester.mu;

import uk.co.modularaudio.util.audio.controlinterpolation.CDLowPass24Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.CDSCLowPass24Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.CDSpringAndDamperDouble24Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.ControlValueInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.HalfHannWindowInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LinearInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LinearLowPass12Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LinearLowPass24Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.NoneInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.RecalculatingLinearInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.SumOfRatiosInterpolator;

public enum InterpolatorType
{
	NONE( "Raw Control", NoneInterpolator.class ),
	SOR( "Sum Of Ratio", SumOfRatiosInterpolator.class ),
	HH( "Half Hann", HalfHannWindowInterpolator.class ),
	LINEAR( "Linear Interpolation", LinearInterpolator.class ),
	RECALC_LINEAR( "Recalculating Linear Interpolation", RecalculatingLinearInterpolator.class ),
	LINLP12( "Linear And Low Pass 12", LinearLowPass12Interpolator.class ),
	CDLP24( "CD Low Pass 24", CDLowPass24Interpolator.class ),
	CDSCLP24( "CDSC Low Pass 24", CDSCLowPass24Interpolator.class ),
	CDSD24( "CD Spring Damper 24", CDSpringAndDamperDouble24Interpolator.class ),
	LINLP24( "Linear And Low Pass 24", LinearLowPass24Interpolator.class );

	private String channelPrefix;
	private Class<? extends ControlValueInterpolator> interpolatorClass;

	private InterpolatorType( final String channelPrefix,
			final Class<? extends ControlValueInterpolator> clazz )
	{
		this.channelPrefix = channelPrefix;
		this.interpolatorClass = clazz;
	}

	public String getChannelPrefix()
	{
		return channelPrefix;
	}

	public Class<? extends ControlValueInterpolator> getInterpolatorClass()
	{
		return interpolatorClass;
	}

}