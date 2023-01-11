package permission_based_APIclients;

import broker_context.Market;
import broker_context.MarketPair;

public interface MarketDataClientManager {
	
	/**Attach client to a specific market pair to allow (depending on the API permissions) data streaming from the corresponding order book, shares deposits/withdrawls of the specified parameter,
	 * orders creation/cancellation and trades setting
	 * @param market : must be of type {@link MarketPair}
	 */
	void connectToMarket(Market market);
	
	void activateAlerts();
	void deactivateAlerts();
	int getActiveUsers();
	double getIstantPrice();
	
	/**@return a string overview of all markets and corresponding liquidity with a narrow offset from current price, to get a wider view use {@link #getWideMarketsOverviews(int)}
	 */
	String getDefaultMarketsOverviews();
	String getDefaultMarketOverview();	
	
	/**@param groupSize : defines the group size that liqudity levels in the order books will be grouped by and returned
	 * @return a string overview of all markets with their corresponding grouped liqudity and current prices
	 */
	String getWideMarketsOverviews(int groupSize);
	String getWideMarketOverview(int groupSize);
	String getLatestPriceAlert();
	
}
