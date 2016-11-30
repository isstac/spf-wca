package challenge.challenge2.util;


public abstract class AbstractBoundedUniformHash implements BoundedUniformHash
{
    protected long mLowerBound;
    protected long mUpperBound;
    
    public void setBounds(final long n) {
        this.setBounds(0L, n);
    }
    
    public void setBounds(final long mLowerBound, final long mUpperBound) {
        this.mLowerBound = mLowerBound;
        this.mUpperBound = mUpperBound;
    }
    
    public void update(final String s) {
      System.out.println("update in bounded uniform hash update. String length " + s.length());
        for (int i = 0; i < s.length(); ++i) {
            this.update(s.charAt(i));
        }
    }
}
