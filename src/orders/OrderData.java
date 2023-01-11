package orders;

import broker_context.Market;

public interface OrderData {
	
	int getID();
	double getQuantity();
	double getQuantityUSD();
	double getRequestedPrice();
	double getActivationPrice();
	Market getMarket();
	String toStringForTesting();
	
}
