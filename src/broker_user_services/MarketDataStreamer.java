package broker_user_services;

import broker_context.Market;

public interface MarketDataStreamer extends MarketObserver {
	
	int activeUsers();
	double istantPrice(Market market);
	String getDefaultOverviews();
	String getDefaultOverview(Market market);	
	String getGroupedOverviews(int groupSize);
	String getGroupedOverview(Market market, int groupSize);
}
