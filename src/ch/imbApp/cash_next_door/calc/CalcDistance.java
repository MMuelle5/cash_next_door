package ch.imbApp.cash_next_door.calc;

public class CalcDistance {
	private static final double PIx = Math.PI;
//    private static final double RADIO = 6378.16;

    private static double Radians(double x)
    {
        return x * PIx / 180;
    }

    public static double DistanceBetweenPlaces(double long1, double lat1, double long2, double lat2)
    {
        double r = 6371; // km
        double dLat = Radians(lat2 - lat1);
        double dLon = Radians(long2 - long1);
        lat1 = Radians(lat1);
        lat2 = Radians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = r * c;

        return d;
    }
}
