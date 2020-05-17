package uk.co.openmoments.hillbagging.location;

import android.graphics.PointF;
import android.location.Location;

import java.util.Arrays;
import java.util.List;

public class LocationHelpers {

    /**
     * Determines four, approximate, points around a given central location to limit lat/lon searches.
     *
     * @param centerLocation - The point of origin for the locations
     * @param radius - The radius around that point, in metres, to return points for
     * @return List of points with positions 0, 90, 180 and 270 degrees from center location
     */
    public static List<PointF> locationThresholdPoints(Location centerLocation, double radius) {
        PointF center = new PointF((float)centerLocation.getLatitude(), (float) centerLocation.getLongitude());
        PointF p1 = LocationHelpers.calculateDerivedPosition(center, 1.1 * radius, 0);
        PointF p2 = LocationHelpers.calculateDerivedPosition(center, 1.1 * radius, 90);
        PointF p3 = LocationHelpers.calculateDerivedPosition(center, 1.1 * radius, 180);
        PointF p4 = LocationHelpers.calculateDerivedPosition(center, 1.1 * radius, 270);

        return Arrays.asList(p1, p2, p3, p4);
    }

    /**
     * Calculates the end-point from a given source at a given range (meters) and bearing (degrees).
     * This methods uses simple geometry equations to calculate the end-point. Taken from:
     * https://stackoverflow.com/questions/3695224/sqlite-getting-nearest-locations-with-latitude-and-longitude
     *
     * @param point - Point of origin
     * @param range - Range in metres
     * @param bearing - Bearing in degrees
     * @return End-point from the source given the desired range and bearing
     */
    private static PointF calculateDerivedPosition(PointF point, double range, double bearing) {
        double earthRadius = 6371000; // Radius of earth in metres
        double latA = Math.toRadians(point.x);
        double lonA = Math.toRadians(point.y);
        double angularDistance = range/earthRadius;
        double trueCourse = Math.toRadians(bearing);

        double lat = Math.asin(
                Math.sin(latA) * Math.cos(angularDistance) + Math.cos(latA) * Math.sin(angularDistance) * Math.cos(trueCourse)
        );
        double dLon = Math.atan2(
                Math.sin(trueCourse) * Math.sin(angularDistance) * Math.cos(latA),
                Math.cos(angularDistance) - Math.sin(latA) * Math.sin(lat)
        );
        double lon = ((lonA + dLon + Math.PI) % (Math.PI * 2)) - Math.PI;

        lat = Math.toDegrees(lat);
        lon = Math.toDegrees(lon);

        return new PointF((float)lat, (float)lon);
    }
}
