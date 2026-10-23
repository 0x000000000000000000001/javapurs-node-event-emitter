# purescript-node-event-emitter

Java port of the PureScript `node-event-emitter` package. See the
[port maintenance guide](../javapurs/docs/port-maintenance.md)
for source layout, local dependency selection and JVM validation.

## JVM tests

`./bin/test` selects `node-event-emitter` in the [common isolated runner](../javapurs/docs/testing.md#port-particulier). It awaits the original Spec tests plus the port regressions and requires 19 successes and a completion marker. A captured Spec assertion failure fails the command.
Use `./bin/test --help` for options and `./bin/test --clean` to rebuild the backend. The guide covers prerequisites, Java settings and retained failure logs; this checkout and its outputs are preserved.

The [M23 validation](../javapurs/docs/testing.md#validation-m23) recorded a failing suite: 1/14 tests passed with Boolean-to-Supplier cast errors. [M25](../javapurs/docs/testing.md#validation-m25) fixes the Java `EffectFn` ABI, once-listener arguments/removal, late-event replay and exception propagation; 19/19 tests and the runner probes now pass.

Bindings for the [`event-emitter`](https://nodejs.org/api/events.html#class-eventemitter) class.
