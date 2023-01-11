package tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import broker_context.BrokerType;
import broker_context.ConcreteContext;
import broker_context.Context;
import broker_context.MarketPair;
import broker_user_services.AbstractService;
import broker_user_services.MarketObserver;

import static org.assertj.core.api.Assertions.*;

public class Context_tests {
	
	private Context broker;
	
	@Before
	public void initContext() {
		this.broker =  ConcreteContext.simulateDefaultContext(BrokerType.STOCKS_BROKER);
	}
	
	@Test
	public void testBrokerMarkets() {
		assertThat(broker
				.getBrokerType()
				.getMarkets())
				.containsExactly(MarketPair.AMZN_USD, MarketPair.AAPL_USD, MarketPair.MSFT_USD, MarketPair.JUNIT_TEST_USD);
		
		broker =  ConcreteContext.simulateDefaultContext(BrokerType.CLASSIC_INDEXES_BROKER);
		assertThat(broker
				.getBrokerType()
				.getMarkets())
				.containsExactly(MarketPair.SPX500_INDEX, MarketPair.NASDAQ_INDEX, MarketPair.GOLD_USD, MarketPair.JUNIT_TEST_USD);
		
		broker =  ConcreteContext.simulateDefaultContext(BrokerType.CRYPTO_CURRENCIES_BROKER);
		assertThat(broker
				.getBrokerType()
				.getMarkets())
				.containsExactly(MarketPair.BTC_USD, MarketPair.ETH_USD, MarketPair.JUNIT_TEST_USD);
		
		assertThatThrownBy(() -> broker.getCurrentOrderBook(MarketPair.AMZN_USD))
								.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> broker.getBrokerType().getMarkets().add(MarketPair.AMZN_USD))
								.isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> broker.getBrokerType().getMarkets().remove(MarketPair.ETH_USD))
								.isInstanceOf(UnsupportedOperationException.class);
	}
	
	@Test
	public void testMarketPairAttributes() {
		assertEquals(1000, broker.getOrderBookPrice(MarketPair.JUNIT_TEST_USD), 0.0);
		assertEquals(0.1, broker.getCurrentOrderBook(MarketPair.JUNIT_TEST_USD).getSpread(),0.0);
	}
	
	@Test
	public void testMarketObserver() {
		String marketDropMessage = "JUNIT_TEST_USD price suddently decreased by 0.0 %, now it is 1000.0 $";
		String marketRiseMessage = "JUNIT_TEST_USD price suddently increased by 0.0 %, now it is 1000.0 $";
		MarketObserver user1 = AbstractService.createDataService(broker);
		MarketObserver user2 = AbstractService.createDataService(broker);
	    broker.subscribe(user1);
	    broker.subscribe(user2);
	    broker.subscribe(user1);
	    assertEquals(2, broker.activeUsers());
	    
	    broker.notifySuddenPriceDrop(MarketPair.JUNIT_TEST_USD);
	    assertEquals(marketDropMessage, user1.getLatestAlert());
	    assertEquals(marketDropMessage, user2.getLatestAlert());
	    
	    broker.unsubscribe(user2);
	    assertEquals(1, broker.activeUsers());
	    
	    broker.notifySuddenPriceRise(MarketPair.JUNIT_TEST_USD);
	    assertEquals(marketRiseMessage, user1.getLatestAlert());
	    assertEquals(marketDropMessage, user2.getLatestAlert());
	    
	    MarketObserver user3 = AbstractService.createDataService(broker);
	    broker.subscribe(user3);
	    assertEquals(2, broker.activeUsers());
	    assertEquals("None", user3.getLatestAlert());
	}
}
