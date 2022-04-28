package gyurix.timedtrials.conf.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import gyurix.timedtrials.util.ItemUtils;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class ItemStackAdapter extends TypeAdapter<ItemStack> {
    @Override
    public ItemStack read(JsonReader jsonReader) throws IOException {
        return ItemUtils.stringToItemStack(jsonReader.nextString());
    }

    @Override
    public void write(JsonWriter jsonWriter, ItemStack item) throws IOException {
        jsonWriter.value(ItemUtils.itemToString(item));
    }
}
