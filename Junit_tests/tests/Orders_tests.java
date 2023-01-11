package tests;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.Test;

import broker_context.BrokerType;
import broker_context.ConcreteContext;
import broker_context.Context;
import broker_context.MarketPair;
import broker_user_services.AbstractService;
import broker_user_services.NotEnoughBalanceExc;
import broker_user_services.UserManager;
import order_book.LevelNotFoundExc;
import order_book.OrderBook;
import orders.*;

public class Orders_tests {
	
	private OrderBook book;
	private Context context;
	private UserManager aUser;
	private double startingPrice;
	
	@Before
	public void createContext() {
		this.context = ConcreteContext.simulateDefaultContext(BrokerType.STOCKS_BROKER);
		this.aUser = AbstractService.createFullService(context, 10000000);
		this.book = context.getCurrentOrderBook(MarketPair.AAPL_USD);
		this.startingPrice = context.getOrderBookPrice(MarketPair.AAPL_USD);
	}
	
	@Test
	public void testOrderTypeAndRepresentation() {
		OrderStatus o1 = Order.buildNewMarketOrder(MarketPair.AAPL_USD, startingPrice, 10, OrderSide.BUY);
		OrderStatus o2 = Order.buildNewLimitOrder(MarketPair.AAPL_USD, 10, 100, OrderSide.BUY);
		OrderStatus o3 = Order.buildNewStopLimitOrder(MarketPair.AAPL_USD, 10, 95, 105, OrderSide.BUY);
		assertTrue(o1.isMarket());
		assertTrue(o2.isLimit());
		assertTrue(o3.isStopLimit());
		assertEquals("\nMARKET ORDER: (BUY '10.0' shares on AAPL_USD at market price [145.0 $]) filled: true", o1.toStringForTesting());
		assertEquals("\nLIMIT ORDER: (BUY '10.0' shares on AAPL_USD at 100.0 $) --> Status {active: true, filled: false}", o2.toStringForTesting());
		assertEquals("\nCONDITIONAL LIMIT ORDER: (BUY '10.0' shares on AAPL_USD at 95.0 $, activaiton price is 105.0) --> Status {active: false, filled: false}", o3.toStringForTesting());
	}
	
	@Test
	public void testOrderDataAndStatus() {
		OrderStatus o = Order.buildNewStopLimitOrder(MarketPair.AAPL_USD, 10, 135, 140, OrderSide.BUY);
		assertTrue(o.isBuy());
		assertFalse(o.getID() > 100); /* even if this test is the last executed no more than 100 are generated from testing across all junit classes, this is due to static order id generation */
		assertEquals(10, o.getQuantity(), 0.0);
		assertEquals(135, o.getRequestedPrice(), 0.0);
		assertEquals(140, o.getActivationPrice(), 0.0);
		assertEquals(1350, o.getQuantityUSD(), 0.0);
		o.updateActivation();
		assertTrue(o.isActive());
		assertFalse(o.isFilled());
		o.updateActivation();
		o.updateFilled();
		assertFalse(o.isActive());
		assertTrue(o.isFilled());
	}
	
	@Test
	public void testForwardingMarketOrder () throws LevelNotFoundExc {
		OrderStatus o = Order.buildNewMarketOrder(MarketPair.AAPL_USD, startingPrice, 51, OrderSide.BUY);
		assertTrue(book.handledOrder(o, aUser));
		assertTrue(book.getCurrentPrice() > startingPrice);
	}
	
	@Test
	public void testForwardingLimitOrder () throws LevelNotFoundExc {
		OrderStatus o = Order.buildNewLimitOrder(MarketPair.AAPL_USD, 51, 140, OrderSide.BUY);
		assertTrue(book.handledOrder(o, aUser));
		assertEquals(startingPrice, book.getCurrentPrice(), 0.0);
	}
	
	@Test
	public void testForwardingStopLimitOrder () throws LevelNotFoundExc {
		OrderStatus o = Order.buildNewStopLimitOrder(MarketPair.AAPL_USD, 51, 140, 143, OrderSide.BUY);
		assertTrue(book.handledOrder(o, aUser));
		assertEquals(startingPrice, book.getCurrentPrice(), 0.0);
	}
	
	@Test
	public void testForwardingBadInputsOrders() throws LevelNotFoundExc {
		assertThatThrownBy(() -> aUser.BuyMarket(-1, MarketPair.AAPL_USD, -1))
									.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> aUser.SellMarket(-1, MarketPair.AAPL_USD, -1))
									.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> aUser.BuyMarket(100000000, MarketPair.AAPL_USD, -1))
									.isInstanceOf(NotEnoughBalanceExc.class);
		assertThatThrownBy(() -> aUser.SellMarket(1, MarketPair.AAPL_USD, -1))
									.isInstanceOf(NotEnoughBalanceExc.class);
		
		assertEquals(book.getCurrentPrice(), startingPrice, 0.0);
		OrderStatus o1 = Order.buildNewLimitOrder(MarketPair.AAPL_USD, 51, startingPrice+1, OrderSide.BUY);
		assertTrue(book.handledOrder(o1, aUser)); 
		assertTrue(book.getCurrentPrice() > startingPrice);
		OrderStatus o2 = Order.buildNewLimitOrder(MarketPair.AAPL_USD, 51, startingPrice-1, OrderSide.SELL);
		assertTrue(book.handledOrder(o2, aUser));
		assertTrue(book.getCurrentPrice() < startingPrice);
	}
	
}
