    // Port of Node/EventEmitter.js. Listeners live on the emitter object.
    // Java EffectFn functions return an Effect thunk, unlike their JavaScript
    // counterparts; the adapter below applies that convention consistently.
    public static class EmitterBase {
        public final java.util.Map<String, java.util.List<Object>> listeners = new java.util.concurrent.ConcurrentHashMap<>();

        public void addListener(String name, Object listener, boolean prepend) {
            java.util.List<Object> registered = listeners.computeIfAbsent(name, key -> new java.util.concurrent.CopyOnWriteArrayList<>());
            if (prepend) registered.add(0, listener);
            else registered.add(listener);
        }

        public void removeListener(String name, Object listener) {
            java.util.List<Object> registered = listeners.get(name);
            if (registered == null) return;
            // Node removes the most recently added matching listener, including
            // a once wrapper whose original listener is being unsubscribed.
            for (int index = registered.size() - 1; index >= 0; index--) {
                Object candidate = registered.get(index);
                if (candidate == listener || (candidate instanceof OnceListener && ((OnceListener) candidate).listener == listener)) {
                    registered.remove(index);
                    return;
                }
            }
        }

        public boolean fire(String name, Object... args) {
            java.util.List<Object> registered = listeners.get(name);
            if (registered == null || registered.isEmpty()) return false;
            for (Object listener : new java.util.ArrayList<>(registered)) {
                if (listener instanceof OnceListener) removeListener(name, listener);
                invoke(listener, args);
            }
            return true;
        }

        public void invoke(Object listener, Object[] args) {
            if (listener instanceof OnceListener) listener = ((OnceListener) listener).listener;
            Object effect = listener;
            if (effect instanceof java.util.function.Supplier) {
                ((java.util.function.Supplier<Object>) effect).get();
            } else if (effect instanceof java.util.function.Function) {
                for (Object arg : args) {
                    if (!(effect instanceof java.util.function.Function)) break;
                    effect = ((java.util.function.Function<Object, Object>) effect).apply(arg);
                }
                if (effect instanceof java.util.function.Supplier) ((java.util.function.Supplier<Object>) effect).get();
            }
        }
    }

    // A one-shot listener removes itself before the first call and forwards the
    // original arguments, matching Node's once wrapper and error behavior.
    private static final class OnceListener {
        final Object listener;
        OnceListener(Object listener) { this.listener = listener; }
    }

    private static Object __once(Object listener) {
        return new OnceListener(listener);
    }

    // EffectFnN is curried on this backend, while unsafeEmitFn accepts any
    // number of event arguments. Buffer each application; the final Supplier
    // step performs Node's emit and returns whether a listener existed.
    private static final class EmitCall
            implements java.util.function.Function<Object, Object>, java.util.function.Supplier<Object> {
        final EmitterBase emitter;
        final String name;
        final Object[] args;
        EmitCall(EmitterBase emitter, String name, Object[] args) {
            this.emitter = emitter; this.name = name; this.args = args;
        }
        public Object apply(Object argument) {
            Object[] extended = java.util.Arrays.copyOf(args, args.length + 1);
            extended[args.length] = argument;
            return new EmitCall(emitter, name, extended);
        }
        public Object get() { return emitter.fire(name, args); }
    }

    public static Object $new = (java.util.function.Supplier<Object>) () -> new EmitterBase();

    public static Object eventNamesImpl = (java.util.function.Function<Object, Object>) (emitter) ->
        ((EmitterBase) emitter).listeners.entrySet().stream()
            .filter(entry -> !entry.getValue().isEmpty()).map(java.util.Map.Entry::getKey)
            .toArray(Object[]::new);

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

    // unsafeEmitFn is used through runEffectFn; each applied argument is an
    // emit payload and the final effect returns an Effect Boolean.
    public static Object unsafeEmitFn = (java.util.function.Function<Object, Object>) (emitter) ->
        (java.util.function.Function<Object, Object>) (name) ->
            new EmitCall((EmitterBase) emitter, (String) name, new Object[0]);

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
                ((EmitterBase) emitter).addListener((String) name, __once(listener), false);
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
                ((EmitterBase) emitter).addListener((String) name, __once(listener), true);
                return null;
            };

    public static Object symbolOrStr = (java.util.function.Function<Object, Object>) (left) ->
        (java.util.function.Function<Object, Object>) (right) ->
        (java.util.function.Function<Object, Object>) (value) ->
            value instanceof String
                ? ((java.util.function.Function<Object, Object>) right).apply(value)
                : ((java.util.function.Function<Object, Object>) left).apply(value);
