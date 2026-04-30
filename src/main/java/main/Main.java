package main;

public class Main {

    static {
        // Disable problematic ical4j timezone provider at the absolute earliest moment
        System.setProperty("net.fortuna.ical4j.timezone.update.enabled", "false");
        System.setProperty("net.fortuna.ical4j.timezone.cache.disabled", "true");
        System.setProperty("net.fortuna.ical4j.timezone.date.fallback", "true");
    }

    public static void main(String[] args) {
        try {
            // Force initialization of system default zone
            java.time.ZoneId.systemDefault();
        } catch (Throwable t) {
            // Ignore potential initialization errors from ical4j
        }

        FxApplication.main(args);
    }
}