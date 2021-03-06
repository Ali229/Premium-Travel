package premiumtravel.rest;

import premiumtravel.cache.PackageRegistry;
import premiumtravel.trip.TravelPackage;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
@Path( "/package" )
public class PackageRESTController extends AbstractRESTController {

	/**
	 * Singleton bean instantiated by Java EE
	 */
	@Inject private PackageRegistry packageRegistry;

	@GET
	@Produces( MediaType.APPLICATION_JSON )
	public Response getTravelAgents() {
		return addHeaders( Response.ok( gson.toJson( packageRegistry.getAll() ) ) ).build();
	}

	@GET
	@Path( "{package-id}" )
	@Produces( MediaType.APPLICATION_JSON )
	public Response getTravelAgent( @DefaultValue( "-1" ) @PathParam( "package-id" ) String packageID ) {
		logger.trace( "GET called on /package/" + packageID );
		for ( TravelPackage travelPackage : packageRegistry.getAll() ) {
			if ( travelPackage.getID().toString().equals( packageID ) ) {
				return addHeaders( Response.ok( gson.toJson( travelPackage ) ) ).build();
			}
		}
		return addHeaders( Response.status( 400, "No Package with that ID exists." ) ).build();
	}
}
