package broker_context;

import java.util.List;

public enum BrokerType {
	
	STOCKS_BROKER(List.of(MarketPair.AMZN_USD, MarketPair.AAPL_USD, MarketPair.MSFT_USD, MarketPair.JUNIT_TEST_USD)), 
	CRYPTO_CURRENCIES_BROKER(List.of(MarketPair.BTC_USD, MarketPair.ETH_USD, MarketPair.JUNIT_TEST_USD)), 
	CLASSIC_INDEXES_BROKER(List.of(MarketPair.SPX500_INDEX, MarketPair.NASDAQ_INDEX, MarketPair.GOLD_USD, MarketPair.JUNIT_TEST_USD));	
	
	private List<Market> markets;

	BrokerType(List<Market> markets) {
		this.markets = markets;
	}

	public List<Market> getMarkets() {
		return markets;
	}

}
