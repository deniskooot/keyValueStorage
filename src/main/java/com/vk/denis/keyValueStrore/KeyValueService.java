package com.vk.denis.keyValueStrore;

import com.google.protobuf.Empty;
import com.vk.denis.*;
import com.vk.denis.keyValueStrore.repository.Repository;
import io.grpc.stub.StreamObserver;


public class KeyValueService extends KeyValueServiceGrpc.KeyValueServiceImplBase {


    private final Repository repository;

    public KeyValueService(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void put(KeyValuePair request, StreamObserver<Empty> responseObserver) {
        repository.put(request.getKey().getData(), request.getValue().getData().toByteArray());
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void get(Key request, StreamObserver<Value> responseObserver) {
        byte[] bytes = repository.get(request.getData());
        Value.Builder builder = Value.newBuilder();
        if (bytes != null) {
            builder.setData(com.google.protobuf.ByteString.copyFrom(bytes));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void delete(Key request, StreamObserver<Empty> responseObserver) {
        repository.delete(request.getData());
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void range(RangeRequest request, StreamObserver<KeyValuePair> responseObserver) {
        for (KeyValuePair keyValuePair : repository.range(request.getFrom().getData(), request.getTo().getData())) {
            responseObserver.onNext(keyValuePair);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void count(Empty request, StreamObserver<CountResponse> responseObserver) {
        long count =  repository.count();
        responseObserver.onNext(CountResponse.newBuilder().setData(count).build());
        responseObserver.onCompleted();
    }
}
