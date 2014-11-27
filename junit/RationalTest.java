import junit.framework.TestCase;

public class RationalTest extends TestCase {

    protected Rational HALF;

    protected void setUp() {
      HALF = new Rational( 1, 2 );
    }

    // Create new test
    public RationalTest (String name) {
        super(name);
    }

    public void testEquality() {
        assertEquals(new Rational(1073741824,1), new Rational(1073741824*2,2));        
    }

    // Test for nonequality
    public void testNonEquality() {
        assertFalse(new Rational(2,0).equals(new Rational(3,0)));
    }

    public void testAccessors() {
    	assertFalse(new Rational(2,0).numerator()==new Rational(3,0).numerator());
    }
    
    public void testAccessorsMore() {
    	assertFalse(new Rational(0,2).denominator()==new Rational(0,3).denominator());
    }

    public void testRoot() {
        Rational s = new Rational( 1, 4 );
        Rational sRoot = null;
        try {
            sRoot = s.root();
        } catch (IllegalArgumentToSquareRootException e) {
            e.printStackTrace();
        }
        assertTrue( sRoot.isLessThan( HALF.plus( Rational.getTolerance() ) ) 
                        && HALF.minus( Rational.getTolerance() ).isLessThan( sRoot ) );
    }
    
    public void testRootMore() {
        Rational s = new Rational( -1, 4 );
        Rational sRoot = null;
        try {
            sRoot = s.root();
        } catch (IllegalArgumentToSquareRootException e) {
            e.printStackTrace();
        }
        assertTrue( sRoot.isLessThan( HALF.plus( Rational.getTolerance() ) )
                   && HALF.minus( Rational.getTolerance() ).isLessThan( sRoot ) );
    }
    
    public void testPlus(){
        Rational x=new Rational(1,0);
        Rational y=new Rational(2,0);
        Rational zero=new Rational(0,1);
        assertTrue(!x.plus(y).isLessThan(zero));
    }
    
    public void testPlusMore(){
        Rational x=new Rational(1073741824,1);       
        Rational y=new Rational(1073741824,1);
        Rational zero=new Rational(0,1);
        assertTrue(!x.plus(y).isLessThan(zero));
    }
    
    public void testMinus(){
        Rational x=new Rational(1073741824,1);       
        Rational y=new Rational(1073741824*2,2);
        Rational zero=new Rational(0,1);
        assertEquals(zero, x.minus(y));
    }
    
    public void testMinusMore(){
        Rational x=new Rational(1073741824,1);       
        Rational y=new Rational(-1073741824,1);
        Rational zero=new Rational(0,1);
        assertTrue(!x.minus(y).isLessThan(zero));
    }
    
    public void testTimes(){
        Rational x=new Rational(1073741824,1);       
        Rational y=new Rational(2,1);
        assertEquals(x,x.times(y).times(HALF));
    }
    
    public void testTimesMore(){
        Rational x=new Rational(1073741824,1);       
        Rational y=new Rational(2,1);
        assertEquals(x.times(HALF).times(y),x.times(y).times(HALF));
    }
    
    public void testDivides(){
        Rational x=new Rational(1073741824,1);       
        Rational y=new Rational(1073741824*2,1);
        Rational z=new Rational(2,1);
        assertEquals(x,y.divides(z));
    }
    
    public void testDividesMore(){
        Rational x=new Rational(1073741824,1); 
        Rational z=new Rational(2,1);
        assertEquals(x,x.plus(x).divides(z));
    }
    
    public void testAbs(){
        Rational x=new Rational(2,0);
        Rational y=new Rational(3,0);
        assertFalse(x.abs().equals(y.abs()));
        
    }

    public static void main(String []args) {
        String[] testCaseName = 
            { RationalTest.class.getName() };
        // junit.swingui.TestRunner.main(testCaseName);
        junit.textui.TestRunner.main(testCaseName);
    }
}
