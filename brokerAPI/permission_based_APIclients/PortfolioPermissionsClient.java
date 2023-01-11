package permission_based_APIclients;

import broker_context.Context;
import broker_user_services.AbstractService;
import broker_user_services.WalletManager;

public class PortfolioPermissionsClient extends APIClient implements PortfolioClientManager {
	
	private WalletManager walletManager;
	
	@SuppressWarnings("unused")
	private PortfolioPermissionsClient(){}
	
	PortfolioPermissionsClient(Context broker, double balance) {
		this.walletManager = createUserService(broker, balance);
		activateAlerts();
	}
	
	@Override
	WalletManager createUserService(Context broker, double balance){
		return AbstractService.createWalletService(broker, balance);
	}
	
	public String getAccountBalances() {
		return walletManager.getAccountBalances();
	}
	
	public void depositFiat(double funds) {
		walletManager.depositFiat(funds);
	}
	
	public void withdrawFiat(double funds) {
		walletManager.withdrawFiat(funds);
	}
	
	public void withdrawAllFiat() {
		walletManager.withdrawAllFiat();
	}
	
	public void depositShares(double qty) {
		walletManager.depositShares(market, qty);
	}

	public void withdrawShares(double qty) {
		walletManager.withdrawShares(market, qty);
	}

	public void withdrawAllShares() {
		walletManager.withdrawAllShares(market);
	}
	
	public void activateAlerts() {
		walletManager.activateMarketAlerts();
	}
	
	public void deactivateAlerts() {
		walletManager.deactivateMarketAlerts();
	}
	
	public String getLatestPriceAlert() {
		return walletManager.getLatestAlert();
	}
	
	public int getActiveUsers() {
		return walletManager.activeUsers();
	}
	
	public double getIstantPrice() {
		return walletManager.istantPrice(market);
	}

	public String getDefaultMarketsOverviews() {
		return walletManager.getDefaultOverviews();
	}

	public String getDefaultMarketOverview() {
		return walletManager.getDefaultOverview(market);
	}

	public String getWideMarketsOverviews(int groupSize) {
		return walletManager.getGroupedOverviews(groupSize);
	}
	
	public String getWideMarketOverview(int groupSize) {
		return walletManager.getGroupedOverview(market, groupSize);
	}

	boolean hasActiveOrders() {
		return false;
	}
	
}
