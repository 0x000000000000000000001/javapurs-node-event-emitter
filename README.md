# purescript-node-event-emitter

## JVM tests

`./bin/test` selects `node-event-emitter` in the [common isolated runner](../javapurs/docs/testing.md#port-particulier). It awaits the original Spec tests and requires 14 successes and a completion marker. A captured Spec assertion failure fails the command.
Use `./bin/test --help` for options and `./bin/test --clean` to rebuild the backend. The guide covers prerequisites, Java settings and retained failure logs; this checkout and its outputs are preserved.

The [M23 validation](../javapurs/docs/testing.md#validation-m23) records a failing suite: 1/14 tests pass, with Boolean-to-Supplier cast errors in the existing event-emitter FFI path. The historical launcher returned before these asynchronous results. Launcher migration does not certify this FFI.

Bindings for the [`event-emitter`](https://nodejs.org/api/events.html#class-eventemitter) class.
