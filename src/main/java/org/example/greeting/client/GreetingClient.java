package org.example.greeting.client;

import com.proto.greet.*;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

    // doUnaryCall(channel);
    // doServerStreamingCall(channel);
    // doClientStreamingCall(channel);
    // doBiDiStreamingCall(channel);
    doUnaryCallWithDeadline(channel);

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

  private void doClientStreamingCall(ManagedChannel channel) {
    // create a asynchronous client
    GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

    CountDownLatch latch = new CountDownLatch(1);

    StreamObserver<LongGreetRequest> requestObserver = asyncClient.longGreet(new StreamObserver<LongGreetResponse>() {
      @Override
      public void onNext(LongGreetResponse value) {
        // we get a response from the server
        System.out.println("Received a response from the server");
        System.out.println(value.getResult());
        // onNext will be called only once
      }

      @Override
      public void onError(Throwable t) {
        // we get an error from the server
      }

      @Override
      public void onCompleted() {
        // the server is done sending us data
        // onCompleted will be called right after onNext()
        System.out.println("Server has completed sending us something");
        latch.countDown();
      }
    });

    // streaming message #1
    System.out.println("sending message 1");
    requestObserver.onNext(LongGreetRequest.newBuilder()
        .setGreeting(Greeting.newBuilder()
            .setFirstName("Jamilur")
            .build())
        .build());

    // streaming message #2
    System.out.println("sending message 2");
    requestObserver.onNext(LongGreetRequest.newBuilder()
        .setGreeting(Greeting.newBuilder()
            .setFirstName("Asadur")
            .build())
        .build());

    // streaming message #3
    System.out.println("sending message 3");
    requestObserver.onNext(LongGreetRequest.newBuilder()
        .setGreeting(Greeting.newBuilder()
            .setFirstName("Adnan")
            .build())
        .build());

    // we tell the server that the client is done sending data
    requestObserver.onCompleted();

    try {
      latch.await(3L, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void doBiDiStreamingCall(ManagedChannel channel) {
    // create a asynchronous client
    GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

    CountDownLatch latch = new CountDownLatch(1);

    StreamObserver<GreetEveryoneRequest> requestObserver = asyncClient.greetEveryone(new StreamObserver<GreetEveryoneResponse>() {
      @Override
      public void onNext(GreetEveryoneResponse value) {
        System.out.println("Response from server: " + value.getResult());
      }

      @Override
      public void onError(Throwable t) {
        latch.countDown();
      }

      @Override
      public void onCompleted() {
        System.out.println("Server is done sending data");
        latch.countDown();
      }
    });

    Arrays.asList("Mohiminul", "Mojahidul", "Jamilur", "Asadur", "Adnan").forEach(
        name -> {
          System.out.println("Sending: " + name);
          requestObserver.onNext(GreetEveryoneRequest.newBuilder()
              .setGreeting(Greeting.newBuilder()
                  .setFirstName(name))
              .build());
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
    );

    requestObserver.onCompleted();

    try {
      latch.await(3L, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }

  private void doUnaryCallWithDeadline(ManagedChannel channel) {
    GreetServiceGrpc.GreetServiceBlockingStub blockingStub = GreetServiceGrpc.newBlockingStub(channel);

    // first call (3000 ms deadline)
    try {
      System.out.println("Sending a request with a deadline of 3000ms ");
      GreetWithDeadlineResponse response = blockingStub.withDeadline(Deadline.after(3000, TimeUnit.MILLISECONDS))
          .greetWithDeadline(GreetWithDeadlineRequest.newBuilder()
              .setGreeting(Greeting.newBuilder()
                  .setFirstName("Jamilur")
                  .build())
              .build());
      System.out.println(response.getResult());
    } catch (StatusRuntimeException e) {
      if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
        System.out.println("Deadline has been exceeded, we won't want the response");
      } else {
        e.printStackTrace();
      }
    }

    // second call (100 ms deadline)
    try {
      System.out.println("Sending a request with a deadline of 100ms ");
      GreetWithDeadlineResponse response = blockingStub.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS))
          .greetWithDeadline(GreetWithDeadlineRequest.newBuilder()
              .setGreeting(Greeting.newBuilder()
                  .setFirstName("Jamilur")
                  .build())
              .build());
      System.out.println(response.getResult());
    } catch (StatusRuntimeException e) {
      if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
        System.out.println("Deadline has been exceeded, we won't want the response");
      } else {
        e.printStackTrace();
      }
    }
  }

}
