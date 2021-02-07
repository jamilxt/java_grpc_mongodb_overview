package org.example.blog.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlogServer {

  public static void main(String[] args) throws IOException, InterruptedException {

    Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
    mongoLogger.setLevel(Level.SEVERE);

    Server server = ServerBuilder.forPort(50051)
        .addService(new BlogServiceImpl())
        .addService(ProtoReflectionService.newInstance()) // reflection
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
