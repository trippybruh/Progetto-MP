package broker_user_services;

/**
 * Thrown to indicate that the current operation can't be carried out due to insufficent resources in {@link UserWalletService}
 * @param string defining the specific error case
 */
public class NotEnoughBalanceExc extends Exception {
	
	private static final long serialVersionUID = 1L;
	private String message;
	
	public NotEnoughBalanceExc(String str) {
		this.message = str;
	}
	
	public void getError() {
		System.out.println(message);
	}
	
}
