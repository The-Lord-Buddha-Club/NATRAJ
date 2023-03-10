---
description: Implementing new audit records and features
---

# Extension

To add support for a new protocol or custom abstraction the following steps need to be performed.

## Protocol Buffer Definitions

First, a type definition of the new audit record type must be added to the AuditRecord protocol buffers definitions, as well as a **Type enumeration** following the naming convention with the **NC prefix**.

First, make sure you have code generator plugin\(s\) that NETCAP is using to accelerate the protocol buffer en- and decoding. Get the plugins with:

```go
$ go get github.com/gogo/protobuf/...
```

The framework for this can be found here:

{% embed url="https://github.com/gogo/protobuf" caption="" %}

Recompile the protocol buffers with:

```go
$ zeus gen-proto-dev
```

This will create the type definitions for your new audit record in the **types** package.

## Encoder Implementation

After recompiling the protocol buffers, a file for the new decoder named after the protocol must be created in the decoder package. The new file must contain a variable created with **CreateLayerEncoder** or **CreateCustomEncoder** depending on the desired decoder type.

Lets take a brief look at a very simple **LayerEncoder**, for example for the ARP protocol:

```go
package decoder

import (
   "github.com/dreadl0ck/gopacket"
   "github.com/dreadl0ck/gopacket/layers"
   "github.com/dreadl0ck/netcap/types"
   "github.com/golang/protobuf/proto"
)

var arpEncoder = CreateLayerEncoder(
   types.Type_NC_ARP, 
   layers.LayerTypeARP, 
   func(layer gopacket.Layer, timestamp string) proto.Message {
      if arp, ok := layer.(*layers.ARP); ok {
         return &types.ARP{
            Timestamp:       timestamp,
            AddrType:        int32(arp.AddrType),
            Protocol:        int32(arp.Protocol),
            HwAddressSize:   int32(arp.HwAddressSize),
            ProtAddressSize: int32(arp.ProtAddressSize),
            Operation:       int32(arp.Operation),
            SrcHwAddress:    arp.SourceHwAddress,
            SrcProtAddress:  arp.SourceProtAddress,
            DstHwAddress:    arp.DstHwAddress,
            DstProtAddress:  arp.DstProtAddress,
         }
      }
      return nil
   },
)
```

 Since **ARP** can be decoded by **gopacket** already, all we have to do is check if the packet has the **ARP** layer, and if yes, convert it to the **types.ARP** audit record and return it.

The constructor for a **LayerEncoder** needs the type enumeration for the new audit record, as well as the **gopacket.LayerType**, followed by the actual decoder function. This function will be called for every network packet.

As you can see, **LayerEncoders** are tied to **gopacket**. If you want to implement custom decoding logic or support for a new protocol, you essentially have two options:

* implement protocol decoding in **gopacket**, then use a **LayerEncoder** in netcap
* implement protocol decoding in a **CustomEncoder**

A **CustomEncoder** works the same way but offers more flexibility for the implementation, like functions for initialisation and teardown. The CustomEncoder constructor signature looks as follows:

```go
func CreateCustomEncoder(
    t types.Type, 
    name string, 
    postinit func(*CustomEncoder) error, 
    handler CustomEncoderHandler, 
    deinit func(*CustomEncoder) error
) *CustomEncoder
```

The **CustomEncoderHandler** will simply receive the raw **gopacket.Packet** and return a **proto.Message**:

```go
CustomEncoderHandler = func(p gopacket.Packet) proto.Message
```

Depending on the choice of the decoder type, the new variable must be added to the customEncoderSlice in **decoder/customEncoder.go** or layerEncoderSlice in **decoder/layerEncoder.go**.

## Audit Record Interface Implementation

Next, the interface for conversion to CSV and JSON and exporting metrics must be implemented in the types package, by creating a new file with the protocol name and implementing the **types.AuditRecord** interface:

```go
// AuditRecord is the interface for basic operations with NETCAP audit records
// this includes dumping as CSV or JSON or prometheus metrics
// and provides access to the timestamp of the audit record
type AuditRecord interface {

   // returns CSV values
   CSVRecord() []string

   // returns CSV header fields
   CSVHeader() []string

   // used to retrieve the timestamp of the audit record for labeling
   Time() string

   // Src returns the source of an audit record
   // for Layer 2 records this shall be the MAC address
   // for Layer 3+ records this shall be the IP address
   Src() string

   // Dst returns the source of an audit record
   // for Layer 2 records this shall be the MAC address
   // for Layer 3+ records this shall be the IP address
   Dst() string

   // increments the metric for the audit record
   Inc()

   // returns the audit record as JSON
   JSON() (string, error)

   // can be implemented to set additional information for each audit record
   // important:
   //  - MUST be implemented on a pointer of an instance
   //  - the passed in packet context MUST be set on the Context field of the current audit record
   SetPacketContext(ctx *PacketContext)
}
```

If the new protocol contains sub-structures, functions to convert them to strings need to be implemented as well. Take a look at other decoders that have lots of substructures, for example **DNS**.

## Add Initializer

Finally, the **InitRecord\(typ types.Type\) \(record proto.Message\)** function in netcap.go needs to be updated, to initialize the structure for the new type.

