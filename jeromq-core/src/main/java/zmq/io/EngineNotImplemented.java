package zmq.io;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

public class EngineNotImplemented implements IEngine
{
    @SideEffectFree
    public EngineNotImplemented()
    {
        throw new UnsupportedOperationException(getClass().getName() + " is not implemented");
    }

    @SideEffectFree
    @Override
    public void plug(IOThread ioThread, SessionBase session)
    {
    }

    @SideEffectFree
    @Override
    public void terminate()
    {
    }

    @SideEffectFree
    @Override
    public void restartInput()
    {
    }

    @SideEffectFree
    @Override
    public void restartOutput()
    {
    }

    @SideEffectFree
    @Override
    public void zapMsgAvailable()
    {
    }

    @Pure
    @Override
    public String getEndPoint()
    {
        return null;
    }
}
