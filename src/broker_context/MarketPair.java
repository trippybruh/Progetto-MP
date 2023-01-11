package broker_context;

/** Various examples of real market pairs and financial indexes + 1 test market pair, each one with its specific reference price and order book spread which is used by {@link #Book} and its builders
 *  to provide consistent liquidity distribution when the corresponding order book is generated;
 */
public enum MarketPair implements Market {
	
	AMZN_USD(96, 0.01), AAPL_USD(145, 0.02), MSFT_USD(254, 0.05), SPX500_INDEX(4000, 0.2), NASDAQ_INDEX(11500, 0.25), 
	BTC_USD(17000, 0.5), ETH_USD(1350, 0.1), GOLD_USD(1790, 0.1), JUNIT_TEST_USD(1000, 0.1);
	
	/**For future reference, these price were last updated on 01/12/2022 */
	private double referencePrice;
	
	/**This field holds the minimum possible shift in the asset price (tied to its type and price) and consequently any order 
	 * which specifies preciser price values will need them to be rounded to their corresponding format (a limit order on AAPL_USD at '100.026' becames '100.02').
	 * liquidity across the order book is distributed across levels that are separated between each other by this nominal value, 
	 * liquidity can also be shown grouped by specific higher values, however it would be a sum of the supply at each single level. */
	private double orderBookSpread;
	
	private MarketPair(double referencePrice, double obSpread) {
		this.referencePrice = referencePrice;
		this.orderBookSpread = obSpread;
	}
	
	public double getReferencePrice() {
		return referencePrice;
	}

	public double getOrderBookSpread() {
		return orderBookSpread;
	}

}
