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

package uk.co.modularaudio.util.pooling.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <P>Seperate Runnable object for adding, and remove Resources from the pool.</P>
 * <P>Typically, this object is passed the number of resources to create or remove by an invocation of an Arbiter.</P>
 * <P>For an example of its use, consult the ExpiringDynamicStackPool, where arbiters are used to check the size of the pool for adding and removing resources.</P>
 * @author dan
 * @version 1.0
 * @see uk.co.modularaudio.util.pooling.common.ExpiringDynamicStackPool
 * @see uk.co.modularaudio.util.pooling.common.Factory
 * @see uk.co.modularaudio.util.pooling.common.Arbiter
 * @see uk.co.modularaudio.util.pooling.common.AddResourcesArbiter
 * @see uk.co.modularaudio.util.pooling.common.RemoveResourcesArbiter*/
public class PoolSizingThread extends Thread
{
	protected static Log log = LogFactory.getLog(PoolSizingThread.class.getName());
	
	private long sizingCheckSleepMilliSeconds = 0;

	public PoolSizingThread(IDynamicSizedPool pool, 
	        Integer poolSemaphore, 
	        Factory factory,
	        long sizingCheckSleepMilliSeconds)
	{
		this.setName("PoolSizingThread");
        
        if (log.isTraceEnabled())
            log.trace("PST created with ID: " + this.hashCode());
        
		// Keep a reference to the pool to put them in,
		synchronized (shouldHaltMutex)
		{
			this.pool = pool;
			this.poolSemaphore = poolSemaphore;
			this.factory = factory;
			this.sizingCheckSleepMilliSeconds = sizingCheckSleepMilliSeconds;
		}
	}

	public void run()
	{
        if (log.isTraceEnabled())
            log.trace("PST run of ID: " + this.hashCode());
		try
		{
			boolean localShouldHalt = false;
			synchronized(shouldHaltMutex)
			{
				localShouldHalt = shouldHalt;
			}

			int numThisRound = 0;
            
            if (numThisRound == 0)
            {
                synchronized( poolSemaphore )
                {
                    pool.arbitrateSize( null );
                    numThisRound = pool.getNumNeeded();
                }
            }
            
    		// Here we loop around checking to see if we should be halted.
    		while (!localShouldHalt)
    		{
    			synchronized (shouldHaltMutex)
    			{
    				while (!localShouldHalt && numThisRound == 0)
    				{
    					try
    					{
    					    //log.debug("Waiting for notification of jobs");
    						shouldHaltMutex.wait( sizingCheckSleepMilliSeconds );
    					}
    					catch (InterruptedException ie)
    					{
    					}
                        
    					localShouldHalt = shouldHalt;
                        
    					if (numThisRound == 0)
    					{
                            synchronized( poolSemaphore )
                            {
                                pool.arbitrateSize( null );
                                numThisRound = pool.getNumNeeded();
                            }
    					}
    
    					if( log.isDebugEnabled() )
    				    {
    					    synchronized( poolSemaphore )
    					    {
//    					        int poolSize = ((Pool)pool).poolStructure.size();
    					        log.debug("Pool " + pool.getName() + " - " + 
                                        pool.toString() + " - numneeded: " + pool.getNumNeeded());
    					    }
    				    }
    				}
                    
                    synchronized( poolSemaphore )
                    {
                        numThisRound = pool.getNumNeeded();
                    }
    
    				if (localShouldHalt)
    				{
    					log.info("PoolSizingThreadhalting.");
    					return;
    				}
    			}
    
    			if (numThisRound > 0)
    			{
    				synchronized(shouldHaltMutex)
    				{
    					localShouldHalt = shouldHalt;
    					if (localShouldHalt)
    					{
    						log.info("PoolSizingThreadhalting.");
    						return;
    					}
    				}
    				// Create 1 new resource using the factory, and
    				// add it in.
                    
                    if (log.isTraceEnabled())
                        log.trace("Sizing thread needs " + numThisRound + " resources.");
    				try
    				{
                        if (log.isTraceEnabled())
                            log.trace("Asking factory to create a resource");
                        Resource res = factory.createResource();
    
                        if (log.isTraceEnabled())
                            log.trace("ST produced resource, adding to pool.");
                        
    					synchronized (poolSemaphore)
    					{
    						pool.addResource(res);
    						poolSemaphore.notifyAll();
                            pool.addToNumNeeded(-1);
                            numThisRound = pool.getNumNeeded();
                            log.debug("ST added resource to pool.");
    					}
    				}
    				catch (FactoryProductionException fpe)
    				{
    					log.error("Error creating resource:" + fpe.toString());
                        log.error("Pool details are: " + pool.toString());
    					// Sleep for a while after a failure to stop a CPU bound cycle
    					try
    					{
    						Thread.sleep( sizingCheckSleepMilliSeconds );
    					}
    					catch (InterruptedException ie)
    					{
    						log.error("Caught interrupted exception waiting inside sizing delay loop.");
    					}
    				}
    			}
    			else if( numThisRound < 0)
    			{
    				synchronized(shouldHaltMutex)
    				{
    					localShouldHalt = shouldHalt;
    					if (localShouldHalt)
    					{
    						log.info("PoolSizingThreadhalting.");
    						return;
    					}
    				}
    				// Call use resource, then remove resource.
                    if (log.isTraceEnabled())
                        log.trace("Creation thread needs " + numThisRound + " resources.");
    				try
    				{
                        if (log.isTraceEnabled())
                            log.trace( "ST removing resource.");
                        
    					synchronized (poolSemaphore)
    					{
                            Resource res = pool.removeAnyFreeResource();
                            postRemoveResource( res );
                            pool.addToNumNeeded( 1 );
                            if (log.isTraceEnabled())
                                log.trace("ST removed resource.");
    					}
    				}
    				catch (ResourceNotAvailableException rnae)
    				{
    					log.warn("Exception caught creating resource: " + rnae.toString());
    					try
    					{
    					    Thread.sleep( sizingCheckSleepMilliSeconds );
    					}
    					catch(InterruptedException ie)
    					{
    					}
    				}
    			}
    		}
    		log.info("PoolSizingThreadhalting.");
    		return;
		}
		catch(Exception e)
		{
			log.error("Caught exception in pool sizing thread: " + e.toString());
		}
	}

	protected void postRemoveResource(Resource res)
    {
    }

    public void halt()
	{
		synchronized (shouldHaltMutex)
		{
			shouldHalt = true;
			shouldHaltMutex.notifyAll();
		}
	}

	private IDynamicSizedPool pool = null;
	private Integer poolSemaphore = null;
	
	private Factory factory = null;
	private boolean shouldHalt = false;
	private Integer shouldHaltMutex = new Integer(0);
}
