/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.elements.network;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.onrc.openvirtex.core.io.OVXSendMsg;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.elements.datapath.Switch;
import net.onrc.openvirtex.elements.link.Link;
import net.onrc.openvirtex.elements.port.Port;
import net.onrc.openvirtex.linkdiscovery.LLDPEventHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 
 * Abstract parent class for networks, maintains data structures for the
 * topology graph.
 * 
 * @param <T1>
 *            Generic Switch type
 * @param <T2>
 *            Generic Port type
 * @param <T3>
 *            Generic Link type
 */
public abstract class Network<T1, T2, T3> implements LLDPEventHandler,
		OVXSendMsg {


    @SerializedName("switches")
    @Expose
	protected final HashSet<T1>              switchSet;
    @SerializedName("links")
    @Expose
	protected final HashSet<T3>              linkSet;
	protected final HashMap<Long, T1>        dpidMap;
	protected final HashMap<T2, T2>          neighborPortMap;
	protected final HashMap<T1, HashSet<T1>> neighborMap;

	Logger log = LogManager.getLogger(Network.class.getName());

	protected Network() {
		this.switchSet = new HashSet<T1>();
		this.linkSet = new HashSet<T3>();
		this.dpidMap = new HashMap<Long, T1>();
		this.neighborPortMap = new HashMap<T2, T2>();
		this.neighborMap = new HashMap<T1, HashSet<T1>>();
	}

	// Protected methods to update topology (only allowed from subclasses)

	/**
	 * Add link to topology
	 * 
	 * @param link
	 */
	protected void addLink(final T3 link) {
		// Actual link creation is in child classes, because creation of generic
		// types sucks
		this.linkSet.add(link);
		final T1 srcSwitch = (T1) ((Link) link).getSrcSwitch();
		final T1 dstSwitch = (T1) ((Link) link).getDstSwitch();
		final Port srcPort = (Port) (T2) ((Link) link).getSrcPort();
		final Port dstPort = (Port) (T2) ((Link) link).getSrcPort();
		srcPort.isEdge(false);
		dstPort.isEdge(false);
		final HashSet<T1> neighbours = this.neighborMap.get(srcSwitch);
		neighbours.add(dstSwitch);
		this.neighborPortMap.put((T2) ((Link) link).getSrcPort(),
				(T2) ((Link) link).getDstPort());
		this.log.info("Adding link " + link.toString());
	}

	/**
	 * Remove link to topology
	 * 
	 * @param link
	 * @return 
	 */
	protected boolean removeLink(final T3 link) {
		this.linkSet.remove(link);
		final T1 srcSwitch = (T1) ((Link) link).getSrcSwitch();
		final T1 dstSwitch = (T1) ((Link) link).getDstSwitch();
		final Port srcPort = (Port) (T2) ((Link) link).getSrcPort();
		final Port dstPort = (Port) (T2) ((Link) link).getSrcPort();
		srcPort.isEdge(true);
		dstPort.isEdge(true);
		final HashSet<T1> neighbours = this.neighborMap.get(srcSwitch);
		neighbours.remove(dstSwitch);
		this.neighborPortMap.remove(((Link) link).getSrcPort());
		this.log.info("Removing link " + link.toString());
		return true;
	}

	/**
	 * Add switch to topology
	 * 
	 * @param sw
	 */
	protected void addSwitch(final T1 sw) {
		if (this.switchSet.add(sw)) {
			this.dpidMap.put(((Switch) sw).getSwitchId(), sw);
			this.neighborMap.put(sw, new HashSet());
		}
	}
	
	/**
	 * Remove switch from topology
	 * @param sw
	 */
	protected boolean removeSwitch(final T1 sw) {
		if (this.switchSet.remove(sw)) {
			this.neighborMap.remove(sw);    	
			this.dpidMap.remove(((Switch) sw).getSwitchId());
			return true;
		}
		return false;
        }

	// Public methods to query topology information

	/**
	 * Return neighbor switches of switch.
	 * 
	 * @param sw
	 * @return Unmodifiable set of switch instances
	 */
	public Set<T1> getNeighbors(final T1 sw) {
		return Collections.unmodifiableSet(this.neighborMap.get(sw));
	}

	/**
	 * Return neighbor port of port
	 * 
	 * @param port
	 * @return
	 */
	public T2 getNeighborPort(final T2 port) {
		return this.neighborPortMap.get(port);
	}

	/**
	 * Return switch instance based on its dpid
	 * 
	 * @param dpid
	 * @return
	 */
	public T1 getSwitch(final Long dpid) {
		return this.dpidMap.get(dpid);
	}

	public Set<T1> getSwitches() {
		return Collections.unmodifiableSet(this.switchSet);
	}

	// TODO: optimize this because we iterate over all links
	public T3 getLink(final T2 srcPort, final T2 dstPort) {
		for (final T3 link : this.linkSet) {
			if (((Link) link).getSrcPort().equals(srcPort)
					&& ((Link) link).getDstPort().equals(dstPort)) {
				return link;
			}
		}
		return null;
	}

	public abstract boolean boot();

}
