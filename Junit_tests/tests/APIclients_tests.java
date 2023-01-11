package tests;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import broker_context.BrokerType;
import broker_context.ConcreteContext;
import broker_context.Context;
import broker_context.MarketPair;
import orders.OrderSide;
import permission_based_APIclients.APIClient;
import permission_based_APIclients.FullPermissionsClient;
import permission_based_APIclients.MarketDataPermissionClient;
import permission_based_APIclients.PortfolioPermissionsClient;
import permission_based_APIclients.MarketDataClientManager;
import permission_based_APIclients.FullClientManager;
import permission_based_APIclients.PortfolioClientManager;

public class APIclients_tests {
	
	private Context broker = ConcreteContext.simulateDefaultContext(BrokerType.CRYPTO_CURRENCIES_BROKER);
	private FullClientManager fullClient;
	private MarketDataClientManager dataStreamer;
	private PortfolioClientManager walletClient;
	
	@Before
	public void connecting() {
		this.fullClient = APIClient.buildFullClient(broker, 10000);
		this.dataStreamer = APIClient.buildDataStreamerClient(broker);
		this.walletClient = APIClient.buildPortfolioManagerClient(broker, 10000);
		fullClient.connectToMarket(MarketPair.JUNIT_TEST_USD);
		dataStreamer.connectToMarket(MarketPair.JUNIT_TEST_USD);
		walletClient.connectToMarket(MarketPair.JUNIT_TEST_USD);
	}
	
	@Test
	public void testPermissisonsAndNegativeBalanceInput() {
		assertTrue(fullClient.getClass() == FullPermissionsClient.class);
		assertTrue(walletClient.getClass() == PortfolioPermissionsClient.class);
		assertTrue(dataStreamer.getClass() == MarketDataPermissionClient.class);
		assertThatThrownBy(() -> APIClient.buildPortfolioManagerClient(broker, -1))
								.isInstanceOf(IllegalArgumentException.class);
	}
	
	@Test
	public void testMarketConnectionForAllClients() {
		assertEquals(1000, fullClient.getIstantPrice(), 0.0);
		assertEquals(1000, dataStreamer.getIstantPrice(), 0.0);
		assertEquals(1000, walletClient.getIstantPrice(), 0.0);
	}
	
	@Test
	public void testMarketDataStringsForAllClients() { /* this test may fail the comparison due to date object being created a second after */
		assertEquals(3, dataStreamer.getActiveUsers());
		APIClient.buildFullClient(broker, 1);
		assertEquals(4, walletClient.getActiveUsers());
		APIClient.buildPortfolioManagerClient(broker, 1);
		assertEquals(5, fullClient.getActiveUsers());
		
		assertEquals(dataStreamer.getWideMarketsOverviews(2), walletClient.getWideMarketsOverviews(2));
		assertEquals(walletClient.getWideMarketsOverviews(2), fullClient.getWideMarketsOverviews(2));
		
		assertEquals("\n(1001.0) ---> '10.0' shares\n"
				+ "(1000.9) ---> '10.0' shares\n"
				+ "(1000.8) ---> '10.0' shares\n"
				+ "(1000.7) ---> '10.0' shares\n"
				+ "(1000.6) ---> '10.0' shares\n"
				+ "(1000.5) ---> '10.0' shares\n"
				+ "(1000.4) ---> '10.0' shares\n"
				+ "(1000.3) ---> '10.0' shares\n"
				+ "(1000.2) ---> '10.0' shares\n"
				+ "----------------\n"
				+ "JUNIT_TEST_USD price -> 1000.0 $\n"
				+ "----------------\n"
				+ "(999.9) ---> '10.0' shares\n"
				+ "(999.8) ---> '10.0' shares\n"
				+ "(999.7) ---> '10.0' shares\n"
				+ "(999.6) ---> '10.0' shares\n"
				+ "(999.5) ---> '10.0' shares\n"
				+ "(999.4) ---> '10.0' shares\n"
				+ "(999.3) ---> '10.0' shares\n"
				+ "(999.2) ---> '10.0' shares\n"
				+ "(999.1) ---> '10.0' shares\n"
				+ "\n"
				+ "Datetime ---> ("+new Date()+")", dataStreamer.getDefaultMarketOverview());
		
		assertEquals("\n(1011.0) ---> '100.0' shares\n"
				+ "(1010.0) ---> '100.0' shares\n"
				+ "(1009.0) ---> '100.0' shares\n"
				+ "(1008.0) ---> '100.0' shares\n"
				+ "(1007.0) ---> '100.0' shares\n"
				+ "(1006.0) ---> '100.0' shares\n"
				+ "(1005.0) ---> '100.0' shares\n"
				+ "(1004.0) ---> '100.0' shares\n"
				+ "(1003.0) ---> '100.0' shares\n"
				+ "(1002.0) ---> '100.0' shares\n"
				+ "(1001.0) ---> '100.0' shares\n"
				+ "----------------\n"
				+ "JUNIT_TEST_USD price -> 1000.0 $\n"
				+ "----------------\n"
				+ "(999.0) ---> '100.0' shares\n"
				+ "(998.0) ---> '100.0' shares\n"
				+ "(997.0) ---> '100.0' shares\n"
				+ "(996.0) ---> '100.0' shares\n"
				+ "(995.0) ---> '100.0' shares\n"
				+ "(994.0) ---> '100.0' shares\n"
				+ "(993.0) ---> '100.0' shares\n"
				+ "(992.0) ---> '100.0' shares\n"
				+ "(991.0) ---> '100.0' shares\n"
				+ "(990.0) ---> '100.0' shares\n"
				+ "(989.0) ---> '100.0' shares\n"
				+ "\n"
				+ "Datetime ---> ("+new Date()+")", fullClient.getWideMarketOverview(1));
	}
	
	@Test
	public void testWalletClientOperations() {
		walletClient.depositShares(5);
		walletClient.depositFiat(10000);
		assertEquals("\nBALANCE OVERVIEW --> Combined assets value: 25000.0 $\n"
				+ "BTC_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "ETH_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "JUNI owned shares: 5.0 (5000.0 $) --> 20.0 % of total allocation\n"
				+ "USD balance: 20000.0 $ --> 80.00 % of total allocation\"", walletClient.getAccountBalances());
		walletClient.withdrawFiat(19000);
		walletClient.withdrawShares(4);
		assertEquals("\nBALANCE OVERVIEW --> Combined assets value: 2000.0 $\n"
				+ "BTC_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "ETH_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "JUNI owned shares: 1.0 (1000.0 $) --> 50.0 % of total allocation\n"
				+ "USD balance: 1000.0 $ --> 50.00 % of total allocation\"", walletClient.getAccountBalances());
		walletClient.withdrawAllFiat();
		walletClient.withdrawAllShares();
		assertEquals("\nBALANCE OVERVIEW --> Combined assets value: 0.01 $\n"
				+ "BTC_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "ETH_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "JUNI owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "USD balance: 0.0 $ --> 0.00 % of total allocation\"", walletClient.getAccountBalances());
	}
	
	@Test
	public void testWalletOfFullClient() {
		fullClient.depositShares(5);
		fullClient.depositFiat(10000);
		assertEquals("\nBALANCE OVERVIEW --> Combined assets value: 25000.0 $\n"
				+ "BTC_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "ETH_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "JUNI owned shares: 5.0 (5000.0 $) --> 20.0 % of total allocation\n"
				+ "USD balance: 20000.0 $ --> 80.00 % of total allocation\"", fullClient.getAccountBalances());
		fullClient.withdrawFiat(19000);
		fullClient.withdrawShares(4);
		assertEquals("\nBALANCE OVERVIEW --> Combined assets value: 2000.0 $\n"
				+ "BTC_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "ETH_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "JUNI owned shares: 1.0 (1000.0 $) --> 50.0 % of total allocation\n"
				+ "USD balance: 1000.0 $ --> 50.00 % of total allocation\"", fullClient.getAccountBalances());
		fullClient.withdrawAllFiat();
		fullClient.withdrawAllShares();
		assertEquals("\n"
				+ "BALANCE OVERVIEW --> Combined assets value: 0.01 $\n"
				+ "BTC_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "ETH_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "JUNI owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "USD balance: 0.0 $ --> 0.00 % of total allocation\"", fullClient.getAccountBalances());
	}
	
	@Test
	public void testMarketBehaviour() {
		fullClient.makeMarketOrder(5, OrderSide.BUY);
		fullClient.makeMarketOrder(2, OrderSide.SELL);
		
		String balanceChangeOnlyAfterMarketOrders = "\nBALANCE OVERVIEW --> Combined assets value: 10000.0 $\n"
				+ "BTC_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "ETH_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "JUNI owned shares: 3.0 (3000.0 $) --> 30.0 % of total allocation\n"
				+ "USD balance: 7000.0 $ --> 70.00 % of total allocation\"";
		
		assertEquals(balanceChangeOnlyAfterMarketOrders, fullClient.getAccountBalances());
		fullClient.makeLimitOrder(1, OrderSide.SELL, 1003.5);
		fullClient.makeConditionalLimitOrder(5, OrderSide.BUY, 990, 995);
		assertEquals(balanceChangeOnlyAfterMarketOrders, fullClient.getAccountBalances());
		
		String orderHistoryShowsBothOrderUnfilled = "\nAll dispatched orders[4] (from last to first): \n"
				+ "CONDITIONAL LIMIT ORDER: (BUY '5.0' shares on JUNIT_TEST_USD at 990.0 $, activaiton price is 995.0) --> Status {active: false, filled: false}\n"
				+ "LIMIT ORDER: (SELL '1.0' shares on JUNIT_TEST_USD at 1003.5 $) --> Status {active: true, filled: false}\n"
				+ "MARKET ORDER: (SELL '2.0' shares on JUNIT_TEST_USD at market price [1000.0 $]) filled: true\n"
				+ "MARKET ORDER: (BUY '5.0' shares on JUNIT_TEST_USD at market price [1000.0 $]) filled: true";
		assertEquals(orderHistoryShowsBothOrderUnfilled,fullClient.getOrderHistory());
		
		/* small orders like the previous carry in/out to few liquidity to move market price significantly */
		FullClientManager marketMover =  APIClient.buildFullClient(broker, 100000000);
		marketMover.connectToMarket(MarketPair.JUNIT_TEST_USD);
		marketMover.makeMarketOrder(2000, OrderSide.BUY);
		marketMover.makeMarketOrder(1500, OrderSide.SELL);
		/* both limit and stop limit orders of 'fullClient' got executed now */
		
		String balanceChangeWithMarketMovement = "\nBALANCE OVERVIEW --> Combined assets value: 9963.2 $\n" 
				+ "BTC_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "ETH_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "JUNI owned shares: 7.0 (6909.7 $) --> 69.35 % of total allocation\n"
				+ "USD balance: 3053.5 $ --> 30.65 % of total allocation\"";
		
		assertEquals(balanceChangeWithMarketMovement, fullClient.getAccountBalances());	
		
		String orderHistoryShowsTheSame = "\nAll dispatched orders[4] (from last to first): \n"
				+ "CONDITIONAL LIMIT ORDER: (BUY '5.0' shares on JUNIT_TEST_USD at 990.0 $, activaiton price is 995.0) --> Status {active: false, filled: true}\n"
				+ "LIMIT ORDER: (SELL '1.0' shares on JUNIT_TEST_USD at 1003.5 $) --> Status {active: false, filled: true}\n"
				+ "MARKET ORDER: (SELL '2.0' shares on JUNIT_TEST_USD at market price [1000.0 $]) filled: true\n"
				+ "MARKET ORDER: (BUY '5.0' shares on JUNIT_TEST_USD at market price [1000.0 $]) filled: true";
		
		assertEquals(orderHistoryShowsTheSame, fullClient.getOrderHistory());
	}
	
	@Test
	public void testMarketObservers() {
		fullClient.depositFiat(100000000); 
		assertEquals("None", fullClient.getLatestPriceAlert()); 
		assertEquals(fullClient.getLatestPriceAlert(), dataStreamer.getLatestPriceAlert());
		assertEquals(fullClient.getLatestPriceAlert(), walletClient.getLatestPriceAlert());
		walletClient.deactivateAlerts(); /* all client activate alerts on creation */
		
		String alertMessageChangeOnlyIfTheMarketMovesMoreThan3percent = "JUNIT_TEST_USD price suddently increased by 4.38 %, now it is 1043.8 $";
		fullClient.makeMarketOrder(4381.4, OrderSide.BUY);
		assertEquals(alertMessageChangeOnlyIfTheMarketMovesMoreThan3percent, fullClient.getLatestPriceAlert());
		assertEquals(alertMessageChangeOnlyIfTheMarketMovesMoreThan3percent, dataStreamer.getLatestPriceAlert());
		assertEquals("None", walletClient.getLatestPriceAlert()); 
		
		fullClient.makeMarketOrder(2999, OrderSide.BUY); /* moves the market by 2.something % */
		assertEquals(alertMessageChangeOnlyIfTheMarketMovesMoreThan3percent, fullClient.getLatestPriceAlert());
		assertEquals(alertMessageChangeOnlyIfTheMarketMovesMoreThan3percent, dataStreamer.getLatestPriceAlert());
		assertEquals("None", walletClient.getLatestPriceAlert());
		dataStreamer.deactivateAlerts();
		walletClient.activateAlerts();
		
		String alertMessageChanged = "JUNIT_TEST_USD price suddently decreased by -12.71 %, now it is 937.3 $";
		fullClient.makeMarketOrder(7000, OrderSide.SELL);
		assertEquals(alertMessageChanged, fullClient.getLatestPriceAlert());
		assertEquals(alertMessageChangeOnlyIfTheMarketMovesMoreThan3percent, dataStreamer.getLatestPriceAlert());
		assertEquals(fullClient.getLatestPriceAlert(), walletClient.getLatestPriceAlert());
	}
	
	
}
