package bane.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.MissingFormatArgumentException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import bane.dataservice.timetables.RESTTimetableRedisDataService;
import bane.dataservice.timetables.TimetableRedisDataService;
import bane.model.Station;
import bane.model.Timetable;

@Path("/timetables")
public class RESTServis {

	private TimetableRedisDataService redisDataService = RESTTimetableRedisDataService.getInstance();

	public RESTServis() throws MissingFormatArgumentException, IOException {
	}

	@GET
	@Path("/stations/{ID}")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<Timetable> getTimetables(@PathParam("ID") int ID) {
		ArrayList<Timetable> list = redisDataService.getTimetables();
		// samo linije koje prolaze kroz datu stanicu
		list.removeIf(t -> !t.getStations().stream().map(s -> s.getID()).anyMatch(s -> s == ID));
		return list;
	}

	@POST
	@Path("/{ID}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response markPassage(@PathParam("ID") String timetableID, Station station) {
		if (redisDataService.update(timetableID, station)) {
			return Response.status(Response.Status.OK).build();
		}
		return Response.status(Response.Status.NOT_FOUND).build();
	}

}
