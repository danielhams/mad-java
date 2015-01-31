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

package uk.co.modularaudio.util.audio.lookuptable.valuemapping;



public class StandardValueMappingWaveTables
{
	private static LinearValueMappingWaveTable linearAttackMappingWaveTable = null;
	private static LogValueMappingWaveTable logAttackMappingWaveTable = null;
	private static LogFreqValueMappingWaveTable logFreqAttackMappingWaveTable = null;
	private static ExpValueMappingWaveTable expAttackMappingWaveTable = null;
	private static ExpFreqValueMappingWaveTable expFreqAttackMappingWaveTable = null;

	private static LinearValueMappingWaveTable linearDecayMappingWaveTable = null;
	private static LogValueMappingWaveTable logDecayMappingWaveTable = null;
	private static LogFreqValueMappingWaveTable logFreqDecayMappingWaveTable = null;
	private static ExpValueMappingWaveTable expDecayMappingWaveTable = null;
	private static ExpFreqValueMappingWaveTable expFreqDecayMappingWaveTable = null;

	static
	{
		linearAttackMappingWaveTable = new LinearValueMappingWaveTable( ValueMappingWaveTableDefines.MAP_BUFFER_LENGTH, false );
		logAttackMappingWaveTable = new LogValueMappingWaveTable( ValueMappingWaveTableDefines.MAP_BUFFER_LENGTH, false );
		logFreqAttackMappingWaveTable = new LogFreqValueMappingWaveTable( ValueMappingWaveTableDefines.MAP_BUFFER_LENGTH, false );
		expAttackMappingWaveTable = new ExpValueMappingWaveTable( ValueMappingWaveTableDefines.MAP_BUFFER_LENGTH, false );
		expFreqAttackMappingWaveTable = new ExpFreqValueMappingWaveTable( ValueMappingWaveTableDefines.MAP_BUFFER_LENGTH, false );

		linearDecayMappingWaveTable = new LinearValueMappingWaveTable( ValueMappingWaveTableDefines.MAP_BUFFER_LENGTH, true );
		logDecayMappingWaveTable = new LogValueMappingWaveTable( ValueMappingWaveTableDefines.MAP_BUFFER_LENGTH, true );
		logFreqDecayMappingWaveTable = new LogFreqValueMappingWaveTable( ValueMappingWaveTableDefines.MAP_BUFFER_LENGTH, true );
		expDecayMappingWaveTable = new ExpValueMappingWaveTable( ValueMappingWaveTableDefines.MAP_BUFFER_LENGTH, true );
		expFreqDecayMappingWaveTable = new ExpFreqValueMappingWaveTable( ValueMappingWaveTableDefines.MAP_BUFFER_LENGTH, true );
	}
	
	public static ValueMappingWaveTable getLinearAttackMappingWaveTable()
	{
		return linearAttackMappingWaveTable;
	}

	public static ValueMappingWaveTable getLogAttackMappingWaveTable()
	{
		return logAttackMappingWaveTable;
	}
	
	public static ValueMappingWaveTable getLogFreqAttackMappingWaveTable()
	{
		return logFreqAttackMappingWaveTable;
	}
	
	public static ValueMappingWaveTable getExpAttackMappingWaveTable()
	{
		return expAttackMappingWaveTable;
	}

	public static ValueMappingWaveTable getExpFreqAttackMappingWaveTable()
	{
		return expFreqAttackMappingWaveTable;
	}

	public static LinearValueMappingWaveTable getLinearDecayMappingWaveTable()
	{
		return linearDecayMappingWaveTable;
	}

	public static LogValueMappingWaveTable getLogDecayMappingWaveTable()
	{
		return logDecayMappingWaveTable;
	}

	public static LogFreqValueMappingWaveTable getLogFreqDecayMappingWaveTable()
	{
		return logFreqDecayMappingWaveTable;
	}

	public static ExpValueMappingWaveTable getExpDecayMappingWaveTable()
	{
		return expDecayMappingWaveTable;
	}

	public static ExpFreqValueMappingWaveTable getExpFreqDecayMappingWaveTable()
	{
		return expFreqDecayMappingWaveTable;
	}
}
