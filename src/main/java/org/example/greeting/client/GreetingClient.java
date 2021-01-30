package org.example.greeting.client;

import com.proto.greet.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GreetingClient {

  public static void main(String[] args) {
    System.out.println("Hello I'm a gRPC client");

    GreetingClient main = new GreetingClient();
    main.run();
  }

  public void run() {
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
        .usePlaintext()
        .build();

    //doUnaryCall(channel);
    //doServerStreamingCall(channel);

    System.out.println("Shutting down channel");
    channel.shutdown();
  }

  private void doUnaryCall(ManagedChannel channel) {
    // created a greet service client (blocking - synchronous
    GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

    // Unary
    // created a protocol buffer greeting message
    Greeting greeting = Greeting.newBuilder()
        .setFirstName("Jamilur")
        .setLastName("Rahman")
        .build();

    // do the same for a GreetRequest
    GreetRequest greetRequest = GreetRequest.newBuilder()
        .setGreeting(greeting)
        .build();

    // call the RPC and get back a GreetResponse (protocol buffers)
    GreetResponse greetResponse = greetClient.greet(greetRequest);

    System.out.println(greetResponse.getResult());
  }

  private void doServerStreamingCall(ManagedChannel channel) {
    // created a greet service client (blocking - synchronous
    GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

    // Server Streaming
    GreetManyTimesRequest greetManyTimesRequest = GreetManyTimesRequest
        .newBuilder()
        .setGreeting(Greeting.newBuilder().setFirstName("Jamilur"))
        .build();

    greetClient.greetManyTimes(greetManyTimesRequest)
        .forEachRemaining(greetManyTimesResponse -> {
          System.out.println(greetManyTimesResponse.getResult());
        });
  }

}
