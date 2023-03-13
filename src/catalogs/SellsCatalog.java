package catalogs;

import java.util.ArrayList;

import domain.Sell;

public class SellsCatalog {

	private static SellsCatalog instance;
	private ArrayList<Sell> sellsCatalog;
	
	private SellsCatalog() {
		sellsCatalog = new ArrayList<Sell>();
	}
	
	public static SellsCatalog getSellsCatalog() {
		if (instance == null)
            instance = new SellsCatalog();
		
        return instance;
	}
	
	public void add(Sell sell) {
		sellsCatalog.add(sell);
	}
	
}
