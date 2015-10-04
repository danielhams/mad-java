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

package uk.co.modularaudio.mads.base.controllertocv.ui;

import uk.co.modularaudio.util.audio.controlinterpolation.CDLowPass24Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.CDLowPass12Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.CDSpringAndDamperDouble24Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.CDSpringAndDamperDouble12Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.ControlValueInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.HalfHannWindowInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LinearInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LinearLowPass12Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LinearLowPass24Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LowPass12Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LowPass24Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.NoneInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.RecalculatingLinearInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.RecalculatingLinearLowPass12Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.RecalculatingLinearLowPass24Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperDouble24Interpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.SumOfRatiosInterpolator;

public enum InterpolationChoice
{
	NONE( "None", NoneInterpolator.class ),
	SUM_OF_RATIOS_FREE( "SORFree", SumOfRatiosInterpolator.class ),
	LINEAR( "Lin", LinearInterpolator.class ),
	RECALC_LINEAR( "RecLin", RecalculatingLinearInterpolator.class ),
	HALF_HANN_FREE( "HH", HalfHannWindowInterpolator.class ),
	SPRING_DAMPER( "SD", SpringAndDamperDouble24Interpolator.class ),
	LOW_PASS12( "LP12", LowPass12Interpolator.class ),
	LOW_PASS24( "LP24", LowPass24Interpolator.class ),
	CD_LOW_PASS( "CDLP", CDLowPass12Interpolator.class ),
	CD_LOW_PASS_24( "CDLP24", CDLowPass24Interpolator.class ),
	CD_SPRING_DAMPER( "CDSD", CDSpringAndDamperDouble12Interpolator.class ),
	CD_SPRING_DAMPER24( "CDSD24", CDSpringAndDamperDouble24Interpolator.class ),
	LINEAR_SC_LOW_PASS_12( "LinLP12", LinearLowPass12Interpolator.class ),
	LINEAR_SC_LOW_PASS_24( "LinLP24", LinearLowPass24Interpolator.class ),
	RECALC_LINEAR_SC_LOW_PASS_12( "RecLinLP12", RecalculatingLinearLowPass12Interpolator.class ),
	RECALC_LINEAR_SC_LOW_PASS_24( "RecLinLP24", RecalculatingLinearLowPass24Interpolator.class );

	private String label;
	private Class<? extends ControlValueInterpolator> iClass;

	private InterpolationChoice( final String label, final Class<? extends ControlValueInterpolator> iClass )
	{
		this.label = label;
		this.iClass = iClass;
	}

	public String getLabel()
	{
		return label;
	}

	public Class<? extends ControlValueInterpolator> getInterpolatorClass()
	{
		return iClass;
	}
}
