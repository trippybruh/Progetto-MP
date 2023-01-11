package broker_user_services;

public class OrderNotFoundExc extends Exception {

	private static final long serialVersionUID = 1L;
	private String message;
	
	OrderNotFoundExc(String str) {
		this.message = str;
	}
	
	public void getError() {
		System.out.println(message);
	}
	
}
