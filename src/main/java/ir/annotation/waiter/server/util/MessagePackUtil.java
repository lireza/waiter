package ir.annotation.waiter.server.util;

import org.msgpack.value.*;
import org.msgpack.value.impl.*;

/**
 * Utility class that is useful to generate message pack's {@link Value}s.
 *
 * @author Alireza Pourtaghi
 */
public class MessagePackUtil {

    /**
     * Generates a message pack's map format.
     *
     * @param keyValues The map elements.
     * @return Newly created and ready to use {@link ImmutableMapValueImpl}.
     */
    public static ImmutableMapValue map(Value... keyValues) {
        return new ImmutableMapValueImpl(keyValues);
    }

    /**
     * Generates a message pack's array format.
     *
     * @param values The array elements.
     * @return Newly created and ready to use {@link ImmutableArrayValueImpl}.
     */
    public static ImmutableArrayValue array(Value... values) {
        return new ImmutableArrayValueImpl(values);
    }

    /**
     * Generates a message pack's string format.
     *
     * @param value The string value.
     * @return Newly created and ready to use {@link ImmutableStringValueImpl}.
     */
    public static ImmutableStringValue string(String value) {
        return new ImmutableStringValueImpl(value);
    }

    /**
     * Generates a message pack's boolean format.
     *
     * @param value The boolean value.
     * @return Newly created and ready to use {@link ImmutableBooleanValueImpl}.
     */
    public static ImmutableBooleanValue bool(boolean value) {
        return value ? ImmutableBooleanValueImpl.TRUE : ImmutableBooleanValueImpl.FALSE;
    }

    /**
     * Generates a message pack's binary format.
     *
     * @param value The byte array value.
     * @return Newly created and ready to use {@link ImmutableBinaryValueImpl}.
     */
    public static ImmutableBinaryValueImpl bytes(byte[] value) {
        return new ImmutableBinaryValueImpl(value);
    }
}
