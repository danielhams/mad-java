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

package test.uk.co.modularaudio.util.audio.math;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.math.MixdownSliderDbToLevelComputer;
import uk.co.modularaudio.util.math.MathFormatter;

public class CheckDbToLevelBounds extends TestCase
{
	private static Log log = LogFactory.getLog( CheckDbToLevelBounds.class.getName() );

//	public void testBoundsOfConversion() throws Exception
//	{
//		float curAmp = 1.0f;
//
//		for( int i = 0 ; i < 10 ; i++ )
//		{
//			curAmp = 1.0f / (i+1);
//			float dbLevel = (float) AudioMath.levelToDb( curAmp );
//			log.debug("For amp " + MathFormatter.floatPrint( curAmp, 3 ) + " db level is " + MathFormatter.floatPrint( dbLevel, 3 ) );
//		}
//
//		log.debug("For amp +0.00001f db level is " + MathFormatter.floatPrint( (float)AudioMath.levelToDb( 0.00001f ) ) );
//
//		log.debug("And now the other way"  );
//
//		for( int i = 0 ; i < 10 ; i++ )
//		{
//			float dbLevel = -39.0f + ( 5f * i );
//			float amp = (float)AudioMath.dbToLevel( dbLevel );
//			log.debug("For db level " + MathFormatter.floatPrint( dbLevel, 3 ) + " amp is " + MathFormatter.floatPrint( amp, 3  ) );
//		}
//	}
//
//	public void testDbToLevelComputer() throws Exception
//	{
//		FirstDbToLevelComputer dbComputer = new FirstDbToLevelComputer( 1000 );
//
//		float dbForSliderHalfway = dbComputer.toDbFromNormalisedLevel( 0.5f );
//		log.debug("DBTL for slider halfway is " + MathFormatter.floatPrint( dbForSliderHalfway, 3 ) );
//
//		float amp = (float)AudioMath.dbToLevel( dbForSliderHalfway );
//		log.debug("DBTL Which is " + MathFormatter.floatPrint( amp, 3 ) + " as a amplification multiplier.");
//
//		float fiveDbAsNormalisedSliderLevel = dbComputer.toNormalisedSliderLevelFromDb( 5.0f );
//		log.debug("DBTL Five DB as normalised slider level is " + fiveDbAsNormalisedSliderLevel );
//
//		float dbForZero = dbComputer.toDbFromNormalisedLevel( 0.0f );
//		log.debug("DBTL DB for zero is " + dbForZero );
//		float zeroDbAmp = (float)AudioMath.dbToLevel( dbForZero );
//		log.debug("DBTL Which is " + MathFormatter.floatPrint( zeroDbAmp, 3 ) );
//	}

	public void testMixdownToLevelComputer() throws Exception
	{
		final MixdownSliderDbToLevelComputer dbComputer = new MixdownSliderDbToLevelComputer( 1000 );

		final float dbForSliderHalfway = dbComputer.toDbFromNormalisedLevel( 0.5f );
		log.debug("MDTOL DB for slider halfway is " + MathFormatter.fastFloatPrint( dbForSliderHalfway, 3, true ) );

		final float dbForSliderAllway = dbComputer.toDbFromNormalisedLevel( 1.0f );
		log.debug("MDTOL DB for slider all the way is " + MathFormatter.fastFloatPrint( dbForSliderAllway, 3, true ) );

		final float dbForSliderNoway = dbComputer.toDbFromNormalisedLevel( 0.0f );
		log.debug("MDTOL DB for slider no way is " + MathFormatter.fastFloatPrint( dbForSliderNoway, 3, true ) );

		final float dbForSliderPoint1 = dbComputer.toDbFromNormalisedLevel( 0.1f );
		log.debug("MDTOL DB for slider point1 is " + MathFormatter.fastFloatPrint( dbForSliderPoint1, 3, true ) );

		final float fiveDbAsNormalisedSliderLevel = dbComputer.toNormalisedSliderLevelFromDb( 5.0f );
		log.debug("MDTOL Five DB as normalised slider level is " + fiveDbAsNormalisedSliderLevel );

		final float amp = (float)AudioMath.dbToLevel( dbForSliderHalfway );
		log.debug("MDTOL Halfway slider db is " + MathFormatter.fastFloatPrint( amp, 3, true ) + " as a amplification multiplier.");

		final float dbForZero = dbComputer.toDbFromNormalisedLevel( 0.0f );
		log.debug("MDTOL DB for zero is " + dbForZero );
		final float zeroDbAmp = (float)AudioMath.dbToLevel( dbForZero );
		log.debug("MDTOL Which is " + MathFormatter.fastFloatPrint( zeroDbAmp, 3, true ) );

		final float dbForFull = dbComputer.toDbFromNormalisedLevel( 1.0f );
		log.debug("Db for full value is " + dbForFull );

		final float normaValForZero = dbComputer.toNormalisedSliderLevelFromDb( 0.0f );
		log.debug("MDTOL nvf0 = " + normaValForZero );

		final float normValFor50 = dbComputer.toNormalisedSliderLevelFromDb( -50.0f );
		log.debug("MDTOL nvf50 = " + normValFor50 );

		final float normValFor90 = dbComputer.toNormalisedSliderLevelFromDb( -90.0f );
		log.debug("MDTOL nvf90 = " + normValFor90 );

		final double thresAsDb = AudioMath.levelToDb( 0.037 );
		log.debug("0.037 as db is " + thresAsDb );
	}

}
