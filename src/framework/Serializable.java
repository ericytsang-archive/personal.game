package framework;

public interface Serializable
{
    /**
     * parses {data}, sets the instance's state to the one described by {data},
     *   created from the {toBytes} method.
     *
     * @param    data   data to parse. this data should have been created using
     *   the Serializable::toBytes method.
     *
     * @return   returns a reference to the calling instance.
     */
    public Serializable fromBytes(byte[] data);

    /**
     * returns the byte version of the {Serializable}.
     *
     * another instance of the same class can be take on the serialized state by
     *   calling {fromBytes}, while passing it the byte array created from
     *   {toBytes}.
     *
     * @return   the byte version of the {Serializable} instance. 
     */
    public byte[] toBytes();
}
