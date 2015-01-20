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

package uk.co.modularaudio.util.hibernate.generator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.context.support.GenericApplicationContext;

import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.hibernate.common.HibernatePersistedBeanDefinition;
import uk.co.modularaudio.util.hibernate.common.ComponentWithHibernatePersistence;
import uk.co.modularaudio.util.hibernate.common.TableNamePrefixer;
import uk.co.modularaudio.util.io.FileUtils;
import uk.co.modularaudio.util.spring.SpringComponentHelper;

public class GeneratorHelper
{
	/**
	 * Generate the DDL according to the list of hibernate mapping files for one component implementing IsHibernatePersistedComponent interface
	 * @param sourceDirectory
	 * @param destinationDirectory
	 * @param outputFileName
	 */
	public void generateDDL(ComponentWithHibernatePersistence component, String dialectName, String destinationDirectory,
			String outputFileName) throws IOException
	{
		Configuration configuration = new Configuration();
		List<HibernatePersistedBeanDefinition> listHBM = component.listHibernatePersistedBeanDefinitions();
		for (HibernatePersistedBeanDefinition hbmDefinition : listHBM)
		{
			String originalHbmResourceName = hbmDefinition.getHbmResourceName();
			
			InputStream hbmInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( originalHbmResourceName );
			
			String originalHbmString = FileUtils.basicReadInputStreamUTF8( hbmInputStream );
			
			String newHbmString = TableNamePrefixer.prefixTableNames( originalHbmString, hbmDefinition.getPersistedBeanTablePrefix());

			configuration.addXML( newHbmString );

		}
		configureAndGenerate(dialectName, destinationDirectory, outputFileName, configuration);
	}

	/**
	 * Method to configure the only properties needed with the hbm files and generate the DDL
	 * @param dialectName
	 * @param destinationDirectory
	 * @param outputFileName
	 * @param configuration
	 */
	private void configureAndGenerate(String dialectName, String destinationDirectory, String outputFileName,
			Configuration configuration)
	{
		Properties dialect = new Properties();
		dialect.setProperty("hibernate.dialect", dialectName);
		configuration.addProperties(dialect);

		SchemaExport se = new SchemaExport(configuration);
		se.setOutputFile(destinationDirectory + outputFileName);
		se.setDelimiter(";\n");
		se.create(true, false);
	}

	/**
	 * Generate the DDL according to the beans.xml file searching for components implementing the IsHibernatePersistedComponent interface
	 * @param sourceDirectory
	 * @param destinationDirectory
	 * @param outputFileName
	 */
	public void generateFromBeansDDL(String beansFilename, String configurationFilename, String dialectName, String destinationDirectory,
			String outputFileName)
		throws DatastoreException, RecordNotFoundException, IOException
	{
		Configuration configuration = new Configuration();
		SpringComponentHelper sch = new SpringComponentHelper();
		GenericApplicationContext appContext = sch.makeAppContext( beansFilename, configurationFilename);

		Map<String,ComponentWithHibernatePersistence> components = appContext.getBeansOfType(ComponentWithHibernatePersistence.class);

		for (Iterator<ComponentWithHibernatePersistence> iter = components.values().iterator(); iter.hasNext();)
		{
			ComponentWithHibernatePersistence component = (ComponentWithHibernatePersistence) iter.next();
			List<HibernatePersistedBeanDefinition> hbmDefinitions = component.listHibernatePersistedBeanDefinitions();
			
			for( HibernatePersistedBeanDefinition beanDefinition : hbmDefinitions )
			{
				String originalHbmResourceName = beanDefinition.getHbmResourceName();
				
				InputStream hbmInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( originalHbmResourceName );
				
				String originalHbmString = FileUtils.basicReadInputStreamUTF8( hbmInputStream );
				
				String newHbmString = TableNamePrefixer.prefixTableNames( originalHbmString, beanDefinition.getPersistedBeanTablePrefix());
	
				configuration.addXML( newHbmString );
			}
		}
		configureAndGenerate(dialectName, destinationDirectory, outputFileName, configuration);
		appContext.close();
	}
}
