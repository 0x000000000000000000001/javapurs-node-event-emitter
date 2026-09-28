    // Port of Node/EventEmitter.js. Listeners live on the emitter object; an
    // event that already fired is replayed to late listeners, which keeps the
    // synchronous streams of this backend observable like the Node ones.
    public static class EmitterBase {
        public final java.util.Map<String, java.util.List<Object>> listeners = new java.util.concurrent.ConcurrentHashMap<>();
        public final java.util.Set<String> fired = java.util.concurrent.ConcurrentHashMap.newKeySet();

        public void addListener(String name, Object listener, boolean prepend) {
            java.util.List<Object> registered = listeners.computeIfAbsent(name, key -> new java.util.concurrent.CopyOnWriteArrayList<>());
            if (prepend) registered.add(0, listener);
            else registered.add(listener);
            if (fired.contains(name)) invoke(listener, new Object[0]);
        }

        public void removeListener(String name, Object listener) {
            java.util.List<Object> registered = listeners.get(name);
            if (registered != null) registered.remove(listener);
        }

        public void fire(String name, Object... args) {
            fired.add(name);
            java.util.List<Object> registered = listeners.get(name);
            if (registered == null) return;
            for (Object listener : new java.util.ArrayList<>(registered)) invoke(listener, args);
        }

        public void invoke(Object listener, Object[] args) {
            try {
                if (listener instanceof java.util.function.Supplier) {
                    ((java.util.function.Supplier<Object>) listener).get();
                } else if (listener instanceof java.util.function.Function) {
                    Object effect = listener;
                    for (Object arg : args) {
                        if (!(effect instanceof java.util.function.Function)) break;
                        effect = ((java.util.function.Function<Object, Object>) effect).apply(arg);
                    }
                    if (effect instanceof java.util.function.Supplier) ((java.util.function.Supplier<Object>) effect).get();
                }
            } catch (Throwable ignored) { }
        }
    }

    // A one-shot listener removes itself after the first call.
    private static Object __once(EmitterBase emitter, String name, Object listener) {
        final Object[] wrapper = new Object[1];
        wrapper[0] = (java.util.function.Supplier<Object>) () -> {
            emitter.removeListener(name, wrapper[0]);
            emitter.invoke(listener, new Object[0]);
            return null;
        };
        return wrapper[0];
    }

    public static Object $new = (java.util.function.Supplier<Object>) () -> new EmitterBase();

    public static Object eventNamesImpl = (java.util.function.Function<Object, Object>) (emitter) ->
        ((EmitterBase) emitter).listeners.keySet().toArray(new Object[0]);

    public static Object getMaxListenersImpl = (java.util.function.Function<Object, Object>) (emitter) ->
        (java.util.function.Supplier<Object>) () -> 10;

    public static Object listenerCountImpl = (java.util.function.Function<Object, Object>) (emitter) ->
        (java.util.function.Function<Object, Object>) (name) ->
            (java.util.function.Supplier<Object>) () -> {
                java.util.List<Object> registered = ((EmitterBase) emitter).listeners.get((String) name);
                return registered == null ? 0 : registered.size();
            };

    public static Object setMaxListenersImpl = (java.util.function.Function<Object, Object>) (emitter) ->
        (java.util.function.Function<Object, Object>) (count) ->
            (java.util.function.Supplier<Object>) () -> null;

    public static Object unsafeEmitFn = (java.util.function.Function<Object, Object>) (emitter) ->
        (java.util.function.Function<Object, Object>) (name) ->
        (java.util.function.Function<Object, Object>) (argument) -> {
            ((EmitterBase) emitter).fire((String) name, argument);
            return true;
        };

    public static Object unsafeOn = (java.util.function.Function<Object, Object>) (emitter) ->
        (java.util.function.Function<Object, Object>) (name) ->
        (java.util.function.Function<Object, Object>) (listener) ->
            (java.util.function.Supplier<Object>) () -> {
                ((EmitterBase) emitter).addListener((String) name, listener, false);
                return null;
            };

    public static Object unsafeOff = (java.util.function.Function<Object, Object>) (emitter) ->
        (java.util.function.Function<Object, Object>) (name) ->
        (java.util.function.Function<Object, Object>) (listener) ->
            (java.util.function.Supplier<Object>) () -> {
                ((EmitterBase) emitter).removeListener((String) name, listener);
                return null;
            };

    public static Object unsafeOnce = (java.util.function.Function<Object, Object>) (emitter) ->
        (java.util.function.Function<Object, Object>) (name) ->
        (java.util.function.Function<Object, Object>) (listener) ->
            (java.util.function.Supplier<Object>) () -> {
                ((EmitterBase) emitter).addListener((String) name, __once((EmitterBase) emitter, (String) name, listener), false);
                return null;
            };

    public static Object unsafePrependListener = (java.util.function.Function<Object, Object>) (emitter) ->
        (java.util.function.Function<Object, Object>) (name) ->
        (java.util.function.Function<Object, Object>) (listener) ->
            (java.util.function.Supplier<Object>) () -> {
                ((EmitterBase) emitter).addListener((String) name, listener, true);
                return null;
            };

    public static Object unsafePrependOnceListener = (java.util.function.Function<Object, Object>) (emitter) ->
        (java.util.function.Function<Object, Object>) (name) ->
        (java.util.function.Function<Object, Object>) (listener) ->
            (java.util.function.Supplier<Object>) () -> {
                ((EmitterBase) emitter).addListener((String) name, __once((EmitterBase) emitter, (String) name, listener), true);
                return null;
            };

    public static Object symbolOrStr = (java.util.function.Function<Object, Object>) (left) ->
        (java.util.function.Function<Object, Object>) (right) ->
        (java.util.function.Function<Object, Object>) (value) ->
            value instanceof String
                ? ((java.util.function.Function<Object, Object>) right).apply(value)
                : ((java.util.function.Function<Object, Object>) left).apply(value);
