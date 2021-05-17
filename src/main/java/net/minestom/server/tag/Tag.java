package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class Tag<T> {

    private final String key;
    private final Function<NBTCompound, T> readFunction;
    private final BiConsumer<NBTCompound, T> writeConsumer;

    private Tag(@NotNull String key,
                @NotNull Function<NBTCompound, T> readFunction,
                @NotNull BiConsumer<NBTCompound, T> writeConsumer) {
        this.key = key;
        this.readFunction = readFunction;
        this.writeConsumer = writeConsumer;
    }

    public @NotNull String getKey() {
        return key;
    }

    protected T read(@NotNull NBTCompound nbtCompound) {
        return readFunction.apply(nbtCompound);
    }

    protected void write(@NotNull NBTCompound nbtCompound, @NotNull T value) {
        this.writeConsumer.accept(nbtCompound, value);
    }

    public static @NotNull Tag<Byte> Byte(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getByte(key),
                (nbtCompound, value) -> nbtCompound.setByte(key, value));
    }

    public static @NotNull Tag<Short> Short(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getShort(key),
                (nbtCompound, value) -> nbtCompound.setShort(key, value));
    }

    public static @NotNull Tag<Integer> Integer(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getInt(key),
                (nbtCompound, integer) -> nbtCompound.setInt(key, integer));
    }

    public static @NotNull Tag<Long> Long(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getLong(key),
                (nbtCompound, value) -> nbtCompound.setLong(key, value));
    }

    public static @NotNull Tag<Float> Float(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getFloat(key),
                (nbtCompound, value) -> nbtCompound.setFloat(key, value));
    }

    public static @NotNull Tag<Double> Double(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getDouble(key),
                (nbtCompound, value) -> nbtCompound.setDouble(key, value));
    }

    public static @NotNull Tag<byte[]> ByteArray(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getByteArray(key),
                (nbtCompound, value) -> nbtCompound.setByteArray(key, value));
    }

    public static @NotNull Tag<String> String(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getString(key),
                (nbtCompound, value) -> nbtCompound.setString(key, value));
    }

    public static @NotNull Tag<NBT> NBT(@NotNull String key) {
        return new Tag<>(key,
                nbt -> {
                    var currentNBT = nbt.get(key);

                    // Avoid a NPE when cloning a null variable.
                    if (currentNBT == null) {
                        return null;
                    }

                    return currentNBT.deepClone();
                },
                ((nbt, value) -> nbt.set(key, value.deepClone())));
    }

    public static @NotNull Tag<int[]> IntArray(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getIntArray(key),
                (nbtCompound, value) -> nbtCompound.setIntArray(key, value));
    }

    public static @NotNull Tag<long[]> LongArray(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getLongArray(key),
                (nbtCompound, value) -> nbtCompound.setLongArray(key, value));
    }
}
