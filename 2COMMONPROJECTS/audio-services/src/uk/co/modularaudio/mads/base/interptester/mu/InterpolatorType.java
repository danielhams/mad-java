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

package uk.co.modularaudio.mads.base.interptester.mu;

import uk.co.modularaudio.util.audio.controlinterpolation.CDLowPass12Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.CDLowPass24Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.ControlValueInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.HalfHannWindowInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LinearInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LinearLowPass12Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LinearLowPass24Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.NoneInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.RecalculatingLinearInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.RecalculatingLinearLowPass12Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.RecalculatingLinearLowPass24Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.SumOfRatiosInterpolator;

public enum InterpolatorType
{
	NONE( "Raw Control", NoneInterpolator.class ),
	SOR( "Sum Of Ratio", SumOfRatiosInterpolator.class ),
	HH( "Half Hann", HalfHannWindowInterpolator.class ),
	LINEAR( "Linear Interpolation", LinearInterpolator.class ),
	RECALC_LINEAR( "Recalculating Linear Interpolation", RecalculatingLinearInterpolator.class ),
	CDLP12( "CD Low Pass 12", CDLowPass12Interpolator.class ),
	CDLP24( "CD Low Pass 24", CDLowPass24Interpolator.class ),
	LINLP12( "Linear And Low Pass 12", LinearLowPass12Interpolator.class ),
	LINLP24( "Linear And Low Pass 24", LinearLowPass24Interpolator.class ),
	RECALC_LINLP12( "Recalculating Linear Low Pass 12", RecalculatingLinearLowPass12Interpolator.class ),
	RECALC_LINLP24( "Recalculating Linear Low Pass 24", RecalculatingLinearLowPass24Interpolator.class );

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
