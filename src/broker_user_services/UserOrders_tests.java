package broker_user_services;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import broker_context.BrokerType;
import broker_context.ConcreteContext;
import broker_context.Context;
import broker_context.MarketPair;
import orders.Order;
import orders.OrderSide;
import orders.OrderStatus;

public class UserOrders_tests {
	
	Context broker = ConcreteContext.simulateDefaultContext(BrokerType.CLASSIC_INDEXES_BROKER);
	private UserOrdersTracker uo;
	
	public void addOrdersToTracker(OrderStatus o1, OrderStatus o2, OrderStatus o3) {
		uo.orderDispatched(o1);
		uo.orderDispatched(o2);
		uo.orderDispatched(o3);
	}
	
	@Before
	public void createTracker() {
		this.uo = UserOrdersTracker.createOrderTracker();
	}
	
	@Test
	public void testRegisteringSame() {
		OrderStatus o1 = Order.buildNewMarketOrder(MarketPair.JUNIT_TEST_USD, broker.getOrderBookPrice(MarketPair.JUNIT_TEST_USD), 5, OrderSide.BUY);
		OrderStatus o2 = Order.buildNewLimitOrder(MarketPair.JUNIT_TEST_USD, 5, 1010, OrderSide.BUY);
		OrderStatus o3 = Order.buildNewStopLimitOrder(MarketPair.JUNIT_TEST_USD, 5, 1010, 1005, OrderSide.BUY);
		
		assertEquals(0, uo.totalDispatchedOrders());
		addOrdersToTracker(o1, o2, o3);
		assertEquals(3, uo.totalDispatchedOrders());
		addOrdersToTracker(o1, o2, o3);
		assertEquals(3, uo.totalDispatchedOrders());
	}
	
	@Test
	public void testOrderRetrieving() throws OrderNotFoundExc {
		OrderStatus o1 = Order.buildNewMarketOrder(MarketPair.JUNIT_TEST_USD, broker.getOrderBookPrice(MarketPair.JUNIT_TEST_USD), 5, OrderSide.BUY);
		OrderStatus o2 = Order.buildNewLimitOrder(MarketPair.JUNIT_TEST_USD, 5, 1010, OrderSide.BUY);
		OrderStatus o3 = Order.buildNewStopLimitOrder(MarketPair.JUNIT_TEST_USD, 5, 1010, 1005, OrderSide.BUY);
		
		assertThatThrownBy(() -> uo.getOrderById(1)).isInstanceOf(OrderNotFoundExc.class);
		assertThatThrownBy(() -> uo.getOrdersByPriceLevel(1005)).isInstanceOf(OrderNotFoundExc.class);
		assertThatThrownBy(() -> uo.getOrdersByPriceLevel(1010)).isInstanceOf(OrderNotFoundExc.class);
		addOrdersToTracker(o1, o2, o3);
		
		assertThat(uo.getOrdersByPriceLevel(1005)).containsExactly(o3);
		assertThat(uo.getOrdersByPriceLevel(1010)).containsExactly(o2, o3);
	}
	
	@Test
	public void testOrderStatusChange() throws OrderNotFoundExc {
		OrderStatus o1 = Order.buildNewMarketOrder(MarketPair.JUNIT_TEST_USD, broker.getOrderBookPrice(MarketPair.JUNIT_TEST_USD), 5, OrderSide.BUY);
		OrderStatus o2 = Order.buildNewLimitOrder(MarketPair.JUNIT_TEST_USD, 5, 1010, OrderSide.BUY);
		OrderStatus o3 = Order.buildNewStopLimitOrder(MarketPair.JUNIT_TEST_USD, 5, 1010, 1005, OrderSide.BUY);
		addOrdersToTracker(o1, o2, o3);
		
		assertThat(uo.getAllPendingOrders()).containsExactly(o2, o3);
		assertEquals(o3, uo.getLastPendingOrder());
		assertTrue(uo.hasActiveOrders());
		assertTrue(uo.hasInactiveOrders());
		assertTrue(uo.hasPendingOrders());

		uo.stopLimitActivated(o3);
		o3.updateActivation();
		uo.limitExecuted(o3);
		o3.updateFilled();
		assertThat(uo.getAllPendingOrders()).containsExactly(o2);
		assertEquals(o2, uo.getLastPendingOrder());
		assertTrue(uo.hasActiveOrders());
		assertFalse(uo.hasInactiveOrders());
		assertTrue(uo.hasPendingOrders());
		
		uo.limitExecuted(o2);
		o2.updateFilled();
		assertThatThrownBy(() -> uo.getAllPendingOrders()).isInstanceOf(OrderNotFoundExc.class);
		assertThatThrownBy(() -> uo.getLastPendingOrder()).isInstanceOf(OrderNotFoundExc.class);
		assertFalse(uo.hasActiveOrders());
		assertFalse(uo.hasInactiveOrders());
		assertFalse(uo.hasPendingOrders());
	}
	
	@Test
	public void testOrderCancelledByUser() throws OrderNotFoundExc {
		OrderStatus o1 = Order.buildNewMarketOrder(MarketPair.JUNIT_TEST_USD, broker.getOrderBookPrice(MarketPair.JUNIT_TEST_USD), 5, OrderSide.BUY);
		OrderStatus o2 = Order.buildNewLimitOrder(MarketPair.JUNIT_TEST_USD, 5, 1010, OrderSide.BUY);
		OrderStatus o3 = Order.buildNewStopLimitOrder(MarketPair.JUNIT_TEST_USD, 5, 1010, 1005, OrderSide.BUY);
		addOrdersToTracker(o1, o2, o3);
		assertTrue(uo.hasActiveOrders());
		assertTrue(uo.hasInactiveOrders());
		assertTrue(uo.hasPendingOrders());
		
		uo.orderCancelled(o1); /* cancelling an order already executed has no effect */
		assertEquals(3, uo.totalDispatchedOrders());
		uo.orderCancelled(o2);
		assertEquals(2, uo.totalDispatchedOrders());
		assertFalse(uo.hasActiveOrders());
		assertTrue(uo.hasInactiveOrders());
		assertTrue(uo.hasPendingOrders());
		
		uo.orderCancelled(o3);
		assertEquals(1, uo.totalDispatchedOrders());
		assertFalse(uo.hasActiveOrders());
		assertFalse(uo.hasInactiveOrders());
		assertFalse(uo.hasPendingOrders());
	}
	
	@Test 
	public void testTradeTPSLsetting() throws OrderNotFoundExc {
		OrderStatus tradeEntry = Order.buildNewMarketOrder(MarketPair.JUNIT_TEST_USD, broker.getOrderBookPrice(MarketPair.JUNIT_TEST_USD), 5, OrderSide.BUY);
		OrderStatus stopLoss = Order.buildNewLimitOrder(MarketPair.JUNIT_TEST_USD, 5, 1010, OrderSide.SELL);
		OrderStatus takeProfit = Order.buildNewStopLimitOrder(MarketPair.JUNIT_TEST_USD, 5, 990, 991, OrderSide.SELL);
		uo.orderDispatched(tradeEntry);
		uo.orderDispatched(stopLoss);
		uo.orderDispatched(takeProfit);
		assertThat(uo.getAllPendingOrders())
					.containsExactly(stopLoss, takeProfit)
					.anyMatch(stoploss -> stoploss.isStopLimit())
					.anyMatch(takeprofit -> takeprofit.isLimit())
					.allMatch(both -> !both.isFilled() && !both.isMarket());
		uo.TPSLsetted();
		assertEquals("\nMARKET ORDER: (BUY '5.0' shares on JUNIT_TEST_USD at market price [1000.0 $]) filled: true\n"
				+ "CONDITIONAL LIMIT ORDER: (SELL '5.0' shares on JUNIT_TEST_USD at 990.0 $, activaiton price is 991.0) "+
				"--> Status {active: false, filled: false} ---> STOP LOSS ORDER ID OF '123456789'\n"
				+ "LIMIT ORDER: (SELL '5.0' shares on JUNIT_TEST_USD at 1010.0 $) "+
				"--> Status {active: true, filled: false} ---> TAKE PROFIT ORDER ID OF '123456789'", tradeEntry.toStringForTesting());
	}
}
