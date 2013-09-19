package net.onrc.openvirtex.elements.datapath;

import org.openflow.util.LRULinkedHashMap;

/**
 * based on Flowvisor XidTranslator by capveg
 */
public class XidTranslator {

	static final int MIN_XID = 256;
	static final int INIT_SIZE = 1 << 12;
	static final int MAX_SIZE = 1 << 14; // must be larger than the max lifetime
											// of an XID * rate of
											// mesgs/sec
	int nextID;
	LRULinkedHashMap<Integer, XidPair> xidMap;

	public XidTranslator() {
		this.nextID = XidTranslator.MIN_XID;
		this.xidMap = new LRULinkedHashMap<Integer, XidPair>(
				XidTranslator.INIT_SIZE, XidTranslator.MAX_SIZE);
	}

	/**
	 * Recovers the source of the message transaction by Xid.
	 * 
	 * @param xid
	 * @return
	 */
	public XidPair untranslate(final int xid) {
		return this.xidMap.get(Integer.valueOf(xid));
	}

	/**
	 * @return the new Xid for the message.
	 */
	public int translate(final int xid, final OVXSwitch sw) {
		final int ret = this.nextID++;
		if (this.nextID < XidTranslator.MIN_XID) {
			this.nextID = XidTranslator.MIN_XID;
		}
		this.xidMap.put(Integer.valueOf(ret), new XidPair(xid, sw));
		return ret;
	}

}
