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

package uk.co.modularaudio.util.audio.gui.spectrumgeneration;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.logdisplay.ampscale.AmpScaleComputer;
import uk.co.modularaudio.util.audio.logdisplay.ampscale.LogarithmicAmpScaleComputer;
import uk.co.modularaudio.util.audio.logdisplay.freqscale.FrequencyScaleComputer;
import uk.co.modularaudio.util.audio.logdisplay.freqscale.LogarithmicFreqScaleComputer;

public class SpectrumGenerator
{
	private static final int NUM_VALUES_TO_DRAW = 2048;
	private static Log log = LogFactory.getLog( SpectrumGenerator.class.getName() );
	
	protected FrequencyScaleComputer freqScaleComputer = null;
	protected AmpScaleComputer ampScaleComputer = null;
	
	public SpectrumGenerator()
	{
		freqScaleComputer = new LogarithmicFreqScaleComputer();
		ampScaleComputer = new LogarithmicAmpScaleComputer();
	}
	
	protected int getNumValuesToDraw()
	{
		return NUM_VALUES_TO_DRAW;
	}
	
	public BufferedImage generateSpectrum( float[] inputAmps, float[] inputPhases, int width, int height )
	{
		BufferedImage bi = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
		paintMagsIntoBufferedImage( inputAmps, inputPhases, bi, 0, 0, width, height );
		return bi;
	}

	public BufferedImage generateSpectrum(float[] inputSpectrumBuffer, int width, int height )
	{
		BufferedImage bi = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
		paintMagsIntoBufferedImage( inputSpectrumBuffer, bi );
		return bi;
	}
	
	protected void paintMagsIntoBufferedImage( float[] amps, float[] phases, BufferedImage outputImage, int ix, int iy, int iwidth, int iheight )
	{
		paintMagsIntoBufferedImageUsingColours(amps, phases, outputImage, ix, iy, iwidth, iheight, null, null);
	}
	
	protected void paintMagsIntoBufferedImageUsingColours( float[] amps, float[] phases, BufferedImage outputImage,
			int ix, int iy, int iwidth, int iheight, int[] binToPeak, Color[] peakColors)
	{
		boolean useColours = (peakColors != null );
		Graphics2D g2d = outputImage.createGraphics();
		
		int numValuesToDraw = getNumValuesToDraw();
		numValuesToDraw = (numValuesToDraw > amps.length ? amps.length : numValuesToDraw );

		// Natural log10 goes from 1 -> 10 for x=0 -> x=1
//		double scaleForIndex = numValuesToDraw / 9;
		
		for( int i = 0 ; i < iwidth ; i++ )
		{
			// Go right to left...
			int actualVal = (iwidth - 1) - i;
			// Convert index into log scaled bin index
			int logScaleBinIndex = freqScaleComputer.displayBinToSpectraBin( numValuesToDraw, iwidth, actualVal );
//			log.debug("So for a width of " + iwidth + " with " + numValuesToDraw + " we map " + actualVal + " to " + logScaleBinIndex );
			
			int pixelXStartIndex = i;

			float curVal = amps[ logScaleBinIndex ];
			
			float ampScaledVal = ampScaleComputer.scaleIt( curVal );
			
			Color c = null;
			if( useColours )
			{
				int binPeakIndex = binToPeak[ logScaleBinIndex ];
				if( binPeakIndex != -1 )
				{
					c = peakColors[ binPeakIndex ];
				}
				else
				{
					int rgb = colorForLogAbsOfValue( ampScaledVal );
					c = new Color( rgb );
				}
			}
			else
			{
				int rgb = colorForLogAbsOfValue( ampScaledVal );
				c = new Color( rgb );
			}
			g2d.setColor( c );
//			log.debug("For index " + i  + " with value " + val + " color is " + c.toString());
			
			int xIndex = pixelXStartIndex;
			int yStopIndex = (int)(ampScaledVal * iheight);
			
			if( yStopIndex > iheight )
			{
				yStopIndex = iheight;
			}
			int x1 = iwidth - xIndex;
			g2d.drawLine( x1, iheight, x1, iheight - yStopIndex );
//			log.debug("Drawing line at " + x1 );
		}	
	}
	
	public void paintMagsIntoBufferedImage( float[] buffer, BufferedImage outputImage )
	{
		float[] amps = new float[ buffer.length / 2 ];
		float[] phases = new float[ buffer.length / 2 ];
		for( int i = 0 ; i < buffer.length / 2 ; i++ )
		{
			amps[i] = buffer[i*2];
			phases[i] = buffer[ (i*2)+1 ];
		}
		paintMagsIntoBufferedImage( amps, phases, outputImage, 0, 0, outputImage.getWidth(), outputImage.getHeight());
	}
	
	private boolean useRed = true;
	
	public int colorForLogAbsOfValue(double normalisedValue)
	{
		double scaledValue = normalisedValue * 512.0;
		int greyVal = (int) (scaledValue);

		if (useRed)
		{
			if (greyVal < 0)
			{
				int greenVal = (64 << 8);
				return greenVal;
			}
			else if (greyVal <= 255)
			{
				return (greyVal << 16) | (64 << 8) | (greyVal);
			}
			else if (greyVal < 512)
			{
				greyVal = greyVal - 256;
				int redVal = 0xff;
				greyVal = 255 - greyVal;
				int retVal = (redVal << 16) | (64 << 8) | (greyVal);
				return retVal;
			}
			else
			{
				return 0xff0000;
			}
		}
		else
		{
			greyVal = Math.min(255, Math.max(0, greyVal));
			return (greyVal << 16) | (64 << 8) | (greyVal);
		}
	}
    
    public static void main( String[] args )
    {
    	SpectrumGenerator sg = new SpectrumGenerator();
    	log.debug("Testing colour mapping.");
    	double valueBefore = 8.639221802543624;
    	double pv1 = 8.544391166203468;
    	double pv2 = 8.544391166203468;
    	double valueAfter = 7.959099279507768;
    	
    	debugIt( sg, valueBefore );
    	debugIt( sg, pv1 );
    	debugIt( sg, pv2 );
    	debugIt( sg, valueAfter );
    }
    
    public static void debugIt( SpectrumGenerator sg, double logValue )
    {
    	int rgb = sg.colorForLogAbsOfValue( logValue );
    	Color c = new Color( rgb );
		log.debug("For value " + logValue + " got color " + c.toString() );
    }
}
