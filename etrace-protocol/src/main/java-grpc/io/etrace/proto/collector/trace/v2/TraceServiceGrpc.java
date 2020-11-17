package io.etrace.proto.collector.trace.v2;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.22.1)",
    comments = "Source: collector/trace/v2/trace_service.proto")
public final class TraceServiceGrpc {

  private TraceServiceGrpc() {}

  public static final String SERVICE_NAME = "io.etrace.proto.collector.trace.v2.TraceService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<io.etrace.proto.collector.trace.v2.TraceServiceRequest,
      io.etrace.proto.collector.trace.v2.TraceServiceResponse> getSendMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Send",
      requestType = io.etrace.proto.collector.trace.v2.TraceServiceRequest.class,
      responseType = io.etrace.proto.collector.trace.v2.TraceServiceResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.etrace.proto.collector.trace.v2.TraceServiceRequest,
      io.etrace.proto.collector.trace.v2.TraceServiceResponse> getSendMethod() {
    io.grpc.MethodDescriptor<io.etrace.proto.collector.trace.v2.TraceServiceRequest, io.etrace.proto.collector.trace.v2.TraceServiceResponse> getSendMethod;
    if ((getSendMethod = TraceServiceGrpc.getSendMethod) == null) {
      synchronized (TraceServiceGrpc.class) {
        if ((getSendMethod = TraceServiceGrpc.getSendMethod) == null) {
          TraceServiceGrpc.getSendMethod = getSendMethod = 
              io.grpc.MethodDescriptor.<io.etrace.proto.collector.trace.v2.TraceServiceRequest, io.etrace.proto.collector.trace.v2.TraceServiceResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "io.etrace.proto.collector.trace.v2.TraceService", "Send"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etrace.proto.collector.trace.v2.TraceServiceRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etrace.proto.collector.trace.v2.TraceServiceResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new TraceServiceMethodDescriptorSupplier("Send"))
                  .build();
          }
        }
     }
     return getSendMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static TraceServiceStub newStub(io.grpc.Channel channel) {
    return new TraceServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static TraceServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new TraceServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static TraceServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new TraceServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class TraceServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void send(io.etrace.proto.collector.trace.v2.TraceServiceRequest request,
        io.grpc.stub.StreamObserver<io.etrace.proto.collector.trace.v2.TraceServiceResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getSendMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getSendMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.etrace.proto.collector.trace.v2.TraceServiceRequest,
                io.etrace.proto.collector.trace.v2.TraceServiceResponse>(
                  this, METHODID_SEND)))
          .build();
    }
  }

  /**
   */
  public static final class TraceServiceStub extends io.grpc.stub.AbstractStub<TraceServiceStub> {
    private TraceServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private TraceServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TraceServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new TraceServiceStub(channel, callOptions);
    }

    /**
     */
    public void send(io.etrace.proto.collector.trace.v2.TraceServiceRequest request,
        io.grpc.stub.StreamObserver<io.etrace.proto.collector.trace.v2.TraceServiceResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSendMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class TraceServiceBlockingStub extends io.grpc.stub.AbstractStub<TraceServiceBlockingStub> {
    private TraceServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private TraceServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TraceServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new TraceServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public io.etrace.proto.collector.trace.v2.TraceServiceResponse send(io.etrace.proto.collector.trace.v2.TraceServiceRequest request) {
      return blockingUnaryCall(
          getChannel(), getSendMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class TraceServiceFutureStub extends io.grpc.stub.AbstractStub<TraceServiceFutureStub> {
    private TraceServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private TraceServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TraceServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new TraceServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.etrace.proto.collector.trace.v2.TraceServiceResponse> send(
        io.etrace.proto.collector.trace.v2.TraceServiceRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getSendMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SEND = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final TraceServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(TraceServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SEND:
          serviceImpl.send((io.etrace.proto.collector.trace.v2.TraceServiceRequest) request,
              (io.grpc.stub.StreamObserver<io.etrace.proto.collector.trace.v2.TraceServiceResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class TraceServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    TraceServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.etrace.proto.collector.trace.v2.TraceServiceProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("TraceService");
    }
  }

  private static final class TraceServiceFileDescriptorSupplier
      extends TraceServiceBaseDescriptorSupplier {
    TraceServiceFileDescriptorSupplier() {}
  }

  private static final class TraceServiceMethodDescriptorSupplier
      extends TraceServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    TraceServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (TraceServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new TraceServiceFileDescriptorSupplier())
              .addMethod(getSendMethod())
              .build();
        }
      }
    }
    return result;
  }
}
