package permission_based_APIclients;

import broker_context.Context;
import broker_user_services.AbstractService;
import broker_user_services.MarketDataStreamer;

public class MarketDataPermissionClient extends APIClient implements MarketDataClientManager {
	
	private MarketDataStreamer dataStreamer;
	
	@SuppressWarnings("unused")
	private MarketDataPermissionClient(){}
	
	MarketDataPermissionClient(Context broker) {
		this.dataStreamer = createUserService(broker, -1);
		activateAlerts();
	}
	
	@Override
	MarketDataStreamer createUserService(Context broker, double balance) {
		return AbstractService.createDataService(broker);
	}
	
	public void activateAlerts() {
		dataStreamer.activateMarketAlerts();
	}
	
	public void deactivateAlerts() {
		dataStreamer.deactivateMarketAlerts();
	}
	
	public int getActiveUsers() {
		return dataStreamer.activeUsers();
	}
	
	public String getLatestPriceAlert() {
		return dataStreamer.getLatestAlert();
	}
	
	public double getIstantPrice() {
		return dataStreamer.istantPrice(market);
	}

	public String getDefaultMarketsOverviews() {
		return dataStreamer.getDefaultOverviews();
	}

	public String getDefaultMarketOverview() {
		return dataStreamer.getDefaultOverview(market);
	}

	public String getWideMarketsOverviews(int groupSize) {
		return dataStreamer.getGroupedOverviews(groupSize);
	}
	
	public String getWideMarketOverview(int groupSize) {
		return dataStreamer.getGroupedOverview(market, groupSize);
	}

	boolean hasActiveOrders() {
		return false;
	}

}
