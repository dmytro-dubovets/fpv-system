package ua.fpv.util;

public class ClientNotFoundException extends RuntimeException
{
    public ClientNotFoundException(String message)
    {
        super(message);
    }
}
