package permission_based_APIclients;

import broker_context.Context;
import broker_context.Market;
import broker_user_services.MarketDataStreamer;

public abstract class APIClient {
	
	protected Market market;
	
	abstract MarketDataStreamer createUserService(Context broker, double balance);
	
	public final void connectToMarket(Market market) {
		this.market = market;
	}
	
	public static FullClientManager buildFullClient(Context broker, double balance) {
		return new ClientInitializer(true, true).initFullClient(broker, balance);
	}
	
	public static PortfolioClientManager buildPortfolioManagerClient(Context broker, double balance) {
		return new ClientInitializer(false, true).initPortfolioManagerClient(broker, balance);
	}
	
	public static MarketDataClientManager buildDataStreamerClient(Context broker) {
		return new ClientInitializer(false, false).initMarketDataClient(broker);
	}
	
	private static class ClientInitializer {
		
		private static boolean marketDataStreaming = true;
		private boolean portfolioManagingAllowed = false;
		private boolean tradingAllowed = false;
		
		/**NOT IMPLEMENTED!, Use {@link #ClientsInitializer(boolean, boolean)} constructor. <br><br>
		 * Creates an istance of a user account client (already registered to the exchange) which is allowed to perform various
		 * operations depending on the chosen API permissions (which are supposed to be setted while creating it) relative to these specific keys.
		 * @param publicKey : alphanumeric string that match a unique registered account in the database
		 * @param secretKey : alphanumeric string used to authenticate this account 
		 */
		private ClientInitializer() {
			/* API login calls */
			/* set internal permission fields based on API permissions */
		}
		
		/**Permissions should be retrieved from a database, this constructor should not be invocked by static methods, this 
		 * was done just for testing and to provide working examples of these clients
		 * @param tradeAllowed 
		 * @param portfolioManagingAllowed
		 */
		private ClientInitializer(boolean tradeAllowed, boolean portfolioManagingAllowed ) {
			this.tradingAllowed = tradeAllowed;
			this.portfolioManagingAllowed  = portfolioManagingAllowed;
			
		}

		private FullClientManager initFullClient(Context broker, double balance) throws IllegalArgumentException {
			if (portfolioManagingAllowed && tradingAllowed) {
				return new FullPermissionsClient(broker, balance);
			} 
			System.out.println(permissionError());
			return null;
		}
		
		private PortfolioClientManager initPortfolioManagerClient(Context broker,double balance) {
			if (portfolioManagingAllowed) {
				return new PortfolioPermissionsClient(broker, balance);
			}
			System.out.println(permissionError());
			return null;
		}
		
		private MarketDataClientManager initMarketDataClient(Context broker) {
			return new MarketDataPermissionClient(broker);
		}
		
		private String permissionError() {
			return "Unable to intialize requested client! ALLOWED OPERATIONS:\nMarket data streaming: "+marketDataStreaming+", "
					+"Can manage portfolio balance: "+portfolioManagingAllowed+", Can trade on markets: "+tradingAllowed;
		}
		
	}

}
