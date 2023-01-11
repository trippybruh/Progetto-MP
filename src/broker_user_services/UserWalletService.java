package broker_user_services;

import java.util.HashMap;
import java.util.Map;

import broker_context.Context;
import broker_context.Market;
import order_book.Book;
import orders.OrderData;

public class UserWalletService extends AbstractService implements WalletManager {	
	
	private MarketDataStreamer dataStreamer;
	private double balance; /* USD */
	private Map<Market, Double> shares = new HashMap<Market, Double>();
	
	UserWalletService(Context broker, double balance) {
		super(broker);
		this.dataStreamer = createDataService(broker);
		this.balance = balance;
	}
	
	private double getOwnedShares(Market asset) {
		if (shares.get(asset) != null) {
			return shares.get(asset);
		} else return 0;
	}
	
	private double ownedSharesValue(Market asset) {
		return getOwnedShares(asset) * istantPrice(asset);
	}
	
	private double totalValue() {
		double value = balance;
		for (Market asset : getBroker().getBrokerType().getMarkets()) {
			value += getOwnedShares(asset) * istantPrice(asset);
		}
		if (value == 0) {
			return 0.01; 
		}
		return value;
	}
	
	public boolean canAffordBuy(double qty, Market market) throws NotEnoughBalanceExc, IllegalArgumentException {
		if (qty > 0) {
			if ((istantPrice(market) * qty) < balance) {
				return true;
			} else throw new NotEnoughBalanceExc("Not enough USD balance in your account to dispatch this buy order!");
		} else throw new IllegalArgumentException("Inserted buy amount cant be negative ("+qty+")!");
	}
	
	public boolean canAffordSell(double qty, Market market) throws NotEnoughBalanceExc, IllegalArgumentException {
		if (qty > 0) {
			if (getOwnedShares(market) >= qty) {
				return true;
			} else throw new NotEnoughBalanceExc("Not enough shares in your balance to dispatch this sell order!");
		} else throw new IllegalArgumentException("Inserted sell amount cant be negative ("+qty+")!");
	}
	
	public void upgradeBalanceBuying(OrderData buyOrder) {
		depositShares(buyOrder.getMarket(), buyOrder.getQuantity());
		withdrawFiat(buyOrder.getQuantityUSD());
	}
	
	public void upgradeBalanceSelling(OrderData sellOrder) {
		withdrawShares(sellOrder.getMarket(), sellOrder.getQuantity());
		depositFiat(sellOrder.getQuantityUSD());
	}

	public void depositFiat(double funds) {
		if (funds > 0) {
			balance += funds;
		} else throw new IllegalArgumentException("Deposit amount must be > 0");
	}
	
	public void withdrawAllFiat() {
		balance = 0;
	}
	
	public void withdrawFiat(double funds) {
		if (funds < balance) {
			balance -= funds;
		} else throw new IllegalArgumentException("Withdrawl amount ("+funds+") must be < than your current balance");
	}
	
	public void depositShares(Market market, double qty) {
		if (qty > 0) {
			shares.put(market, getOwnedShares(market) + qty);
		} else throw new IllegalArgumentException("Deposit amount must be > 0");
	}
	
	public void withdrawShares(Market market, double qty) {
		if (qty > 0 && qty < getOwnedShares(market)) {
			shares.put(market, getOwnedShares(market) - qty);
		} else throw new IllegalArgumentException();
	}

	public void withdrawAllShares(Market market) {
		if (getOwnedShares(market) > 0) {
			shares.put(market, 0.0);
		} else throw new IllegalArgumentException();
	}
	
	public String getAccountBalances() {
		double total = totalValue();
		String str = "\nBALANCE OVERVIEW --> Combined assets value: "+total+" $";
		for (Market asset : getBroker().getBrokerType().getMarkets()) {
			String symbol = asset+"";
			double sharesValue = ownedSharesValue(asset);
			str += "\n"+symbol.substring(0, 4)+" owned shares: "+getOwnedShares(asset)+" ("+sharesValue+" $) --> "
					+Book.uniformRounding(((sharesValue/total)*100)).doubleValue()+" % of total allocation";
		}
		return str += "\nUSD balance: "+balance+" $ --> "+Book.uniformRounding(((balance/total)*100))+" % of total allocation\"";
	}

	public String getDefaultOverviews() {
		return dataStreamer.getDefaultOverviews();
	}

	public String getDefaultOverview(Market market) {
		return dataStreamer.getDefaultOverview(market);
	}

	public String getGroupedOverviews(int groupSize) {
		return dataStreamer.getGroupedOverviews(groupSize);
	}

	public String getGroupedOverview(Market market, int groupSize) {
		return dataStreamer.getGroupedOverview(market, groupSize);
	}
	
	public int activeUsers() {
		return dataStreamer.activeUsers();
	}
	
}
