# calvin-constrained

calvin-constrained is a C based implementation of Calvin, https://github.com/EricssonResearch/calvin-base, targeting devices with constraints on memory, processing power and power consumption.

All functionality included in the standard Calvin implementation is not feasible to include on a constrained device and a standard Calvin runtime is used to offload functionality to:

- Route data to and from the constrained runtime
- Access the shared key/value store
- Deploy and redeploy applications

The constrained runtime finds a Calvin runtime to offload functionality by using a discovery mechanism or a predefined list of runtime URIs.

The state of the implementation is experimental and it goes without saying that it should not be used in production in its current state, but it does provide an excellent starting point for further experimentation.

## Setup

Clone the constrained runtime repository with the recursive flag to include used third party modules:

```
git clone --recursive https://github.com/EricssonResearch/calvin-constrained.git
```
or get them later with  
```
git submodule init
git submodule update
```

And follow the platform specific instructions in the runtime/south/platform/X/README.md to build and start the runtime.
For example to build for x86 use:
```
make -f runtime/south/platform/x86/Makefile
```

## Runtime ports

Calvin-constrained is designed to be portable across different architectures and can execute on anything from bare metal to on top of an operating system.

The platform specific parts, such as initializing the platform and accessing hardware resources, are isolated from the core functionality. Runtime ports and instructions on how to create a new port are placed in the [runtime/south/platform](runtime/south/platform) folder.

## Transport clients

Transport clients handles the communication with the Calvin runtime acting as a proxy to offload functionality. The constrained runtime can support and use multiple transport clients but only one is active handling the link to the runtime acting as a proxy for the constrained runtime.

Implementations and instructions on how to create new transport clients are available in the [runtime/south/transport](runtime/south/transport) folder.

## Security

TLS can be enabled for all transport clients, the [runtime/south/crypto](runtime/south/crypto) folder contains instructions to enable it.

## Actors

The calvin-constrained runtime supports actors written:
- C, the [actors](actors) folder holds existing actors and instructions on how to write and add new actors
- Python, the [libmpy](libmpy) folder holds existing actors and instructions on how to add new actors

## Build flags

- CC_TRANSPORT_X, enables transport X.
- CC_DEBUG, enables debug logs.
- CC_DEEPSLEEP_ENABLED, enables and enters platform deep sleep for CC_SLEEP_TIME seconds after CC_INACTIVITY_TIMEOUT seconds of inactivity.
- CC_STORAGE_ENABLED, enables serialization and deserialization of the runtime state to and from nonvolatile memory.
- CC_RUNTIME_STATE_BUFFER_SIZE, size of the buffer used to read and write serialized runtime state.
- CC_STATE_CHECKPOINTING, enables serialization of the runtime state to nonvolatile memory on every state change.
- CC_TLS_ENABLED, enables TLS for all transport clients.
- CC_PYTHON_ENABLED, enables support for Python actors.
- CC_PYTHON_HEAP_SIZE, set heap size used by MicroPython.
- CC_GETOPT_ENABLED, enables runtime command line options when started using main.c.
