package orders;

final class StopLimitOrder extends Order {
	
	private boolean filled = false;
	private boolean active = false;
	
	public void updateFilled() {
		this.filled = true;
	}
	
	public void updateActivation() {
		this.active = !active;
	}
	
	public boolean isFilled() {
		return filled;
	}
	
	public boolean isActive(){
		return active;
	}
	
	public boolean isMarket() {
		return false;
	}

	public boolean isLimit() {
		return false;
	}
	
	public boolean isStopLimit() {
		return true;
	}

	public String toString() {
		return "\nCONDITIONAL LIMIT ORDER: (ORDER ID: " + getID() +", "+getSide()+" '"+getQuantity()+"' shares on "+getMarket()+ " at "+getRequestedPrice()
		+" $, activaiton price is "+getActivationPrice()+") --> Status {active: "+isActive()+", filled: "+isFilled()+"}";
	}
	
	public String toStringForTesting() {
		return "\nCONDITIONAL LIMIT ORDER: ("+getSide()+" '"+getQuantity()+"' shares on "+getMarket()+ " at "+getRequestedPrice()
		+" $, activaiton price is "+getActivationPrice()+") --> Status {active: "+isActive()+", filled: "+isFilled()+"}";
	}

	public void markStopLossOrder(OrderStatus SL) {}

	public void markTakeProfitOrder(OrderStatus TP) {}

}
