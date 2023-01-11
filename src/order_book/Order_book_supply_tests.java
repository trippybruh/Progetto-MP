package order_book;

import static org.junit.Assert.*;
import org.junit.Test;

import broker_context.BrokerType;
import broker_context.ConcreteContext;
import broker_context.Context;
import broker_context.MarketPair;
import broker_user_services.AbstractService;
import broker_user_services.NotEnoughBalanceExc;
import broker_user_services.UserManager;

public class Order_book_supply_tests {

	@Test
	public void testSupplyManagerBasics()  {
		PriceLevel ask = SupplyLevel.newAskLevel(1000, 78.5);
		PriceLevel bid = SupplyLevel.newBidLevel(350, 78);
		ask.removeLiquidity(300);
		bid.addLiquidity(350);
		assertEquals(700, ask.getLiquidity(), 0.0);
		assertEquals(700, bid.getLiquidity(), 0.0);
		ask.removeLiquidity(650);
		bid.removeLiquidity(750);
		assertTrue(bid.isEmpty());
		assertFalse(ask.isEmpty());
	}
	
	@Test
	public void testLiquidityTransfer()  {
		PriceLevel bid1 = SupplyLevel.newBidLevel(350, 78);
		PriceLevel bid2 = SupplyLevel.newBidLevel(100, 77.95);
		bid1.removeLiquidity(400);
		assertEquals(-50, bid1.getLiquidity(), 0.0);
		assertTrue(bid1.isEmpty());
		bid1.updateLevel(bid2);
		assertEquals(50, bid2.getLiquidity(), 0.0);
	}
	
	@Test
	public void testSameLevel() {
		PriceLevel ask = SupplyLevel.newAskLevel(1000, 78.5);
		assertTrue(ask.isCloseEnough(78.499999));
		assertTrue(ask.isCloseEnough(78.500001));
		assertFalse(ask.isCloseEnough(78.51));
		assertFalse(ask.isCloseEnough(78.49));
	}
	
	@Test
	public void testLiquidityGrouping() {
		PriceLevel bid1 = SupplyLevel.newBidLevel(350, 78);
		PriceLevel bid2 = SupplyLevel.newBidLevel(100, 77.95);
		PriceLevel bid3 = SupplyLevel.newBidLevel(100, 77.90);
		PriceLevel group = bid1.groupLiquidity(bid2);
		group = group.groupLiquidity(bid3);
		assertEquals(bid1.getLevelValue(), group.getLevelValue(), 0.0);
		assertEquals(bid1.getLiquidity()+bid2.getLiquidity()+bid3.getLiquidity(), group.getLiquidity(), 0.0);
		assertEquals(550, group.getLiquidity(), 0.0);
	}
	
	@Test
	public void testOrderObserver() throws IllegalArgumentException, NotEnoughBalanceExc, LevelNotFoundExc {
		double priceLevelValue = 995;
		boolean inactive = false;
		boolean active = true;
		boolean unfilled = false;
		boolean filled = true;
		
		Context broker = ConcreteContext.simulateDefaultContext(BrokerType.CLASSIC_INDEXES_BROKER);
		UserManager user1 = AbstractService.createFullService(broker, 1001);
		user1.BuyLimit(1, MarketPair.JUNIT_TEST_USD, priceLevelValue);
		
		assertEquals("\nAll dispatched orders[1] (from last to first): \n"
				+ "LIMIT ORDER: (BUY '1.0' shares on JUNIT_TEST_USD at 995.0 $) --> Status {active: "+active+", filled: "+unfilled+"}", user1.getOrderHistory());
		broker.getCurrentOrderBook(MarketPair.JUNIT_TEST_USD)
		.getLevel(priceLevelValue)
		.notifyLevelCrossed();
		assertEquals("\nAll dispatched orders[1] (from last to first): \n"
				+ "LIMIT ORDER: (BUY '1.0' shares on JUNIT_TEST_USD at 995.0 $) --> Status {active: "+inactive+", filled: "+filled+"}", user1.getOrderHistory());
		
		UserManager user2 = AbstractService.createFullService(broker, 1001);
		user2.BuyStopLimit(1, MarketPair.JUNIT_TEST_USD, priceLevelValue-1, priceLevelValue);
		
		assertEquals("\nAll dispatched orders[1] (from last to first): \n"
				+ "CONDITIONAL LIMIT ORDER: (BUY '1.0' shares on JUNIT_TEST_USD at 994.0 $, activaiton price is 995.0) "+
				"--> Status {active: "+inactive+", filled: "+unfilled+"}", user2.getOrderHistory());
		broker.getCurrentOrderBook(MarketPair.JUNIT_TEST_USD)
		.getLevel(priceLevelValue)
		.notifyLevelCrossed();
		assertEquals("\nAll dispatched orders[1] (from last to first): \n"
				+ "CONDITIONAL LIMIT ORDER: (BUY '1.0' shares on JUNIT_TEST_USD at 994.0 $, activaiton price is 995.0) "+
				"--> Status {active: "+active+", filled: "+unfilled+"}", user2.getOrderHistory());
		broker.getCurrentOrderBook(MarketPair.JUNIT_TEST_USD)
		.getLevel(priceLevelValue-1)
		.notifyLevelCrossed();
		assertEquals("\nAll dispatched orders[1] (from last to first): \n"
				+ "CONDITIONAL LIMIT ORDER: (BUY '1.0' shares on JUNIT_TEST_USD at 994.0 $, activaiton price is 995.0) "+
				"--> Status {active: "+inactive+", filled: "+filled+"}", user2.getOrderHistory());
	}
	
}
