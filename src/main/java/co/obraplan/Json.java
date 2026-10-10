package co.obraplan;

import java.util.*;

/** Small dependency-free JSON codec for the local application API and store. */
public final class Json {
    private Json() {}

    public static Object parse(String source) {
        return new Parser(source).parse();
    }

    public static String stringify(Object value) {
        StringBuilder out = new StringBuilder();
        write(value, out);
        return out.toString();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> object(Object value) {
        return (Map<String, Object>) value;
    }

    public static Map<String, Object> map(Object... pairs) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) m.put((String) pairs[i], pairs[i + 1]);
        return m;
    }

    private static void write(Object v, StringBuilder b) {
        if (v == null) {
            b.append("null");
            return;
        }
        if (v instanceof String s) {
            b.append('"');
            for (char c : s.toCharArray())
                switch (c) {
                    case '"' -> b.append("\\\"");
                    case '\\' -> b.append("\\\\");
                    case '\n' -> b.append("\\n");
                    case '\r' -> b.append("\\r");
                    case '\t' -> b.append("\\t");
                    default -> {
                        if (c < 32) b.append(String.format("\\u%04x", (int) c));
                        else b.append(c);
                    }
                }
            b.append('"');
            return;
        }
        if (v instanceof Number || v instanceof Boolean) {
            b.append(v);
            return;
        }
        if (v instanceof Map<?, ?> m) {
            b.append('{');
            boolean first = true;
            for (var e : m.entrySet()) {
                if (!first) b.append(',');
                first = false;
                write(String.valueOf(e.getKey()), b);
                b.append(':');
                write(e.getValue(), b);
            }
            b.append('}');
            return;
        }
        if (v instanceof Iterable<?> a) {
            b.append('[');
            boolean first = true;
            for (Object x : a) {
                if (!first) b.append(',');
                first = false;
                write(x, b);
            }
            b.append(']');
            return;
        }
        write(String.valueOf(v), b);
    }

    private static final class Parser {
        final String s;
        int i;

        Parser(String s) {
            this.s = s == null ? "" : s;
        }

        Object parse() {
            skip();
            Object v = value();
            skip();
            if (i < s.length()) throw error("Unexpected trailing content");
            return v;
        }

        Object value() {
            skip();
            if (i >= s.length()) throw error("Expected a value");
            char c = s.charAt(i);
            if (c == '{') return object();
            if (c == '[') return array();
            if (c == '\"') return string();
            if (c == 't' && literal("true")) return true;
            if (c == 'f' && literal("false")) return false;
            if (c == 'n' && literal("null")) return null;
            return number();
        }

        Map<String, Object> object() {
            i++;
            Map<String, Object> m = new LinkedHashMap<>();
            skip();
            if (take('}')) return m;
            while (true) {
                skip();
                if (i >= s.length() || s.charAt(i) != '\"') throw error("Expected an object key");
                String k = string();
                skip();
                if (!take(':')) throw error("Expected ':'");
                m.put(k, value());
                skip();
                if (take('}')) return m;
                if (!take(',')) throw error("Expected ',' or '}'");
            }
        }

        List<Object> array() {
            i++;
            List<Object> a = new ArrayList<>();
            skip();
            if (take(']')) return a;
            while (true) {
                a.add(value());
                skip();
                if (take(']')) return a;
                if (!take(',')) throw error("Expected ',' or ']'");
            }
        }

        String string() {
            i++;
            StringBuilder b = new StringBuilder();
            while (i < s.length()) {
                char c = s.charAt(i++);
                if (c == '\"') return b.toString();
                if (c == '\\') {
                    if (i >= s.length()) throw error("Invalid escape");
                    char e = s.charAt(i++);
                    switch (e) {
                        case '\"', '\\', '/' -> b.append(e);
                        case 'b' -> b.append('\b');
                        case 'f' -> b.append('\f');
                        case 'n' -> b.append('\n');
                        case 'r' -> b.append('\r');
                        case 't' -> b.append('\t');
                        case 'u' -> {
                            if (i + 4 > s.length()) throw error("Invalid unicode escape");
                            b.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                            i += 4;
                        }
                        default -> throw error("Invalid escape");
                    }
                } else b.append(c);
            }
            throw error("Unterminated string");
        }

        Object number() {
            int start = i;
            if (take('-')) {}
            while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
            if (i < s.length() && s.charAt(i) == '.') {
                i++;
                while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
            }
            if (i < s.length() && (s.charAt(i) == 'e' || s.charAt(i) == 'E')) {
                i++;
                if (i < s.length() && (s.charAt(i) == '+' || s.charAt(i) == '-')) i++;
                while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
            }
            try {
                double d = Double.parseDouble(s.substring(start, i));
                return d == (long) d ? (Object) (long) d : d;
            } catch (Exception e) {
                throw error("Invalid number");
            }
        }

        boolean literal(String x) {
            if (s.startsWith(x, i)) {
                i += x.length();
                return true;
            }
            return false;
        }

        boolean take(char c) {
            if (i < s.length() && s.charAt(i) == c) {
                i++;
                return true;
            }
            return false;
        }

        void skip() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        }

        IllegalArgumentException error(String m) {
            return new IllegalArgumentException(m + " at character " + i);
        }
    }
}
