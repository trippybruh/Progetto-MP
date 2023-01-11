package order_book;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import broker_context.Market;
import broker_context.MarketPair;
import broker_user_services.OrderObserver;
import orders.OrderHandler;
import orders.OrderStatus;

public class Book implements OrderBook {
	
	private Market market;
	private List<PriceLevel> bids;
	private List<PriceLevel> asks;
	private List<OrderHandler> orderHandlers;
	private double currentPrice;
	private double lastPriceChange = 0.0;
	
	/**This constant holds the maximum shares that can be generated to fill the void space in the order book 
	 * when a large market order empties one or more price levels, can be seen as the maximum expected market reaction to the price shift 
	 * that has occured, changed to 1 for easier testing */	
	private static final int MAX_NEW_SHARES = 1;
	
	/**This constant holds the minimum required percentage shift caused by a single order to trigger a warning notice that will be dispacthed to all clients 
	 */
	private static final int MIN_CHANGE = 3;
	
	/**This constant holds the showed level above and below current price when clients request market book data
	 */
	private static final int VIEW_SIZE = 10;
	
	/**This constant holds the rounding precision after the integer part for all order book double values, higher numbers means higher precision
	 */
	private static final int DEFAULT_ROUNDING = 2;
	
	public static OrderBook simulateDefaultOrderBook(Market market) {
		return new BookBuilder(market)
				.generateDefaultMarket(true)
				.createBook();
	}
	
	public static OrderBook simulateUnstableOrderBook(Market market) {
		return new BookBuilder(market)
				.generateDefaultMarket(false)
				.createBook();
	}
	
	public static OrderBook simulateOverBoughtOrderBook(Market market) {
		return new BookBuilder(market)
				.generateOverboughtMarket()
				.createBook();
	}
	
	public static OrderBook simulateOverSoldOrderBook(Market market) {
		return new BookBuilder(market)
				.generateOversoldMarket()
				.createBook();
	}
	
	private Book(Market market, List<PriceLevel> bids, List<PriceLevel> asks, double startingPrice) {
		this.market = market;
		this.bids = bids;
		this.asks = asks;
		this.currentPrice = startingPrice;
		addOrderHandlers();
	}
	
	private void addOrderHandlers() {
		OrderHandler slHandler = OrderHandler.stopLimitHandler(this);
		OrderHandler limitHandler = OrderHandler.limitHandler(this);
		OrderHandler marketHandler = OrderHandler.marketHandler(this);
		slHandler.setNextHandler(limitHandler);
		limitHandler.setNextHandler(marketHandler);
		marketHandler.setNextHandler(null);
		this.orderHandlers = List.of(slHandler, limitHandler, marketHandler);		
	}
	
	public double getSpread() {
		return market.getOrderBookSpread();
	}
	
	public double getCurrentPrice() {
		return currentPrice;
	}
	
	public Market getMarketPair() {
		return market;
	}
	
	public double getLastPriceChange() {
		return lastPriceChange;
	}
	
	public boolean verifyPriceChange() {
		return Math.abs(lastPriceChange) >= MIN_CHANGE;
	}
	
	public void updateCurrentPrice(double newPrice) {
		lastPriceChange = (((newPrice/currentPrice)-1)*100);
		currentPrice = uniformRounding(newPrice).doubleValue();
	}
	
	public boolean handledOrder(OrderStatus order, OrderObserver sender) throws LevelNotFoundExc {
		orderHandlers.forEach(handler -> handler.setOrder(order));
		return orderHandlers.get(0).process(sender);
	}
	
	public PriceLevel getLevel(double level) throws LevelNotFoundExc {
		BigDecimal roundedLevel = uniformRounding(level);
		if (roundedLevel.doubleValue() > currentPrice && asks.stream().anyMatch(ask -> ask.isCloseEnough(roundedLevel.doubleValue()))) {
			for (PriceLevel ask : asks) {
				if (ask.isCloseEnough(roundedLevel.doubleValue())) {
					return ask;
				}
			}
		} else if (roundedLevel.doubleValue() < currentPrice && bids.stream().anyMatch(bid -> bid.isCloseEnough(roundedLevel.doubleValue()))) {
			for (PriceLevel bid : bids) {
				if (bid.isCloseEnough(roundedLevel.doubleValue())) {
					return bid;
				}
			}
		} 
		throw new LevelNotFoundExc(roundedLevel.doubleValue());
	}
	
	public void fillEmptyLevels(boolean wasBuy) {
		if (wasBuy) {
			int toBeFilled = asks.size();
			asks.removeIf(ask -> ask.getLiquidity() < 0);
			toBeFilled -= asks.size() - 1;
			while (toBeFilled > 0) {
				bids.add(0, SupplyLevel.newBidLevel(MAX_NEW_SHARES, uniformRounding(currentPrice - getSpread() * toBeFilled).doubleValue()));
				toBeFilled--;
			}
		} else {
			int toBeFilled = bids.size();
			bids.removeIf(bid -> bid.getLiquidity() < 0);
			toBeFilled -= bids.size() - 1;
			while (toBeFilled > 0) {
				asks.add(0, SupplyLevel.newAskLevel(MAX_NEW_SHARES, uniformRounding(currentPrice + getSpread() * toBeFilled).doubleValue()));
				toBeFilled--;
			}
		}
	}
	
	public String toString() {
		double viewSize = getSpread()*VIEW_SIZE; /* order book view size is limited to 10 price levels above and below price */
		String asksView = "\n";
		String bidsView = "\n";
		List<PriceLevel> asksCopy = new ArrayList<>();
		asksCopy.addAll(asks);
		asksCopy.removeIf(a -> a.getLevelValue() > currentPrice+viewSize); 
		
		ListIterator<PriceLevel> asksNearPrice = asksCopy.listIterator(asksCopy.size()); /* cannot specify the reversed iterator with streams */
		Iterator<PriceLevel> bidsNearPrice = bids.stream().filter(bid -> bid.getLevelValue() > currentPrice-viewSize).iterator();
		while (asksNearPrice.hasPrevious() && bidsNearPrice.hasNext()) {
			asksView += asksNearPrice.previous().toString();
			bidsView += bidsNearPrice.next().toString();
		}
		return asksView+"----------------\n"+market+" price -> "+currentPrice+" $\n----------------"+bidsView;
	}
	
	private PriceLevel levelGrouper(ListIterator<PriceLevel> levelsIter) {
		PriceLevel referenceLevel = levelsIter.previous();
		while (levelsIter.hasPrevious()) {
			referenceLevel = referenceLevel.groupLiquidity(levelsIter.previous());
		}
		return referenceLevel;	
	}
	
	public String toStringGroupedBy(int groupSize) {
		try {
			int actualSize = (int) (groupSize/getSpread());
			String asksView = "\n";
			String bidsView = "\n";
 			List<PriceLevel> asksGrouped = new ArrayList<>();
			List<PriceLevel> bidsGrouped = new ArrayList<>();
			
			while (asksGrouped.size() <= VIEW_SIZE) {
				ListIterator<PriceLevel> asksIter = asks.subList(asksGrouped.size()*actualSize, (asksGrouped.size()*actualSize)+actualSize).listIterator(actualSize);
				ListIterator<PriceLevel> bidsIter = bids.subList(bidsGrouped.size()*actualSize, (bidsGrouped.size()*actualSize)+actualSize).listIterator(actualSize);
				asksGrouped.add(levelGrouper(asksIter));
				bidsGrouped.add(levelGrouper(bidsIter));
			}
			
			ListIterator<PriceLevel> asksGroupedIter = asksGrouped.listIterator(asksGrouped.size());
			Iterator<PriceLevel> bidsGroupedIter = bidsGrouped.iterator();
			while (asksGroupedIter.hasPrevious() && bidsGroupedIter.hasNext())  {
				asksView += asksGroupedIter.previous().toString();
				bidsView += bidsGroupedIter.next().toString();
			}
			return asksView+"----------------\n"+market+" price -> "+currentPrice+" $\n----------------"+bidsView;
		} catch (IndexOutOfBoundsException exc) {
			System.out.println(exc.getMessage());
		}
		return "Unable to provide selected grouped view, try reducing group size parameter!";
	}
	
	public static BigDecimal uniformRounding(double value) {
		return new BigDecimal(value).setScale(DEFAULT_ROUNDING, RoundingMode.HALF_UP);
	}
	
	private static class BookBuilder {
		
		/**This constant holds the maximum shares that can be generated for any given liquidity level while creating the orderbook itself
		 */	
		private static final int MAX_SHARES = 50;
		
		/**This constant holds the total generated levels of both asks and bids in the order book, therefore
		 * orders that specify prices that are about more than 50% higher/lower than the starting reference price could be rejcted
		 * throwing {@link #LevelNotFoundExc}, to avoid this price related parameters should stay within the current price and the following thresholds:
		 * <ul>
		 * 	<li>1) ASSET PRICE - (SPREADxTHIS VALUE) for buy orders.</li>
		 * 	<li>2) ASSET PRICE + (SPREADxTHIS VALUE) for sell orders.</li>
		 * </ul> */
		private static final int ORDERBOOK_DEPTH = 5000;

		private List<PriceLevel> bids = new ArrayList<>();
		private List<PriceLevel> asks = new ArrayList<>(); 
		private double startingPrice;
		private double spread;
		private Market market;
		private Random generator = new Random();
		
		private BookBuilder(Market market) {
			this.market = market;
			this.startingPrice = market.getReferencePrice();
			this.spread = market.getOrderBookSpread();
		}
		
		private Book createBook() {
			return new Book(market, bids, asks, startingPrice);
		}
		
		/**The order book generated with this method has a fixed liquidity value of 10 for any created price level, it's only
		 * purpose is to provide an easier Junit testing target for the developer
		 */
		private BookBuilder TestMarket() {
			BigDecimal OB_Spread = uniformRounding(spread);
			BigDecimal currentBidLevel = uniformRounding(startingPrice).subtract(OB_Spread);
			BigDecimal currentAskLevel = currentBidLevel.add(OB_Spread).add(OB_Spread);
			double defaultTestingLiquidity = 10.0;
			while (bids.size() < ORDERBOOK_DEPTH) {
				bids.add(SupplyLevel.newBidLevel(defaultTestingLiquidity, currentBidLevel.doubleValue()));
				asks.add(SupplyLevel.newAskLevel(defaultTestingLiquidity, currentAskLevel.doubleValue()));
				currentBidLevel = currentBidLevel.subtract(OB_Spread);
				currentAskLevel = currentAskLevel.add(OB_Spread);
			}
			return this;
		}
		
		/**Default order book generation method
		 * @param stable : If true creates an order book where generated liquidty is randomly scattered across all price levels,
		 * if false creates an order book where liquidity gets distributed between ask and bid levels in a perfectly simmetrical way 
		 * and ascending order, this may result in a very high volatily scenario since there will be close to zero liquidity near
		 * the current price, indicative of a situation with both low buy and sell volume.
		 */
		private BookBuilder generateDefaultMarket(boolean stable) {
			if (market == MarketPair.JUNIT_TEST_USD) {
				return TestMarket();
			}
			
			Iterator<Double> liquidityForLevels = generator.doubles(ORDERBOOK_DEPTH, 0, MAX_SHARES).iterator();
			if (!stable) {
				liquidityForLevels = generator.doubles(ORDERBOOK_DEPTH, 0, MAX_SHARES).sorted().iterator();
			}
			BigDecimal OB_Spread = uniformRounding(spread);
			BigDecimal currentBidLevel = uniformRounding(startingPrice).subtract(OB_Spread);
			BigDecimal currentAskLevel = currentBidLevel.add(OB_Spread).add(OB_Spread);
			 
			while (liquidityForLevels.hasNext()) {
				bids.add(SupplyLevel.newBidLevel(uniformRounding(liquidityForLevels.next()).doubleValue(), currentBidLevel.doubleValue()));
				asks.add(SupplyLevel.newAskLevel(uniformRounding(liquidityForLevels.next()).doubleValue(), currentAskLevel.doubleValue()));
				currentBidLevel = currentBidLevel.subtract(OB_Spread);
				currentAskLevel = currentAskLevel.add(OB_Spread);
			}
			return this;
		}
		
		/**Creates an order book which represent a situation of high buy volume in the market, modifiying generated
		 * liquidty levels and depth accordingly.
		 */
		private BookBuilder generateOverboughtMarket () {
			if (market == MarketPair.JUNIT_TEST_USD) {
				return TestMarket();
			}
			
			int bias = new Random().nextInt(4) + 2;
			Iterator<Double> liquidityForBids = generator.doubles(ORDERBOOK_DEPTH/bias, 10, MAX_SHARES*(bias/2)).iterator();
			Iterator<Double> liquidityForAsks = generator.doubles(ORDERBOOK_DEPTH, 0, MAX_SHARES/bias).iterator();
			BigDecimal OB_Spread = uniformRounding(spread);
			BigDecimal currentBidLevel = uniformRounding(startingPrice).subtract(OB_Spread);
			BigDecimal currentAskLevel = currentBidLevel.add(OB_Spread).add(OB_Spread);
			
			while(liquidityForAsks.hasNext()) {
				asks.add(SupplyLevel.newAskLevel(uniformRounding(liquidityForAsks.next()).doubleValue(), currentBidLevel.doubleValue()));
				currentAskLevel = currentAskLevel.add(OB_Spread);
				if (liquidityForBids.hasNext()) {
					bids.add(SupplyLevel.newBidLevel(uniformRounding(liquidityForBids.next()).doubleValue(), currentAskLevel.doubleValue()));
					currentBidLevel = currentBidLevel.subtract(OB_Spread);
					currentAskLevel = currentAskLevel.add(OB_Spread);
				}
			}
			return this;
		}
		
		/** Creates an order book which represent a situation of high sell volume in the market, modifiying generated
		 *  liquidty levels and depth accordingly.
		 */
		private BookBuilder generateOversoldMarket() {
			if (market == MarketPair.JUNIT_TEST_USD) {
				return TestMarket();
			}
			
			int bias = new Random().nextInt(3) + 2;
			Iterator<Double> liquidityForBids = generator.doubles(ORDERBOOK_DEPTH, 0, MAX_SHARES/bias).iterator();
			Iterator<Double> liquidityForAsks = generator.doubles(ORDERBOOK_DEPTH/bias, 10, MAX_SHARES*(bias/2)).iterator();
			BigDecimal OB_Spread = uniformRounding(spread);
			BigDecimal currentBidLevel = uniformRounding(startingPrice).subtract(OB_Spread);
			BigDecimal currentAskLevel = currentBidLevel.add(OB_Spread).add(OB_Spread);
			
			while(liquidityForBids.hasNext()) {
				bids.add(SupplyLevel.newBidLevel(uniformRounding(liquidityForBids.next()).doubleValue(), currentBidLevel.doubleValue()));
				currentBidLevel = currentBidLevel.subtract(OB_Spread);
				if (liquidityForAsks.hasNext()) {
					asks.add(SupplyLevel.newAskLevel(uniformRounding(liquidityForAsks.next()).doubleValue(), currentAskLevel.doubleValue()));
					currentAskLevel = currentAskLevel.add(OB_Spread);
				}
			}
			return this;
		}
	}
}
