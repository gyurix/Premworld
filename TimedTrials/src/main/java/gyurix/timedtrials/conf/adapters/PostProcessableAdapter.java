package gyurix.timedtrials.conf.adapters;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import gyurix.timedtrials.conf.PostProcessable;
import lombok.SneakyThrows;

import java.io.IOException;

public class PostProcessableAdapter implements TypeAdapterFactory {
    @SneakyThrows
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        if (PostProcessable.class.isAssignableFrom(typeToken.getRawType())) {
            TypeAdapter<T> delegateAdapter = gson.getDelegateAdapter(this, typeToken);
            return new TypeAdapter<T>() {
                @SneakyThrows
                @Override
                public T read(JsonReader jsonReader) {
                    T result = delegateAdapter.read(jsonReader);
                    ((PostProcessable) result).postProcess();
                    return result;
                }

                @Override
                public void write(JsonWriter jsonWriter, T object) throws IOException {
                    jsonWriter.value(object.toString());
                }
            }.nullSafe();
        }
        return null;
    }
}