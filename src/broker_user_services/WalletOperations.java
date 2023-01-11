package broker_user_services;

import broker_context.Market;

public interface WalletOperations {

	void depositShares(Market type, double qty);
	void depositFiat(double funds);
	void withdrawShares(Market type, double qty);
	void withdrawAllShares(Market type);
	void withdrawFiat(double funds);
	void withdrawAllFiat();
	String getAccountBalances();

}