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

package uk.co.modularaudio.componentdesigner.generators;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import uk.co.modularaudio.componentdesigner.ComponentDesigner;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveShape;
import uk.co.modularaudio.util.audio.oscillatortable.StandardBandLimitedWaveTables;
import uk.co.modularaudio.util.audio.oscillatortable.StandardWaveTables;

public class ComponentDesignerSupportFileGenerator
{
	private static Log log = LogFactory.getLog( ComponentDesignerSupportFileGenerator.class.getName() );

	private final ComponentDesigner cd;

	private final String outputDirectory;
	private final String inputImagesDirectory;

	public ComponentDesignerSupportFileGenerator( final String outputDirectory, final String inputImagesDirectory )
			throws Exception
	{
		cd = new ComponentDesigner();

		this.outputDirectory = outputDirectory;
		this.inputImagesDirectory = inputImagesDirectory;
	}

	public void init() throws Exception
	{
		cd.setupApplicationContext( ComponentDesigner.CDRELEASEGENERATOR_PROPERTIES, null, null, true, true );
	}

	public void destroy() throws Exception
	{
		cd.signalPreExit();
		cd.signalPostExit();
	}

	public void generateFiles() throws Exception
	{
		generateBlw();
		copyComponentImages();
	}

//	public void initialiseThingsNeedingComponentGraph() throws Exception
//	{
//	}

	public static void main( final String[] args ) throws Exception
	{
		if( args.length != 2 )
		{
			throw new Exception( "Missing required directories: outputDir inputImagesDir" );
		}

		final LoggerContext ctx = (LoggerContext) LogManager.getContext( false );
		final Configuration config = ctx.getConfiguration();
		final LoggerConfig loggerConfig = config.getLoggerConfig( LogManager.ROOT_LOGGER_NAME );
		loggerConfig.setLevel( Level.INFO );
		ctx.updateLoggers();

		final ComponentDesignerSupportFileGenerator sfg = new ComponentDesignerSupportFileGenerator( args[0], args[1] );
		sfg.generateFiles();
//		sfg.init();
//		sfg.initialiseThingsNeedingComponentGraph();
//		sfg.destroy();
	}

	private void generateBlw() throws Exception
	{
		log.info( "Check if wave tables need to be generated..." );

		final String waveTablesOutputDirectory = outputDirectory + File.separatorChar + "wavetables";

		final StandardWaveTables swt = StandardWaveTables.getInstance( waveTablesOutputDirectory );
		final StandardBandLimitedWaveTables sblwt = StandardBandLimitedWaveTables
				.getInstance( waveTablesOutputDirectory );

		for( final OscillatorWaveShape shape : OscillatorWaveShape.values() )
		{
			swt.getTableForShape( shape );
			// No band limited tables for sine - it has no harmonics
			if( shape != OscillatorWaveShape.SINE )
			{
				sblwt.getMapForShape( shape );
			}
		}
	}

	private void copyComponentImages() throws Exception
	{
		log.info( "Checking for new component images..." );
		final File inputImageDir = new File( inputImagesDirectory );

		final File outputImageDir = new File( outputDirectory + File.separatorChar + "images" );
		outputImageDir.mkdirs();

		final Path inputImagesPath = inputImageDir.toPath();

		final DirectoryStream<Path> stream = Files.newDirectoryStream( inputImagesPath, "*.png" );

		for( final Path entry : stream )
		{
			final String outputName = entry.toFile().getName();
			final String outputPath = outputImageDir.getAbsolutePath() + File.separatorChar + outputName;
			// copyFile( entry.toFile(), new File(outputPath) );
			final File outputImageFile = new File( outputPath );
			final long inputFileLastMod = entry.toFile().lastModified();
			long outputFileLastMod = inputFileLastMod;
			if( outputImageFile.exists() )
			{
				outputFileLastMod = outputImageFile.lastModified();
			}

			if( !outputImageFile.exists() || outputFileLastMod < inputFileLastMod )
			{
				if( outputImageFile.exists() )
				{
					outputImageFile.delete();
				}
				log.info( "Copying image file: " + outputName );
				Files.copy( entry, new File( outputPath ).toPath() );
			}
		}
	}
}
