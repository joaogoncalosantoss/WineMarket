package domain;

public class Sell {
	
	private String id;
	private String seller;
	private String photo;
	private int value;
	private int quantity;
	
	public Sell(String id, String photo, int value, int quantity, String seller) {
		this.id = id;
		this.photo = photo;
		this.seller = seller;
		this.quantity = quantity;
		this.value = value;
	}

	public String getSeller() {
		return seller;
	}

	public String getPhoto() {
		return photo;
	}

	public String getId() {
		return id;
	}


	public int getValue() {
		return value;
	}

	public int getQuantity() {
		return quantity;
	}
	
}
