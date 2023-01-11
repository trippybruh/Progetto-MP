package order_book;

public class LevelNotFoundExc extends Exception {
	
	private static final long serialVersionUID = 1L;
	private String message = "Unable to find a supply level matching '";
	
	LevelNotFoundExc(double level) {
		this.message += level + "' value";
	}

	public void getError() {
		System.out.println(message);
	}
	
}
