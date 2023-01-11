package broker_context;

import java.util.*;

import broker_user_services.MarketObserver;
import order_book.Book;
import order_book.OrderBook;

/**
 * Define a simulated broker enviroment with different markets described in {@link #MarketPair} and {@link #BrokerType}
 * generating the corresponding order books, also allowing the creation of various market scenarios.
 */
public class ConcreteContext implements Context {
	
	private BrokerType type;
	private List<MarketObserver> users = new ArrayList<>();
	private List<OrderBook> orderBooks;
	
	public static Context simulateDefaultContext(BrokerType type) {
		return new ConcreteContext(type, new ContextBuilder(type).createDefaultContext());
	}
	
	public static Context simulateRandomContext(BrokerType type) {
		return new ConcreteContext(type, new ContextBuilder(type).createRandomContext());
	}
	
	private ConcreteContext(BrokerType brokerType, List<OrderBook> orderBooks) {
		this.orderBooks = orderBooks;
		this.type = brokerType;
	}
	
	/**
	 * @param market : the market pair of type {@link MarketPair}
	 * @return The corresponding order book interface 
	 * @throws IllegalArgumentException if context have not been initializied
	 */
	public OrderBook getCurrentOrderBook(Market market) throws IllegalArgumentException {
		for (OrderBook book : orderBooks) {
			if (book.getMarketPair() == market) {
				return book;
			}
		}
		throw new IllegalArgumentException("There is no market pair associated to the parameter value '" +market+ "'!");
	}
	
	public double getOrderBookPrice(Market market) {
		return getCurrentOrderBook(market).getCurrentPrice();
	}
	
	private double getOrderBookPriceChange(Market market) {
		return getCurrentOrderBook(market).getLastPriceChange();
	}
	
	public BrokerType getBrokerType() {
		return type;
	}
	
	public int activeUsers() {
		return users.size();
	}
	
	public void subscribe(MarketObserver user) {
		if (!users.contains(user)) {
			users.add(user);
		}
	}
	
	public void unsubscribe(MarketObserver user) {
		if (users.contains(user)) {
			users.remove(user);
		}
	}
	
	public void notifySuddenPriceRise(Market market) {
		users.stream().forEach(obs -> obs.bigPriceRise(market, getOrderBookPriceChange(market)));
	}
	
	public void notifySuddenPriceDrop(Market market) {
		users.stream().forEach(obs -> obs.bigPriceDrop(market, getOrderBookPriceChange(market)));
	}

	private static class ContextBuilder {
		
		private BrokerType type;
		private List<OrderBook> ob = new ArrayList<>();
		
		private ContextBuilder(BrokerType brokerType) {
			this.type = brokerType;
		}
		
		private List<OrderBook> createDefaultContext() {
			for (Market market : type.getMarkets()) {
				ob.add(Book.simulateDefaultOrderBook(market));
			}
			return List.copyOf(ob);
		}
		
		private List<OrderBook> createRandomContext() {
			for (Market market : type.getMarkets()) {
				ob.add(marketScenarios(market));
			}
			return List.copyOf(ob);
		}
		
		/**This method randomly pick 1 out of 4 possible market scenarios for the specified market pair and calls it's corresponding order book builder
		 * @param market : the market pair of type {@link MarketPair}
		 * @return The corresponding order book for the requested market pair
		 */
		private OrderBook marketScenarios(Market market) {
			int scenarioID = new Random().nextInt(4);
			if (scenarioID == 0) {
				return Book.simulateDefaultOrderBook(market);
			} else if (scenarioID == 1){
				return Book.simulateUnstableOrderBook(market);
			} else if (scenarioID == 2) {
				return Book.simulateOverBoughtOrderBook(market);
			} else return Book.simulateOverSoldOrderBook(market);
		}
	}

}
