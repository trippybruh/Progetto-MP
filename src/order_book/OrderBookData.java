package order_book;

import broker_context.Market;

public interface OrderBookData {
	
	double getSpread();
	double getCurrentPrice();
	double getLastPriceChange();
	boolean verifyPriceChange();
	Market getMarketPair();
	PriceLevel getLevel(double level) throws LevelNotFoundExc;
	
}
