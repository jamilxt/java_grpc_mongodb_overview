package org.example.blog.client;

import com.proto.blog.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {

  public static void main(String[] args) {
    System.out.println("Hello I'm a gRPC client");

    BlogClient main = new BlogClient();
    main.run();
  }


  private void run() {
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
        .usePlaintext()
        .build();

    BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

    Blog blog = Blog.newBuilder()
        .setAuthorId("jamilxt")
        .setTitle("New blog!")
        .setContent("Hello world this is my new blog!")
        .build();

    CreateBlogResponse createResponse = blogClient.createBlog(
        CreateBlogRequest.newBuilder()
            .setBlog(blog)
            .build()
    );

    System.out.println("Received create blog response");
    System.out.println(createResponse.toString());

    String blogId = createResponse.getBlog().getId();

    System.out.println("Reading blog...");
    ReadBlogResponse readBlogResponse = blogClient.readBlog(ReadBlogRequest.newBuilder()
        .setBlogId(blogId)
        .build());

    System.out.println(readBlogResponse.toString());
//    trigger a not found error
//    System.out.println("Reading blog with non existing id...");
//    ReadBlogResponse readBlogResponseNotFound = blogClient.readBlog(ReadBlogRequest.newBuilder()
//        .setBlogId("601f897fdeef8f0b89efeb94")
//        .build());

  }

}
