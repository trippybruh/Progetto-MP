package tests;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import broker_context.BrokerType;
import broker_context.ConcreteContext;
import broker_context.Context;
import broker_context.MarketPair;
import broker_user_services.AbstractService;
import broker_user_services.UserManager;
import order_book.LevelNotFoundExc;
import order_book.OrderBook;
import orders.Order;
import orders.OrderSide;
import orders.OrderStatus;

public class Order_book_tests {
	
	private Context context;
	private OrderBook book;
	private final static int MAX_DEPTH = 5000;
	private double startingPrice;
	private double randomBuyLevelLiquidity;
	private double randomSellLevelLiquidity;
	private UserManager aUser;

	@Before
	public void createContextAndOrders() throws LevelNotFoundExc {
		this.context = ConcreteContext.simulateDefaultContext(BrokerType.STOCKS_BROKER);
		this.aUser = AbstractService.createFullService(context, 10000000);
		this.book = context.getCurrentOrderBook(MarketPair.AAPL_USD);
		this.startingPrice = book.getCurrentPrice();
		this.randomBuyLevelLiquidity = book.getLevel(startingPrice-10).getLiquidity();
		this.randomSellLevelLiquidity = book.getLevel(startingPrice+10).getLiquidity();
	}
	
	@Test
	public void testOrderBooksProperties() throws LevelNotFoundExc {
		assertEquals(MarketPair.AAPL_USD.getReferencePrice(), startingPrice, 0.0);
		assertEquals(MarketPair.AAPL_USD.getReferencePrice()-book.getSpread(), book.getLevel(MarketPair.AAPL_USD.getReferencePrice()-book.getSpread()).getLevelValue(), 0.0); 
		assertEquals(MarketPair.AAPL_USD.getReferencePrice()+book.getSpread(), book.getLevel(MarketPair.AAPL_USD.getReferencePrice()+book.getSpread()).getLevelValue(), 0.0);
		assertTrue(randomBuyLevelLiquidity<50); 
		assertTrue(randomSellLevelLiquidity>=0);
	}
	
	@Test
	public void testOrderBookBidsDepth() throws LevelNotFoundExc {
		assertThatThrownBy(() -> book.getLevel(MarketPair.AAPL_USD.getReferencePrice()-(book.getSpread()*(MAX_DEPTH+1)))).isInstanceOf(LevelNotFoundExc.class);
	}
	
	@Test
	public void testOrderBookAsksDepth() throws LevelNotFoundExc {
		assertThatThrownBy(() -> book.getLevel(MarketPair.AAPL_USD.getReferencePrice()+(book.getSpread()*(MAX_DEPTH+1)))).isInstanceOf(LevelNotFoundExc.class);
	}
	
	@Test
	public void testBuyOrdersEffects () throws LevelNotFoundExc {
		OrderStatus o1 = Order.buildNewMarketOrder(MarketPair.AAPL_USD, startingPrice, 500, OrderSide.BUY);
		assertTrue(book.handledOrder(o1, aUser));
		assertTrue(book.getCurrentPrice() > startingPrice);
		OrderStatus o2 = Order.buildNewLimitOrder(MarketPair.AAPL_USD, 1000, startingPrice-10, OrderSide.BUY);
		assertTrue(book.handledOrder(o2, aUser));
		assertTrue(book.getLevel(startingPrice-10).getLiquidity() > randomBuyLevelLiquidity);
		assertTrue(book.getLevel(startingPrice-10).containsOrders());
		OrderStatus o3 = Order.buildNewStopLimitOrder(MarketPair.AAPL_USD, 1000, startingPrice-20, startingPrice-15, OrderSide.BUY);
		assertTrue(book.handledOrder(o3, aUser));
		assertTrue(book.getLevel(startingPrice-20).getLiquidity() < 50); 
		assertTrue(book.getLevel(startingPrice-15).containsOrders());
		assertFalse(book.getLevel(startingPrice-20).containsOrders());
	}
	
	@Test
	public void testSellOrdersEffects () throws LevelNotFoundExc {
		OrderStatus o1 = Order.buildNewMarketOrder(MarketPair.AAPL_USD, startingPrice, 500, OrderSide.SELL);
		assertTrue(book.handledOrder(o1, aUser));
		assertTrue(book.getCurrentPrice() < startingPrice);
		OrderStatus o2 = Order.buildNewLimitOrder(MarketPair.AAPL_USD, 1000, startingPrice+10, OrderSide.SELL);
		assertTrue(book.handledOrder(o2, aUser));
		assertTrue(book.getLevel(startingPrice+10).getLiquidity() > randomSellLevelLiquidity);
		assertTrue(book.getLevel(startingPrice+10).containsOrders());
		OrderStatus o3 = Order.buildNewStopLimitOrder(MarketPair.AAPL_USD, 1000, startingPrice+20, startingPrice+15, OrderSide.SELL);
		assertTrue(book.handledOrder(o3, aUser));
		assertTrue(book.getLevel(startingPrice+20).getLiquidity() < 50); 
		assertTrue(book.getLevel(startingPrice+15).containsOrders());
		assertFalse(book.getLevel(startingPrice+20).containsOrders());
	}
	
}
