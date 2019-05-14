public class RemoteTestInterfaceImpl implements RemoteTestInterface {
    private int x = 0;

    @Override
    public int read() {
        return x++;
    }
}
