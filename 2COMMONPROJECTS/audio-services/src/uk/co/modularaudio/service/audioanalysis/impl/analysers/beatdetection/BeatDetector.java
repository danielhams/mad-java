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

package uk.co.modularaudio.service.audioanalysis.impl.analysers.beatdetection;

import uk.co.modularaudio.util.audio.dsp.ButterworthFilter;
import uk.co.modularaudio.util.audio.dsp.ButterworthFilterRT;
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
import uk.co.modularaudio.util.audio.fft.FftUtils;

public class BeatDetector
{
//	private static Log log = LogFactory.getLog( BeatDetector.class.getName() );
	
	public void detect(BeatDetectionRT rt, float[] input, float[] output)
	{
		// First low pass filter the input
		ButterworthFilterRT butterWorthRt = rt.butterworthRt;
		ButterworthFilter.filter( butterWorthRt, input, 0, input.length, 60.0f, 10.0f, FrequencyFilterMode.LP, 44100);
		
		int step = rt.step;
		int laglen = rt.rwv.length;
		int winlen = rt.winlen;
		int maxindex = 0;
		int numelem = 4;

		float phase;
		float beat;
		float bp;
		
		int kmax;
		
		System.arraycopy( input,0, rt.dfrev, 0, input.length );
		FftUtils.weight( rt.dfrev, rt.dfwv );
		FftUtils.rev( rt.dfrev );

		FftUtils.autocorr(input, rt.acf);

		if (rt.timesig == 0)
		{
			numelem = 4;
		}
		else
		{
			numelem = rt.timesig;
		}
		
		FftUtils.zeros( rt.acfout );

		for (int i = 1; i < laglen - 1; i++)
		{
			for (int a = 1; a <= numelem; a++)
			{
				for (int b = (1 - a); b < a; b++)
				{
					float val1 = rt.acf[ a * (i + 1) + b - 1];
					float tstval = val1 * 1.0f / (2.0f*a-1.0f);
					rt.acfout[i] +=  tstval;
//					log.debug("tstval=" + tstval + " and acfout=" + rt.acfout[i]);
				}
			}
		}
		
		FftUtils.weight( rt.acfout, rt.rwv );

		maxindex = FftUtils.maxValuePos( rt.acfout );
		rt.rp = (maxindex == 0 ? 1 : FftUtils.quadint( rt.acfout, maxindex) );
		rt.rp = (maxindex == (rt.acfout.length - 1) ? rt.rayparam : maxindex);

		checkstate(rt);

		bp = rt.bp;
		
		kmax = (int)Math.floor(winlen / bp);

		FftUtils.zeros( rt.phout );

		for (int i = 0; i < bp; i++)
		{
			for (int k = 0; k < kmax; k++)
			{
				int dfrevIndex = i + (int)Math.floor(bp * k);
				rt.phout[i] += rt.dfrev[dfrevIndex];
			}
		}
		FftUtils.weight( rt.phout, rt.phwv );

		maxindex = FftUtils.maxValuePos(rt.phout);
		if (maxindex >= winlen - 1)
		{
			phase = step - rt.lastbeat;
		}
		else
		{
			phase = FftUtils.quadint( rt.phout, maxindex );
		}
		phase += 1.0f;
		
		FftUtils.zeros( output );

		int i = 1;
		beat = bp - phase;
		
		if( ( step - rt.lastbeat - phase ) < -0.40 * bp )
		{
			beat += bp;
		}
		
		while( beat + bp < 0 )
		{
			beat += bp;
		}

		if (beat >= 0)
		{
			output[i] = beat;
			i++;
		}

		while (beat + bp <= step)
		{
			beat += bp;
			output[i] = beat;
			i++;
		}

		rt.lastbeat = (int)beat;
		output[0] = i;
	}

	private void checkstate(BeatDetectionRT rt)
	{
		int flagconst = 0;
		int counter = rt.counter;
		int flagstep = rt.flagstep;
		float gp = rt.gp;
		float bp = rt.bp;
		float rp = rt.rp;
		float rp1 = rt.rp1;
		float rp2 = rt.rp2;
		int laglen = rt.rwv.length;
		int acflen = rt.acf.length;
		int step = rt.step;
		float[] acf = rt.acf;
		float[] acfout = rt.acfout;

		if (gp != 0.0f)
		{
			FftUtils.zeros( acfout );
			for (int i = 1; i < laglen - 1; i++)
			{
				for (int a = 1; a <= rt.timesig; a++)
				{
					for (int b = (1 - a); b < a; b++)
					{
						acfout[i] += acf[a * (i + 1) + b - 1];
					}
				}
			}
			FftUtils.weight( acfout, rt.gwv );
			gp = FftUtils.quadint( acfout, FftUtils.maxValuePos( acfout ) );
		}
		else
		{
			gp = 0.0f;
		}

		if (counter == 0)
		{
			if (Math.abs(gp - rp) > 2.0 * rt.g_var)
			{
				// Detected a beat.
				flagstep = 1;
				counter = 3;
			}
			else
			{
				// Seeking a match
				flagstep = 0;
			}
		}

		if (counter == 1 && flagstep == 1)
		{
			if (Math.abs(2.0f * rp - rp1 - rp2) < rt.g_var)
			{
				flagconst = 1;
				counter = 0;
			}
			else
			{
				// First beat detected - waiting for second to begin
				flagconst = 0;
				counter = 2;
			}
		}
		else if (counter > 0)
		{
			counter--;
		}
		
		rp2 = rp1;
		rp1 = rp;

		if (flagconst != 0)
		{
			gp = rp;
			rt.timesig = gettimesig(acf, acflen, gp);
			for (int j = 0; j < laglen; j++)
			{
				// gwv[j] = EXP(-.5* SQR((smpl_t)(j+1.-gp)) / SQR(bt->g_var) );
				float t1sq = (j + 1.0f - gp) * (j + 1.0f - gp);
				float gvarsq = rt.g_var * rt.g_var;
				rt.gwv[j] = (float) Math.exp(-0.5 * t1sq / gvarsq);
			}
			flagconst = 0;
			bp = gp;
			FftUtils.ones( rt.phwv );
		}
		else if (rt.timesig != 0 )
		{
			bp = gp;
			if (step > rt.lastbeat)
			{
				for (int j = 0; j < 2 * laglen; j++)
				{
					float t1sq = 1.0f + j - step + rt.lastbeat;
					t1sq = t1sq * t1sq;
					rt.phwv[j] = (float) Math.exp(-0.5 * t1sq / (bp / 8.0));
				}
			}
			else
			{
				FftUtils.ones( rt.phwv );
			}
		}
		else
		{
			// Still seeking a first match
			bp = rp;
			FftUtils.ones( rt.phwv );
		}

		while (bp < 25)
		{
//			log.debug("Doubling bp from " + bp );
			if( bp == 0.0f )
			{
				bp = 1;
			}
			bp *= 2;
		}

		rt.counter = counter;
		rt.flagstep = flagstep;
		rt.gp = gp;
		rt.bp = bp;
		rt.rp1 = rp1;
		rt.rp2 = rp2;
	}

	private int gettimesig(float[] acf, int acflen, float gp)
	{
//		return 4;
		/**/
		float three_energy = 0.0f;
		float four_energy = 0.0f;
//		int gp = ( inGp < 0 ? -inGp : inGp );

		if (acflen < 6 * gp + 2)
		{
			for (int k = -2; k < 2; k++)
			{
				int threeIndex = (int)(3 * gp + k);
				int fourIndex = (int)(4 * gp + k);
				three_energy += acf[ threeIndex ];
				four_energy += acf[ fourIndex ];
			}
		}
		else
		{
			for (int k = -2; k < 2; k++)
			{
				int twoIndex = (int)(2 * gp + k);
				int threeIndex = (int)(3 * gp + k);
				int fourIndex = (int)(4 * gp + k);
				int sixIndex = (int)(6 * gp + k);
				if( threeIndex > 0 && sixIndex > 0 )
				{
					three_energy += acf[threeIndex] + acf[sixIndex];
				}
				if( fourIndex > 0 && twoIndex > 0 )
				{
					four_energy += acf[fourIndex] + acf[twoIndex];
				}
			}
		}
		// return (three_energy > four_energy) ? 3 : 4;
		return (three_energy > four_energy ? 3 : 4);
		/**/
	}
}
