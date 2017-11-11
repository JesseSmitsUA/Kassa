package kassa.core.analytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import kassa.core.items.Items;
import kassa.core.storage.Storage;

public class TransactionDB {
	private HashMap<Integer, ArrayList<Integer>> m_tidlist;
	
	public TransactionDB(Items items, Storage db) throws Exception {
		m_tidlist = new HashMap<Integer, ArrayList<Integer>>();
		
		for(int i = 0; i < items.getNrItems(); i++) {
			m_tidlist.put(items.getItem(i).getDatabaseId(), new ArrayList<Integer>());
		}
		
		HashMap<Integer, HashSet<Integer>> orders = db.getAllOrders();
		for (int client : orders.keySet()) {
			for (int item : orders.get(client)) {
				if( !m_tidlist.get(item).contains(client) )
					m_tidlist.get(item).add(client);
			}
		}
	}
	
	public Set<Integer> getIDs() {
		return m_tidlist.keySet();
	}
	
	public ArrayList<Integer> getClients(int item) {
		ArrayList<Integer> tmp = m_tidlist.get(item);
		Collections.sort(tmp);
		return tmp;
	}
}
