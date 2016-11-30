package challenge.challenge2onebucket.util;

public interface BoundedUniformHash
{
    void setBounds(final long p0);
    
    void setBounds(final long p0, final long p1);
    
    void reset();
    
    void update(final long p0);
    
    void update(final String p0);
    
    long getHash();
}
