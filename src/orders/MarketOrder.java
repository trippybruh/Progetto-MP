package orders;

final class MarketOrder extends Order {
	
	private String TPSLreference = "";
	
	public void updateFilled(){}
	
	public void updateActivation(){}

	public boolean isFilled() {
		return true;
	}
	
	public boolean isActive() {
		return false;
	}
	
	public boolean isMarket() {
		return true;
	}

	public boolean isLimit() {
		return false;
	}
	
	public boolean isStopLimit() {
		return false;
	}
	
	public String toString() {
		return "\nMARKET ORDER: (ORDER ID: "+getID()+", "+getSide()+" '"+getQuantity()+"' shares on "+getMarket()+
				" at market price ["+getQuantityUSD()/getQuantity()+" $]) filled: "+isFilled()+TPSLreference;
	}
	
	public String toStringForTesting() {
		return "\nMARKET ORDER: ("+getSide()+" '"+getQuantity()+"' shares on "+getMarket()+
				" at market price ["+getQuantityUSD()/getQuantity()+" $]) filled: "+isFilled()+TPSLreference;
	}

	public void markStopLossOrder(OrderStatus SLOrder) {
		TPSLreference += SLOrder.toStringForTesting()+" ---> STOP LOSS ORDER ID OF '" /*getID()*/+123456789+"'";
	}

	public void markTakeProfitOrder(OrderStatus TPOrder) {
		TPSLreference += TPOrder.toStringForTesting()+" ---> TAKE PROFIT ORDER ID OF '"/*getID()*/+123456789+"'";
	}

}


