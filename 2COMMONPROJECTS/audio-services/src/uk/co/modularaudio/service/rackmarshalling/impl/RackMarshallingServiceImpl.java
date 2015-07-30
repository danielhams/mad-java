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

package uk.co.modularaudio.service.rackmarshalling.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.subrack.mu.SubRackMadInstance;
import uk.co.modularaudio.mads.subrack.ui.SubRackMadUiInstance;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.service.rackmarshalling.RackMarshallingService;
import uk.co.modularaudio.service.rackmarshalling.generated.madrack.ObjectFactory;
import uk.co.modularaudio.service.rackmarshalling.generated.madrack.RackComponentParameterValueXmlType;
import uk.co.modularaudio.service.rackmarshalling.generated.madrack.RackComponentXmlType;
import uk.co.modularaudio.service.rackmarshalling.generated.madrack.RackControlXmlType;
import uk.co.modularaudio.service.rackmarshalling.generated.madrack.RackIOLinkXmlType;
import uk.co.modularaudio.service.rackmarshalling.generated.madrack.RackLinkXmlType;
import uk.co.modularaudio.service.rackmarshalling.generated.madrack.RackPositionXmlType;
import uk.co.modularaudio.service.rackmarshalling.generated.madrack.RackXmlType;
import uk.co.modularaudio.service.rackmarshalling.generated.madrack.SubRackXmlType;
import uk.co.modularaudio.service.userpreferences.UserPreferencesService;
import uk.co.modularaudio.util.atomicio.FileUtilities;
import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackIOLink;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackLink;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.table.ContentsAlreadyAddedException;
import uk.co.modularaudio.util.table.TableCellFullException;
import uk.co.modularaudio.util.table.TableIndexOutOfBoundsException;
import uk.co.modularaudio.util.table.TablePosition;

public class RackMarshallingServiceImpl implements ComponentWithLifecycle, RackMarshallingService
{
	private static Log log = LogFactory.getLog( RackMarshallingServiceImpl.class.getName() );

	private RackService rackService;
	private MadComponentService componentService;
	private UserPreferencesService userPreferencesService;

	private ObjectFactory objectFactory;
	private JAXBContext jbContext;
	private Unmarshaller unmarshaller;
	private Marshaller marshaller;

	@Override
	public RackDataModel loadBaseRackFromFile( final String filename ) throws DatastoreException, IOException
	{
		String fullFilename = filename;
		// If it's relative, tack on the front the user sub racks dir
		if( FileUtilities.isRelativePath( fullFilename ) )
		{
			final String userPatchesDir = userPreferencesService.getUserPatchesDir();
			fullFilename = userPatchesDir + File.separatorChar + fullFilename;
		}
		return loadRackFromAbsFile( fullFilename );
	}

	@Override
	public RackDataModel loadSubRackFromFile( final String filename ) throws DatastoreException, IOException
	{
		String fullFilename = filename;
		// If it's relative, tack on the front the user sub racks dir
		if( FileUtilities.isRelativePath( fullFilename ) )
		{
			final String userSubRackPatchesDir = userPreferencesService.getUserSubRackPatchesDir();
			fullFilename = userSubRackPatchesDir + File.separatorChar + fullFilename;
		}
		return loadRackFromAbsFile( fullFilename );
	}

	private RackDataModel loadRackFromAbsFile( final String filename ) throws DatastoreException, IOException
	{
//		if( log.isDebugEnabled() )
//		{
//			log.debug("Load rack from file (" + filename + ")");
//		}
		try
		{
			@SuppressWarnings("unchecked")
			final
			JAXBElement<RackXmlType> jbRackElement = (JAXBElement<RackXmlType>)unmarshaller.unmarshal( new File( filename ) );
			final RackXmlType jbRackXml = jbRackElement.getValue();
			final RackDataModel retVal = loadRackFromStructure( filename, jbRackXml );
			retVal.setDirty( false );
			return retVal;
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught loading rack from file: " + e.toString();
			log.error( msg, e );
			throw new DatastoreException( msg, e );
		}
	}

	private RackDataModel loadRackFromStructure( final String filename, final RackXmlType jbRackXml )
			throws DatastoreException, RecordNotFoundException, MadProcessingException, MAConstraintViolationException, IOException, ContentsAlreadyAddedException, TableCellFullException, TableIndexOutOfBoundsException
	{
//		if( log.isDebugEnabled() )
//		{
//			log.debug("Load rack from structure (" + filename + ")");
//		}
		final String rackName = jbRackXml.getName();
		final int numCols = jbRackXml.getCols();
		final int numRows = jbRackXml.getRows();

		// TODO: Move the size check of the rack master io to the post init
		// so it's only done once.
		// Work out how big the IO component is
		final RackDataModel blah = rackService.createNewRackDataModel( "tmp", "", 8, 10, true );
		final RackComponent tmpmirc = blah.getContentsAtPosition( 0, 0 );

		final int masterIOYOffset = tmpmirc.getCellSpan().y;

		final RackDataModel retVal = rackService.createNewRackDataModel( rackName, filename, numCols, numRows + masterIOYOffset, true );

		final RackComponent mirc = retVal.getContentsAtPosition( 0, 0 );

		// Now loop over all the components creating them
		final List<RackComponentXmlType> jbRackComponentXmls = jbRackXml.getRackComponent();
		for( final RackComponentXmlType jbRackComponentXml : jbRackComponentXmls )
		{
			final String definitionId = jbRackComponentXml.getDefinitionId();
			final String componentName = jbRackComponentXml.getName();
			final MadDefinition<?,?> madDefinition = componentService.findDefinitionById( definitionId );
			final RackPositionXmlType rackPositionXml = jbRackComponentXml.getRackPosition();
			final int col = rackPositionXml.getColumn();
			final int row = rackPositionXml.getRow() + masterIOYOffset;

			final Map<MadParameterDefinition, String> parameterValues = new HashMap<MadParameterDefinition, String>();
			final List<RackComponentParameterValueXmlType> jbParamValues = jbRackComponentXml.getRackComponentParameterValue();
			for( final RackComponentParameterValueXmlType jbParamValue : jbParamValues )
			{
				lookupAndAddParameterValue( madDefinition, parameterValues, jbParamValue );
			}

			final RackComponent rackComponent = rackService.createComponentAtPosition( retVal, madDefinition, parameterValues, componentName, col, row );

			// If it's a subgraph, check to see if we load the library path, or we "load" from the incoming data.
			if( jbRackComponentXml instanceof SubRackXmlType )
			{
				final SubRackMadUiInstance subRackUiInstance = (SubRackMadUiInstance)rackComponent.getUiInstance();

				final SubRackXmlType jbSubRackType = (SubRackXmlType)jbRackComponentXml;
				if( jbSubRackType.isLocalSubRack() )
				{
					// Create the rack structure from what's below in the file
					final RackDataModel subRackDataModel = loadRackFromStructure( "", jbSubRackType.getRack() );

					subRackUiInstance.setSubRackDataModel( subRackDataModel, true );
				}
				else
				{
					// Load the rack structure from the library path
					final String libraryPath = jbSubRackType.getLibraryPath();
					final RackDataModel subRackDataModel = loadSubRackFromFile( libraryPath );

					subRackUiInstance.setSubRackDataModel( subRackDataModel, true );
				}
			}

			final List<RackControlXmlType> jbRackControlXmls = jbRackComponentXml.getRackControl();
			final AbstractMadUiControlInstance<?,?,?>[] uiControlInstances = rackComponent.getUiControlInstances();
			for( final RackControlXmlType jbControlType : jbRackControlXmls )
			{
				final String name = jbControlType.getName();
				final String value = jbControlType.getValue();
				// Find the control
				final AbstractMadUiControlInstance<?,?,?> uiControlInstance = lookupUiControlInstanceByName( uiControlInstances, name );
				if( uiControlInstance != null )
				{
					uiControlInstance.receiveControlValue( value );
				}
				else
				{
					if( log.isWarnEnabled() )
					{
						log.warn("File contains control value for " + name + " but I couldn't find a control with that name");
					}
				}
			}
		}

		// Now the Graph IO links
		final List<RackIOLinkXmlType> rackIOLinkXmls = jbRackXml.getRackIOLink();
		for( final RackIOLinkXmlType rackIOLinkXml : rackIOLinkXmls )
		{
			final String rackChannelName = rackIOLinkXml.getRackChannelName();
			final String componentName = rackIOLinkXml.getRackComponentName();
			final String componentChannelName = rackIOLinkXml.getRackComponentChannelInstanceName();
			try
			{
				final MadChannelInstance rackChannelInstance = mirc.getInstance().getChannelInstanceByName( rackChannelName );
				final RackComponent rackComponent = slowLookupByName( retVal,  componentName );
				final MadChannelInstance rackComponentChannelInstance = rackComponent.getInstance().getChannelInstanceByName( componentChannelName );
				rackService.addRackIOLink( retVal, rackChannelInstance, rackComponent, rackComponentChannelInstance );
			}
			catch( final RecordNotFoundException rnfe )
			{
				final String msg = "RecordNotFound caught attempting to add rack IO link: " +
						" rackChannel(" + rackChannelName +")->(" + componentName + " " +
						componentChannelName + ")";
				log.error( msg, rnfe );
				throw rnfe;
			}
		}

		// And the links
		final List<RackLinkXmlType> rackLinkXmls = jbRackXml.getRackLink();
		for( final RackLinkXmlType rackLinkXml : rackLinkXmls )
		{
			final String producerName = rackLinkXml.getProducerRackComponentName();
			final String producerChannelName = rackLinkXml.getProducerChannelName();
			final String consumerName = rackLinkXml.getConsumerRackComponentName();
			final String consumerChannelName = rackLinkXml.getConsumerChannelName();
			try
			{
				final RackComponent producerRackComponent = slowLookupByName( retVal, producerName );
				final MadChannelInstance producerChannelInstance = producerRackComponent.getInstance().getChannelInstanceByName( producerChannelName );
				final RackComponent consumerRackComponent = slowLookupByName( retVal, consumerName );
				final MadChannelInstance consumerChannelInstance = consumerRackComponent.getInstance().getChannelInstanceByName( consumerChannelName );
				rackService.addRackLink( retVal, producerRackComponent, producerChannelInstance, consumerRackComponent, consumerChannelInstance );
			}
			catch( final RecordNotFoundException rnfe )
			{
				final String msg = "RecordNotFound caught attempting to add rack link: " +
						" prod(" + producerName + " " + producerChannelName + ")->(" +
						consumerName + " " + consumerChannelName + ")";
				log.error( msg, rnfe );
				throw rnfe;
			}
		}
		return retVal;
	}

	private void lookupAndAddParameterValue( final MadDefinition<?,?> definition,
			final Map<MadParameterDefinition, String> parameterValues,
			final RackComponentParameterValueXmlType jbParamValue )
					throws RecordNotFoundException
	{
		final MadParameterDefinition[] paramDefs = definition.getParameterDefinitions();
		final String paramId = jbParamValue.getParameterName();
		boolean foundIt = false;
		for( int i = 0; !foundIt && i < paramDefs.length ; i++ )
		{
			final MadParameterDefinition aupd = paramDefs[ i ];
			if( aupd.getKey().equals( paramId ) )
			{
				foundIt = true;
				parameterValues.put( aupd, jbParamValue.getValue() );
			}
		}
		if( !foundIt )
		{
			final String msg = "Unable to find parameter with id " + paramId + " for definition " + definition.getId();
			throw new RecordNotFoundException( msg );
		}
	}

	private AbstractMadUiControlInstance<?,?,?> lookupUiControlInstanceByName( final AbstractMadUiControlInstance<?,?,?>[] uiControlInstances,
			final String name)
					throws RecordNotFoundException
					{
		AbstractMadUiControlInstance<?,?,?> retVal = null;
		boolean foundIt = false;
		for( int i = 0 ; !foundIt && i < uiControlInstances.length ; i++ )
		{
			final AbstractMadUiControlInstance<?,?,?> uci = uiControlInstances[ i ];
			if( uci.getUiControlDefinition().getControlName().equals( name ) )
			{
				foundIt = true;
				retVal = uci;
			}
		}

		return retVal;
					}

	private RackComponent slowLookupByName( final RackDataModel rackDataModel, final String componentName ) throws RecordNotFoundException
	{
		final List<RackComponent> entries = rackDataModel.getEntriesAsList();
		for( int i = 0 ; i < entries.size() ; i++ )
		{
			final RackComponent test = entries.get( i );
			if( test.getComponentName().equals( componentName ) )
			{
				return test;
			}
		}
		throw new RecordNotFoundException();
	}

	@Override
	public void saveBaseRackToFile( final RackDataModel dataModel, final String filename ) throws DatastoreException, IOException
	{
		String fullFilename = filename;
		// If it's relative, tack on the front the user sub racks dir for the save filename
		if( FileUtilities.isRelativePath( fullFilename ) )
		{
			final String userPatchesDir = userPreferencesService.getUserPatchesDir();
			fullFilename = userPatchesDir + File.separatorChar + fullFilename;
		}
		saveRackToAbsFile( dataModel, fullFilename );
	}

	@Override
	public void saveSubRackToFile( final RackDataModel dataModel, final String filename ) throws DatastoreException, IOException
	{
		String fullFilename = filename;
		// If it's relative, tack on the front the user sub racks dir for the save filename
		if( FileUtilities.isRelativePath( fullFilename ) )
		{
			final String userSubRackPatchesDir = userPreferencesService.getUserSubRackPatchesDir();
			fullFilename = userSubRackPatchesDir + File.separatorChar + fullFilename;
		}
		saveRackToAbsFile( dataModel, fullFilename );
	}

	private void saveRackToAbsFile( final RackDataModel dataModel, final String filename ) throws DatastoreException, IOException
	{
		dataModel.setPath( filename );

		final RackXmlType jbRackXmlType = rackDataModelToXmlTypes( dataModel );

		final JAXBElement<RackXmlType> jbRack = objectFactory.createRack( jbRackXmlType );

		try
		{
			// Now write it out
			marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
			marshaller.marshal( jbRack, new FileOutputStream( filename ) );
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught marshalling rack into XML: " + e.toString();
			log.error( msg, e );
			throw new DatastoreException( msg, e );
		}
	}

	private RackXmlType rackDataModelToXmlTypes( final RackDataModel rackDataModel )
	{
		final RackComponent rackMasterIOComponent = rackDataModel.getContentsAtPosition( 0, 0 );
		final int yOffset = rackMasterIOComponent.getCellSpan().y;

		final RackXmlType jbRackXmlType = objectFactory.createRackXmlType();
		jbRackXmlType.setName( rackDataModel.getName() );
		jbRackXmlType.setCols( rackDataModel.getNumCols() );
		jbRackXmlType.setRows( rackDataModel.getNumRows() - yOffset );

		final List<RackComponentXmlType> jbRootRackComponents = jbRackXmlType.getRackComponent();
		final List<RackIOLinkXmlType> jbRootRackIOLinks = jbRackXmlType.getRackIOLink();
		final List<RackLinkXmlType> jbRootRackLinks = jbRackXmlType.getRackLink();

		final Set<RackIOLink> rackIOLinks = rackDataModel.getRackIOLinks();
		final ArrayList<RackIOLink> sortedRackIOLinks = new ArrayList<RackIOLink>( rackIOLinks );
		Collections.sort( sortedRackIOLinks, new Comparator<RackIOLink>(){

			@Override
			public int compare( final RackIOLink o1, final RackIOLink o2 )
			{
				if( o1.getRackChannelInstance() == o2.getRackChannelInstance() )
				{
					if( o1.getRackComponent() == o2.getRackComponent() )
					{
						return o1.getRackChannelInstance().definition.name.compareTo( o2.getRackChannelInstance().definition.name );
					}
					else
					{
						return o1.getRackComponent().getComponentName().compareTo( o2.getRackComponent().getComponentName() );
					}
				}
				else
				{
					return o1.getRackChannelInstance().definition.name.compareTo( o2.getRackChannelInstance().definition.name );
				}
			}
		}
				);

		final List<RackComponent> rackComponents = rackDataModel.getEntriesAsList();
		final ArrayList<RackComponent> sortedRackComponents = new ArrayList<RackComponent>( rackComponents );
		Collections.sort( sortedRackComponents, new Comparator<RackComponent>(){

			@Override
			public int compare( final RackComponent o1, final RackComponent o2 )
			{
				return o1.getComponentName().compareTo( o2.getComponentName() );
			}
		}
				);

		final Set<RackLink> rackLinks = rackDataModel.getLinks();
		final ArrayList<RackLink> sortedRackLinks = new ArrayList<RackLink>( rackLinks );
		Collections.sort( sortedRackLinks, new Comparator<RackLink>(){

			@Override
			public int compare( final RackLink o1, final RackLink o2 )
			{
				final RackComponent o1producerRackComponent = o1.getProducerRackComponent();
				final RackComponent o2producerRackComponent = o2.getProducerRackComponent();
				final MadChannelInstance o1producerChannelInstance = o1.getProducerChannelInstance();
				final MadChannelInstance o2producerChannelInstance = o2.getProducerChannelInstance();
				if( o1producerRackComponent == o2producerRackComponent )
				{
					if( o1producerChannelInstance == o2producerChannelInstance )
					{
						final RackComponent o1consumerRackComponent = o1.getConsumerRackComponent();
						final RackComponent o2consumerRackComponent = o2.getConsumerRackComponent();
						if( o1consumerRackComponent == o2consumerRackComponent )
						{
							final MadChannelInstance o1consumerChannelInstance = o1.getConsumerChannelInstance();
							final MadChannelInstance o2consumerChannelInstance = o2.getConsumerChannelInstance();
							return o1consumerChannelInstance.definition.name.compareTo( o2consumerChannelInstance.definition.name );
						}
						else
						{
							return o1consumerRackComponent.getComponentName().compareTo( o2consumerRackComponent.getComponentName() );
						}
					}
					else
					{
						return o1producerChannelInstance.definition.name.compareTo( o2producerChannelInstance.definition.name );
					}
				}
				else
				{
					return o1producerRackComponent.getComponentName().compareTo( o2producerRackComponent.getComponentName() );
				}
			}
		}
				);

		for( final RackIOLink rackIOLink : sortedRackIOLinks )
		{
			final MadChannelInstance rackChannelInstance = rackIOLink.getRackChannelInstance();
			final RackComponent rackComponent = rackIOLink.getRackComponent();
			final MadChannelInstance rackComponentChannelInstance = rackIOLink.getRackComponentChannelInstance();

			final RackIOLinkXmlType jbRackIOLinkType = objectFactory.createRackIOLinkXmlType();
			jbRackIOLinkType.setRackChannelName( rackChannelInstance.definition.name );
			jbRackIOLinkType.setRackComponentName( rackComponent.getComponentName() );
			jbRackIOLinkType.setRackComponentChannelInstanceName( rackComponentChannelInstance.definition.name );

			jbRootRackIOLinks.add( jbRackIOLinkType );
		}

		// Create the components
		for( final RackComponent component : sortedRackComponents )
		{
			if( component == rackMasterIOComponent )
			{
				continue;
			}

			final MadInstance<?,?> instance = component.getInstance();
			if( instance instanceof SubRackMadInstance )
			{
				final SubRackXmlType jbSubRackType = objectFactory.createSubRackXmlType();

				final RackPositionXmlType jbRackPositionType = objectFactory.createRackPositionXmlType();
				final TablePosition contentsOrigin = rackDataModel.getContentsOriginReturnNull(component);
				jbRackPositionType.setColumn( contentsOrigin.x );
				jbRackPositionType.setRow( contentsOrigin.y - yOffset );

				jbSubRackType.setRackPosition( jbRackPositionType );

				final SubRackMadInstance sraui = (SubRackMadInstance)instance;

				jbSubRackType.setName( sraui.getInstanceName() );
				final MadDefinition<?,?> aud = instance.getDefinition();
				jbSubRackType.setDefinitionId( aud.getId() );

				String srp = sraui.getSubRackDataModel().getPath();
				if( srp != null && !srp.equals("") && !sraui.isDirty() )
				{
					// Is not local, just persist the file path

					// First we'll see if we can make it a relative path
					final String userSubRackPatchesStr = userPreferencesService.getUserSubRackPatchesDir();
					final File userSubRackPatchesFile = new File(userSubRackPatchesStr);
					final String userSubRackPatchesDir = userSubRackPatchesFile.getAbsolutePath();

					if( srp.startsWith( userSubRackPatchesDir ) )
					{
						srp = srp.substring( userSubRackPatchesDir.length() + 1 );
					}

					jbSubRackType.setLocalSubRack( false );
					jbSubRackType.setLibraryPath( srp );
				}
				else
				{
					// Not a preset, or we are dirty - persist the subgraph data directly underneath
					jbSubRackType.setLibraryPath( "" );
					jbSubRackType.setLocalSubRack( true );

					final RackXmlType localSubRack = rackDataModelToXmlTypes( sraui.getSubRackDataModel() );
					jbSubRackType.setRack( localSubRack );
				}
				jbRootRackComponents.add( jbSubRackType );
			}
			else
			{
				final RackComponentXmlType jbRackComponentType = objectFactory.createRackComponentXmlType();

				final Map<MadParameterDefinition, String> parameterValueMap = instance.getCreationParameterValues();
				final ArrayList<MadParameterDefinition> paramsByName = new ArrayList<MadParameterDefinition>();
				paramsByName.addAll( parameterValueMap.keySet() );
				Collections.sort( paramsByName, new Comparator<MadParameterDefinition>() {

					@Override
					public int compare( final MadParameterDefinition o1, final MadParameterDefinition o2 )
					{
						return o1.getKey().compareTo( o2.getKey() );
					}
				} );

				for( final MadParameterDefinition paramDef : paramsByName )
				{
					final String paramValue = parameterValueMap.get( paramDef );
					final RackComponentParameterValueXmlType jbRackParamType = objectFactory.createRackComponentParameterValueXmlType();
					jbRackParamType.setParameterName( paramDef.getKey() );
					jbRackParamType.setValue( paramValue );
					jbRackComponentType.getRackComponentParameterValue().add( jbRackParamType );
				}

				jbRackComponentType.setName( component.getComponentName() );
				jbRackComponentType.setDefinitionId( component.getInstance().getDefinition().getId() );

				final RackPositionXmlType jbRackPositionType = objectFactory.createRackPositionXmlType();
				final TablePosition contentsOrigin = rackDataModel.getContentsOriginReturnNull(component);
				jbRackPositionType.setColumn( contentsOrigin.x );
				jbRackPositionType.setRow( contentsOrigin.y - yOffset );

				jbRackComponentType.setRackPosition( jbRackPositionType );

				// Grab the controls and persist their value too
				final AbstractMadUiControlInstance<?,?,?>[] uiInstances = component.getUiControlInstances();
				final ArrayList<AbstractMadUiControlInstance<?,?,?>> controlsByName = new ArrayList<AbstractMadUiControlInstance<?, ?, ?>>();
				for( final AbstractMadUiControlInstance<?,?,?> muci : uiInstances )
				{
					controlsByName.add( muci );
				}
				Collections.sort( controlsByName, new Comparator<AbstractMadUiControlInstance<?,?,?>>() {

					@Override
					public int compare( final AbstractMadUiControlInstance<?, ?, ?> o1,
							final AbstractMadUiControlInstance<?, ?, ?> o2 )
					{
						return o1.getUiControlDefinition().getControlName().compareTo(
								o2.getUiControlDefinition().getControlName() );
					}
				} );
				for( final AbstractMadUiControlInstance<?,?,?> cui : controlsByName )
				{
					//					log.debug("Found a UI instance to persist: " + cui.getClass().getName() );
					final MadUiControlDefinition<?,?,?> cud = cui.getUiControlDefinition();
					final String controlName = cud.getControlName();
					final String controlValue = cui.getControlValue();

					final RackControlXmlType jbRackControlType = objectFactory.createRackControlXmlType();

					jbRackControlType.setName( controlName );
					jbRackControlType.setValue( controlValue );
					jbRackComponentType.getRackControl().add( jbRackControlType );
				}
				jbRootRackComponents.add( jbRackComponentType );
			}
		}

		// Same for the links
		for( final RackLink link : sortedRackLinks )
		{
			final RackLinkXmlType jbRackLinkType = objectFactory.createRackLinkXmlType();

			final MadInstance<?,?> consumerInstance = link.getConsumerChannelInstance().instance;
			final MadChannelInstance consumerChannelInstance = link.getConsumerChannelInstance();
			final MadChannelDefinition consumerChannelDefinition = consumerChannelInstance.definition;

			jbRackLinkType.setConsumerRackComponentName( consumerInstance.getInstanceName() );
			jbRackLinkType.setConsumerChannelName( consumerChannelDefinition.name );

			final MadInstance<?,?> producerInstance = link.getProducerChannelInstance().instance;
			final MadChannelInstance producerChannelInstance = link.getProducerChannelInstance();
			final MadChannelDefinition producerChannelDefinition = producerChannelInstance.definition;

			jbRackLinkType.setProducerRackComponentName( producerInstance.getInstanceName() );
			jbRackLinkType.setProducerChannelName( producerChannelDefinition.name );

			jbRootRackLinks.add( jbRackLinkType );
		}

		return jbRackXmlType;
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( rackService == null ||
				componentService == null ||
				userPreferencesService == null )
		{
			throw new ComponentConfigurationException( "Service missing dependencies. Check configuration" );
		}

		try
		{
			objectFactory = new ObjectFactory();
			jbContext = JAXBContext.newInstance(  objectFactory.getClass().getPackage().getName() );
			unmarshaller = jbContext.createUnmarshaller();
			marshaller =jbContext.createMarshaller();
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught initialising jaxb infrastructure.";
			log.error( msg, e );
			throw new ComponentConfigurationException( msg, e  );
		}
	}

	public void setRackService(final RackService rackService)
	{
		this.rackService = rackService;
	}

	public void setComponentService(final MadComponentService componentService)
	{
		this.componentService = componentService;
	}

	public void setUserPreferencesService( final UserPreferencesService userPreferencesService )
	{
		this.userPreferencesService = userPreferencesService;
	}

}
