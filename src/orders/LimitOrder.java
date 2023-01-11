package orders;

final class LimitOrder extends Order {
	
	private boolean filled = false;
	private boolean active = true;
	
	public void updateFilled() {
		this.filled = true;
	}
	
	public void updateActivation(){
		this.active = false;
	}

	public boolean isFilled() {
		return filled;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public boolean isMarket() {
		return false;
	}

	public boolean isLimit() {
		return true;
	}
	
	public boolean isStopLimit() {
		return false;
	}

	public String toString() {
		return "\nLIMIT ORDER: (ORDER ID: " + getID() +", "+getSide()+" '"+getQuantity()+"' shares on "+getMarket()+ " at "+getRequestedPrice()+" $) "
				+ "--> Status {active: "+isActive()+", filled: "+isFilled()+"}";
	}
	
	public String toStringForTesting() {
		return "\nLIMIT ORDER: ("+getSide()+" '"+getQuantity()+"' shares on "+getMarket()+ " at "+getRequestedPrice()+" $) "
				+ "--> Status {active: "+isActive()+", filled: "+isFilled()+"}";
	}

	public void markStopLossOrder(OrderStatus SL) {}

	public void markTakeProfitOrder(OrderStatus TP) {}
	
}
