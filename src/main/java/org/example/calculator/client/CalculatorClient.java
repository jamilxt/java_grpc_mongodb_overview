package org.example.calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {

  public static void main(String[] args) {
    System.out.println("Hello I'm a gRPC client");

    CalculatorClient main = new CalculatorClient();
    main.run();
  }


  private void run() {
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
        .usePlaintext()
        .build();

//    doUnaryCall(channel);
//    doServerStreamingCall(channel);
//    doClientStreamingCall(channel);
    doBiDiStreamingCall(channel);

    System.out.println("Shutting down channel");
    channel.shutdown();
  }

  private void doUnaryCall(ManagedChannel channel) {
    // created a calculator service client (blocking - synchronous)
    CalculatorServiceGrpc.CalculatorServiceBlockingStub stub =
        CalculatorServiceGrpc.newBlockingStub(channel);

    // Unary
    SumRequest request = SumRequest.newBuilder()
        .setFirstNumber(10)
        .setSecondNumber(25)
        .build();

    SumResponse response = stub.sum(request);
    System.out.println(request.getFirstNumber() + " + " + request.getSecondNumber()
        + " = " + response.getSumResult());
  }

  private void doServerStreamingCall(ManagedChannel channel) {
    CalculatorServiceGrpc.CalculatorServiceBlockingStub stub =
        CalculatorServiceGrpc.newBlockingStub(channel);

    // Streaming Server
    long number = 567890304098342L;
    stub.primeNumberDecomposition(PrimeNumberDecompositionRequest
        .newBuilder()
        .setNumber(number)
        .build()).forEachRemaining(primeNumberDecompositionResponse -> {
      System.out.println(primeNumberDecompositionResponse.getPrimeFactor());
    });
  }

  private void doClientStreamingCall(ManagedChannel channel) {
    CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);

    CountDownLatch latch = new CountDownLatch(1);

    StreamObserver<ComputeAverageRequest> requestObserver = asyncClient.computerAverage(new StreamObserver<ComputeAverageResponse>() {
      @Override
      public void onNext(ComputeAverageResponse value) {
        System.out.println("Received a response from the server");
        System.out.println(value.getAverage());
      }

      @Override
      public void onError(Throwable t) {

      }

      @Override
      public void onCompleted() {
        System.out.println("Server has completed sending us data");
        latch.countDown();
      }
    });

    // we send 10000 messages to our server (client streaming as it's best)
    for (int i = 0; i < 10000; i++) {
      requestObserver.onNext(ComputeAverageRequest.newBuilder()
          .setNumber(i)
          .build());
    }

    // we expect the average to be 10 / 4 = 2.5
    requestObserver.onCompleted();

    try {
      latch.await(3L, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }

  private void doBiDiStreamingCall(ManagedChannel channel) {
    CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);

    CountDownLatch latch = new CountDownLatch(1);

    StreamObserver<FindMaximumRequest> requestObserver = asyncClient.findMaximum(new StreamObserver<FindMaximumResponse>() {
      @Override
      public void onNext(FindMaximumResponse value) {
        System.out.println("Got new maximum from server: " + value.getMaximum());
      }

      @Override
      public void onError(Throwable t) {
        latch.countDown();
      }

      @Override
      public void onCompleted() {
        System.out.println("Server is done sending messages");
      }
    });

    Arrays.asList(3, 5, 17, 9, 8, 30, 12).forEach(
        number -> {
          System.out.println("Sending number: " + number);
          requestObserver.onNext(FindMaximumRequest.newBuilder()
              .setNumber(number)
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

}
