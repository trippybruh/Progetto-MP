package broker_user_services;

import broker_context.Market;

public interface MarketObserver  {
	
	void activateMarketAlerts();
	void deactivateMarketAlerts();
	void bigPriceRise(Market market, double percentage);
	void bigPriceDrop(Market market, double percentage);
	String getLatestAlert();
	
}
