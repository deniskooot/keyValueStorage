package com.vk.denis.keyValueStrore.repository;


import com.vk.denis.KeyValuePair;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface Repository {

    void put(@Nonnull String key, @Nullable byte[] value) throws FailedOperationException;

    @Nullable
    byte[] get(String key) throws FailedOperationException;

    void delete(@Nonnull String key) throws FailedOperationException;

    Iterable<KeyValuePair> range(String from, String to) throws FailedOperationException;

    long count();

    class FailedOperationException extends RuntimeException {
        public FailedOperationException(String message) {
            super(message);
        }

        public FailedOperationException(Throwable e) {
            super(e);
        }
    }
}
