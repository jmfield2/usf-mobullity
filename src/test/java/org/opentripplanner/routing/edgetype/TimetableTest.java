/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.routing.edgetype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.opentripplanner.util.TestUtils.AUGUST;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.google.common.collect.Iterables;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.opentripplanner.ConstantsForTests;
import org.opentripplanner.gtfs.GtfsContext;
import org.opentripplanner.gtfs.GtfsLibrary;
import org.opentripplanner.routing.algorithm.AStar;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.edgetype.factory.GTFSPatternHopFactory;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.opentripplanner.routing.trippattern.TripTimes;
import org.opentripplanner.routing.vertextype.TransitStopDepart;
import org.opentripplanner.util.TestUtils;

import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeEvent;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;

public class TimetableTest {

    private static Graph graph;
    private AStar aStar = new AStar();


    private static GtfsContext context;

    private static Map<AgencyAndId, TripPattern> patternIndex;

    private static TripPattern pattern;

    private static Timetable timetable;

    private static TimeZone timeZone = TimeZone.getTimeZone("America/New_York");

    private static ServiceDate serviceDate = new ServiceDate(2009, 8, 7);

    @BeforeClass
    public static void setUp() throws Exception {

        context = GtfsLibrary.readGtfs(new File(ConstantsForTests.FAKE_GTFS));
        graph = new Graph();

        GTFSPatternHopFactory factory = new GTFSPatternHopFactory(context);
        factory.run(graph);
        graph.putService(CalendarServiceData.class,
                GtfsLibrary.createCalendarServiceData(context.getDao()));

        patternIndex = new HashMap<AgencyAndId, TripPattern>();
        for (TransitStopDepart tsd : Iterables.filter(graph.getVertices(), TransitStopDepart.class)) {
            for (TransitBoardAlight tba : Iterables.filter(tsd.getOutgoing(), TransitBoardAlight.class)) {
                if (!tba.boarding)
                    continue;
                TripPattern pattern = tba.getPattern();
                for (Trip trip : pattern.getTrips()) {
                    patternIndex.put(trip.getId(), pattern);
                }
            }
        }

        pattern = patternIndex.get(new AgencyAndId("agency", "1.1"));
        timetable = pattern.scheduledTimetable;
    }

    @Test
    public void testUpdate() {
    	TripUpdate tripUpdate;
        TripUpdate.Builder tripUpdateBuilder;
        TripDescriptor.Builder tripDescriptorBuilder;
        StopTimeUpdate.Builder stopTimeUpdateBuilder;
        StopTimeEvent.Builder stopTimeEventBuilder;

        int trip_1_1_index = timetable.getTripIndex(new AgencyAndId("agency", "1.1"));

        Vertex stop_a = graph.getVertex("agency:A");
        Vertex stop_c = graph.getVertex("agency:C");
        RoutingRequest options = new RoutingRequest();

        ShortestPathTree spt;
        GraphPath path;

        // non-existing trip
        tripDescriptorBuilder = TripDescriptor.newBuilder();
        tripDescriptorBuilder.setTripId("b");
        tripDescriptorBuilder.setScheduleRelationship(TripDescriptor.ScheduleRelationship.CANCELED);
        tripUpdateBuilder = TripUpdate.newBuilder();
        tripUpdateBuilder.setTrip(tripDescriptorBuilder);
        tripUpdate = tripUpdateBuilder.build();
        TripTimes updatedTripTimes = timetable.createUpdatedTripTimes(tripUpdate, timeZone, serviceDate); 
        assertNull(updatedTripTimes);

        // update trip with bad data
        tripDescriptorBuilder = TripDescriptor.newBuilder();
        tripDescriptorBuilder.setTripId("1.1");
        tripDescriptorBuilder.setScheduleRelationship(
                TripDescriptor.ScheduleRelationship.SCHEDULED);
        tripUpdateBuilder = TripUpdate.newBuilder();
        tripUpdateBuilder.setTrip(tripDescriptorBuilder);
        stopTimeUpdateBuilder = tripUpdateBuilder.addStopTimeUpdateBuilder(0);
        stopTimeUpdateBuilder.setStopSequence(0);
        stopTimeUpdateBuilder.setScheduleRelationship(StopTimeUpdate.ScheduleRelationship.SKIPPED);
        tripUpdate = tripUpdateBuilder.build();
        updatedTripTimes = timetable.createUpdatedTripTimes(tripUpdate, timeZone, serviceDate); 
        assertNull(updatedTripTimes);

        // update trip with non-increasing data
        tripDescriptorBuilder = TripDescriptor.newBuilder();
        tripDescriptorBuilder.setTripId("1.1");
        tripDescriptorBuilder.setScheduleRelationship(TripDescriptor.ScheduleRelationship.SCHEDULED);
        tripUpdateBuilder = TripUpdate.newBuilder();
        tripUpdateBuilder.setTrip(tripDescriptorBuilder);
        stopTimeUpdateBuilder = tripUpdateBuilder.addStopTimeUpdateBuilder(0);
        stopTimeUpdateBuilder.setStopSequence(2);
        stopTimeUpdateBuilder.setScheduleRelationship(
                StopTimeUpdate.ScheduleRelationship.SCHEDULED);
        stopTimeEventBuilder = stopTimeUpdateBuilder.getArrivalBuilder();
        stopTimeEventBuilder.setTime(TestUtils.dateInSeconds(
                "America/New_York", 2009, AUGUST, 7, 0, 10, 1));
        stopTimeEventBuilder = stopTimeUpdateBuilder.getDepartureBuilder();
        stopTimeEventBuilder.setTime(TestUtils.dateInSeconds(
                "America/New_York", 2009, AUGUST, 7, 0, 10, 0));
        tripUpdate = tripUpdateBuilder.build();
        updatedTripTimes = timetable.createUpdatedTripTimes(tripUpdate, timeZone, serviceDate); 
        assertNull(updatedTripTimes);

        //---
        long startTime = TestUtils.dateInSeconds("America/New_York", 2009, AUGUST, 7, 0, 0, 0);
        long endTime;
        options.dateTime = startTime;

        //---
        options.setRoutingContext(graph, stop_a, stop_c);
        spt = aStar.getShortestPathTree(options);
        path = spt.getPath(stop_c, false);
        assertNotNull(path);
        endTime = startTime + 20 * 60;
        assertEquals(endTime, path.getEndTime());

        // update trip
        tripDescriptorBuilder = TripDescriptor.newBuilder();
        tripDescriptorBuilder.setTripId("1.1");
        tripDescriptorBuilder.setScheduleRelationship(
                TripDescriptor.ScheduleRelationship.SCHEDULED);
        tripUpdateBuilder = TripUpdate.newBuilder();
        tripUpdateBuilder.setTrip(tripDescriptorBuilder);
        stopTimeUpdateBuilder = tripUpdateBuilder.addStopTimeUpdateBuilder(0);
        stopTimeUpdateBuilder.setStopSequence(1);
        stopTimeUpdateBuilder.setScheduleRelationship(
                StopTimeUpdate.ScheduleRelationship.SCHEDULED);
        stopTimeEventBuilder = stopTimeUpdateBuilder.getArrivalBuilder();
        stopTimeEventBuilder.setTime(TestUtils.dateInSeconds(
                "America/New_York", 2009, AUGUST, 7, 0, 2, 0));
        stopTimeEventBuilder = stopTimeUpdateBuilder.getDepartureBuilder();
        stopTimeEventBuilder.setTime(TestUtils.dateInSeconds(
                "America/New_York", 2009, AUGUST, 7, 0, 2, 0));
        tripUpdate = tripUpdateBuilder.build();
        assertEquals(20*60, timetable.getTripTimes(trip_1_1_index).getArrivalTime(2));
        updatedTripTimes = timetable.createUpdatedTripTimes(tripUpdate, timeZone, serviceDate); 
        assertNotNull(updatedTripTimes);
        timetable.setTripTimes(trip_1_1_index, updatedTripTimes);
        assertEquals(20*60 + 120, timetable.getTripTimes(trip_1_1_index).getArrivalTime(2));

        //---
        options.setRoutingContext(graph, stop_a, stop_c);
        spt = aStar.getShortestPathTree(options);
        path = spt.getPath(stop_c, false);
        assertNotNull(path);
        endTime = startTime + 20 * 60 + 120;
        assertEquals(endTime, path.getEndTime());

        // cancel trip
        tripDescriptorBuilder = TripDescriptor.newBuilder();
        tripDescriptorBuilder.setTripId("1.1");
        tripDescriptorBuilder.setScheduleRelationship(TripDescriptor.ScheduleRelationship.CANCELED);
        tripUpdateBuilder = TripUpdate.newBuilder();
        tripUpdateBuilder.setTrip(tripDescriptorBuilder);
        tripUpdate = tripUpdateBuilder.build();
        updatedTripTimes = timetable.createUpdatedTripTimes(tripUpdate, timeZone, serviceDate); 
        assertNotNull(updatedTripTimes);
        timetable.setTripTimes(trip_1_1_index, updatedTripTimes);

        TripTimes tripTimes = timetable.getTripTimes(trip_1_1_index);
        for (int i = 0; i < tripTimes.getNumStops(); i++) {
            assertEquals(TripTimes.UNAVAILABLE, tripTimes.getDepartureTime(i));
            assertEquals(TripTimes.UNAVAILABLE, tripTimes.getArrivalTime(i));
        }

        //---
        options.setRoutingContext(graph, stop_a, stop_c);
        spt = aStar.getShortestPathTree(options);
        path = spt.getPath(stop_c, false);
        assertNotNull(path);
        endTime = startTime + 40 * 60;
        assertEquals(endTime, path.getEndTime());

        // update trip arrival time incorrectly
        tripDescriptorBuilder = TripDescriptor.newBuilder();
        tripDescriptorBuilder.setTripId("1.1");
        tripDescriptorBuilder.setScheduleRelationship(
                TripDescriptor.ScheduleRelationship.SCHEDULED);
        tripUpdateBuilder = TripUpdate.newBuilder();
        tripUpdateBuilder.setTrip(tripDescriptorBuilder);
        stopTimeUpdateBuilder = tripUpdateBuilder.addStopTimeUpdateBuilder(0);
        stopTimeUpdateBuilder.setStopSequence(1);
        stopTimeUpdateBuilder.setScheduleRelationship(
                StopTimeUpdate.ScheduleRelationship.SCHEDULED);
        stopTimeEventBuilder = stopTimeUpdateBuilder.getArrivalBuilder();
        stopTimeEventBuilder.setDelay(0);
        tripUpdate = tripUpdateBuilder.build();
        updatedTripTimes = timetable.createUpdatedTripTimes(tripUpdate, timeZone, serviceDate); 
        assertNotNull(updatedTripTimes);
        timetable.setTripTimes(trip_1_1_index, updatedTripTimes);

        // update trip arrival time only
        tripDescriptorBuilder = TripDescriptor.newBuilder();
        tripDescriptorBuilder.setTripId("1.1");
        tripDescriptorBuilder.setScheduleRelationship(
                TripDescriptor.ScheduleRelationship.SCHEDULED);
        tripUpdateBuilder = TripUpdate.newBuilder();
        tripUpdateBuilder.setTrip(tripDescriptorBuilder);
        stopTimeUpdateBuilder = tripUpdateBuilder.addStopTimeUpdateBuilder(0);
        stopTimeUpdateBuilder.setStopSequence(2);
        stopTimeUpdateBuilder.setScheduleRelationship(
                StopTimeUpdate.ScheduleRelationship.SCHEDULED);
        stopTimeEventBuilder = stopTimeUpdateBuilder.getArrivalBuilder();
        stopTimeEventBuilder.setDelay(1);
        tripUpdate = tripUpdateBuilder.build();
        updatedTripTimes = timetable.createUpdatedTripTimes(tripUpdate, timeZone, serviceDate); 
        assertNotNull(updatedTripTimes);
        timetable.setTripTimes(trip_1_1_index, updatedTripTimes);

        // update trip departure time only
        tripDescriptorBuilder = TripDescriptor.newBuilder();
        tripDescriptorBuilder.setTripId("1.1");
        tripDescriptorBuilder.setScheduleRelationship(
                TripDescriptor.ScheduleRelationship.SCHEDULED);
        tripUpdateBuilder = TripUpdate.newBuilder();
        tripUpdateBuilder.setTrip(tripDescriptorBuilder);
        stopTimeUpdateBuilder = tripUpdateBuilder.addStopTimeUpdateBuilder(0);
        stopTimeUpdateBuilder.setStopSequence(2);
        stopTimeUpdateBuilder.setScheduleRelationship(
                StopTimeUpdate.ScheduleRelationship.SCHEDULED);
        stopTimeEventBuilder = stopTimeUpdateBuilder.getDepartureBuilder();
        stopTimeEventBuilder.setDelay(-1);
        tripUpdate = tripUpdateBuilder.build();
        updatedTripTimes = timetable.createUpdatedTripTimes(tripUpdate, timeZone, serviceDate); 
        assertNotNull(updatedTripTimes);
        timetable.setTripTimes(trip_1_1_index, updatedTripTimes);

        // update trip using stop id
        tripDescriptorBuilder = TripDescriptor.newBuilder();
        tripDescriptorBuilder.setTripId("1.1");
        tripDescriptorBuilder.setScheduleRelationship(
                TripDescriptor.ScheduleRelationship.SCHEDULED);
        tripUpdateBuilder = TripUpdate.newBuilder();
        tripUpdateBuilder.setTrip(tripDescriptorBuilder);
        stopTimeUpdateBuilder = tripUpdateBuilder.addStopTimeUpdateBuilder(0);
        stopTimeUpdateBuilder.setStopId("B");
        stopTimeUpdateBuilder.setScheduleRelationship(
                StopTimeUpdate.ScheduleRelationship.SCHEDULED);
        stopTimeEventBuilder = stopTimeUpdateBuilder.getDepartureBuilder();
        stopTimeEventBuilder.setDelay(-1);
        tripUpdate = tripUpdateBuilder.build();
        updatedTripTimes = timetable.createUpdatedTripTimes(tripUpdate, timeZone, serviceDate); 
        assertNotNull(updatedTripTimes);
        timetable.setTripTimes(trip_1_1_index, updatedTripTimes);

        // update trip arrival time at first stop and make departure time incoherent at second stop
        tripDescriptorBuilder = TripDescriptor.newBuilder();
        tripDescriptorBuilder.setTripId("1.1");
        tripDescriptorBuilder.setScheduleRelationship(
                TripDescriptor.ScheduleRelationship.SCHEDULED);
        tripUpdateBuilder = TripUpdate.newBuilder();
        tripUpdateBuilder.setTrip(tripDescriptorBuilder);
        stopTimeUpdateBuilder = tripUpdateBuilder.addStopTimeUpdateBuilder(0);
        stopTimeUpdateBuilder.setStopSequence(1);
        stopTimeUpdateBuilder.setScheduleRelationship(
                StopTimeUpdate.ScheduleRelationship.SCHEDULED);
        stopTimeEventBuilder = stopTimeUpdateBuilder.getArrivalBuilder();
        stopTimeEventBuilder.setDelay(0);
        stopTimeUpdateBuilder = tripUpdateBuilder.addStopTimeUpdateBuilder(1);
        stopTimeUpdateBuilder.setStopSequence(2);
        stopTimeUpdateBuilder.setScheduleRelationship(
                StopTimeUpdate.ScheduleRelationship.SCHEDULED);
        stopTimeEventBuilder = stopTimeUpdateBuilder.getDepartureBuilder();
        stopTimeEventBuilder.setDelay(-1);
        tripUpdate = tripUpdateBuilder.build();
        updatedTripTimes = timetable.createUpdatedTripTimes(tripUpdate, timeZone, serviceDate); 
        assertNull(updatedTripTimes);
    }

    @Test
    public void testUpdateFreqTrip() throws Exception {

        GtfsContext context2 = GtfsLibrary.readGtfs(new File(ConstantsForTests.FAKE_FREQ_GTFS));
        Graph graph2 = new Graph();

        GTFSPatternHopFactory factory = new GTFSPatternHopFactory(context2);
        factory.run(graph2);
        graph2.putService(CalendarServiceData.class,
                GtfsLibrary.createCalendarServiceData(context2.getDao()));

        patternIndex = new HashMap<AgencyAndId, TripPattern>();
        for (TransitStopDepart tsd : Iterables.filter(graph2.getVertices(), TransitStopDepart.class)) {
            for (TransitBoardAlight tba : Iterables.filter(tsd.getOutgoing(), TransitBoardAlight.class)) {
                if (!tba.boarding)
                    continue;
                TripPattern pattern = tba.getPattern();
                for (Trip trip : pattern.getTrips()) {
                    patternIndex.put(trip.getId(), pattern);
                }
            }
        }

        pattern = patternIndex.get(new AgencyAndId("agency", "1"));
        timetable = pattern.scheduledTimetable;

        TripUpdate tripUpdate;
        TripUpdate.Builder tripUpdateBuilder;
        TripDescriptor.Builder tripDescriptorBuilder;
        StopTimeUpdate.Builder stopTimeUpdateBuilder;
        StopTimeEvent.Builder stopTimeEventBuilder;

        int trip_1_index = timetable.getTripIndex(new AgencyAndId("agency", "1"));

        // update arrival time of second stop along the trip
        tripDescriptorBuilder = TripDescriptor.newBuilder();
        tripDescriptorBuilder.setTripId("1");
        tripDescriptorBuilder
                .setScheduleRelationship(TripDescriptor.ScheduleRelationship.UNSCHEDULED);
        tripUpdateBuilder = TripUpdate.newBuilder();
        tripUpdateBuilder.setTrip(tripDescriptorBuilder);

        stopTimeUpdateBuilder = tripUpdateBuilder.addStopTimeUpdateBuilder(0);
        stopTimeUpdateBuilder.setStopSequence(2);
        stopTimeEventBuilder = stopTimeUpdateBuilder.getArrivalBuilder();
        stopTimeEventBuilder.setTime(TestUtils.dateInSeconds("America/New_York", 2009, AUGUST, 7,
                07, 13, 0));
        VehicleDescriptor.Builder vehicleDescriptor = VehicleDescriptor.newBuilder();
        vehicleDescriptor.setId("a");
        tripUpdateBuilder.setVehicle(vehicleDescriptor);
        tripUpdate = tripUpdateBuilder.build();

        assertTrue(timetable.updateFreqTrip(tripUpdate, timeZone, serviceDate));
        assertEquals(7 * 3600 + 13 * 60, timetable.getTripTimes(trip_1_index).getArrivalTime(1));
        // check the back-propagation of delay
        assertEquals(7 * 3600 + 3 * 60, timetable.getTripTimes(trip_1_index).getArrivalTime(0));
        assertEquals(7 * 3600 + 33 * 60, timetable.getTripTimes(trip_1_index).getArrivalTime(3));

        // updates one trip with two different tripUpdates (different vehicle id)
        TripUpdate tripUpdate2;
        tripDescriptorBuilder = TripDescriptor.newBuilder();
        tripDescriptorBuilder.setTripId("1");
        tripDescriptorBuilder
                .setScheduleRelationship(TripDescriptor.ScheduleRelationship.UNSCHEDULED);
        tripUpdateBuilder = TripUpdate.newBuilder();
        tripUpdateBuilder.setTrip(tripDescriptorBuilder);
        stopTimeUpdateBuilder = tripUpdateBuilder.addStopTimeUpdateBuilder(0);
        stopTimeUpdateBuilder.setStopSequence(2);
        stopTimeEventBuilder = stopTimeUpdateBuilder.getArrivalBuilder();
        stopTimeEventBuilder.setTime(TestUtils.dateInSeconds("America/New_York", 2009, AUGUST, 7,
                07, 45, 0));
        vehicleDescriptor.setId("b");
        tripUpdateBuilder.setVehicle(vehicleDescriptor);
        tripUpdate2 = tripUpdateBuilder.build();
        assertTrue(timetable.updateFreqTrip(tripUpdate2, timeZone, serviceDate));
        assertEquals(2, timetable.tripTimes.size());

        assertEquals(7 * 3600 + 45 * 60, timetable.getTripTimes(1).getArrivalTime(1));
        // check the back-propagation of delay
        assertEquals(7 * 3600 + 35 * 60, timetable.getTripTimes(1).getArrivalTime(0));
        assertEquals(8 * 3600 + 5 * 60, timetable.getTripTimes(1).getArrivalTime(3));

        // shortest path after tripUpdate
        long startTime = TestUtils.dateInSeconds("America/New_York", 2009, AUGUST, 7, 07, 0, 0);
        long endTime;
        Vertex stop_a = graph2.getVertex("agency:A");
        Vertex stop_c = graph2.getVertex("agency:C");
        RoutingRequest options = new RoutingRequest();
        options.dateTime = startTime;

        ShortestPathTree spt;
        GraphPath path;
        options.setRoutingContext(graph2, stop_a, stop_c);
        GenericAStar aStar2 = new GenericAStar();
        spt = aStar2.getShortestPathTree(options);
        path = spt.getPath(stop_c, false);
        assertNotNull(path);
        endTime = startTime + 23 * 60;
        assertEquals(endTime, path.getEndTime());
        
        stopTimeEventBuilder.setDelay(180);
        tripUpdate = tripUpdateBuilder.build();
        assertFalse(timetable.updateFreqTrip(tripUpdate, timeZone, serviceDate));
    }

}
