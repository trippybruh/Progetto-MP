package tests;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import broker_context.BrokerType;
import broker_context.ConcreteContext;
import broker_context.Context;
import broker_context.Market;
import broker_context.MarketPair;
import broker_user_services.AbstractService;
import broker_user_services.NotEnoughBalanceExc;
import broker_user_services.WalletManager;
import orders.Order;
import orders.OrderSide;

public class Wallet_service_test {
	
	private Market m = MarketPair.JUNIT_TEST_USD;
	private WalletManager wallet;
	private Context broker;
	
	@Before
	public void walletCreation() {
		this.broker =  ConcreteContext.simulateDefaultContext(BrokerType.CRYPTO_CURRENCIES_BROKER);
		this.wallet = AbstractService.createWalletService(broker, 10000);
	}
	
	@Test
	public void testUserOperations() {
		wallet.depositShares(m, 5);
		wallet.depositFiat(10000);
		assertEquals("\nBALANCE OVERVIEW --> Combined assets value: 25000.0 $\n"
				+ "BTC_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "ETH_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "JUNI owned shares: 5.0 (5000.0 $) --> 20.0 % of total allocation\n"
				+ "USD balance: 20000.0 $ --> 80.00 % of total allocation\"", wallet.getAccountBalances());
		wallet.withdrawFiat(19000);
		wallet.withdrawShares(m, 4);
		assertEquals("\nBALANCE OVERVIEW --> Combined assets value: 2000.0 $\n"
				+ "BTC_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "ETH_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "JUNI owned shares: 1.0 (1000.0 $) --> 50.0 % of total allocation\n"
				+ "USD balance: 1000.0 $ --> 50.00 % of total allocation\"", wallet.getAccountBalances());
		wallet.withdrawAllFiat();
		wallet.withdrawAllShares(m);
		assertEquals("\nBALANCE OVERVIEW --> Combined assets value: 0.01 $\n"
				+ "BTC_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "ETH_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "JUNI owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "USD balance: 0.0 $ --> 0.00 % of total allocation\"", wallet.getAccountBalances());
	}
	
	@Test
	public void testInnerOperations() throws IllegalArgumentException, NotEnoughBalanceExc {
		/* 10 shares * 1000usd each = starting balance */
		assertTrue(wallet.canAffordBuy(9, m));
		assertThatThrownBy(() -> wallet.canAffordBuy(11, m))
					.isInstanceOf(NotEnoughBalanceExc.class);
		assertThatThrownBy(() -> wallet.canAffordBuy(-11, m))
					.isInstanceOf(IllegalArgumentException.class);
		wallet.depositShares(m, 10);
		assertTrue(wallet.canAffordSell(9, m));
		assertThatThrownBy(() -> wallet.canAffordSell(11, m))
					.isInstanceOf(NotEnoughBalanceExc.class);
		assertThatThrownBy(() -> wallet.canAffordSell(-11, m))
					.isInstanceOf(IllegalArgumentException.class);
		
		wallet.upgradeBalanceBuying(Order.buildNewMarketOrder(m, broker.getOrderBookPrice(m), 5, OrderSide.BUY));
		assertTrue(wallet.canAffordSell(11, m));
		assertThatThrownBy(() -> wallet.canAffordBuy(9, m))
					.isInstanceOf(NotEnoughBalanceExc.class);
		assertEquals("\nBALANCE OVERVIEW --> Combined assets value: 20000.0 $\n"
				+ "BTC_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "ETH_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "JUNI owned shares: 15.0 (15000.0 $) --> 75.0 % of total allocation\n"
				+ "USD balance: 5000.0 $ --> 25.00 % of total allocation\"", wallet.getAccountBalances());
		
		wallet.upgradeBalanceSelling(Order.buildNewMarketOrder(m, broker.getOrderBookPrice(m), 5, OrderSide.SELL));
		assertTrue(wallet.canAffordBuy(9, m));
		assertThatThrownBy(() -> wallet.canAffordSell(11, m))
					.isInstanceOf(NotEnoughBalanceExc.class);
		assertEquals("\nBALANCE OVERVIEW --> Combined assets value: 20000.0 $\n"
				+ "BTC_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "ETH_ owned shares: 0.0 (0.0 $) --> 0.0 % of total allocation\n"
				+ "JUNI owned shares: 10.0 (10000.0 $) --> 50.0 % of total allocation\n"
				+ "USD balance: 10000.0 $ --> 50.00 % of total allocation\"", wallet.getAccountBalances());
	}
	
}
