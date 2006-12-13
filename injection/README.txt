Injection Framework

With the injection framework it should be possible to easily inject
values into beans. Note that the injection framework doesn't specify
properties of a bean in the same way as JavaBeans does. Any field or
setter method can constitute a property.

The injection works in two phases:
1. Setup of the environment
2. Injection of objects

The environment could be setup in JNDI, but this is not required
by the injection framework. For JNDI setup an implementation is
provided.

Injection of objects can be specified programatically. For
annotation based injection an implementation is provided.

It's not IoC or MC injection, because it doesn't take into
account dependencies.

Although the injection framework contains annotation processors JSR-269
is left out of scope.