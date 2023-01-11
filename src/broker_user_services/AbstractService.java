package broker_user_services;

import broker_context.Context;
import broker_context.Market;
import order_book.Book;

public abstract class AbstractService implements MarketObserver {
	
	private Context broker;
	private String marketAlert = "None";
	
	AbstractService(Context broker){
		this.broker = broker;
	}
	
	public Context getBroker() {
		return broker;
	}
	
	public double istantPrice(Market market) {
		return broker.getOrderBookPrice(market);
	}
	
	public static MarketDataStreamer createDataService(Context broker) {
		return new DataStreamingService(broker);
	}
	
	public static WalletManager createWalletService(Context broker, double balance) {
		if (balance >= 0) {
			return new UserWalletService(broker, balance);
		} else throw new IllegalArgumentException("Cannot initialize a user client with negative balance ("+balance+")");
 	}
	
	public static UserManager createFullService(Context broker, double balance) {
		if (balance >= 0) {
			return new UserFullServices(broker, balance);
		} else throw new IllegalArgumentException("Cannot initialize a user client with negative balance ("+balance+")");
	}
	
	public void activateMarketAlerts() {
		getBroker().subscribe(this);
	}

	public void deactivateMarketAlerts() {
		getBroker().unsubscribe(this);
	}
	
	public void bigPriceRise(Market market, double percentage) {
		marketAlert = market+" price suddently increased by "+Book.uniformRounding(percentage).doubleValue()+" %, now it is "+istantPrice(market)+" $";
	}

	public void bigPriceDrop(Market market, double percentage) {
		marketAlert = market+" price suddently decreased by "+Book.uniformRounding(percentage).doubleValue()+" %, now it is "+istantPrice(market)+" $";
	}
	
	public String getLatestAlert() {
		return marketAlert;
	}
	
	
	
}
