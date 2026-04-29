package main;

public class Main {

    public static void main(String[] args) {
        // Workaround for CalendarFX / ical4j ZoneRulesProvider crash on startup
        try {
            java.time.ZoneId.systemDefault();
        } catch (Exception e) {}
        System.setProperty("net.fortuna.ical4j.timezone.date.fallback", "true");

        FxApplication.main(args);
    }
}