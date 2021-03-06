package premiumtravel.trip;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import premiumtravel.billing.Bill;
import premiumtravel.billing.PaymentType;
import premiumtravel.billing.Product;
import premiumtravel.cache.PremiumTravelCache;
import premiumtravel.cache.RegistryObject;
import premiumtravel.itinerary.AbstractItineraryComponent;
import premiumtravel.itinerary.DecorativeItineraryComponent;
import premiumtravel.itinerary.ItineraryComponent;
import premiumtravel.itinerary.ReservationItineraryComponent;
import premiumtravel.people.TravelAgent;
import premiumtravel.people.Traveller;
import premiumtravel.state.StateController;
import premiumtravel.state.States;

import java.math.BigDecimal;
import java.util.*;

/**
 *
 */
public class Trip implements Product, RegistryObject {

	private static final Logger logger = LogManager.getLogger( "premiumtravel.PremiumTravelServer" );
	private static final Set<UUID> tripIDList = new LinkedHashSet<>();
	private static final long serialVersionUID = -6566205810191038527L;

	private final UUID tripID;

	private List<Reservation> reservations = new LinkedList<>();
	private List<UUID> travellerIDs = new LinkedList<>();
	private PaymentType paymentType;
	private transient PremiumTravelCache cache;
	private UUID travelAgentID;
	private String thankYouNote;
	private States state;
	private Bill bill;
	private ItineraryComponent itinerary = AbstractItineraryComponent.getBaseComponent();

	/**
	 * Creates a new Trip, organized by the given {@link TravelAgent}.
	 *
	 * @param travelAgent The agent responsible for the new trip.
	 */
	public Trip( PremiumTravelCache cache, TravelAgent travelAgent ) {
		super();
		this.cache = cache;
		this.state = States.ADD_TRAVELLERS;
		this.travelAgentID = travelAgent.getID();

		// Generate a new ID
		UUID generatedID = null;
		boolean idSet = false;
		while ( !idSet ) {
			try {
				generatedID = UUID.randomUUID();
				tripIDList.add( generatedID );
				idSet = true;
			} catch ( UnsupportedOperationException e ) {
				// Do nothing
			}
		}

		this.tripID = generatedID;
	}

	public ItineraryComponent getItinerary() {
		return itinerary;
	}

	public PaymentType getPaymentType() {
		return paymentType;
	}

	public void setPaymentType( PaymentType paymentType ) {
		this.paymentType = paymentType;
	}

	public List<Reservation> getReservations() {
		return reservations;
	}

	public void addReservation( Reservation reservation ) {
		this.reservations.add( reservation );
		this.itinerary = new ReservationItineraryComponent( this.itinerary, reservation );
	}

	public List<Traveller> getTravellers() {
		List<Traveller> travellers = new LinkedList<>();
		for ( UUID travellerID : this.travellerIDs ) {
			travellers.add( this.cache.getTravellerRegistry().get( travellerID ) );
		}
		return travellers;
	}

	public void addTraveller( Traveller traveller ) {
		this.travellerIDs.add( traveller.getID() );
	}

	public TravelAgent getTravelAgent() {
		return this.cache.getTravelAgentRegistry().get( this.travelAgentID );
	}

	public String getThankYouNote() {
		return thankYouNote;
	}

	public void setThankYouNote( String thankYouNote ) {
		this.thankYouNote = thankYouNote;
		this.itinerary = new DecorativeItineraryComponent(
				new DecorativeItineraryComponent( this.itinerary, "\n-------------------------\n\n" ), thankYouNote );
	}

	public States getState() {
		return state;
	}

	public void setState( States state ) {
		this.state = state;
		if ( this.state == States.PAYMENT ) {

		} else {

		}
	}

	public Bill getBill() {
		if ( this.state != States.ADD_TRAVELLERS && this.state != States.ADD_PACKAGES ) {
			if ( this.bill == null ) {
				this.bill = new Bill( this );
			}
			return this.bill;
		}
		throw new RuntimeException( "The bill can not be retrieved in the " + this.state + " state." );
	}

	/**
	 *
	 */
	public StateController getStateController() {
		return this.state.getStateController( this );
	}

	/**
	 *
	 */
	@Override
	public BigDecimal getPrice() {
		BigDecimal totalPrice = new BigDecimal( 0.0 );
		for ( Reservation reservation : this.reservations ) {
			totalPrice = totalPrice.add( reservation.getPrice() );
		}
		return totalPrice;
	}

	@Override
	public String getBillText() {
		StringBuilder stringBuilder = new StringBuilder();
		for ( Reservation reservation : this.reservations ) {
			stringBuilder.append( reservation.getBillText() );
		}
		stringBuilder.append( "\n\tTotal Price: $" ).append( getPrice() );
		return stringBuilder.toString();
	}

	@Override
	public UUID getID() {
		return this.tripID;
	}
}

