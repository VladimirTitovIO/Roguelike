package datalayer;

import java.util.*;

public final class Json {
    private Json() {}

    public static Object parse(String s) {
        return new Parser(s).parseValue();
    }

    public static String stringify(Object v) {
        StringBuilder sb = new StringBuilder();
        write(sb, v);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void write(StringBuilder sb, Object v) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof String str) {
            sb.append('"').append(escape(str)).append('"');
        } else if (v instanceof Boolean b) {
            sb.append(b ? "true" : "false");
        } else if (v instanceof Number n) {
            sb.append(n.toString());
        } else if (v instanceof Map<?, ?> map) {
            sb.append('{');
            boolean first = true;
            for (var e : ((Map<String, Object>) map).entrySet()) {
                if (!first) sb.append(',');
                first = false;
                sb.append('"').append(escape(e.getKey())).append('"').append(':');
                write(sb, e.getValue());
            }
            sb.append('}');
        } else if (v instanceof List<?> list) {
            sb.append('[');
            boolean first = true;
            for (Object x : list) {
                if (!first) sb.append(',');
                first = false;
                write(sb, x);
            }
            sb.append(']');
        } else {
            throw new IllegalArgumentException("Unsupported JSON type: " + v.getClass());
        }
    }

    private static String escape(String s) {
        StringBuilder out = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '"' -> out.append("\\\"");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> out.append(c);
            }
        }
        return out.toString();
    }

    //  Parser
    private static final class Parser {
        private final String s;
        private int i;

        Parser(String s) {
            this.s = s;
            this.i = 0;
            skipWs();
        }

        Object parseValue() {
            skipWs();
            if (i >= s.length()) throw err("Unexpected end");

            char c = s.charAt(i);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            if (c == '-' || Character.isDigit(c)) return parseNumber();

            throw err("Unexpected char: " + c);
        }

        private Map<String, Object> parseObject() {
            expect('{');
            skipWs();
            Map<String, Object> obj = new LinkedHashMap<>();
            if (peek('}')) { expect('}'); return obj; }

            while (true) {
                skipWs();
                String key = parseString();
                skipWs();
                expect(':');
                Object val = parseValue();
                obj.put(key, val);
                skipWs();
                if (peek('}')) { expect('}'); return obj; }
                expect(',');
            }
        }

        private List<Object> parseArray() {
            expect('[');
            skipWs();
            List<Object> arr = new ArrayList<>();
            if (peek(']')) { expect(']'); return arr; }

            while (true) {
                Object v = parseValue();
                arr.add(v);
                skipWs();
                if (peek(']')) { expect(']'); return arr; }
                expect(',');
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder out = new StringBuilder();
            while (i < s.length()) {
                char c = s.charAt(i++);
                if (c == '"') return out.toString();
                if (c == '\\') {
                    if (i >= s.length()) throw err("Bad escape");
                    char e = s.charAt(i++);
                    switch (e) {
                        case '"', '\\', '/' -> out.append(e);
                        case 'n' -> out.append('\n');
                        case 'r' -> out.append('\r');
                        case 't' -> out.append('\t');
                        default -> throw err("Bad escape: \\" + e);
                    }
                } else {
                    out.append(c);
                }
            }
            throw err("Unterminated string");
        }

        private Boolean parseBoolean() {
            if (s.startsWith("true", i)) { i += 4; return true; }
            if (s.startsWith("false", i)) { i += 5; return false; }
            throw err("Bad boolean");
        }

        private Object parseNull() {
            if (s.startsWith("null", i)) { i += 4; return null; }
            throw err("Bad null");
        }

        private Number parseNumber() {
            int start = i;
            if (s.charAt(i) == '-') i++;
            while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
            String num = s.substring(start, i);
            try {
                return Long.parseLong(num);
            } catch (NumberFormatException e) {
                throw err("Bad number: " + num);
            }
        }

        private void skipWs() {
            while (i < s.length()) {
                char c = s.charAt(i);
                if (c == ' ' || c == '\n' || c == '\r' || c == '\t') i++;
                else break;
            }
        }

        private boolean peek(char c) {
            return i < s.length() && s.charAt(i) == c;
        }

        private void expect(char c) {
            if (i >= s.length() || s.charAt(i) != c) {
                throw err("Expected '" + c + "'");
            }
            i++;
            skipWs();
        }

        private RuntimeException err(String msg) {
            return new IllegalArgumentException(msg + " at pos " + i);
        }
    }
}
