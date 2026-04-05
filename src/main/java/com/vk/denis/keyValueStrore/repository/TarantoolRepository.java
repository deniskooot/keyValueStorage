package com.vk.denis.keyValueStrore.repository;

import com.google.protobuf.ByteString;
import com.vk.denis.Key;
import com.vk.denis.KeyValuePair;
import com.vk.denis.Value;
import io.tarantool.client.box.TarantoolBoxClient;
import io.tarantool.client.box.TarantoolBoxSpace;
import io.tarantool.mapping.SelectResponse;
import io.tarantool.mapping.TarantoolResponse;
import io.tarantool.mapping.Tuple;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TarantoolRepository implements Repository {
    public static final String SPACE = "KV";
    private static final Logger log = LoggerFactory.getLogger(TarantoolRepository.class);
    public static final int INDEX_OF_KEY_FIELD = 1;
    public static final int INDEX_OF_VALUE_FIELD = INDEX_OF_KEY_FIELD + 1;

    private final TarantoolBoxClient client;

    public TarantoolRepository(TarantoolBoxClient client) {
        this.client = client;
        client.eval(
                "box.cfg{};" +
                        "if not box.space." + SPACE + " then " +
                        "local kv = box.schema.space.create('" + SPACE + "', {if_not_exists = true, format = " +
                        "{{name='key', type='string'}, {name='value', type='varbinary', is_nullable=true}}});" +
                        "kv:create_index('primary', {type='tree', parts={1,'string'}, if_not_exists=true});" +
                        "end"
        ).join();
    }

    @Override
    public void put(@NonNull String key, @Nullable byte[] value) {
        TarantoolBoxSpace space = client.space(SPACE);
        List<Serializable> tuple = Arrays.asList(key, value);
        space.upsert(Arrays.asList(key, value),
                Collections.singletonList(Arrays.asList("=", INDEX_OF_VALUE_FIELD, value))).join();
    }

    @Override
    public @Nullable byte[] get(String key) {
        TarantoolBoxSpace space = client.space(SPACE);

        try {
            SelectResponse<List<Tuple<List<?>>>> result = space.select(key).get();
            List<Tuple<List<?>>> tuples = result.get();
            if (tuples.isEmpty()) {
                return null;
            }
            return (byte[]) tuples.getFirst().get().get(1);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Get operation failed", e);
            throw new FailedOperationException(e);
        }
    }

    @Override
    public void delete(@NonNull String key) {
        TarantoolBoxSpace space = client.space(SPACE);
        space.delete(List.of(key)).join();
    }

    @Override
    public Iterable<KeyValuePair> range(String from, String to) {
        return serverSideRange(from, to);
    }

    private List<KeyValuePair> serverSideRange(String from, String to) {
        String script = """
                local result = {}
                local start_key = '%s'
                local end_key = '%s'
                for _, tuple in box.space.%s:pairs(start_key, {iterator = 'GE'}) do
                    if not (tuple.key <= end_key) then
                        break
                    end
                    table.insert(result, tuple)
                end
                return result
                """.formatted(from, to, SPACE);
        try {
            List<?> total = client.eval(script).get().get();
            if (total.isEmpty()) {
                return Collections.emptyList();
            }
            List<List<?>> input = (List<List<?>>) total.getFirst();
            List<KeyValuePair> result = new ArrayList<>(input.size());
            for (List<?> row : input) {
                KeyValuePair current = KeyValuePair.newBuilder()
                        .setKey(Key.newBuilder().setData((String) row.getFirst()).build())
                        .setValue(Value.newBuilder().setData(ByteString.copyFrom((byte[]) row.get(1))).build())
                        .build();
                result.add(current);
            }

            return result;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Range operation failed", e);
            throw new FailedOperationException(e);
        }
    }

    @Override
    public long count() {
        try {
            CompletableFuture<TarantoolResponse<List<?>>> result =
                    client.eval("return box.space." + SPACE + ":count()");
            Number out = (Number) result.get().get().getFirst();
            return out.longValue();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Count operation failed", e);
            throw new FailedOperationException(e);
        }
    }
}
