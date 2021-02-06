package org.example.greeting.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.File;
import java.io.IOException;

public class GreetingServer {

  public static void main(String[] args) throws IOException, InterruptedException {
    System.out.println("Hello gRPC");

    // DEVELOPMENT - plaintext server
//    Server server = ServerBuilder.forPort(50051)
//        .addService(new GreetServiceImpl())
//        .build();

    // PRODUCTION - secured server
    // With server authentication SSL/TLS; custom CA root certificates; not on Android
    Server server = ServerBuilder.forPort(50052)
        .addService(new GreetServiceImpl())
        .useTransportSecurity(
            new File("ssl/server.crt"),
            new File("ssl/server.pem")
        )
        .build();

    server.start();

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.println("Received Shutdown Request");
      server.shutdown();
      System.out.println("Successfully stopped the server");
    }));

    server.awaitTermination();
  }

}
