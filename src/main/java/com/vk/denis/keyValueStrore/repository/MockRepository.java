package com.vk.denis.keyValueStrore.repository;

import com.google.protobuf.ByteString;
import com.vk.denis.Key;
import com.vk.denis.KeyValuePair;
import com.vk.denis.Value;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class MockRepository implements Repository {
    private final SortedMap<String, byte[]> map = new TreeMap<>();

    @Override
    public void put(@NonNull String key, @Nullable byte[] value) {
        map.put(key, value);
    }

    @Override
    public byte @Nullable [] get(String key) {
        return map.get(key);
    }

    @Override
    public void delete(@NonNull String key) {
        map.remove(key);
    }

    @Override
    public Iterable<KeyValuePair> range(String from, String to) {
        return map.subMap(from, to).entrySet().stream()
                .map(MockRepository::toKeyValuePair)
                .toList();
    }

    @Override
    public long count() {
        return map.size();
    }

    private static KeyValuePair toKeyValuePair(Map.Entry<String, byte[]> entry) {
        return KeyValuePair.newBuilder()
                .setKey(Key.newBuilder().setData(entry.getKey()).build())
                .setValue(Value.newBuilder().setData(ByteString.copyFrom(entry.getValue())).build())
                .build();
    }
}
