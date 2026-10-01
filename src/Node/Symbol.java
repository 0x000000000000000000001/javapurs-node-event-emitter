    // Port of Node/Symbol.js: symbols are canonical per-key wrappers, so
    // `for` and `keyFor` round-trip.
    public static final class JsSymbolRepr {
        public final String key;
        JsSymbolRepr(String key) { this.key = key; }
    }

    private static final java.util.concurrent.ConcurrentHashMap<String, JsSymbolRepr> __symbolRegistry =
        new java.util.concurrent.ConcurrentHashMap<>();

    public static Object showSymbolImpl = (java.util.function.Function<Object, Object>) (symbol) ->
        "Symbol(" + ((JsSymbolRepr) symbol).key + ")";

    public static Object forImpl = (java.util.function.Function<Object, Object>) (key) ->
        (java.util.function.Supplier<Object>) () ->
            __symbolRegistry.computeIfAbsent((String) key, JsSymbolRepr::new);

    public static Object keyForImpl = (java.util.function.Function<Object, Object>) (symbol) ->
        (java.util.function.Supplier<Object>) () -> ((JsSymbolRepr) symbol).key;
