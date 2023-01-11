package permission_based_APIclients;

public interface PortfolioClientManager extends MarketDataClientManager {
	
	void depositShares(double qty);
	void depositFiat(double funds);
	void withdrawShares(double qty);
	void withdrawAllShares();
	void withdrawFiat(double funds);
	void withdrawAllFiat();
	String getAccountBalances();
}
