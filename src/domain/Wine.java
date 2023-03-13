package domain;

public class Wine {
	
	private String id;
	private String photo;
	private int classicationSum;
	private int classicationCount;
	private String avgClassification;
	
	public Wine(String id, String photo) {
		this.id = id;
		this.photo = photo;
	}
	
	public String getID() {
		return this.id;
	}

	public String getPhoto() {
		return photo;
	}
	
	public void updateClassification(int stars) {
		this.classicationSum =+ stars;
		this.classicationCount++;
		
		this.setAvgClassification(String.format("%.2f", 
				Float.parseFloat(String.valueOf(this.classicationSum))
				/ Float.parseFloat(String.valueOf(this.classicationCount)) ));
	}
	
	public String getClassification() {
		return photo;
	}

	public String getAvgClassification() {
		return avgClassification;
	}

	public void setAvgClassification(String avgClassification) {
		this.avgClassification = avgClassification;
	}
	
}
