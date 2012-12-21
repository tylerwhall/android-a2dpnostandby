android-a2dpnostandby
=====================

Prevent bluetooth A2DP standby on Android

Plays silent audio whenever A2DP is connected. This is a workaround for a buggy
A2DP receiver that does not support standby. Standby causes it to disconnect.

It is lower overhead to disable standby in the framework when using a custom
rom, but this works on stock at the expense of starting a thread when A2DP is
connected.
