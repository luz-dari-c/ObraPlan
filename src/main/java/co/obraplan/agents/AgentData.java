package co.obraplan.agents;

import java.time.LocalDate;

public final class AgentData {
    private AgentData() {}

    public static LocalDate date(Object value) {
        try {
            return value == null ? null : LocalDate.parse(value.toString());
        } catch (RuntimeException error) {
            return null;
        }
    }

    public static String str(Object value) {
        return value == null ? "" : value.toString();
    }

    public static int num(Object value, int fallback) {
        try {
            return (int) Double.parseDouble(value.toString());
        } catch (RuntimeException error) {
            return fallback;
        }
    }

    public static double num(Object value, double fallback) {
        try {
            return Double.parseDouble(value.toString());
        } catch (RuntimeException error) {
            return fallback;
        }
    }
}
