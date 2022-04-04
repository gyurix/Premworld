package gyurix.shopsystem.conf.adapters;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import gyurix.shopsystem.conf.StringSerializable;
import lombok.SneakyThrows;

import java.io.IOException;
import java.lang.reflect.Constructor;

public class StringSerializableAdapter implements TypeAdapterFactory {
    @SneakyThrows
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        if (StringSerializable.class.isAssignableFrom(typeToken.getRawType())) {
            Constructor<?> con = typeToken.getRawType().getConstructor(String.class);
            return new TypeAdapter<T>() {
                @SneakyThrows
                @Override
                public T read(JsonReader jsonReader) {
                    return (T) con.newInstance(jsonReader.nextString());
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