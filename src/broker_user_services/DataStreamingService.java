package broker_user_services;

import java.util.Date;

import broker_context.Context;
import broker_context.Market;

public class DataStreamingService extends AbstractService implements MarketDataStreamer {
	
	private int openMarkets;
	
	DataStreamingService(Context broker) {
		super(broker);
		this.openMarkets = broker.getBrokerType().getMarkets().size();
	}
	
	public int activeUsers() {
		return getBroker().activeUsers();
	}

	public String getDefaultOverviews() {
		String str = "\nMARKETS OVERVIEW";
		for(Market market : getBroker().getBrokerType().getMarkets()) {
			str += getBroker().getCurrentOrderBook(market).toString();
		}
		return str+openMarkets+" markets are currently opened /// Datetime ---> ("+new Date()+")";
	}

	public String getDefaultOverview(Market market) {
		return getBroker().getCurrentOrderBook(market).toString()+"\nDatetime ---> ("+new Date()+")";
	}

	public String getGroupedOverviews(int groupSize) {
		String str = "\nMARKETS OVERVIEW";
		for(Market market : getBroker().getBrokerType().getMarkets()) {
			str += getBroker().getCurrentOrderBook(market).toStringGroupedBy(groupSize);
		}
		return str+openMarkets+" markets are currently opened /// Datetime ---> ("+new Date()+")";
	}

	public String getGroupedOverview(Market market, int groupSize) {
		return getBroker().getCurrentOrderBook(market).toStringGroupedBy(groupSize)+"\nDatetime ---> ("+new Date()+")";
	}
	
}
