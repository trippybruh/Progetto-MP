package permission_based_APIclients;

import orders.OrderSide;

public interface FullClientManager extends PortfolioClientManager {
	
	/**Forwards a market order and 2 additional limit orders that will be dispatched to the order book if the starting market order
	 * is executed. Thesee 2 limit orders are denominated TP (take profit order) and SL (stop loss order) and are always both buy orders or both sell orders,
	 * a specific label is also attached to them with a reference to it's parent order. However TP/SL orders can be indipendently cancelled with {@link #cancelOrder(int)} method.
	 * @param quantity : The amount of shares of the preselected market symbol that user desires to acquire or sell
	 * @param side : Indicates whether to BUY or to SELL the specified amount with {@link OrderSide}. Make sure to own that 
	 * shares amount before forwarding a sell order with {@link #getAccountResources()}
	 * @param percentage : Indicates a percentage distance from the main order execution price that will be used to calculate 
	 * the additional limit order levels. Must be > 0, if not the market order is still executed but no other orders is created.
	 */
	void setTrade(double quantity, OrderSide side, double percentage);
	
	/**
	 * Forwards a market order to the preselected market, a market order is always immediatly
	 * executed at the best available price in the order book but if its liqudity is not enough to fill the whole order at the same 
	 * price level, the order book will fill the remaing quantity at lower (if selling) or higher (if buying) levels. 
	 * Note that maximum supported precision for all double type parameters is '0.01,
	 * any preciser input will be rounded to the closest allowed precision (51.127 -> 51.13).
	 * @param quantity : The amount of shares of the preselected market symbol that user desires to acquire or sell
	 * @param side : Indicates whether to BUY or to SELL the specified amount with {@link OrderSide}. Make sure to own that 
	 * shares amount before forwarding a sell order with {@link #getAccountResources()}
	 */
	void makeMarketOrder(double quantity, OrderSide side);
	
	/** Forwards a limit order to the preselected market, a limit order is never executed below(if selling)/over(if buying) the 
	 * 	specified limit price, this can cause a limit order to be partially filled and completely filled later on when the market
	 *  price crosses again the limit price. Note that maximum supported precision for all double type parameters is '0.01,
	 *  any preciser input will be rounded to the closest allowed precision (51.127 -> 51.13).
	 *  @param quantity : The amount of shares of the preselected market symbol that user desires to acquire or sell
	 *  @param side : Indicates whether to BUY or to SELL the specified amount with {@link OrderSide}. Make sure to own that 
	 * shares amount before forwarding a sell order with {@link #getAccountResources()}
	 * 	@param requestedPrice : Indicates the exact price where the order gets triggered
	 */
	void makeLimitOrder(double quantity, OrderSide side, double requestedPrice);
	
	/** This order is a limit order that allows to add another price condition that keeps the order
	 *  from being registered in the order book until an activation price is crossed, then it behaves exactly
	 *  like a limit order. Note that maximum supported precision for all double type parameters is '0.01,
	 *  any preciser input will be rounded to the closest allowed precision (51.127 -> 51.13).
	 * 	@param quantity : The amount of shares of the preselected market symbol that user desires to acquire or sell. Minimum is '0.01'
	 *  @param side : Indicates whether to BUY or to SELL the specified amount with {@link OrderSide}. Make sure to own that 
	 * shares amount before forwarding a sell order with {@link #getAccountResources()}
	 * 	@param requestedPrice : Indicates the exact price where the order gets triggered
	 * 	@param activationPrice : Indicates the price threeshold where the limit order is forwared to the order book
	 */
	void makeConditionalLimitOrder(double quantity, OrderSide side, double requestedPrice, double activationPrice);
	
	/** This method allows the user to delete a previously dispatched limit or stop limit type order from the order book, note that trying to cancel
	 *  an order which is already filled will raise an {@link #OrderNotFoundExc()}
	 *  @param id : unique integer representing a specific order, dispatched order id'scan be retrieved calling {@link #getOrderHistory()}
	 */
	void cancelOrder(int id);
	
	/** This method calls {@link #cancelOrder(int id)} on the last still pending dispatched order
	 */
	void cancelLastOrder();
	
	String getOrderHistory();
	
}
